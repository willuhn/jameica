/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/Start.java,v $
 * $Revision: 1.9 $
 * $Date: 2004/01/23 00:29:03 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.views;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.Plugin;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.views.parts.LabelGroup;
import de.willuhn.util.I18N;


public class Start extends AbstractView
{

  /**
   * ct.
   * @param parent
   */
  public Start(Composite parent)
  {
    super(parent);
  }


  public void bind()
  {
    addHeadline("Jameica");
    
    LabelGroup plugins = new LabelGroup(getParent(),I18N.tr("installierte Plugins"));
    
    ArrayList installedPlugins = PluginLoader.getInstalledPlugins();
    for (int i=0;i<installedPlugins.size();++i)
    {
      Plugin plugin = (Plugin) installedPlugins.get(i);
      plugins.addText(plugin.getName() + " " + plugin.getVersion(),false);
    }
  }        


  public void unbind()
  {
  }

}

/***************************************************************************
 * $Log: Start.java,v $
 * Revision 1.9  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.7  2003/12/29 17:44:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.5  2003/12/05 17:12:23  willuhn
 * @C SelectInput
 *
 * Revision 1.4  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
 * Revision 1.3  2003/10/29 00:41:27  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/10/23 22:36:34  willuhn
 * @N added Menu
 *
 * Revision 1.1  2003/10/23 21:50:06  willuhn
 * initial checkin
 *
 ***************************************************************************/