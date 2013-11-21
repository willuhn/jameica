/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.controller;

import java.net.URL;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.internal.parts.PluginTree;
import de.willuhn.jameica.services.RepositoryService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.Repository;
import de.willuhn.util.ApplicationException;

/**
 * Controller fuer die Repository-Details.
 */
public class RepositoryContol extends AbstractControl
{
  private Repository repo    = null;
  private PluginTree plugins = null;

  /**
   * ct.
   * @param view
   */
  public RepositoryContol(AbstractView view)
  {
    super(view);
  }
  
  /**
   * Liefert dasa Repository zur URL.
   * @return Repository.
   * @throws ApplicationException
   */
  public synchronized Repository getRepository() throws ApplicationException
  {
    if (this.repo == null)
    {
      RepositoryService service = Application.getBootLoader().getBootable(RepositoryService.class);
      this.repo = service.open((URL) getCurrentObject());
    }
    return this.repo;
  }
  
  /**
   * Liefert eine Liste mit den Plugins aus dem Repository.
   * @return Liste mit den Plugins aus dem Repository.
   * @throws ApplicationException
   */
  public PluginTree getPlugins() throws ApplicationException
  {
    if (this.plugins == null)
      this.plugins = new PluginTree(this.getRepository());
    return this.plugins;
  }
}
