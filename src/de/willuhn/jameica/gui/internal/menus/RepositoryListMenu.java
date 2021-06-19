/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.menus;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.internal.action.RepositoryAdd;
import de.willuhn.jameica.gui.internal.action.RepositoryDisable;
import de.willuhn.jameica.gui.internal.action.RepositoryEnable;
import de.willuhn.jameica.gui.internal.action.RepositoryRemove;
import de.willuhn.jameica.gui.internal.parts.RepositoryList.UrlObject;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.services.RepositoryService;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Context-Menu fuer die Repository-Liste.
 */
public class RepositoryListMenu extends ContextMenu
{
  private static final I18N i18n = Application.getI18n();

  /**
   * ct.
   */
  public RepositoryListMenu()
  {
    addItem(new SystemCheckedContextMenuItem(i18n.tr("Löschen..."),new RepositoryRemove(),"user-trash-full.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new ChangeStateCheckedContextMenuItem(i18n.tr("Deaktivieren..."),new RepositoryDisable(),"network-offline.png",false));
    addItem(new ChangeStateCheckedContextMenuItem(i18n.tr("Aktivieren..."),new RepositoryEnable(),"network-transmit-receive.png",true));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new ContextMenuItem(i18n.tr("Neues Repository hinzufügen..."),new RepositoryAdd(),"document-new.png"));
  }
  
  /**
   * Hilfsklasse, um das System-Repository read only zu machen.
   */
  private class SystemCheckedContextMenuItem extends CheckedContextMenuItem
  {
    /**
     * ct.
     * @param text
     * @param a
     * @param icon
     */
    public SystemCheckedContextMenuItem(String text, Action a, String icon)
    {
      super(text, a, icon);
    }

    /**
     * @see de.willuhn.jameica.gui.parts.CheckedContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      return super.isEnabledFor(o) && !RepositoryService.SYSTEM_REPOSITORY.equalsIgnoreCase(o.toString());
    }
    
  }

  /**
   * Hilfsklasse zum Aktivieren/Deaktivieren der Menu-Eintraege fuer das Repository.
   */
  private class ChangeStateCheckedContextMenuItem extends SystemCheckedContextMenuItem
  {
    private boolean enabled = true;
    
    /**
     * ct.
     * @param text
     * @param a
     * @param icon
     * @param enabled true, wenn das Repository damit aktiviert werden soll.
     */
    public ChangeStateCheckedContextMenuItem(String text, Action a, String icon, boolean enabled)
    {
      super(text, a, icon);
      this.enabled = enabled;
    }
    
    /**
     * @see de.willuhn.jameica.gui.internal.menus.RepositoryListMenu.SystemCheckedContextMenuItem#isEnabledFor(java.lang.Object)
     */
    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o == null || !(o instanceof UrlObject))
        return false;

      UrlObject u = (UrlObject) o;
      
      return (enabled != u.isEnabled()) && super.isEnabledFor(o); 
    }
  }
  

}
