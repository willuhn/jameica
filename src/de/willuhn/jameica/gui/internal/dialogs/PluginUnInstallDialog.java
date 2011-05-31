/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/dialogs/PluginUnInstallDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/05/31 16:39:05 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.dialogs;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bestaetigungsdialog fuer die Deinstallation eines Plugins.
 */
public class PluginUnInstallDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getI18n();
  
  private Manifest manifest      = null;
  private Boolean delete         = Boolean.FALSE;
  private Boolean deleteUserData = Boolean.FALSE;
  
  /**
   * ct.
   * @param mf das zu deinstallierende Plugin.
   */
  public PluginUnInstallDialog(Manifest mf)
  {
    super(PluginUnInstallDialog.POSITION_CENTER);
    this.setTitle(i18n.tr("Plugin {0} deinstallieren",mf.getName()));
    this.setSideImage(SWTUtil.getImage("dialog-warning-large.png"));
    this.manifest = mf;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container container = new SimpleContainer(parent);
    container.addText(i18n.tr("Sind Sie sicher, dass Sie das Plugin jetzt deinstallieren wollen?"),true);
    
    // Warnhinweis
    final LabelInput warning  = new LabelInput("\n\n");
    warning.setName("");
    warning.setColor(Color.ERROR);

    // Checkbox fuer die Userdaten
    final CheckboxInput check = new CheckboxInput(false);
    check.setName(i18n.tr("Benutzerdaten dieses Plugins löschen"));
    check.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        if (((Boolean)check.getValue()).booleanValue())
          warning.setValue(i18n.tr("Die Benutzerdaten gehen unwiederbringlich verloren!\n" +
                                   "Erstellen Sie im Zweifel ein Backup von\n" +
                                   "{0}",Application.getConfig().getWorkDir()));
        else
          warning.setValue("\n\n");
      }
    });
      
    // Wenn das Plugin nicht installiert ist, muessen auch keine User-Daten geloescht werden
    final AbstractPlugin plugin = Application.getPluginLoader().getPlugin(this.manifest.getPluginClass());
    if (plugin != null)
      container.addInput(check);

    container.addInput(warning);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Plugin jetzt deinstallieren"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          boolean b = ((Boolean)check.getValue()).booleanValue();
          
          // Sicherheitshalber noch eine Abfrage, wenn die User-Daten geloescht werden sollen
          if (b && !Application.getCallback().askUser(i18n.tr("Sind Sie wirklich sicher, dass Sie auch die Benutzerdaten\n" +
          		                                                "des Plugins löschen möchten?")))
          {
            check.setValue(false);
            warning.setValue("\n\n");
            return;
          }
          
          delete = true;
          deleteUserData = b;
          close();
        }
        catch (ApplicationException ae)
        {
          throw ae;
        }
        catch (Exception e)
        {
          Logger.error("unable to perform action",e);
          throw new ApplicationException(i18n.tr(e.getMessage()));
        }
      }
    },null,false,"user-trash-full.png");
    buttons.addButton(i18n.tr("Abbrechen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException("operation cancelled");
      }
    },null,true,"process-stop.png");
    
    container.addButtonArea(buttons);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   * Liefert true, wenn das Plugin geloescht werden soll.
   */
  protected Object getData() throws Exception
  {
    return delete;
  }
  
  /**
   * Liefert true, wenn auch die Benutzerdaten des Plugins geloescht werden sollen.
   * @return true, wenn auch die Benutzerdaten des Plugins geloescht werden sollen.
   */
  public boolean getDeleteUserData()
  {
    return this.deleteUserData;
  }

}



/**********************************************************************
 * $Log: PluginUnInstallDialog.java,v $
 * Revision 1.1  2011/05/31 16:39:05  willuhn
 * @N Funktionen zum Installieren/Deinstallieren von Plugins direkt in der GUI unter Datei->Einstellungen->Plugins
 *
 **********************************************************************/