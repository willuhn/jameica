/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.ext;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.SpinnerInput;
import de.willuhn.jameica.gui.internal.parts.RepositoryList;
import de.willuhn.jameica.gui.internal.views.Settings;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.SettingsChangedMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.UpdateService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Erweitert die View mit dem System-Einstellungen um die Update-Optionen.
 */
public class UpdateSettingsView implements Extension
{
  private CheckboxInput updateCheck   = null;
  private SpinnerInput updateInterval = null;
  private SelectInput updateInstall   = null;
  
  private MessageConsumer mc = null;
  
  private UpdateService service = null;
  
  /**
   * Liefert den Update-Service.
   * @return der Update-Service.
   */
  private UpdateService getUpdateService()
  {
    if (this.service == null)
      this.service = Application.getBootLoader().getBootable(UpdateService.class);
    
    return this.service;
  }
  
  /**
   * @see de.willuhn.jameica.gui.extension.Extension#extend(de.willuhn.jameica.gui.extension.Extendable)
   */
  public void extend(Extendable extendable)
  {
    if (extendable == null || !(extendable instanceof Settings))
      return;

    this.mc = new MessageConsumer() {

      /**
       * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
       */
      public void handleMessage(Message message) throws Exception
      {
        handleStore();
      }

      /**
       * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
       */
      public Class[] getExpectedMessageTypes()
      {
        return new Class[]{SettingsChangedMessage.class};
      }

      /**
       * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
       */
      public boolean autoRegister()
      {
        return false;
      }
    };
    Application.getMessagingFactory().registerMessageConsumer(this.mc);

    
    Settings settings = (Settings) extendable;
    
    try
    {
      TabGroup tab = new TabGroup(settings.getTabFolder(),Application.getI18n().tr("Updates"),true);
      
      // Da wir keine echte View sind, haben wir auch kein unbind zum Aufraeumen.
      // Damit wir unsere GUI-Elemente aber trotzdem disposen koennen, registrieren
      // wir einen Dispose-Listener an der Tabgroup
      tab.getComposite().addDisposeListener(new DisposeListener() {
      
        public void widgetDisposed(DisposeEvent e)
        {
          updateCheck = null;
          updateInstall = null;
          updateInterval = null;
          Application.getMessagingFactory().unRegisterMessageConsumer(mc);
        }
      
      });
      tab.addHeadline(Application.getI18n().tr("Plugin-Repositories"));
      tab.addText(Application.getI18n().tr("Klicken Sie doppelt auf eine URL, um die dort verfügbaren Plugins anzuzeigen."),true);
      tab.addPart(new RepositoryList());
      
      tab.addHeadline(Application.getI18n().tr("Einstellungen"));
      tab.addInput(this.getUpdateCheck());
      tab.addInput(this.getUpdateInterval());
      tab.addInput(this.getUpdateInstall());
    }
    catch (Exception e)
    {
      Logger.error("unable to extend settings",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Anzeigen der Update-Einstellungen"), StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Liefert eine Checkbox mit der Auswahl, ob automatisch nach Updates gesucht werden soll.
   * @return Checkbox.
   */
  private CheckboxInput getUpdateCheck()
  {
    if (this.updateCheck == null)
    {
      Listener l = new Listener() {
        /**
         * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
         */
        public void handleEvent(Event event)
        {
          Boolean b = (Boolean) getUpdateCheck().getValue();
          getUpdateInstall().setEnabled(b.booleanValue());
          getUpdateInterval().setEnabled(b.booleanValue());
        }
      };

      this.updateCheck = new CheckboxInput(getUpdateService().getUpdateCheck());
      this.updateCheck.setName(Application.getI18n().tr("Automatisch nach Updates von installierten Plugins suchen"));
      this.updateCheck.addListener(l);
      
      // einmal manuell auslesen fuer den initialen Status
      l.handleEvent(null);
    }
    return this.updateCheck;
  }
  
  /**
   * Liefert eine Auswahlbox mit den moeglichen Intervallen.
   * @return Auswahlbox.
   */
  private SpinnerInput getUpdateInterval()
  {
    if (this.updateInterval == null)
    {
      this.updateInterval = new SpinnerInput(1,365,getUpdateService().getUpdateInterval());
      this.updateInterval.setComment(Application.getI18n().tr("Tage"));
      this.updateInterval.setMandatory(true);
      this.updateInterval.setName(Application.getI18n().tr("Nach Updates suchen alle"));
    }
    return this.updateInterval;
  }
  
  /**
   * Liefert eine Auswahlbox fuer die moeglichen Aktionen bei Vorhandensein von Updates.
   * @return Auswahlbox.
   */
  private SelectInput getUpdateInstall()
  {
    if (this.updateInstall == null)
    {
      I18N i18n = Application.getI18n();
      
      List<Option> values = new ArrayList<Option>();
      values.add(new Option(false,i18n.tr("Nur benachrichtigen")));
      values.add(new Option(true,i18n.tr("Automatisch herunterladen und installieren")));
      
      this.updateInstall = new SelectInput(values,new Option(getUpdateService().getUpdateInstall(),null));
      this.updateInstall.setName(i18n.tr("Wenn Updates vorhanden sind"));
      this.updateInstall.setComment("");
      this.updateInstall.setMandatory(true);
    }
    return this.updateInstall;
  }
  
  /**
   * Speichert die Einstellungen.
   */
  private void handleStore()
  {
    UpdateService service = this.getUpdateService();
    service.setUpdateCheck(((Boolean)this.getUpdateCheck().getValue()).booleanValue());
    service.setUpdateInterval(((Integer)this.getUpdateInterval().getValue()).intValue());
    
    Option action = (Option) this.getUpdateInstall().getValue();
    service.setUpdateInstall(action.value);
  }
  
  /**
   * Hilfsklasse fuer die Auswahl der Aktionen.
   */
  private class Option
  {
    private boolean value;
    private String text;
    
    /**
     * ct.
     * @param value Wert der Option.
     * @param text Bezeichnung der Option.
     */
    private Option(boolean value,String text)
    {
      this.value = value;
      this.text = text;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return this.text;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
      if (obj == null || !(obj instanceof Option))
        return false;
      return this.value == ((Option)obj).value;
    }
  }
}
