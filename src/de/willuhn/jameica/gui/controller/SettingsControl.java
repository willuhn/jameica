/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/controller/Attic/SettingsControl.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/01/04 19:51:01 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.controller;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.I18N;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.views.parts.Controller;
import de.willuhn.jameica.rmi.DBObject;

/**
 * 
 */
public class SettingsControl extends Controller
{

  /**
   * @param object
   */
  public SettingsControl(DBObject object)
  {
    super(object);
    // TODO Auto-generated constructor stub
  }

  /**
   * @see de.willuhn.jameica.gui.views.parts.Controller#handleDelete()
   */
  public void handleDelete()
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see de.willuhn.jameica.gui.views.parts.Controller#handleDelete(java.lang.String)
   */
  public void handleDelete(String id)
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see de.willuhn.jameica.gui.views.parts.Controller#handleCancel()
   */
  public void handleCancel()
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see de.willuhn.jameica.gui.views.parts.Controller#handleStore()
   */
  public void handleStore()
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see de.willuhn.jameica.gui.views.parts.Controller#handleCreate()
   */
  public void handleCreate()
  {
    // TODO Auto-generated method stub

  }
  
  /**
   * @see de.willuhn.jameica.gui.views.parts.Controller#handleLoad(java.lang.String)
   */
  public void handleLoad(String id)
  {
    // TODO Auto-generated method stub

  }
  
  /**
   * Setzt die Einstellungen zurueck.
   */
  public void handleRestore()
  {
  	try {
			Application.getConfig().restore();
  	}
  	catch (Exception e)
  	{
  		Application.getLog().error("error while restoreing config",e);
  		GUI.setActionText(I18N.tr("Fehler beim Wiederherstellen der Konfiguration"));
  	}
  	
  }

}


/**********************************************************************
 * $Log: SettingsControl.java,v $
 * Revision 1.1  2004/01/04 19:51:01  willuhn
 * *** empty log message ***
 *
 **********************************************************************/