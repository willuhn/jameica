/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Attic/Formatter.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/03/11 08:56:55 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.parts;

import de.willuhn.jameica.gui.util.StyleContainer;

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

  /**
   * Liefert einen Style-Container, der alle relevanten Styling-Informationen enthaelt.
   * @return
   */
  public StyleContainer getStyle();
  
  /**
   * Speichert den Style-Container.
   * @param style
   */
  public void setStyle(StyleContainer style);
  
  
}

/*********************************************************************
 * $Log: Formatter.java,v $
 * Revision 1.2  2004/03/11 08:56:55  willuhn
 * @C some refactoring
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.1  2003/12/29 20:07:19  willuhn
 * @N Formatter
 *
 **********************************************************************/