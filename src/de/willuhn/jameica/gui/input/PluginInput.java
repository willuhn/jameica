/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/PluginInput.java,v $
 * $Revision: 1.2 $
 * $Date: 2012/03/28 22:28:07 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.input;

import de.willuhn.jameica.plugin.Plugin;
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
  public PluginInput(Plugin preselected)
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
    if (!(bean instanceof Plugin))
      return null;
    
    return ((Plugin)bean).getManifest().getName();
  }
}



/**********************************************************************
 * $Log: PluginInput.java,v $
 * Revision 1.2  2012/03/28 22:28:07  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.1  2011-08-11 10:36:53  willuhn
 * @N PluginInput - Selectbox zur Auswahl eines Plugins
 * @N SelectInput#setList, um den Inhalt der Liste zur Laufzeit zu aendern
 *
 **********************************************************************/