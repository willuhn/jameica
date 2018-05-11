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

import java.rmi.server.RMIFailureHandler;

import de.willuhn.logging.Logger;

/**
 * @author willuhn
 */
public class SSLRMIFailureHandler implements RMIFailureHandler
{

  /**
   * @see java.rmi.server.RMIFailureHandler#failure(java.lang.Exception)
   */
  public boolean failure(Exception ex)
  {
    Logger.error("error while creating server socket",ex);
    return true;
  }
}

/*****************************************************************************
 * $Log: SSLRMIFailureHandler.java,v $
 * Revision 1.1  2005/01/19 02:14:00  willuhn
 * @N Wallet zum Verschluesseln von Benutzerdaten
 *
 * Revision 1.1  2005/01/12 11:32:43  willuhn
 * *** empty log message ***
 *
*****************************************************************************/