/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.dialogs;

import java.util.Arrays;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.plugin.Dependency;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.PluginData;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * FIXME: Es waere schoen, wenn der Dialog nicht nur fehlende Abhaengigkeiten anzeigen wuerde
 * sondern auch gleich den Download (insofern im Repo verfuegbar) uebernehmen koennte, sodass
 * man nicht jedes Plugin einzeln runterladen muss sondern jameica.update alle benoetigten
 * Abhaengigkeiten gleich mit runterladen wuerde.
 * Dialog, der die Plugin-Abhaengigkeiten anzeigt.
 */
public class DependencyDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getI18n();
  private PluginData data = null;

  /**
   * ct.
   * @param position Dialog-Position.
   * @param data die Plugin-Daten.
   */
  public DependencyDialog(int position, PluginData data)
  {
    super(position);
    this.data = data;
    this.setTitle(i18n.tr("Fehlende Abhängigkeiten"));
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
    SimpleContainer container = new SimpleContainer(parent);
    container.addText(i18n.tr("Das Plugin \"{0}\" kann nicht installiert werden, da es noch weitere Plugins benötigt,\n" +
    		                      "die noch nicht (oder nicht in der passenden Version) installiert sind. Eventuell ist\n" +
    		                      "das Plugin auch bereits im Programm-Ordner von Jameica installiert und Sie besitzen dort\n" +
    		                      "keine Schreibrechte.\n\n" +
    		                      "Bitte installieren Sie zunächst die abhängigen (und nicht als optional gekennzeichneten)\n" +
    		                      "Plugins und versuchen Sie es dann erneut.",this.data.getName()),true);
    
    TablePart deps = new TablePart(Arrays.asList(data.getDependencies()),null);
    deps.addColumn(i18n.tr("Name des Plugins"), "name");
    deps.addColumn(i18n.tr("Benötigte Version"),"version");
    deps.addColumn(i18n.tr("optional"),         "version");
    deps.addColumn(i18n.tr("Abhängigkeit erfüllt"),"version");
    deps.setFormatter(new TableFormatter() {
    
      public void format(TableItem item)
      {
        try
        {
          Dependency dep = (Dependency) item.getData();
          item.setText(2,dep.isRequired() ? i18n.tr("nein") : i18n.tr("ja"));
          if (dep.check())
          {
            item.setText(3,i18n.tr("ja"));
            item.setForeground(Color.SUCCESS.getSWTColor());
          }
          else
          {
            item.setText(3,i18n.tr("nein"));
            item.setForeground(Color.ERROR.getSWTColor());
          }
          
        }
        catch (Exception e)
        {
          Logger.error("unable to format column",e);
        }
      }
    });
    deps.setMulti(false);
    deps.setRememberColWidths(false);
    deps.setRememberOrder(true);
    deps.setSummary(false);
    
    container.addPart(deps);
    
    ButtonArea buttons = new ButtonArea(parent,1);
    buttons.addButton(i18n.tr("Schließen"),new Action() {
    
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,true,"process-stop.png");
    this.getShell().pack();
  }

}
