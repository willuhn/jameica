/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/LoginVerifier.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/09/27 12:01:15 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
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