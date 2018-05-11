/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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

/**
 * Vorkonfigurierter Panel-Button fuer Zurueck.
 */
public class PanelButtonBack extends PanelButton
{
  private final static String backKeyStroke = SWTKeySupport.getKeyFormatterForPlatform().format(KeyStroke.getInstance(Application.getPlatform().mapSWTKey(SWT.ALT), SWT.ARROW_LEFT));

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
