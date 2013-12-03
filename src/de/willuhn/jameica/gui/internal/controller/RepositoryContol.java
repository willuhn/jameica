/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.controller;

import java.net.URL;
import java.util.List;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.internal.action.RepositoryOpen;
import de.willuhn.jameica.gui.internal.parts.PluginTree;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.RepositoryService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.Repository;
import de.willuhn.util.ApplicationException;

/**
 * Controller fuer die Repository-Details.
 */
public class RepositoryContol extends AbstractControl
{
  private Repository repo          = null;
  private PluginTree plugins       = null;
  private SelectInput repositories = null;

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
   * Liefert eine Selectbox zur Auswahl des Repositories.
   * @return Selectbox fuer das Repository.
   */
  public SelectInput getRepositories()
  {
    if (this.repositories != null)
      return this.repositories;
    
    RepositoryService service = Application.getBootLoader().getBootable(RepositoryService.class);
    final List<URL> list = service.getRepositories();
    final URL current    = (URL) getCurrentObject();
    this.repositories = new SelectInput(list,current);
    this.repositories.setName(Application.getI18n().tr("Plugin-Repository"));
    this.repositories.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        URL u = (URL) repositories.getValue();
        if (u == null)
          return;
        
        if (current != null && u.equals(current)) // Nichts geaendert
          return;
        
        try
        {
          new RepositoryOpen().handleAction(u);
        }
        catch (ApplicationException ae)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
        }
        
      }
    });
    return this.repositories;
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
