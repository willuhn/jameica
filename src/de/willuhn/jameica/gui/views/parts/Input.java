/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/Input.java,v $
 * $Revision: 1.2 $
 * $Date: 2003/11/21 02:10:21 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.views.parts;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Group;

/**
 * Basisklasse fuer Eingabefelder.
 * @author willuhn
 */
public abstract class Input
{

  /**
   * Liefert den Wert des Eingabefelds.
   * Diese Funktion muss von allen abgeleiteten Klassen ueberschrieben
   * werden, denn ueber diese Funktion kommt der Controller an die Daten
   * des Dialogs.
   * @return Wert des Feldes.
   */
  public abstract String getValue();

  /**
   * Malt das Inputfeld in die uebergebene Gruppe.
   * Grund: Jedes Inputfeld sieht anders aus (Text, Combo, etc.) - daher muss
   * das auch jedes Input selbst machen.
   * @param group
   */
  public abstract void paint(Group group);

  /**
   * Erzeugt ein neues Grid, welches fuer das Input verwendet werden muss.
   * @return
   */
  protected GridData createGrid()
  {
    final GridData inputGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
    inputGrid.widthHint = 200;
    return inputGrid;
  }

}

/*********************************************************************
 * $Log: Input.java,v $
 * Revision 1.2  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 * Revision 1.1  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 **********************************************************************/