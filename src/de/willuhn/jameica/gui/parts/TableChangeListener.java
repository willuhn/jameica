/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts;

import de.willuhn.util.ApplicationException;

/**
 * Ein Listener, der an eine Tabelle via <code>addChangeListener()</code> gehaengt werden kann, wenn einzelne
 * Spalten direkt in der Tabelle aenderbar sein sollen. Die Spalten muessen
 * mit der Funktion <code>table.addColumn(String title, String field, Formatter f, boolean changeable)</code>
 * hinzugefuegt werden, wobei changeable=true sein muss um die Spalten als aenderbar
 * zu markieren. Sofern der Wert eines solchen Feldes vom Benutzer geaendert
 * wurde, werden alle registrieren TableChangeListener ueber die Aenderung
 * informiert. Es ist dann deren Aufgabe, den geaenderten Wert im Fachobjekt
 * zu uebernehmen.  
 */
public interface TableChangeListener
{
  /**
   * Wird aufgerufen, wenn der Wert eines Feldes geaendert wurde.
   * @param object das zugehoerige Fachobjekt.
   * @param attribute der Name des geaenderten Attributes.
   * @param newValue der neue Wert des Attributes.
   * @throws ApplicationException
   */
  public void itemChanged(Object object, String attribute, String newValue) throws ApplicationException;
  
}


/*********************************************************************
 * $Log: TableChangeListener.java,v $
 * Revision 1.2  2007/04/10 23:42:56  willuhn
 * @N TablePart Redesign (removed dependencies from GenericIterator/GenericObject)
 *
 * Revision 1.1  2005/06/29 16:54:38  web0
 * @N editierbare Tabellen
 *
 *********************************************************************/