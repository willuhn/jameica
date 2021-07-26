/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.security;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import de.willuhn.jameica.messaging.KeystoreChangedMessage;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;

/**
 * Implementierung einer RMI-Socketfactory mit Unterstuetzung fuer SSL.
 */
public class SSLRMISocketFactory extends RMISocketFactory
{

	private SSLServerSocketFactory serverSocketFactory;
  private SSLSocketFactory socketFactory;

  /**
   * ct.
   * @throws Exception
   */
  public SSLRMISocketFactory() throws Exception
  {
  	super();
    RMISocketFactory.setFailureHandler(new SSLRMIFailureHandler());
    init();
    Application.getMessagingFactory().registerMessageConsumer(new MessageConsumer() {
    
      /**
       * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
       */
      public void handleMessage(Message message) throws Exception
      {
        init();
      }
    
      /**
       * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
       */
      public Class[] getExpectedMessageTypes()
      {
        return new Class[]{KeystoreChangedMessage.class};
      }
    
      /**
       * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
       */
      public boolean autoRegister()
      {
        return false;
      }
    
    });
  }
  
  /**
   * Initialisiert die Sockets.
   * @throws Exception
   */
  private synchronized void init() throws Exception
  {
    Logger.info("(re)init \"rmi over ssl\" socket factory");
    SSLContext context  = Application.getSSLFactory().getSSLContext();
    serverSocketFactory = context.getServerSocketFactory();
    socketFactory       = context.getSocketFactory();
  }

  /**
   * @see java.rmi.server.RMIClientSocketFactory#createSocket(java.lang.String, int)
   */
  public Socket createSocket(String host, int port) throws IOException
  {
		Logger.debug("Creating client socket to " + host + ":" + port);
    SSLSocket socket = (SSLSocket) socketFactory.createSocket(host, port);
		log(socket);
    return socket;
  }

  /**
   * @see java.rmi.server.RMIServerSocketFactory#createServerSocket(int)
   */
  public ServerSocket createServerSocket(int port) throws IOException
  {
		Logger.info("Creating server socket at port " + port + "/tcp");
    SSLServerSocket socket = (SSLServerSocket) serverSocketFactory.createServerSocket(port);
		log(socket);
    boolean clientAuth = Application.getConfig().getRmiUseClientAuth();
    Logger.info("Client Auth: " + clientAuth);
    socket.setWantClientAuth(clientAuth);
    socket.setNeedClientAuth(clientAuth);
    return socket;
  }

  /** Log-Level, um in {@link #log(Object)} Informationen zu dokumentieren */
  private static final Level LOG_LEVEL = Level.DEBUG;

	/**
   * Loggt die vom Socket unterstuetzten Protokolle und Cipher-Suites.
   * @param socket zu loggender Socket.
	 * @throws IOException
   */
  private void log(Object socket) throws IOException
	{
    if (!Logger.isLogging(LOG_LEVEL))
      return;

    String[] protos;
    String[] cipher;
    String socketType =  "CLIENT";
    if (socket instanceof SSLServerSocket)
    {
      socketType = "SERVER";
      SSLServerSocket s = (SSLServerSocket) socket;
      protos = s.getEnabledProtocols();
      cipher = s.getEnabledCipherSuites();
      Logger.write(LOG_LEVEL, socketType + " Socket receive buffer size: " + s.getReceiveBufferSize());
    }
    else
    {
      SSLSocket s = (SSLSocket) socket;
      protos = s.getEnabledProtocols();
      cipher = s.getEnabledCipherSuites();
      Logger.write(LOG_LEVEL, socketType + " Socket receive buffer size: " + s.getReceiveBufferSize());
    }
    StringBuffer sb = new StringBuffer("enabled protocols for " + socketType + " socket: ");
    for (int i=0;i<protos.length;++i) 
    {
      sb.append(protos[i]);
      sb.append(" ");
    } 
    Logger.write(LOG_LEVEL, sb.toString());

    sb = new StringBuffer("enabled cipher suites for " + socketType + " socket: ");
    for (int i=0;i<cipher.length;++i) 
    {
      sb.append(cipher[i]);
      sb.append(" ");
    } 
    Logger.write(LOG_LEVEL, sb.toString());
  }
}

/*********************************************************************
 * $Log: SSLRMISocketFactory.java,v $
 * Revision 1.10  2007/06/21 14:08:12  willuhn
 * @N Client-Authentifizierung bei RMI over SSL konfigurierbar
 *
 * Revision 1.9  2006/11/10 00:38:50  willuhn
 * @N notify when keystore changed
 *
 * Revision 1.8  2006/11/08 01:04:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2006/10/31 23:35:12  willuhn
 * @N Benachrichtigen der SSLRMISocketFactory wenn sich Keystore geaendert hat
 *
 * Revision 1.6  2005/10/24 20:40:48  web0
 * @C rollback to 2004/06
 *
 * Revision 1.4  2005/06/21 20:02:03  web0
 * @C cvs merge
 *
 * Revision 1.3  2005/06/10 10:12:26  web0
 * @N Zertifikats-Dialog ergonomischer gestaltet
 * @C TrustManager prueft nun zuerst im Java-eigenen Keystore
 *
 * Revision 1.2  2005/03/09 01:06:36  web0
 * @D javadoc fixes
 *
 * Revision 1.1  2005/01/19 02:14:00  willuhn
 * @N Wallet zum Verschluesseln von Benutzerdaten
 *
 * Revision 1.10  2005/01/15 16:20:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2005/01/14 00:48:57  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2005/01/13 19:31:37  willuhn
 * @C SSLFactory geaendert
 * @N Settings auf property-Format umgestellt
 *
 * Revision 1.7  2005/01/12 11:32:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2005/01/12 01:44:57  willuhn
 * @N added test https server
 *
 * Revision 1.5  2005/01/12 00:17:17  willuhn
 * @N JameicaTrustManager
 *
 * Revision 1.4  2005/01/11 00:52:52  willuhn
 * @RMI over SSL works
 *
 * Revision 1.3  2005/01/11 00:00:52  willuhn
 * @N SSLFactory
 *
 * Revision 1.2  2004/09/13 23:27:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/08/31 18:57:23  willuhn
 * *** empty log message ***
 *
 **********************************************************************/
