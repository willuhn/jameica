/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.boxes;

import java.rmi.RemoteException;
import java.util.Objects;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.internal.action.RepositoryEdit;
import de.willuhn.jameica.gui.internal.action.Settings;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.services.RepositoryService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Eine Box, die darauf hinweist, wenn zusätzliche Plugin-Repositories installiert sind.
 */
public class PluginRepositories extends AbstractBox
{
  private final static I18N i18n = Application.getI18n();
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Jameica: " + Application.getI18n().tr("Aktive Plugin-Repositories von Drittanbietern");
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#getHeight()
   */
  @Override
  public int getHeight()
  {
    return 260;
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isActive()
   */
  @Override
  public boolean isActive()
  {
    return this.haveExternal();
  }
  
  /**
   * Liefert true, wenn externe Repositories aktiv sind.
   * @return true, wenn externe Repositories aktiv sind.
   */
  private boolean haveExternal()
  {
    final RepositoryService service = Application.getBootLoader().getBootable(RepositoryService.class);
    return service.getRepositories().stream().anyMatch(u -> !Objects.equals(u.getHost(),"www.willuhn.de"));
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return this.haveExternal();
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
    final InfoPanel panel = new InfoPanel() {
      /**
       * @see de.willuhn.jameica.gui.parts.InfoPanel#extend(de.willuhn.jameica.gui.parts.InfoPanel.DrawState, org.eclipse.swt.widgets.Composite, java.lang.Object)
       */
      @Override
      public Composite extend(DrawState state, Composite comp, Object context)
      {
        if (state == DrawState.COMMENT_AFTER)
        {
          final CheckboxInput dismiss = new CheckboxInput(false);
          dismiss.setName(i18n.tr("Diesen Hinweis nicht mehr anzeigen"));
          dismiss.addListener(e -> {
            if (!((Boolean)dismiss.getValue()).booleanValue())
              return; // Den Fall gibt es nicht, weil die Box dann nicht angezeigt wird
            try
            {
              if (Application.getCallback().askUser(i18n.tr("Sind Sie sicher, dass dieser Hinweis nicht mehr angezeigt werden soll?")))
              {
                PluginRepositories.this.setEnabled(false);
                GUI.getCurrentView().reload();
              }
              else
                dismiss.setValue(false);
            }
            catch (Exception ex)
            {
              Logger.error("unable to dismiss box",ex);
            }
          });
          dismiss.paint(comp);
        }
        return super.extend(state, comp, context);
      }
    };
    panel.setTitle(i18n.tr(i18n.tr("Hinweis zu Plugin-Repositories von Drittanbietern")));
    panel.setIcon("dialog-question-large.png");

    panel.setText(i18n.tr("Derzeit sind Plugin-Repositories von Drittanbietern aktiviert. " +
                          "Bei der Suche nach Updates wird Jameica diese kontaktieren und ggf. Sicherheitsabfragen für die Zertifikate " +
                          "der Server anzeigen."));
    panel.setComment(i18n.tr("Klicken Sie auf \"Repositories bearbeiten...\", um Plugin-Repositories zu deaktivieren, " +
                             "von denen Sie keine Plugins nutzen. Sie können diese später jederzeit wieder in \"Datei->Einstellungen->Plugins\" aktivieren"));
    panel.setUrl("https://www.willuhn.de/wiki/doku.php?id=support:faq#sicherheitsabfrage_fuer_zertifikat");
    
    panel.addButton(new Button(i18n.tr("Repositories bearbeiten..."),new RepositoryEdit(),null,false,"document-properties.png"));
    panel.addButton(new Button(i18n.tr("Installierte Plugins..."),new Settings(),1,false,"emblem-package.png"));
    panel.paint(parent);
  }
}
