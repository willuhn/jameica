/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/controller/Attic/SettingsControl.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/01/08 20:50:33 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.controller;

import de.willuhn.datasource.db.rmi.DBObject;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.Config;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.views.ServiceSettings;
import de.willuhn.jameica.gui.views.Settings;
import de.willuhn.jameica.gui.views.Start;
import de.willuhn.jameica.gui.views.parts.Controller;
import de.willuhn.util.I18N;

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
  }

  /**
   * @see de.willuhn.jameica.gui.views.parts.Controller#handleDelete()
   */
  public void handleDelete()
  {
  }

  /**
   * @see de.willuhn.jameica.gui.views.parts.Controller#handleDelete(java.lang.String)
   */
  public void handleDelete(String id)
  {
  }

  /**
   * @see de.willuhn.jameica.gui.views.parts.Controller#handleCancel()
   */
  public void handleCancel()
  {
  	GUI.startView(Start.class.getName(),null);
  }

  /**
   * @see de.willuhn.jameica.gui.views.parts.Controller#handleStore()
   */
  public void handleStore()
  {
  	Config config = Application.getConfig();

  	config.setLoglevel(getField("loglevel").getValue());
  	Application.getLog().setLevel(config.getLogLevel()); // live umschaltung
  	config.setLogFile(getField("logfile").getValue());

  	try
    {
      config.store();
      GUI.setActionText(I18N.tr("Konfiguaration gespeichert."));
    }
    catch (Exception e)
    {
    	Application.getLog().error("error while writing config",e);
    	GUI.setActionText(I18N.tr("Fehler beim Speichern der Konfiguration."));
    }
  	
  }

  /**
   * @see de.willuhn.jameica.gui.views.parts.Controller#handleCreate()
   */
  public void handleCreate()
  {
  }
  
  /**
   * @see de.willuhn.jameica.gui.views.parts.Controller#handleLoad(java.lang.String)
   */
  public void handleLoad(String id)
  {
  	GUI.startView(ServiceSettings.class.getName(),id);
  }
  
  /**
   * Setzt die Einstellungen zurueck.
   */
  public void handleRestore()
  {
  	try {
			Application.getConfig().restore();
			GUI.startView(Settings.class.getName(),null);
			GUI.setActionText(I18N.tr("letzte gespeicherte Konfiguaration wieder hergestellt."));
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
 * Revision 1.4  2004/01/08 20:50:33  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.3  2004/01/06 20:11:22  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/01/06 01:27:30  willuhn
 * @N table order
 *
 * Revision 1.1  2004/01/04 19:51:01  willuhn
 * *** empty log message ***
 *
 **********************************************************************/