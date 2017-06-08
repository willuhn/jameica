/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.views;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.action.PluginDownload;
import de.willuhn.jameica.gui.internal.action.Settings;
import de.willuhn.jameica.gui.internal.controller.RepositoryContol;
import de.willuhn.jameica.gui.internal.parts.PluginTree;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.PluginData;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Detail-Ansicht eines Repositories.
 */
public class RepositoryDetails extends AbstractView
{
  private final static I18N i18n = Application.getI18n();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    RepositoryContol control = new RepositoryContol(this);

    GUI.getView().setTitle(i18n.tr("Repository: {0}",control.getRepository().getName()));
    
    SimpleContainer group = new SimpleContainer(getParent(),true);
    group.addText(Application.getI18n().tr("Wählen Sie ein Plugin-Repository, um die dort verfügbaren Plugins anzuzeigen."),true);
    group.addInput(control.getRepositories());

    final PluginTree tree = control.getPlugins();
    final Button download = new Button(Application.getI18n().tr("Herunterladen und installieren..."),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        Object ctx = tree.getSelection();
        if (ctx instanceof PluginData)
          new PluginDownload().handleAction(ctx);
      }
    },null,false,"document-save.png");
    download.setEnabled(false);
    
    tree.addSelectionListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        download.setEnabled(event.data instanceof PluginData);
      }
    });

    group.addPart(tree);
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(download);
    buttons.addButton(i18n.tr("Plugin-Repositories verwalten"),new Settings(),i18n.tr("Updates"),false,"document-properties.png");
    group.addButtonArea(buttons);
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#canBookmark()
   */
  public boolean canBookmark()
  {
    return false;
  }

}
