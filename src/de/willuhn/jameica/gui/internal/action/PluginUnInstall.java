/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/action/PluginUnInstall.java,v $
 * $Revision: 1.5 $
 * $Date: 2012/03/28 22:28:07 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.BackgroundTaskDialog;
import de.willuhn.jameica.gui.internal.dialogs.PluginUnInstallDialog;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.Plugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Action zum Deinstallieren eines Plugins.
 */
public class PluginUnInstall implements Action
{
  private final static I18N i18n = Application.getI18n();
  
  /**
   * Erwartet ein Objekt vom Typ "AbstractPlugin" oder "Manifest".
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie das zu deinstallierende Plugin aus"));
    
    Manifest mf = null;
    if (context instanceof Manifest)
      mf = (Manifest) context;
    if (context instanceof Plugin)
      mf = ((Plugin) context).getManifest();

    if (mf == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie das zu deinstallierende Plugin aus"));

    // Bevor wir den Dialog anzeigen, checken wir, ob eine Deinstallation ueberhaupt moeglich ist
    Application.getPluginLoader().canUnInstall(mf);

    boolean deleteUserData = false;
    // Sicherheitsabfrage
    try
    {
      PluginUnInstallDialog d = new PluginUnInstallDialog(mf);
      Boolean b = (Boolean) d.open();
      if (b == null || !b.booleanValue())
        return;
      
      deleteUserData = d.getDeleteUserData();
    }
    catch (OperationCanceledException oce)
    {
      return;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to perform action",e);
      throw new ApplicationException(i18n.tr("Fehler: {0}",e.getMessage()));
    }

    final Manifest m = mf;
    final boolean  d = deleteUserData;
    BackgroundTask task = new BackgroundTask() {
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        Application.getPluginLoader().unInstall(m,d,monitor);
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
      bd.setSideImage(SWTUtil.getImage("user-trash-full.png"));
      bd.setTitle(i18n.tr("Deinstalliere..."));
      bd.setPanelText(i18n.tr("Deinstalliere Plugin"));
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
      Logger.error("unable to uninstall plugin",e);
      throw new ApplicationException(i18n.tr("Deinstallation fehlgeschlagen: {0}",e.getMessage()));
    }
  }

}



/**********************************************************************
 * $Log: PluginUnInstall.java,v $
 * Revision 1.5  2012/03/28 22:28:07  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.4  2011-06-02 11:04:55  willuhn
 * @N Noch ein Icon
 *
 * Revision 1.3  2011-06-02 11:01:57  willuhn
 * @C Installation/Deinstallation ueber neuen modalen Backgroundtask-Dialog
 *
 * Revision 1.2  2011-06-01 11:03:40  willuhn
 * @N ueberarbeiteter Install-Check - das Plugin muss jetzt nicht mehr temporaer entpackt werden - die Pruefung geschieht on-the-fly auf der ZIP-Datei
 *
 * Revision 1.1  2011-05-31 16:39:04  willuhn
 * @N Funktionen zum Installieren/Deinstallieren von Plugins direkt in der GUI unter Datei->Einstellungen->Plugins
 *
 **********************************************************************/