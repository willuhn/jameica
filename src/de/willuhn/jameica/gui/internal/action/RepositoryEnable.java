/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

/**
 * Action zum Aktivieren eines Repository.
 */
public class RepositoryEnable extends AbstractRepositoryChangeState
{
  /**
   * @see de.willuhn.jameica.gui.internal.action.AbstractRepositoryChangeState#getEnabled()
   */
  @Override
  boolean getEnabled()
  {
    return true;
  }

}


