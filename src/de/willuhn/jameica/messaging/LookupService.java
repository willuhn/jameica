/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/LookupService.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/12/14 13:41:02 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Hashtable;

import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.net.MulticastClient;

/**
 * Dieser Service erlaubt die automatische Suche nach Service-URLs
 * im LAN via Multicast.
 */
public class LookupService implements MessageConsumer
{
  private static Hashtable lookup = new Hashtable();
  private static Client client    = null;
  
  /**
   * Registriert einen Service mit dem angegebenen Namen.
   * @param name Name des Services.
   * Das entspricht dem Keyword, welches fuer das Multicast-Lookup verwendet wird.
   * @param url URL, die in dem Fall zurueckgeliefert werden soll.
   */
  public static void register(String name, String url)
  {
    if (Application.getConfig().getMulticastLookup())
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
    if (Application.getConfig().getMulticastLookup())
    {
      Logger.info("un-register lookup name: " + name);
      lookup.remove(name);
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return Application.getConfig().getMulticastLookup();
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
    if (!Application.getConfig().getMulticastLookup())
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
    if (msg.getStatusCode() == SystemMessage.SYSTEM_SHUTDOWN)
    {
      Logger.info("stop multicast lookup client");
      if (client != null)
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
      if (s == null)
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
      
      Logger.info("got lookup request for service " + s + " from " + sender.getCanonicalHostName());
      String url = (String) lookup.get(s);
      Logger.info("sending url: " + url);
      send(url.getBytes());
    }
  }
}


/*********************************************************************
 * $Log: LookupService.java,v $
 * Revision 1.2  2007/12/14 13:41:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2007/12/14 13:29:05  willuhn
 * @N Multicast Lookup-Service
 *
 **********************************************************************/