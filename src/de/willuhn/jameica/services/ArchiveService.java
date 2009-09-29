/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/ArchiveService.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/09/29 00:03:27 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Properties;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.messaging.LookupService;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Ein Archiv-Service, der Daten an eine andere Jameica-Instanz senden kann,
 * auf der das Plugin "jameica.messaging" installiert ist. Da diese andere
 * Jameica-Instanz via Multicast-Lookup automatisch im LAN gefunden wird,
 * kann man auf diese Weise einen konfigurationsfreien Archiv-Server aufsetzen
 * und Dokumente einfach via QueryMessage an den Server senden. Beispielcode
 * kann so aussehen:
 * 
 * <pre>
 * 
 * // Laedt den Archiv-Service on demand
 * Application.getBootLoader().getBootable(ArchiveService.class);
 * 
 * // 1. Neue Datei zum Archiv hinzufuegen. Die Nachricht wird synchron geschickt,
 * //    damit wir die vergebene UUID erhalten. Anhand dieser UUID koennen wir die
 * //    archivierte Datei jederzeit wieder abrufen.
 * //    "test.remote" ist hier der Name eines Channels. Mit dem laesst sich
 * //    das Archiv auf dem Server hierarich strukturieren.
 * 
 * QueryMessage qm = new QueryMessage("test.remote","Das ist der Dateiinhalt".getBytes());
 * Application.getMessagingFactory().getMessagingQueue("jameica.messaging.put").sendSyncMessage(qm);
 * String uuid = qm.getData().toString();
 * 
 *  
 * // 2. Die Datei mit der genannten UUID wieder abrufen. Der Dateiinhalt wird
 * //    anschliessend in qm.getData() als Byte-Array bereitgestellt
 *
 * qm = new QueryMessage(uuid,null);
 * Application.getMessagingFactory().getMessagingQueue("jameica.messaging.get").sendSyncMessage(qm);
 * byte[] data = (byte[]) qm.getData();
 *
 * 
 * // 3. Die Datei noch mit zusaetzlichen Properties versehen
 * 
 * Map map = new HashMap();
 * map.put("filename","test.html");
 * qm = new QueryMessage(uuid,map);
 * Application.getMessagingFactory().getMessagingQueue("jameica.messaging.putmeta").sendMessage(qm);
 * 
 * 
 * // 4. Die Properties wieder abfragen
 * 
 * qm = new QueryMessage(uuid,null);
 * Application.getMessagingFactory().getMessagingQueue("jameica.messaging.getmeta").sendSyncMessage(qm);
 * map = (Map) qm.getData();
 * 
 * 
 * // 5. Die naechste Datei aus einem genannten Channel abrufen. Mit der "next"-
 * //    Funktion kann man eine Rechner-uebergreifende Message-Queue bereitstellen.
 * //    Die Funktion liefert die naechste verfuegbare Datei aus dem Channel
 * //    (nach dem FIFO-Prinzip) und loescht(!) sie nach der Uebertragung automatisch
 * //    vom Server. Auf diese Weise kann ein Rechner regelmaessig Messages an
 * //    den Server uebergeben, waehrend ein anderer Rechner sie abarbeitet.
 * 
 * qm = new QueryMessage("test.remote",null);
 * Application.getMessagingFactory().getMessagingQueue("jameica.messaging.next").sendSyncMessage(qm);
 * byte[] data = (byte[]) qm.getData();
 * 
 * 
 * // 6. Eine Datei mit der genannten UUID auf dem Server loeschen.
 * 
 *  qm = new QueryMessage(uuid,null);
 *  Application.getMessagingFactory().getMessagingQueue("jameica.messaging.del").sendSyncMessage(qm);
 * </pre>
 */
public class ArchiveService implements Bootable
{
  private String host = null;
  private int port    = -1;
  
