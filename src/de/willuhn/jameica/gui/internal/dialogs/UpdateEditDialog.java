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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.SpinnerInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.services.UpdateService;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zum Bearbeiten der Update-Einstellungen.
 */
public class UpdateEditDialog extends AbstractDialog<Void>
{
  private final static int WINDOW_WIDTH = 460;

  private CheckboxInput updateCheck   = null;
  private SpinnerInput updateInterval = null;
  private SelectInput updateInstall   = null;
  private UpdateService service       = null;
  
  /**
   * ct.
   * @param position
   */
  public UpdateEditDialog(int position)
  {
    super(position);
    this.setTitle(i18n.tr("Automatische Updates konfigurieren"));
    this.setSize(WINDOW_WIDTH,SWT.DEFAULT);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void paint(Composite parent) throws Exception
  {
    SimpleContainer c = new SimpleContainer(parent);
    c.addText(i18n.tr("Aktivieren Sie die automatische Suche nach Updates, wenn Jameica regelmäßig prüfen soll, " +
                      "ob zu den installierten Plugins neue Updates verfügbar sind."),true);

    c.addInput(this.getUpdateCheck());
    c.addInput(this.getUpdateInterval());
    c.addInput(this.getUpdateInstall());
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Übernehmen"),new Action() {
      
      public void handleAction(Object context) throws ApplicationException
      {
        handleStore();
        close();
      }
    },null,false,"document-save.png");
    buttons.addButton(i18n.tr("Schließen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,false,"window-close.png");
    c.addButtonArea(buttons);
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
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
      this.updateInstall.setMandatory(true);
    }
    return this.updateInstall;
  }
  
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


  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  protected Void getData() throws Exception
  {
    return null;
  }

}


