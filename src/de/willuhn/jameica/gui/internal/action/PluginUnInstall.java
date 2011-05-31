/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/action/PluginUnInstall.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/05/31 16:39:04 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.internal.dialogs.PluginUnInstallDialog;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.plugin.Manifest;
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
    if (context instanceof AbstractPlugin)
      mf = ((AbstractPlugin) context).getManifest();

    if (mf == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie das zu deinstallierende Plugin aus"));

    // Bevor wir den Dialog anzeigen, checken wir, ob eine Deinstallation ueberhaupt moeglich ist
    Application.getPluginLoader().checkUnInstall(mf);

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


    // Deinstallation starten
    final Manifest m = mf;
    final boolean  d = deleteUserData;
    Application.getController().start(new BackgroundTask() {
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
    });
  }

}



/**********************************************************************
 * $Log: PluginUnInstall.java,v $
 * Revision 1.1  2011/05/31 16:39:04  willuhn
 * @N Funktionen zum Installieren/Deinstallieren von Plugins direkt in der GUI unter Datei->Einstellungen->Plugins
 *
 **********************************************************************/