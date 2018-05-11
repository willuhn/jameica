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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

/**
 * Implementierung einer RMI-Socketfactory, welche nur an 127.0.0.1 gebundene ServerSockets liefert
 */
public class LocalRMISocketFactory extends RMISocketFactory
{
  /**
   * @see java.rmi.server.RMISocketFactory#createServerSocket(int)
   */
  public ServerSocket createServerSocket(int port) throws IOException
  {
    return new ServerSocket(port, 0, InetAddress.getByName("127.0.0.1"));
  }

  /**
   * @see java.rmi.server.RMISocketFactory#createSocket(java.lang.String, int)
   */
  public Socket createSocket(String host, int port) throws IOException
  {
     return new Socket(host, port);
  }
}

/**********************************************************************
 * $Log: LocalRMISocketFactory.java,v $
 * Revision 1.1  2009/04/02 15:00:31  willuhn
 * @N RMI-Patch von Jan
 *
 **********************************************************************/