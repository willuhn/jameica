/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/Start.java,v $
 * $Revision: 1.4 $
 * $Date: 2003/11/24 23:01:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.views;

import java.util.ArrayList;

import de.willuhn.jameica.I18N;
import de.willuhn.jameica.Plugin;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.views.parts.Headline;
import de.willuhn.jameica.views.parts.LabelGroup;
import de.willuhn.jameica.views.parts.LabelInput;


public class Start extends AbstractView
{

  public Start(Object o)
  {
    super(o);
  }

  public void bind()
  {
    Headline headline = new Headline(getParent(),"Jameica");
    
    LabelGroup plugins = new LabelGroup(getParent(),I18N.tr("installierte Plugins"));
    
    ArrayList installedPlugins = PluginLoader.getInstalledPlugins();
    for (int i=0;i<installedPlugins.size();++i)
    {
      Plugin plugin = (Plugin) installedPlugins.get(i);
      LabelInput pluginLabel = new LabelInput("Version: " + plugin.getVersion());
      plugins.addLabelPair(plugin.getName(),pluginLabel);
    }
  }        


  public void unbind()
  {
  }

}

/***************************************************************************
 * $Log: Start.java,v $
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