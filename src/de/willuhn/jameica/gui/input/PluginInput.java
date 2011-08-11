/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/PluginInput.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/08/11 10:36:53 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.input;

import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;

/**
 * Selectbox zur Auswahl eines Plugins.
 */
public class PluginInput extends SelectInput
{
  /**
   * ct.
   */
  public PluginInput()
  {
    this(null);
  }

  /**
   * ct.
   * @param preselected das vorausgewaehle Plugin.
   */
  public PluginInput(AbstractPlugin preselected)
  {
    super(Application.getPluginLoader().getInstalledPlugins(), preselected);
    this.setName(Application.getI18n().tr("Plugin"));
    this.setPleaseChoose(Application.getI18n().tr("Bitte wählen..."));
  }

  /**
   * @see de.willuhn.jameica.gui.input.SelectInput#format(java.lang.Object)
   */
  protected String format(Object bean)
  {
    if (!(bean instanceof AbstractPlugin))
      return null;
    
    return ((AbstractPlugin)bean).getManifest().getName();
  }
}



/**********************************************************************
 * $Log: PluginInput.java,v $
 * Revision 1.1  2011/08/11 10:36:53  willuhn
 * @N PluginInput - Selectbox zur Auswahl eines Plugins
 * @N SelectInput#setList, um den Inhalt der Liste zur Laufzeit zu aendern
 *
 **********************************************************************/