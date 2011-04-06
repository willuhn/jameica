/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/PanelButtonBack.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/04/06 16:13:16 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.action.Back;
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.system.Application;

/**
 * Vorkonfigurierter Panel-Button fuer Zurueck.
 */
public class PanelButtonBack extends PanelButton
{
  public PanelButtonBack()
  {
    super("go-previous.png",new Back(),Application.getI18n().tr("Zurück"));
  }
  
  /**
   * Wir liefern nur dann true, wenn eine vorherige Seite existiert.
   * @see de.willuhn.jameica.gui.parts.PanelButton#isEnabled()
   */
  public boolean isEnabled()
  {
    return GUI.hasPreviousView();
  }
}



/**********************************************************************
 * $Log: PanelButtonBack.java,v $
 * Revision 1.1  2011/04/06 16:13:16  willuhn
 * @N BUGZILLA 631
 *
 **********************************************************************/