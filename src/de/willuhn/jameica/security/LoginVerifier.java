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

/**
 * Generisches Interface fuer einen Login-Verifier.
 */
public interface LoginVerifier
{
  /**
   * Prueft das Login.
   * @param username der Username. Optional.
   * @param password das Passwort.
   * @return true, wenn das Login korrekt ist.
   */
  public boolean verify(String username, char[] password);
}



/**********************************************************************
 * $Log: LoginVerifier.java,v $
 * Revision 1.1  2011/09/27 12:01:15  willuhn
 * @N Speicherung der Checksumme des Masterpasswortes nicht mehr noetig - jetzt wird schlicht geprueft, ob sich der Keystore mit dem eingegebenen Passwort oeffnen laesst
 *
 **********************************************************************/