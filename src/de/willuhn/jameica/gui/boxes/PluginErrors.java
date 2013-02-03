/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/boxes/FirstStart.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/06/09 10:07:59 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.boxes;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.internal.parts.PluginDetailPart;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;

/**
 * Eine Box, die angezeigt wird, wenn Plugins nicht geladen werden konnten.
 */
public class PluginErrors extends AbstractBox
{
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Jameica: " + Application.getI18n().tr("Plugin-Fehler");
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isActive()
   */
  public boolean isActive()
  {
    return isEnabled();
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isEnabled()
   */
  public boolean isEnabled()
  {
    // Nur anzeigen, wenn wir Fehler haben
    return Application.getPluginLoader().getInitErrors().size() > 0;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled)
  {
    // Das darf der User nicht.
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 0;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    Map<Manifest,Throwable> map = Application.getPluginLoader().getInitErrors();
    Iterator<Manifest> i = map.keySet().iterator();
    
    while (i.hasNext())
    {
      Manifest mf = i.next();
      PluginDetailPart detail = new PluginDetailPart(mf);
      detail.paint(parent);
    }
  }
}
