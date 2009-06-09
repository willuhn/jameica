/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/AuthenticateService.java,v $
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

package de.willuhn.jameica.services;

import java.net.Authenticator;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.security.JameicaAuthenticator;


/**
 * Authentifizierungs-Service fuer HTTP-Basic-Authentication.
 */
public class AuthenticateService implements Bootable
{

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{ProxyService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    Authenticator.setDefault(new JameicaAuthenticator());
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
  }
}


/**********************************************************************
 * $Log: AuthenticateService.java,v $
 * Revision 1.1  2009/06/09 12:43:01  willuhn
 * @N Erster Code fuer Jameica Authenticator
 *
 **********************************************************************/
