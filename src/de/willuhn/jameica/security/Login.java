/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/Login.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/01/31 13:07:52 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.security;

import java.io.Serializable;

/**
 * Ein Login-Objekt mit Username und Passwort.
 */
public class Login implements Serializable
{
  private String username = null;
  private char[] password = null;

  /**
   * ct.
   * @param username
   * @param password
   */
  public Login(String username, char[] password)
  {
    this.username = username;
    this.password = password;
  }
  
  /**
   * Liefert den Usernamen.
   * @return Username.
   */
  public String getUsername()
  {
    return this.username;
  }
  
  /**
   * Liefert das Passwort.
   * @return das Passwort.
   */
  public char[] getPassword()
  {
    return this.password;
  }

}


/*********************************************************************
 * $Log: Login.java,v $
 * Revision 1.1  2007/01/31 13:07:52  willuhn
 * @N Login-Dialog
 * @N SystemMessage
 *
 **********************************************************************/