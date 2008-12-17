/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/formatter/TreeFormatter.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/12/17 22:44:18 $
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
 * Revision 1.2  2008/12/17 22:44:18  willuhn
 * @R t o d o  tag entfernt
 *
 * Revision 1.1  2007/03/21 18:42:16  willuhn
 * @N Formatter fuer TreePart
 * @C mehr gemeinsamer Code in AbstractTablePart
 *
 **********************************************************************/