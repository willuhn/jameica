/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Hashtable;

import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.net.MulticastClient;

/**
 * Dieser Service erlaubt die automatische Suche nach Service-URLs
 * im LAN via Multicast.
 */
public class LookupService implements MessageConsumer
{
  private static Hashtable<String,String> lookup = new Hashtable<String, String>();
  private static Client client    = null;
  
  /**
   * Registriert einen Service mit dem angegebenen Namen.
   * @param name Name des Services.
   * Das entspricht dem Keyword, welches fuer das Multicast-Lookup verwendet wird.
   * @param url URL, die in dem Fall zurueckgeliefert werden soll.
   */
  public static void register(String name, String url)
  {
    if (enabled())
    {
      Logger.info("register multicast lookup. name: " + name + ", url: " + url);
      lookup.put(name,url);
    }
  }

  /**
   * Deregistriert einen Service mit dem angegebenen Namen.
   * @param name Name des Services.
   */
  public static void unRegister(String name)
  {
    if (enabled())
    {
      Logger.info("un-register lookup name: " + name);
      lookup.remove(name);
    }
  }
  
  /**
   * Prueft, ob der Service genutzt werden soll.
   * @return true, wenn er genutzt wird.
   */
  private static boolean enabled()
  {
    return Application.inServerMode() && Application.getConfig().getMulticastLookup();
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return enabled();
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{SystemMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    if (!enabled())
      return;

    if (message == null || !(message instanceof SystemMessage))
      return;

    SystemMessage msg = (SystemMessage) message;
    if (msg.getStatusCode() == SystemMessage.SYSTEM_STARTED)
    {
      if (lookup.size() == 0)
      {
        Logger.debug("no lookup urls registered, skip multicast lookup");
        return;
      }
      client = new Client();
      Logger.info("multicast lookup service started");
    }
    if (msg.getStatusCode() == SystemMessage.SYSTEM_SHUTDOWN && client != null)
    {
      Logger.info("stop multicast lookup client");
      client.stop();
    }
  }

  /**
   * Client, der die Lookups beantwortet.
   */
  private class Client extends MulticastClient
  {
    /**
     * ct
     * @throws IOException
     */
    public Client() throws IOException
    {
      super();
    }

    /**
     * @see de.willuhn.net.MulticastClient#received(java.net.DatagramPacket)
     */
    public void received(DatagramPacket packet) throws IOException
    {
      InetAddress sender = packet.getAddress();
      InetAddress self   = InetAddress.getLocalHost();
      Logger.debug("got datagram packet from " + sender.getHostName());
      if (sender.equals(self))
        return; // ignore

      String s = new String(packet.getData());
      if (s.length() == 0)
      {
        Logger.debug("ignoring empty message");
        return; // nix erhalten
      }
      
      s = s.trim(); // Zeilenumbruch am Ende entfernen
      
      // Wenn der Request nicht nur Buchstaben und Punkte enthielt, 
      // ignorieren wir ihn
      if (s.matches("[^a-zA-Z\\.]")) 
      {
        Logger.debug("ignoring message: " + s);
        return; // nix sinnvolles
      }
      
      String url = lookup.get(s);
      if (url == null)
      {
        Logger.debug("service " + s + " not found on this system, ignoring");
        return; // wir haben den Service nicht.
      }
      
      Logger.info("got lookup request for service " + s + " from " + sender.getCanonicalHostName());
      Logger.info("sending url: " + url);
      send(url.getBytes());
    }
  }
  
  
  
  /**
   * Sucht im lokalen Netz nach einem Dienst mit dem angegeben Namen.
   * @param name Name des Dienstes.
   * @return die URL des Dienstes oder {@code null}, wenn
   * er nicht gefunden wurde.
   */
  public static synchronized String lookup(String name)
  {
    if (name == null || name.length() == 0)
      return null;

    Logger.info("performing multicast lookup for service name: " + name);
    ServerLookup s = null;
    try
    {
      s = new ServerLookup(name);
      s.send();
      long started = System.currentTimeMillis();
      
      // Wir warten maximal 5 Sekunden auf Antwort 
      while (System.currentTimeMillis() - started < 5000l)
      {
        if (s.url != null)
          break;
        try
        {
          Thread.sleep(100l);
        }
        catch (InterruptedException e)
        {
          // ignore
        }
      }
      if (s.url != null)
      {
        Logger.info("found server for service name: " + name + " - url: "+ s.url);
        return s.url;
      }
    }
    catch (Exception e)
    {
      // Wenn das fehlschlaegt, kann der Lookup-Dienst halt nicht verwendet werden.
      Logger.write(Level.DEBUG,"multicast lookup failed, stacktrace for debugging purpose",e);
      Logger.info("multicast lookup not possible: " + e.getMessage());
    }
    finally
    {
      // Multicast-Client beenden
      if (s != null)
      {
        try
        {
          s.stop();
        } catch (Exception e) {} // useless
      }
    }
    Logger.info("no server found for service name: " + name);
    return null;
  }
  
  /**
   * Fuehrt ein Lookup nach Nummern-Servern im Netz durch.
   */
  private static class ServerLookup extends MulticastClient
  {
    private String name = null;
    private String url = null;
    
    /**
     * ct
     * @param name Name des gesuchten Service.
     * @throws IOException
     */
    public ServerLookup(String name) throws IOException
    {
      super();
      this.name = name;
    }
    
    /**
     * Sendet die Anfrage.
     * @throws IOException
     */
    public void send() throws IOException
    {
      super.send(name.getBytes());
    }

    /**
     * @see de.willuhn.net.MulticastClient#received(java.net.DatagramPacket)
     */
    public void received(DatagramPacket packet) throws IOException
    {
      InetAddress sender = packet.getAddress();
      InetAddress self   = InetAddress.getLocalHost();
      if (sender.equals(self))
        return; // sind wir selbst

      String s = new String(packet.getData());
      if (s.length() == 0)
        return; // nix erhalten
      s = s.trim();
      
      if (s.equals(this.name))
        return; // ist unsere eigene Anfrage
      this.url = s;
      Logger.info("got answer from " + sender.getCanonicalHostName() + ", url: " + this.url);
      Thread.currentThread().interrupt();
    }
  }

}


/*********************************************************************
 * $Log: LookupService.java,v $
 * Revision 1.9  2011/04/26 12:15:49  willuhn
 * @B Potentielle Bugs gemaess Code-Checker
 *
 * Revision 1.8  2009/09/29 14:50:08  willuhn
 * @B Service-Request ignorieren, wenn wir ihn nicht haben
 *
 * Revision 1.7  2009/03/11 23:10:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2008/02/20 16:36:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2008/01/16 22:08:26  willuhn
 * @C Multicast-Lookupservice nur im Server-Mode aktivieren
 *
 * Revision 1.4  2008/01/05 00:29:48  willuhn
 * @C changed logging
 *
 * Revision 1.3  2007/12/14 13:52:54  willuhn
 * @N Lookup-Client
 *
 * Revision 1.2  2007/12/14 13:41:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2007/12/14 13:29:05  willuhn
 * @N Multicast Lookup-Service
 *
 **********************************************************************/