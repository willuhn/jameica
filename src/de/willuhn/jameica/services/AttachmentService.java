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
import java.util.List;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.attachment.Attachment;
import de.willuhn.jameica.attachment.storage.StorageProvider;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

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
   * @throws ApplicationException
   */
  public List<Attachment> find() throws ApplicationException
  {
    final AbstractView view = GUI.getCurrentView();
    final String viewClass  = view.getClass().getName();
    final Object ctx        = view.getCurrentObject();
    
    return null;
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


