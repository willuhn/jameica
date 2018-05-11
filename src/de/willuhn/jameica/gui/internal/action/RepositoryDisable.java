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


