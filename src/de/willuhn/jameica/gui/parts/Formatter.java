/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Attic/Formatter.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/04/05 23:29:26 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.parts;


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
 * Revision 1.3  2004/04/05 23:29:26  willuhn
 * *** empty log message ***
 *
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