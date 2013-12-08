/**********************************************************************
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.views;

import java.util.List;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.action.PluginDownload;
import de.willuhn.jameica.gui.internal.parts.PluginTree;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.PluginData;
import de.willuhn.util.ApplicationException;

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
    
    final PluginTree tree = new PluginTree((List<PluginData>)this.getCurrentObject());
    final Button button = new Button(Application.getI18n().tr("Herunterladen und installieren..."),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        Object ctx = tree.getSelection();
        if (ctx instanceof PluginData)
          new PluginDownload().handleAction(ctx);
      }
    },null,false,"document-save.png");
    button.setEnabled(false);

    Container container = new SimpleContainer(this.getParent(),true);
    tree.paint(container.getComposite());
    tree.addSelectionListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        button.setEnabled(event.data instanceof PluginData);
      }
    });
    
    ButtonArea buttons = new ButtonArea();
    
    buttons.addButton(button);
    buttons.paint(container.getComposite());
  }
  
  /**
   * @see de.willuhn.jameica.gui.AbstractView#canBookmark()
   */
  public boolean canBookmark()
  {
    return false;
  }

}