  private AbstractCommand get     = null;
  private AbstractCommand put     = null;
  private AbstractCommand del     = null;
  private AbstractCommand next    = null;
  private AbstractCommand getMeta = null;
  private AbstractCommand putMeta = null;

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{MessagingService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    // Wir checken, ob das Plugin "jameica.messaging" lokal installiert ist.
    // Wenn das der Fall ist, werden wir nicht gebraucht, weil dann bereits
    // MessageConsumer fuer lokale Archiv-Zustellung aktiv sind.
    if (Application.getPluginLoader().getPlugin("de.willuhn.jameica.messaging.Plugin") != null)
    {
      Logger.info("remote message delivery not needed, jameica.messaging is locally installed");
      return;
    }
    
    try
    {
      // Wir checken, ob ein Archiv-Server verfuegbar ist
      String uri = LookupService.lookup("tcp:de.willuhn.jameica.messaging.Plugin.connector.tcp");
      if (uri != null)
      {
        int colon = uri.indexOf(':');
        if (colon != -1)
        {
          // Jepp, wir haben eine URI
          this.host = uri.substring(0,colon);
          this.port = Integer.parseInt(uri.substring(colon+1));
        }
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to determine uri to archive service",e);
    }
    
    if (this.host == null || this.port == -1)
    {
      Logger.warn("no archive server found");
      return;
    }
    
    // Wir registrieren die Message-Consumer fuer die Channels.
    this.get     = new Get();
    this.put     = new Put();
    this.del     = new Del();
    this.next    = new Next();
    this.getMeta = new GetMeta();
    this.putMeta = new PutMeta();
    
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.get").registerMessageConsumer(this.get);
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.put").registerMessageConsumer(this.put);
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.del").registerMessageConsumer(this.del);
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.next").registerMessageConsumer(this.next);
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.getmeta").registerMessageConsumer(this.getMeta);
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.putmeta").registerMessageConsumer(this.putMeta);
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
    if (this.get != null) Application.getMessagingFactory().getMessagingQueue("jameica.messaging.get").unRegisterMessageConsumer(this.get);
    if (this.put != null) Application.getMessagingFactory().getMessagingQueue("jameica.messaging.put").unRegisterMessageConsumer(this.put);
    if (this.del != null) Application.getMessagingFactory().getMessagingQueue("jameica.messaging.del").unRegisterMessageConsumer(this.del);
    if (this.next != null) Application.getMessagingFactory().getMessagingQueue("jameica.messaging.next").unRegisterMessageConsumer(this.next);
    if (this.getMeta != null) Application.getMessagingFactory().getMessagingQueue("jameica.messaging.getmeta").unRegisterMessageConsumer(this.getMeta);
    if (this.putMeta != null) Application.getMessagingFactory().getMessagingQueue("jameica.messaging.putmeta").unRegisterMessageConsumer(this.putMeta);
  }

  /**
   * Erstellt einen TCP-Socket zum Archiv-Server.
   * @return Socket zum Archiv-Server oder null wenn kein Archiv-Server existiert.
   */
  private Socket createSocket()
  {
    // Wenn wir keinen Host oder keinen Port haben, koennen wir keinen Socket erstellen
    if (host == null || port <= 0)
      return null;
    
    try
    {
      return new Socket(host,port);
    }
    catch (Exception e)
    {
      Logger.error("unable to create socket to archive server, host: " + host + ", port: " + port,e);
    }
    return null;
  }

  
  /**
   * Abstrakte Basis-Implementierung der Kommandos.
   */
  private abstract class AbstractCommand implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{QueryMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      Socket socket = createSocket();
      if (socket == null)
      {
        Logger.debug("skip remote message delivery, no archive server found");
        return; // kein Socket. Also entweder kein Archiv-Server oder er ist nicht
                // erreichbar. Egal. Wir stellen nichts zu.
      }

      QueryMessage msg = (QueryMessage) message;
      
      try
      {
        handle(msg,socket);
      }
      catch (Exception e)
      {
        Logger.error("error while delivering message",e);
      }
      finally
      {
        if (socket != null) {
          try { socket.close(); }
          catch (Exception e) { Logger.error("error while closing socket",e); }
        }
      }
      
    }
    
    /**
     * Muss von den abgeleiteten Klassen implementiert werden, um den Datentransfer durchzufuehren.
     * @param message die Message.
     * @param socket der Socket.
     * @throws Exception
     */
    abstract void handle(QueryMessage message, Socket socket) throws Exception;
  }
  
