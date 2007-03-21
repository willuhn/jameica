/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/formatter/TreeFormatter.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/03/21 18:42:16 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.formatter;

import org.eclipse.swt.widgets.TreeItem;

/**
 * Klasse, welche fuer die Formatierung von Trees verwendet werden kann.
 * TODO: Waere schoen, wenn man das mit TableFormatter zusammenlegen
 * koennte. Nur leider haben swt.TableItem und swt.TreeItem keine
 * gemeinsame Basisklasse mit Format-Funktionen.
 */
public interface TreeFormatter {
	
	/**
	 * Formatiert ein TreeItem.
   * @param item das zu formatierende Item.
   */
  public void format(TreeItem item); 
}


/**********************************************************************
 * $Log: TreeFormatter.java,v $
 * Revision 1.1  2007/03/21 18:42:16  willuhn
 * @N Formatter fuer TreePart
 * @C mehr gemeinsamer Code in AbstractTablePart
 *
 **********************************************************************/