/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/action/PluginInstall.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/06/02 11:01:57 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.BackgroundTaskDialog;
import de.willuhn.jameica.plugin.ZippedPlugin;
import de.willuhn.jameica.services.DeployService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Action zum Installieren eines Plugins.
 */
public class PluginInstall implements Action
{
  private final static Settings settings = new Settings(PluginInstall.class);
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    I18N i18n = Application.getI18n();
    FileDialog d = new FileDialog(GUI.getShell(),SWT.OPEN);
    d.setText(i18n.tr("Bitte wählen Sie die ZIP-Datei mit dem zu installierenden Plugin aus."));
    d.setFilterExtensions(new String[]{"*.zip"});
    d.setFilterPath(settings.getString("lastdir",System.getProperty("user.home")));
    
    String file = d.open();
    if (file == null)
      return;
    
    final File f = new File(file);
    if (!f.canRead() || !f.isFile())
      throw new ApplicationException(i18n.tr("Die Datei {0} ist nicht lesbar",f.getName()));
    
    // Wir merken uns das Verzeichnis
    settings.setAttribute("lastdir",f.getParent());

    // Hier drin wird der korrekte Aufbau des Plugins gecheckt.
    final ZippedPlugin plugin = new ZippedPlugin(f);
    
    // Und hier die Abhaengigkeiten, korrekten Versionsnummerm, etc.
    plugin.getManifest().canDeploy();
    
    // Installation starten
    BackgroundTask task = new BackgroundTask() {
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        DeployService service = Application.getBootLoader().getBootable(DeployService.class);
        service.deploy(plugin,monitor);
      }
      public boolean isInterrupted()
      {
        return false;
      }
      public void interrupt()
      {
      }
    };
    
    try
    {
      BackgroundTaskDialog bd = new BackgroundTaskDialog(BackgroundTaskDialog.POSITION_CENTER,task);
      bd.setTitle(i18n.tr("Installiere..."));
      bd.setPanelText(i18n.tr("Installiere Plugin"));
      bd.open();
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled");
    }
    catch (Exception e)
    {
      Logger.error("unable to install plugin",e);
      throw new ApplicationException(i18n.tr("Installation fehlgeschlagen: {0}",e.getMessage()));
    }
  }

}



/**********************************************************************
 * $Log: PluginInstall.java,v $
 * Revision 1.5  2011/06/02 11:01:57  willuhn
 * @C Installation/Deinstallation ueber neuen modalen Backgroundtask-Dialog
 *
 * Revision 1.4  2011-06-01 15:18:42  willuhn
 * @N Die Deploy-Funktion kriegt jetzt direkt ein ZippedPlugin - das erspart das extra "canDeploy()"
 *
 * Revision 1.3  2011-06-01 13:00:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2011-06-01 11:03:40  willuhn
 * @N ueberarbeiteter Install-Check - das Plugin muss jetzt nicht mehr temporaer entpackt werden - die Pruefung geschieht on-the-fly auf der ZIP-Datei
 *
 * Revision 1.1  2011-05-31 16:39:04  willuhn
 * @N Funktionen zum Installieren/Deinstallieren von Plugins direkt in der GUI unter Datei->Einstellungen->Plugins
 *
 **********************************************************************/