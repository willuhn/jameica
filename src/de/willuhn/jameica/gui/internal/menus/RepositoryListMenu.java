/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.menus;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.internal.action.RepositoryAdd;
import de.willuhn.jameica.gui.internal.action.RepositoryOpen;
import de.willuhn.jameica.gui.internal.action.RepositoryRemove;
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
  private final static I18N i18n = Application.getI18n();

  /**
   * ct.
   */
  public RepositoryListMenu()
  {
    addItem(new CheckedContextMenuItem(i18n.tr("Öffnen..."),new RepositoryOpen(),"document-open.png"));
    addItem(new SystemCheckedContextMenuItem(i18n.tr("Löschen..."),new RepositoryRemove(),"user-trash-full.png"));
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

}
