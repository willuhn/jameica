/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/dialogs/AppointmentProviderDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/10/06 10:49:08 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.dialogs;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.calendar.AppointmentProvider;
import de.willuhn.jameica.gui.calendar.AppointmentProviderRegistry;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Dialog, ueber den konfiguriert werden kann, welche Appointment-Provider
 * fuer die Termin-Anzeige verwendet werden sollen.
 */
public class AppointmentProviderDialog extends AbstractDialog<List<AppointmentProvider>>
{
  private TablePart table                    = null;
  private List<AppointmentProvider> selected = null;
  
  /**
   * ct.
   * @param position Position des Dialogs.
   */
  public AppointmentProviderDialog(int position)
  {
    super(position);
    this.setSize(400,400);
    setTitle(i18n.tr("Auswahl der anzuzeigenden Kalender"));
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container c = new SimpleContainer(parent,true);
    c.addText(i18n.tr("Wählen Sie die anzuzeigenden Kalender aus."),true);

    this.table = new TablePart(null);
    this.table.addColumn(i18n.tr("Bezeichnung"),"name");
    this.table.setCheckable(true);
    this.table.setMulti(false);
    this.table.setSummary(false);
    this.table.setRememberOrder(false);
    this.table.setFormatter(new TableFormatter() {
      public void format(TableItem item)
      {
        try
        {
          AppointmentProvider provider = (AppointmentProvider) item.getData();
          AbstractPlugin plugin = Application.getPluginLoader().findByClass(provider.getClass());
          String name = provider.getName();
          if (plugin != null)
            name = plugin.getManifest().getName() + ": " + name;
          item.setText(name);
        }
        catch (Exception e)
        {
          Logger.error("unable to format item",e);
        }
      }
    });
    c.addPart(table);

    List<AbstractPlugin> plugins = Application.getPluginLoader().getInstalledPlugins();
    for (AbstractPlugin plugin:plugins)
    {
      List<AppointmentProvider> providers = AppointmentProviderRegistry.getAppointmentProviders(plugin);
      Collections.sort(providers,new Comparator<AppointmentProvider>() {
        public int compare(AppointmentProvider a1, AppointmentProvider a2)
        {
          return StringUtils.trimToEmpty(a1.getName()).compareTo(StringUtils.trimToEmpty(a2.getName()));
        }
        
      });
      for (AppointmentProvider provider:providers)
      {
        this.table.addItem(provider,AppointmentProviderRegistry.isEnabled(provider));
      }
    }

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Übernehmen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          // Ausgewaehlte uebernehmen
          selected = table.getItems(true);
          
          // Aenderung fuer alle speichern
          List<AppointmentProvider> all = table.getItems(false);
          for (AppointmentProvider provider:all)
          {
            AppointmentProviderRegistry.setEnabled(provider,selected.contains(provider));
          }
        }
        catch (RemoteException re)
        {
          Logger.error("unable to get items",re);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehlgeschlagen: {0}",re.getMessage()),StatusBarMessage.TYPE_ERROR));
        }
        close();
      }
    },null,true,"ok.png");
    buttons.addButton(i18n.tr("Abbrechen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    },null,false,"process-stop.png");
    c.addButtonArea(buttons);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   * die Liste der derzeit aktiven Kalender.
   */
  protected List<AppointmentProvider> getData() throws Exception
  {
    return selected;
  }
}



/**********************************************************************
 * $Log: AppointmentProviderDialog.java,v $
 * Revision 1.1  2011/10/06 10:49:08  willuhn
 * @N Termin-Provider konfigurierbar
 *
 **********************************************************************/