/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/Input.java,v $
 * $Revision: 1.5 $
 * $Date: 2003/11/24 23:01:58 $
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
import org.eclipse.swt.widgets.Composite;

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
   * Malt das Inputfeld in das uebergebene Composite.
   * Grund: Jedes Inputfeld sieht anders aus (Text, Combo, etc.) - daher muss
   * das auch jedes Input selbst machen.
   * @param group
   */
  public abstract void paint(Composite parent);

  /**
   * Gibt diesem Eingabefeld den Focus.
   */
  public abstract void focus();
  
  /**
   * Erzeugt ein neues Grid, welches fuer das Input verwendet werden muss.
   * @return
   */
  protected GridData createGrid()
  {
    final GridData inputGrid = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    inputGrid.widthHint = 240;
    return inputGrid;
  }

}

/*********************************************************************
 * $Log: Input.java,v $
 * Revision 1.5  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
 * Revision 1.4  2003/11/24 14:21:53  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/11/22 20:43:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 * Revision 1.1  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 **********************************************************************/