/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/action/PluginDetails.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/06/14 23:15:30 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.internal.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer Plugin anzeigen.
 */
public class PluginDetails implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	GUI.startView(de.willuhn.jameica.gui.internal.views.PluginDetails.class.getName(),context);
  }

}


/**********************************************************************
 * $Log: PluginDetails.java,v $
 * Revision 1.1  2005/06/14 23:15:30  web0
 * @N added settings for plugins/services
 *
 * Revision 1.1  2004/12/17 01:10:39  willuhn
 * *** empty log message ***
 *
 **********************************************************************/