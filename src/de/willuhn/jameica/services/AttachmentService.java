/**********************************************************************
 *
 * Copyright (c) 2022 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.attachment.Attachment;
import de.willuhn.jameica.attachment.Context;
import de.willuhn.jameica.attachment.storage.StorageProvider;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.plugin.Plugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Service zum Laden und Speichern von Attachments.
 */
public class AttachmentService implements Bootable
{
  private List<StorageProvider> providers = new ArrayList<>();
  
  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  @Override
  public Class<Bootable>[] depends()
  {
    return new Class[]{ArchiveService.class, BeanService.class};
  }
  
  /**
   * Liefert die Attachments für die aktuelle View mit dem aktuellen Objekt.
   * @return die Liste der Attachments.
   * @throws Exception
   */
  public List<Attachment> find() throws Exception
  {
    final AbstractView view = GUI.getCurrentView();
    final Object o          = view.getCurrentObject();
    final Plugin plugin     = Application.getPluginLoader().findByClass(view.getClass());
    
    final Context ctx = new Context();
    ctx.setClassName(o != null ? o.getClass().getName() : null);
    ctx.setId((o instanceof GenericObject) ? ((GenericObject)o).getID() : null);
    ctx.setPlugin(plugin != null ? plugin.getManifest().getPluginClass() : null);
    
    final List<Attachment> result = new LinkedList<>();
    for (StorageProvider p:this.providers)
    {
      result.addAll(p.getAttachments(ctx));
    }
    
    return result;
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  @Override
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    Logger.info("init attachment service");
    final BeanService bs = loader.getBootable(BeanService.class);
    Logger.info("searching available storage providers");
    try
    {
      for (Class<StorageProvider> c:Application.getClassLoader().getClassFinder().findImplementors(StorageProvider.class))
      {
        try
        {
          final StorageProvider sp = bs.get(c);
          Logger.info("  [" + sp.getId() + "] " + sp.getName() + ": " + sp.isEnabled());
          if (sp.isEnabled())
            this.providers.add(sp);
        }
        catch (Throwable t)
        {
          Logger.error("failed to load " + c,t);
        }
      }
    }
    catch (ClassNotFoundException cne)
    {
      Logger.error("suspect - no storage providers found",cne);
    }
    Logger.info("found storage providers: " + this.providers.size());
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  @Override
  public void shutdown()
  {
    this.providers.clear();
  }
}


