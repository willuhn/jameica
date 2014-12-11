/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

/**
 * Action zum Deaktivieren eines Repository.
 */
public class RepositoryDisable extends AbstractRepositoryChangeState
{
  /**
   * @see de.willuhn.jameica.gui.internal.action.AbstractRepositoryChangeState#getEnabled()
   */
  @Override
  boolean getEnabled()
  {
    return false;
  }

}


