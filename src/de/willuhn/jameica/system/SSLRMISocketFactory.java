/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Attic/SSLRMISocketFactory.java,v $
 * $Revision: 1.6 $
 * $Date: 2005/01/12 01:44:57 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.system;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import de.willuhn.logging.Logger;

public class SSLRMISocketFactory extends RMISocketFactory {

	private SSLServerSocketFactory serverSocketFactory;
  private SSLSocketFactory socketFactory;

  private boolean clientAuth = false;

  /**
   * ct.
   * @throws Exception
   */
  public SSLRMISocketFactory() throws Exception
  {
  	super();
  	Logger.info("init \"rmi over ssl\" socket factory");
  	SSLContext context 	= Application.getSSLFactory().getSSLContext();
    serverSocketFactory = context.getServerSocketFactory();
    socketFactory 			= context.getSocketFactory();
  }

  /**
   * @see java.rmi.server.RMIClientSocketFactory#createSocket(java.lang.String, int)
   */
  public Socket createSocket(String host, int port) throws IOException
  {
		Logger.debug("Creating client socket to " + host + ":" + port);
    SSLSocket socket = (SSLSocket) socketFactory.createSocket(host, port);
    socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
    socket.setEnabledProtocols(socket.getSupportedProtocols());
		//log(socket);
    return socket;
  }

  /**
   * @see java.rmi.server.RMIServerSocketFactory#createServerSocket(int)
   */
  public ServerSocket createServerSocket(int port) throws IOException
  {
		Logger.info("Creating server socket at port " + port + "/tcp");
    SSLServerSocket socket = (SSLServerSocket) serverSocketFactory.createServerSocket(port);
		socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
		socket.setEnabledProtocols(socket.getSupportedProtocols());
		//log(socket);
    socket.setNeedClientAuth(clientAuth);
    socket.setWantClientAuth(clientAuth);
    return socket;
  }

	private void log(SSLSocket socket)
	{
		String[] protos = socket.getEnabledProtocols();
		Logger.debug("enabled protocols for CLIENT socket");
		for (int i=0;i<protos.length;++i) Logger.debug("  " + protos[i]);

		String[] cipher = socket.getEnabledCipherSuites();
		Logger.debug("enabled cipher suites for CLIENT socket");
		for (int i=0;i<cipher.length;++i) Logger.debug("  " + cipher[i]);

		protos = socket.getSupportedProtocols();
		Logger.debug("supported protocols for CLIENT socket");
		for (int i=0;i<protos.length;++i) Logger.debug("  " + protos[i]);

		cipher = socket.getSupportedCipherSuites();
		Logger.debug("supported cipher suites for CLIENT socket");
		for (int i=0;i<cipher.length;++i) Logger.debug("  " + cipher[i]);

	}
	private void log(SSLServerSocket socket)
	{
		String[] protos = socket.getEnabledProtocols();
		Logger.debug("enabled protocols for SERVER socket");
		for (int i=0;i<protos.length;++i) Logger.debug("  " + protos[i]);

		String[] cipher = socket.getEnabledCipherSuites();
		Logger.debug("enabled cipher suites for SERVER socket");
		for (int i=0;i<cipher.length;++i) Logger.debug("  " + cipher[i]);

		protos = socket.getSupportedProtocols();
		Logger.debug("supported protocols for SERVER socket");
		for (int i=0;i<protos.length;++i) Logger.debug("  " + protos[i]);

		cipher = socket.getSupportedCipherSuites();
		Logger.debug("supported cipher suites for SERVER socket");
		for (int i=0;i<cipher.length;++i) Logger.debug("  " + cipher[i]);
	}
}

/*********************************************************************
 * $Log: SSLRMISocketFactory.java,v $
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
