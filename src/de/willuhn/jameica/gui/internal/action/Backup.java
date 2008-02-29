/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/action/Backup.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/02/29 01:12:30 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;


/**
 * Startet die Backup-GUI.
 */
public class Backup implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    GUI.startView(de.willuhn.jameica.gui.internal.views.Backup.class,null);
  }

}


/**********************************************************************
 * $Log: Backup.java,v $
 * Revision 1.1  2008/02/29 01:12:30  willuhn
 * @N Erster Code fuer neues Backup-System
 * @N DirectoryInput
 * @B Fixes an FileInput, TextInput
 *
 **********************************************************************/
