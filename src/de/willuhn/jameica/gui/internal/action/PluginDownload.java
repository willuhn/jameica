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

import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.dialogs.DependencyDownloadDialog;
import de.willuhn.jameica.gui.internal.dialogs.DependencyMissingDialog;
import de.willuhn.jameica.plugin.Dependency;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.services.RepositoryService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.update.PluginData;
import de.willuhn.jameica.update.Repository;
import de.willuhn.jameica.update.ResolverResult;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Laedt das uebergebene Plugin herunter und installiert es.
 */
public class PluginDownload implements Action
{
  private final static I18N i18n = Application.getI18n();

  /**
   * Erwartet ein Objekt vom Typ PluginData.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null)
      throw new ApplicationException(i18n.tr("Kein Plugin angegeben"));

    // Wenn man in PluginTree doppelt auf eine Plugin-Gruppe klickt, kommt hier die PluginGroup an
    if (!(context instanceof PluginData))
      return;
    
    final PluginData data = (PluginData) context;
    
    try
    {
      // Wenn es direkt installierbar ist, dann laden wir es sofort herunter
      if (data.isInstallable())
      {
        if (!Application.getCallback().askUser(i18n.tr("Sind Sie sicher, daß Sie {0} (Version {1})\nherunterladen und installieren möchten?",data.getName(),data.getAvailableVersion().toString()),false))
          return;

        Repository repo = data.getPluginGroup().getRepository();
        repo.download(data,true);
        return;
      }
      
      // Wir pruefen die System-Voraussetzungen - aber noch ohne die Abhaengigkeiten. Denn die koennen
      // wir ja direkt mit installieren.
      final Manifest mf = data.getManifest();
      mf.canDeploy(false);
      
      final RepositoryService service = Application.getBootLoader().getBootable(RepositoryService.class);
      final DelayedResult r = new DelayedResult();
      
      // Wenn wir hier sind, koennen wir pruefen, ob wir es installieren koennen.
      // Waehrenddessen Sanduhr anzeigen
      GUI.startSync(new Runnable() {
        public void run()
        {
          try
          {
            r.result = service.resolve(data);
          }
          catch (ApplicationException ae)
          {
            r.exception = ae;
          }
        }
      });
      
      if (r.exception != null)
        throw r.exception;
      
      final List<PluginData> resolved = r.result.getResolved();
      final List<Dependency> missing  = r.result.getMissing();
      
      final boolean haveMissing = missing.size() > 0;
      final boolean haveDeps    = resolved.size() > 0;
      
      // Wir haben weder zu installierende noch fehlende Abhaengigkeiten
      if (!haveDeps && !haveMissing)
      {
        if (!Application.getCallback().askUser(i18n.tr("Sind Sie sicher, daß Sie das Plugin\nherunterladen und installieren möchten?"),false))
          return;

        Repository repo = data.getPluginGroup().getRepository();
        repo.download(data,true);
        return;
      }
      
      // Wir haben Abhaengigkeiten, die wir nicht erfuellen konnten
      if (haveMissing)
      {
        DependencyMissingDialog d = new DependencyMissingDialog(DependencyMissingDialog.POSITION_CENTER,missing);
        d.open();
        return;
      }
      
      // Ansonsten geben wir Bescheid, welche Abhaengigkeiten mit installiert werden.
      DependencyDownloadDialog d = new DependencyDownloadDialog(DependencyDownloadDialog.POSITION_CENTER,resolved);
      d.open();
      
      List<PluginData> all = new ArrayList<PluginData>();
      all.addAll(resolved);
      all.add(data);
      
      service.downloadMulti(all.toArray(new PluginData[all.size()]));
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
      Logger.error("unable to ask user",e);
      throw new ApplicationException(i18n.tr("Fehler beim Download des Plugins: {0}",e.getMessage()));
    }
  }
  
  /**
   * Container zum Zurueckgeben der Daten.
   */
  private class DelayedResult
  {
    private ResolverResult result;
    private ApplicationException exception;
  }
}
