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

import java.io.Serializable;

/**
 * Ein Login-Objekt mit Username und Passwort.
 */
public class Login implements Serializable
{
  private static final long serialVersionUID = 6787174774166153140L;
  private String username = null;
  private char[] password = null;

  /**
   * ct.
   */
  public Login()
  {
    this(null,null);
  }

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
   * Speichert den Usernamen.
   * @param username der Username.
   */
  public void setUsername(String username)
  {
    this.username = username;
  }
  
  /**
   * Liefert das Passwort.
   * @return das Passwort.
   */
  public char[] getPassword()
  {
    return this.password;
  }
  
  /**
   * Speichert das Passwort.
   * @param password das Passwort.
   */
  public void setPassword(char[] password)
  {
    this.password = password;
  }

  /**
   * Speichert das Passwort.
   * @param password das Passwort.
   */
  public void setPassword(String password)
  {
    this.password = password == null ? null : password.toCharArray();
  }
}


/*********************************************************************
 * $Log: Login.java,v $
 * Revision 1.2  2009/06/10 11:25:53  willuhn
 * @N Transparente HTTP-Authentifizierung ueber Jameica (sowohl in GUI- als auch in Server-Mode) mittels ApplicationCallback
 *
 * Revision 1.1  2007/01/31 13:07:52  willuhn
 * @N Login-Dialog
 * @N SystemMessage
 *
 **********************************************************************/