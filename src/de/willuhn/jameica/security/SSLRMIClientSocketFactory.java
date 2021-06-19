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
import java.io.Serializable;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;

import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Implementierung der clientseitigen Socket-Factory fuer RMI mit SSL-Support.
 */
public class SSLRMIClientSocketFactory implements Serializable,
    RMIClientSocketFactory
{

  private static final long serialVersionUID = 5971693750839330147L;

  /**
   * @see java.rmi.server.RMIClientSocketFactory#createSocket(java.lang.String, int)
   */
  public Socket createSocket(String host, int port) throws IOException
  {
    try
    {
      SSLContext context = Application.getSSLFactory().getSSLContext();
      SocketFactory socketFactory = context.getSocketFactory();
      return socketFactory.createSocket(host, port);
    }
    catch (IOException ioe)
    {
      throw ioe;
    }
    catch (Exception e)
    {
      Logger.error("unable to create client socket",e);
      throw new IOException(e.toString());
    }
  }

}


/*********************************************************************
 * $Log: SSLRMIClientSocketFactory.java,v $
 * Revision 1.1  2007/10/30 11:49:28  willuhn
 * @C RMI-SSL Zeug nochmal gemaess http://java.sun.com/j2se/1.4.2/docs/guide/rmi/socketfactory/index.html ueberarbeitet. Funktioniert aber trotzdem noch nicht
 *
 **********************************************************************/