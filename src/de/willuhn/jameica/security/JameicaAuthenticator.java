/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/JameicaAuthenticator.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/06/09 12:43:01 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.security;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;

/**
 * Jameica-Implementierung des Java-Authenticators.
 */
public class JameicaAuthenticator extends Authenticator
{

  /**
   * @see java.net.Authenticator#getPasswordAuthentication()
   */
  protected PasswordAuthentication getPasswordAuthentication()
  {
    try
    {
      Login login = Application.getCallback().login(this);
      return login == null ? null : new PasswordAuthentication(login.getUsername(),login.getPassword());
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      throw new OperationCanceledException(e);
    }
  }

}


/**********************************************************************
 * $Log: JameicaAuthenticator.java,v $
 * Revision 1.1  2009/06/09 12:43:01  willuhn
 * @N Erster Code fuer Jameica Authenticator
 *
 **********************************************************************/
