/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/JameicaAuthenticator.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/06/10 11:25:53 $
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
   * Ueber diese Enums koennen die Properties des Authentifizierungs-Requests abgefragt
   * werden.
   */
  public static enum RequestParam
  {
    HOST,
    SITE,
    PORT,
    PROTOCOL,
    PROMPT,
    SCHEME,
    URL,
    AUTHTYPE
  }

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
  
  /**
   * Ueber diese Funktion koennen Properties des Authentifizierungs-Requqests
   * abgefragt werden. Die unten aufgerufenen Funktionen sind leider alle
   * protected final, sodass sie nur von abgeleiteten Klassen aufgerufen
   * werden koennen. Wir wollen den Authentifizierungsdialog aber nicht
   * direkt hier drin sondern im Jameica-Callback implementieren. Daher machen
   * wir sie mit folgender Funktion auch von aussen abrufbar.
   * @param param Name des Parameters.
   * @return der Wert des Parameters oder NULL, wenn er nicht existiert oder keinen Wert hat.
   */
  public Object getRequestParam(RequestParam param)
  {
    switch (param)
    {
      case HOST:
        return this.getRequestingHost();
      case SITE:
        return this.getRequestingSite();
      case PORT:
        return this.getRequestingPort();
      case PROTOCOL:
        return this.getRequestingProtocol();
      case PROMPT:
        return this.getRequestingPrompt();
      case SCHEME:
        return this.getRequestingScheme();
      case URL:
        return this.getRequestingURL();
      case AUTHTYPE:
        return this.getRequestorType();
    }
    return null;
  }
}


/**********************************************************************
 * $Log: JameicaAuthenticator.java,v $
 * Revision 1.2  2009/06/10 11:25:53  willuhn
 * @N Transparente HTTP-Authentifizierung ueber Jameica (sowohl in GUI- als auch in Server-Mode) mittels ApplicationCallback
 *
 * Revision 1.1  2009/06/09 12:43:01  willuhn
 * @N Erster Code fuer Jameica Authenticator
 *
 **********************************************************************/
