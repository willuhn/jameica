/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/views/Appointments.java,v $
 * $Revision: 1.5 $
 * $Date: 2012/03/28 22:28:07 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.views;

import java.util.Date;
import java.util.List;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.calendar.AppointmentProvider;
import de.willuhn.jameica.gui.calendar.AppointmentProviderRegistry;
import de.willuhn.jameica.gui.calendar.ReminderCalendarPart;
import de.willuhn.jameica.gui.internal.action.ReminderAppointmentDetails;
import de.willuhn.jameica.gui.internal.dialogs.AppointmentProviderDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.Plugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt die Termine an.
 */
public class Appointments extends AbstractView
{
  private static Date currentDate       = null;
  private ReminderCalendarPart calendar = null;
  
  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    I18N i18n = Application.getI18n();

    GUI.getView().setTitle(i18n.tr("Termine"));
    
    final Action configure = new Configure();
    
    this.calendar = new ReminderCalendarPart(); // hier sind schon die Jameica-Termine drin
    this.calendar.setCurrentDate(currentDate);

    GUI.getView().addPanelButton(new PanelButton("document-properties.png",configure,i18n.tr("Anzuzeigende Kalender auswählen")));
    
    // Appointment-Provider der Plugins hinzufuegen 
    List<Plugin> plugins = Application.getPluginLoader().getInstalledPlugins();
    for (Plugin plugin:plugins)
    {
      List<AppointmentProvider> providers = AppointmentProviderRegistry.getAppointmentProviders(plugin);
      for (AppointmentProvider provider:providers)
      {
        if (!AppointmentProviderRegistry.isEnabled(provider))
          continue;
        this.calendar.addAppointmentProvider(provider);
      }
    }

    this.calendar.paint(this.getParent());

    // Button zum Hinzufuegen eines Termins
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Neuer Termin..."),new ReminderAppointmentDetails(),null,true,"document-new.png");
    buttons.addButton(i18n.tr("Kalender auswählen..."),configure,null,false,"document-properties.png");
    buttons.paint(this.getParent());
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
    if (this.calendar != null)
      currentDate = this.calendar.getCurrentDate();
  }
  
  /**
   * Aktion zum Konfigurieren der Kalender.
   */
  private class Configure implements Action
  {
    public void handleAction(Object context) throws ApplicationException
    {
      try
      {
        AppointmentProviderDialog d = new AppointmentProviderDialog(AppointmentProviderDialog.POSITION_CENTER);
        List<AppointmentProvider> selected = d.open();
        
        // Kalender-Anzeige aktualisieren
        calendar.removeAll();
        for (AppointmentProvider p:selected)
        {
          calendar.addAppointmentProvider(p);
        }
        calendar.refresh();
      }
      catch (ApplicationException ae)
      {
        throw ae;
      }
      catch (OperationCanceledException oce)
      {
        // ignore
      }
      catch (Exception e)
      {
        Logger.error("unable to configure appointment providers",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehlgeschlagen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
      }
    }
  }
}



/**********************************************************************
 * $Log: Appointments.java,v $
 * Revision 1.5  2012/03/28 22:28:07  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.4  2011/12/13 23:09:13  willuhn
 * @R Kein Context-Lifecycle - die Appointment-Provider werden sonst nicht mehr aktualisiert
 *
 * Revision 1.3  2011-10-07 11:01:40  willuhn
 * @N Zusaetzlicher Config-Button
 *
 * Revision 1.2  2011-10-06 10:49:08  willuhn
 * @N Termin-Provider konfigurierbar
 *
 * Revision 1.1  2011-10-05 16:57:04  willuhn
 * @N Refactoring des Reminder-Frameworks. Hat jetzt eine brauchbare API und wird von den Freitext-Remindern von Jameica verwendet
 * @N Jameica besitzt jetzt einen integrierten Kalender, der die internen Freitext-Reminder anzeigt (dort koennen sie auch angelegt, geaendert und geloescht werden) sowie die Appointments aller Plugins
 *
 **********************************************************************/