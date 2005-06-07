/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/extension/Extension.java,v $
 * $Revision: 1.4 $
 * $Date: 2005/06/07 21:57:32 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.extension;

/**
 * Basis-Interface aller Extensions.
 * Jede Komponente, die in Jameica andere Komponenten (zB Context-Menus)
 * erweitern will, muss dieses Interface implementieren und einen
 * parameterlosen Konstruktor mit dem Modifier <code>public</code>
 * besitzen, um korrekt registriert zu werden.
 */
public interface Extension
{
  /**
   * Diese Funktion wird von der ExtensionRegistry aufgerufen.
   * Die Funktion erhaelt als Parameter die zu erweiternde Komponente.
   * @param extendable
   */
  public void extend(Extendable extendable);
}


/*********************************************************************
 * $Log: Extension.java,v $
 * Revision 1.4  2005/06/07 21:57:32  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/06/06 10:10:43  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/05/27 17:31:46  web0
 * @N extension system
 *
 * Revision 1.1  2005/05/25 16:11:47  web0
 * @N first code for extension system
 *
 *********************************************************************/