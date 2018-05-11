/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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