/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Attic/SSLRMISocketFactory.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/09/13 23:27:12 $
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

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SSLRMISocketFactory extends RMISocketFactory {

	private SSLServerSocketFactory serverSocketFactory;
  private SSLSocketFactory socketFactory;

  private boolean clientAuth = true;

  /**
   * ct.
   */
  public SSLRMISocketFactory()
  {
    serverSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
    socketFactory 			= (SSLSocketFactory) SSLSocketFactory.getDefault();
  }

  /**
   * @see java.rmi.server.RMIClientSocketFactory#createSocket(java.lang.String, int)
   */
  public Socket createSocket(String host, int port) throws IOException
  {
    SSLSocket socket = (SSLSocket) socketFactory.createSocket(host, port);
    return socket;
  }

  /**
   * @see java.rmi.server.RMIServerSocketFactory#createServerSocket(int)
   */
  public ServerSocket createServerSocket(int port) throws IOException
  {
    SSLServerSocket socket = (SSLServerSocket) serverSocketFactory.createServerSocket(port);

    socket.setNeedClientAuth(clientAuth);
    return socket;
  }
}

/*********************************************************************
 * $Log: SSLRMISocketFactory.java,v $
 * Revision 1.2  2004/09/13 23:27:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/08/31 18:57:23  willuhn
 * *** empty log message ***
 *
 **********************************************************************/
