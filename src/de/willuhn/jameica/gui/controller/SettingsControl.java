/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/controller/Attic/SettingsControl.java,v $
 * $Revision: 1.5 $
 * $Date: 2004/01/23 00:29:04 $
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
import de.willuhn.jameica.Config;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.gui.views.ServiceSettings;
import de.willuhn.jameica.gui.views.Settings;
import de.willuhn.jameica.gui.views.Start;
import de.willuhn.jameica.gui.views.parts.FileInput;
import de.willuhn.jameica.gui.views.parts.Input;
import de.willuhn.jameica.gui.views.parts.SelectInput;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * 
 */
public class SettingsControl extends AbstractControl
{

	private Config config = Application.getConfig();
	
	private Input logFile  = new FileInput(config.getLogFile());
	private Input logLevel = new SelectInput(Logger.LEVEL_TEXT,config.getLogLevel());

  /**
   * ct.
   * @param view
   */
  public SettingsControl(AbstractView view)
  {
    super(view);
  }


	/**
	 * Liefert das Eingabe-Feld fuer das Logfile.
   * @return Eingabe-Feld fuer das Logfile.
   */
  public Input getLogFile()
	{
		return logFile;
	}

	/**
	 * Liefert das Eingabefeld fuer das Loglevel.
   * @return Eingabe-Feld fuer das Loglevel.
   */
  public Input getLoglevel()
	{
		return logLevel;
	}

  /**
   * @see de.willuhn.jameica.gui.views.parts.AbstractControl#handleDelete()
   */
  public void handleDelete()
  {
  }

  /**
   * @see de.willuhn.jameica.gui.views.parts.AbstractControl#handleCancel()
   */
  public void handleCancel()
  {
  	GUI.startView(Start.class.getName(),null);
  }

  /**
   * @see de.willuhn.jameica.gui.views.parts.AbstractControl#handleStore()
   */
  public void handleStore()
  {
  	Config config = Application.getConfig();

  	config.setLoglevel(logLevel.getValue());
  	Application.getLog().setLevel(config.getLogLevel()); // live umschaltung
  	config.setLogFile(logFile.getValue());

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
   * @see de.willuhn.jameica.gui.views.parts.AbstractControl#handleCreate()
   */
  public void handleCreate()
  {
  }
  
  /**
   * @see de.willuhn.jameica.gui.views.parts.AbstractControl#handleLoad(java.lang.String)
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
 * Revision 1.5  2004/01/23 00:29:04  willuhn
 * *** empty log message ***
 *
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