/**********************************************************************
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.views;

import java.util.List;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.parts.PluginTree;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.PluginData;

/**
 * Zeigt eine Liste der gefundenen Updates an.
 */
public class UpdatesView extends AbstractView
{
  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    GUI.getView().setTitle(Application.getI18n().tr("Verfügbare Updates"));
    
    Container container = new SimpleContainer(this.getParent(),true);
    new PluginTree((List<PluginData>)this.getCurrentObject()).paint(container.getComposite());
  }
  
  /**
   * @see de.willuhn.jameica.gui.AbstractView#canBookmark()
   */
  public boolean canBookmark()
  {
    return false;
  }

}
