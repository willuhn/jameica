/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Attic/AbstractFormatter.java,v $
 * $Revision: 1.1 $
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
 * Basis-Klasse fuer die Formatter.
 * Enthaelt bereits des Styling-Kram.
 */
public abstract class AbstractFormatter implements Formatter {

  private StyleContainer style;

  /**
   * ct.
   */
  public AbstractFormatter() {
    style = new StyleContainer();
  }

  /**
   * @see de.willuhn.jameica.gui.parts.Formatter#getStyle()
   */
  public StyleContainer getStyle() {
    return style;
  }

  /**
   * @see de.willuhn.jameica.gui.parts.Formatter#setStyle(de.willuhn.jameica.gui.util.StyleContainer)
   */
  public void setStyle(StyleContainer style) {
    this.style = style;
  }
}


/**********************************************************************
 * $Log: AbstractFormatter.java,v $
 * Revision 1.1  2004/03/11 08:56:55  willuhn
 * @C some refactoring
 *
 **********************************************************************/