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
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.BackgroundTaskDialog;
import de.willuhn.jameica.gui.internal.dialogs.PluginSourceDialog;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.PluginSource;
import de.willuhn.jameica.plugin.ZippedPlugin;
import de.willuhn.jameica.services.DeployService;
import de.willuhn.jameica.services.PluginSourceService;
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
    
    try
    {
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
      
      // Checken, ob das Plugin schon installiert ist. Wenn ja, muss es erst deinstalliert werden.
      Manifest mf = plugin.getManifest();
      Manifest installed = Application.getPluginLoader().getManifestByName(mf.getName());
      if (installed != null)
        throw new ApplicationException(i18n.tr("Das Plugin ist bereits installiert."));
      
      // Und hier die Abhaengigkeiten, korrekten Versionsnummerm, etc.
      mf.canDeploy();
      
      // Zielverzeichnis fuer die Installation vom User erfragen. Aber nur, wenn mehrere zur Wahl stehen
      final PluginSource source = this.getPluginSource(mf);
      
      // Installation starten
      BackgroundTask task = new BackgroundTask() {
        public void run(ProgressMonitor monitor) throws ApplicationException
        {
          DeployService service = Application.getBootLoader().getBootable(DeployService.class);
          service.deploy(plugin,source,monitor);
        }
        public boolean isInterrupted()
        {
          return false;
        }
        public void interrupt()
        {
        }
      };
      
      
      BackgroundTaskDialog bd = new BackgroundTaskDialog(BackgroundTaskDialog.POSITION_CENTER,task);
      bd.setTitle(i18n.tr("Installiere..."));
      bd.setSideImage(SWTUtil.getImage("emblem-package.png"));
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
  
  /**
   * Liefert die Plugin-Quelle, in der das Plugin installiert werden soll.
   * @param mf das Manifest des Plugins.
   * @return die Plugin-Quelle.
   * @throws Exception
   */
  private PluginSource getPluginSource(Manifest mf) throws Exception
  {
    PluginSourceService service = Application.getBootLoader().getBootable(PluginSourceService.class);
    List<PluginSource> sources = service.getWritableSources();
    
    if (sources.size() == 1)
      return sources.get(0); // wenn wir eh nur die eine haben
    
    if (sources.size() > 1)
    {
      PluginSourceDialog psd = new PluginSourceDialog(PluginSourceDialog.POSITION_CENTER,mf);
      return (PluginSource) psd.open();
    }
    
    return null; // Dann nicht. Das soll der Deploy-Service entscheiden
  }
}
