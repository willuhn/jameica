/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.dialogs;

import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.internal.buttons.Cancel;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.PluginData;
import de.willuhn.jameica.update.PluginGroup;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog, der die Plugin-Abhaengigkeiten anzeigt, die mit heruntergeladen werden koennen.
 */
public class DependencyDownloadDialog extends AbstractDialog<Object>
{
  private static final I18N i18n = Application.getI18n();
  private List<PluginData> data;

  /**
   * ct.
   * @param position Dialog-Position.
   * @param data die Plugin-Daten.
   */
  public DependencyDownloadDialog(int position, List<PluginData> data)
  {
    super(position);
    this.data = data;
    this.setTitle(i18n.tr("Zu installierende Abhängigkeiten"));
    this.setSize(460,300);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    SimpleContainer container = new SimpleContainer(parent,true);
    container.addText(i18n.tr("Das Plugin benötigt noch weitere abhängige Plugins, die noch nicht (oder nicht in der passenden Version) " +
    		                      "installiert sind. Sie werden automatisch ebenfalls mit heruntergeladen und installiert."),true);
    
    TablePart deps = new TablePart(this.data,null);
    deps.addColumn(i18n.tr("Name des Plugins"), "name");
    deps.addColumn(i18n.tr("Version"),"availableVersion");
    deps.addColumn(i18n.tr("Installation von"),"pluginGroup",new Formatter() {
      private static final long serialVersionUID = 968216114721601752L;

      public String format(Object o)
      {
        if (!(o instanceof PluginGroup))
          return "";
        
        PluginGroup g = (PluginGroup) o;
        return g.getName() + " (" + g.getRepository().getUrl() + ")";
      }
    });
    deps.setMulti(false);
    deps.setRememberColWidths(false);
    deps.setRememberOrder(true);
    deps.removeFeature(FeatureSummary.class);
    
    container.addPart(deps);
    
    container.addText("\n" + i18n.tr("Sind Sie sicher, daß Sie das Plugin und die genannten Abhängigkeiten jetzt herunterladen und installieren möchten?"),true);
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton("   " + i18n.tr("Ja" + "   "),new Action() {
    
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,true,"ok.png");
    buttons.addButton(new Cancel());
    
    container.addButtonArea(buttons);
  }

}
