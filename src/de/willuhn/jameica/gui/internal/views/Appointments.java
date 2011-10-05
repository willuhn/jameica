/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/views/Appointments.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/10/05 16:57:04 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.views;

import java.util.Date;
import java.util.List;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.calendar.AppointmentProvider;
import de.willuhn.jameica.gui.calendar.AppointmentProviderRegistry;
import de.willuhn.jameica.gui.calendar.ReminderCalendarPart;
import de.willuhn.jameica.gui.internal.action.ReminderAppointmentDetails;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt die Termine an.
 */
@Lifecycle(Type.CONTEXT)
public class Appointments extends AbstractView
{
  private Date currentDate              = null;
  private ReminderCalendarPart calendar = null;
  
  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    I18N i18n = Application.getI18n();
    GUI.getView().setTitle(i18n.tr("Termine"));
    
    this.calendar = new ReminderCalendarPart(); // hier sind schon die Jameica-Termine drin
    this.calendar.setCurrentDate(this.currentDate);
    
    // Appointment-Provider der Plugins hinzufuegen 
    List<AbstractPlugin> plugins = Application.getPluginLoader().getInstalledPlugins();
    for (AbstractPlugin plugin:plugins)
    {
      List<AppointmentProvider> providers = AppointmentProviderRegistry.getAppointmentProviders(plugin);
      for (AppointmentProvider provider:providers)
      {
        this.calendar.addAppointmentProvider(provider);
      }
    }

    this.calendar.paint(this.getParent());

    // Button zum Hinzufuegen eines Termins
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Neuer Termin..."),new ReminderAppointmentDetails(),null,false,"document-new.png");
    buttons.paint(this.getParent());
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
    if (this.calendar != null)
      this.currentDate = this.calendar.getCurrentDate();
  }
}



/**********************************************************************
 * $Log: Appointments.java,v $
 * Revision 1.1  2011/10/05 16:57:04  willuhn
 * @N Refactoring des Reminder-Frameworks. Hat jetzt eine brauchbare API und wird von den Freitext-Remindern von Jameica verwendet
 * @N Jameica besitzt jetzt einen integrierten Kalender, der die internen Freitext-Reminder anzeigt (dort koennen sie auch angelegt, geaendert und geloescht werden) sowie die Appointments aller Plugins
 *
 **********************************************************************/