  /**
   * Implementierung des PUT-Kommandos.
   */
  private class Put extends AbstractCommand
  {
    /**
     * @see de.willuhn.jameica.messaging.ArchiveMessageConsumer.AbstractCommand#handle(de.willuhn.jameica.messaging.QueryMessage, java.net.Socket)
     */
    void handle(QueryMessage message, Socket socket) throws Exception
    {
      Object o = message.getData();
      if (o == null || !(o instanceof byte[]))
        throw new Exception("message contains no byte array as data");

      byte[] data = (byte[]) o;
      
      String channel = message.getName();
      if (channel == null)
        channel = "";
      
      OutputStream os = null;
      InputStream is = null;
      try
      {
        // Request senden
        os = new BufferedOutputStream(socket.getOutputStream());
        os.write(("put " + channel + "\r\n").getBytes());
        os.write(data);
        os.flush();
        socket.shutdownOutput(); // teilt dem Server mit, dass nichts mehr kommt
        
        // Response holen
        is = new BufferedInputStream(socket.getInputStream());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        copy(is,bos);

        String uuid = bos.toString().trim();
        Logger.info("sent " + data.length + " bytes to channel " + channel + ", generated uuid: " + uuid);
        message.setData(uuid);
      }
      finally
      {
        if (is != null) try {is.close();} catch (Exception e) {Logger.error("unable to close inputstream",e);}
        if (os != null) try {os.close();} catch (Exception e) {Logger.error("unable to close outputstream",e);}
      }
    }
  }


  /**
   * Implementierung des GET-Kommandos.
   */
  private class Get extends AbstractCommand
  {
    /**
     * @see de.willuhn.jameica.messaging.ArchiveMessageConsumer.AbstractCommand#handle(de.willuhn.jameica.messaging.QueryMessage, java.net.Socket)
     */
    void handle(QueryMessage message, Socket socket) throws Exception
    {
      String uuid = message.getName();
      if (uuid == null || uuid.length() == 0)
        throw new Exception("no uuid given");
      
      OutputStream os = null;
      InputStream is = null;
      try
      {
        // Request senden
        os = new BufferedOutputStream(socket.getOutputStream());
        os.write(("get " + uuid + "\r\n").getBytes());
        os.flush();
        socket.shutdownOutput(); // wir senden nichts mehr

        // Response holen
        is = new BufferedInputStream(socket.getInputStream());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        long count = copy(is,bos);

        message.setData(bos.toByteArray());
        Logger.info("got " + count + " bytes for message, uuid " + uuid);
      }
      finally
      {
        if (is != null) try {is.close();} catch (Exception e) {Logger.error("unable to close inputstream",e);}
        if (os != null) try {os.close();} catch (Exception e) {Logger.error("unable to close outputstream",e);}
      }
    }
  }

  /**
   * Implementierung des DEL-Kommandos.
   */
  private class Del extends AbstractCommand
  {
    /**
     * @see de.willuhn.jameica.messaging.ArchiveMessageConsumer.AbstractCommand#handle(de.willuhn.jameica.messaging.QueryMessage, java.net.Socket)
     */
    void handle(QueryMessage message, Socket socket) throws Exception
    {
      String uuid = message.getName();
      if (uuid == null || uuid.length() == 0)
        throw new Exception("no uuid given");
      
      OutputStream os = null;
      try
      {
        // Request senden
        os = new BufferedOutputStream(socket.getOutputStream());
        os.write(("delete " + uuid + "\r\n").getBytes());
        os.flush();
        socket.shutdownOutput(); // wir senden nichts mehr
        Logger.info("deleted message, uuid " + uuid);
      }
      finally
      {
        if (os != null) try {os.close();} catch (Exception e) {Logger.error("unable to close outputstream",e);}
      }
    }
  }

