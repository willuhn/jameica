/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Attic/HTTPsServer.java,v $
 * $Revision: 1.1 $
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

import java.net.ServerSocket;

/**
 */
public class HTTPsServer extends HTTPServer
{
  public static void main(String args[])
  {
		HTTPsServer server = new HTTPsServer();
    server.run();
  }
  public HTTPsServer(String name,String version,int port, boolean requireClientAuthentication)
  {
    super(name, version, port);
  }
  public HTTPsServer()
  {
    this("SecureServer", "1.0", 4430, false);
  }
  ServerSocket getServerSocket() throws Exception
  {
  	return Application.getSSLFactory().getSSLContext().getServerSocketFactory().createServerSocket(this.serverPort);
  }
}

/**********************************************************************
 * $Log: HTTPsServer.java,v $
 * Revision 1.1  2005/01/12 01:44:57  willuhn
 * @N added test https server
 *
 **********************************************************************/