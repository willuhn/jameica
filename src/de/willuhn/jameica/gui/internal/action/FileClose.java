/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/action/FileClose.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/10/08 13:38:19 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import de.willuhn.jameica.gui.AbstractAction;
import de.willuhn.util.ApplicationException;

/**
 * @author willuhn
 */
public class FileClose extends AbstractAction
{

  /**
   * @see de.willuhn.jameica.gui.AbstractAction#handleAction()
   */
  public void handleAction() throws ApplicationException
  {
    System.exit(0);
  }

}


/*********************************************************************
 * $Log: FileClose.java,v $
 * Revision 1.1  2004/10/08 13:38:19  willuhn
 * *** empty log message ***
 *
 **********************************************************************/