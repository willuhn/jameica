/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/Formatter.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/12/29 20:07:19 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.views.parts;

/**
 * Kleine Formatierer-Klasse.
 * Hintergrund: Will man z.Bsp. die Formatierung von Geldbetraegen zentral
 * erledigen, kann man einen solchen Formatter verwenden.
 * @author willuhn
 */
public interface Formatter
{

  /**
   * Formatiert das uebergebene Objekt.
   * @param o das zu formatierende Objekt.
   * @return formatierte String-Repraesentation.
   */
  public String format(Object o);
}

/*********************************************************************
 * $Log: Formatter.java,v $
 * Revision 1.1  2003/12/29 20:07:19  willuhn
 * @N Formatter
 *
 **********************************************************************/