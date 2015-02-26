/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/PanelButtonBack.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/09/12 07:55:45 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.swt.SWT;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.action.Back;
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Platform;

/**
 * Vorkonfigurierter Panel-Button fuer Zurueck.
 */
public class PanelButtonBack extends PanelButton
{
  private final static String backKeyStroke =
      SWTKeySupport.getKeyFormatterForPlatform().format(KeyStroke.getInstance(Application.getPlatform().getOS() == Platform.OS_MAC?SWT.COMMAND:SWT.ALT, SWT.ARROW_LEFT));

  /**
   * ct.
   */
  public PanelButtonBack()
  {
    super("go-previous.png",new Back(),Application.getI18n().tr("Zurück") + " (" + backKeyStroke + ")");
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
 * Revision 1.3  2011/09/12 07:55:45  willuhn
 * @C BUGZILLA 811
 *
 * Revision 1.2  2011-04-26 12:01:42  willuhn
 * @D javadoc Fixes
 *
 * Revision 1.1  2011-04-06 16:13:16  willuhn
 * @N BUGZILLA 631
 *
 **********************************************************************/