  /**
   * Implementierung des GETMETA-Kommandos.
   */
  private class GetMeta extends AbstractCommand
  {
    /**
     * @see de.willuhn.jameica.messaging.ArchiveMessageConsumer.AbstractCommand#handle(de.willuhn.jameica.messaging.QueryMessage, java.net.Socket)
     */
    void handle(QueryMessage message, Socket socket) throws Exception
    {
      String uuid = message.getName();
      if (uuid == null || uuid.length() == 0)
        throw new Exception("no uuid given");
      
      OutputStream os = null;
      InputStream is = null;
      try
      {
        // Request senden
        os = new BufferedOutputStream(socket.getOutputStream());
        os.write(("getmeta " + uuid + "\r\n").getBytes());
        os.flush();
        socket.shutdownOutput(); // wir senden nichts mehr
        
        // Response holen
        is = new BufferedInputStream(socket.getInputStream());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        copy(is,bos);

        Properties props = new Properties();
        props.load(new ByteArrayInputStream(bos.toByteArray()));
        message.setData(props);
        Logger.info("got " + props.size() + " properties for message, uuid " + uuid);
      }
      finally
      {
        if (is != null) try {is.close();} catch (Exception e) {Logger.error("unable to close inputstream",e);}
        if (os != null) try {os.close();} catch (Exception e) {Logger.error("unable to close outputstream",e);}
      }
    }
  }

  /**
   * Implementierung des PUTMETA-Kommandos.
   */
  private class PutMeta extends AbstractCommand
  {
    /**
     * @see de.willuhn.jameica.messaging.ArchiveMessageConsumer.AbstractCommand#handle(de.willuhn.jameica.messaging.QueryMessage, java.net.Socket)
     */
    void handle(QueryMessage message, Socket socket) throws Exception
    {
      Object data = message.getData();

      if (data == null || !(data instanceof Map))
        throw new Exception("message contains no map as data");

      String uuid = message.getName();
      if (uuid == null || uuid.length() == 0)
        throw new Exception("no uuid given");
      
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      Properties props = new Properties();
      props.putAll((Map) data);
      props.store(bos,"");

      OutputStream os = null;
      try
      {
        // Request senden
        os = new BufferedOutputStream(socket.getOutputStream());
        os.write(("putmeta " + uuid + "\r\n").getBytes());
        os.write(bos.toByteArray());
        os.flush();
        socket.shutdownOutput(); // wir senden nichts mehr
        Logger.info("sent " + props.size() + " properties for message, uuid " + uuid);
      }
      finally
      {
        if (os != null) try {os.close();} catch (Exception e) {Logger.error("unable to close outputstream",e);}
      }
    }
  }

  /**
   * Implementierung des NEXT-Kommandos.
   */
  private class Next extends AbstractCommand
  {
    /**
     * @see de.willuhn.jameica.messaging.ArchiveMessageConsumer.AbstractCommand#handle(de.willuhn.jameica.messaging.QueryMessage, java.net.Socket)
     */
    void handle(QueryMessage message, Socket socket) throws Exception
    {
      String channel = message.getName();
      if (channel == null)
        channel = "";
      
      OutputStream os = null;
      InputStream is = null;
      try
      {
        // Request senden
        os = new BufferedOutputStream(socket.getOutputStream());
        os.write(("next " + channel + "\r\n").getBytes());
        os.flush();
        socket.shutdownOutput(); // wir senden nichts mehr

        // Response holen
        is = new BufferedInputStream(socket.getInputStream());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        long count = copy(is,bos);

        message.setData(bos.toByteArray());
        Logger.info("got " + count + " bytes for channel " + channel);
      }
      finally
      {
        if (is != null) try {is.close();} catch (Exception e) {Logger.error("unable to close inputstream",e);}
        if (os != null) try {os.close();} catch (Exception e) {Logger.error("unable to close outputstream",e);}
      }
    }
  }
  
  /**
   * Kopiert Daten von is nach os.
   * Schliesst/flusht keinen der Streams.
   * @param is Datenquelle.
   * @param os Datenziel.
   * @return Anzahl der geschriebenen Bytes.
   * @throws IOException
   */
  private static long copy(InputStream is, OutputStream os) throws IOException
  {
    byte[] buf = new byte[4096];
    long count = 0;
    int read   = 0;
    while ((read = is.read(buf)) != -1)
    {
      if (read > 0) // Nur schreiben, wenn wirklich was gelesen wurde
        os.write(buf,0,read);
      count += read;
    }
    return count;
  }
}


/**********************************************************************
 * $Log: ArchiveService.java,v $
 * Revision 1.2  2009/09/29 00:03:27  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2009/09/28 23:55:16  willuhn
 * @N Archiv-Service fuer die Zustellung von Messages/Dateien an einen Jameica Archiv-Server (jameica.messaging) via TCP-Port 9000
 *
 **********************************************************************/
