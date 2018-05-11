/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.BackgroundTaskDialog;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.plugin.Manifest;
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
 * Action zum Aktualisieren eines Plugins.
 */
public class PluginUpdate implements Action
{
  private final static Settings settings = new Settings(PluginUpdate.class);
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    I18N i18n = Application.getI18n();
    
    if (!(context instanceof Manifest))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie das zu aktualisierende Plugin aus"));
    
    final Manifest installed = (Manifest) context;
    
    FileDialog d = new FileDialog(GUI.getShell(),SWT.OPEN);
    d.setText(i18n.tr("Bitte wählen Sie die ZIP-Datei mit der neuen Version des Plugins aus."));
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
    
    // Update starten
    BackgroundTask task = new BackgroundTask() {
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        DeployService service = Application.getBootLoader().getBootable(DeployService.class);
        service.update(installed,plugin,monitor);
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
      bd.setTitle(i18n.tr("Aktualisiere..."));
      bd.setSideImage(SWTUtil.getImage("emblem-package.png"));
      bd.setPanelText(i18n.tr("Aktualisiere Plugin"));
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
 * $Log: PluginUpdate.java,v $
 * Revision 1.1  2011/06/02 12:15:16  willuhn
 * @B Das Handling beim Update war noch nicht sauber
 *
 **********************************************************************/