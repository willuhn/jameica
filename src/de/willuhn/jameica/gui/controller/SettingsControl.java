/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/controller/Attic/SettingsControl.java,v $
 * $Revision: 1.11 $
 * $Date: 2004/03/11 08:56:56 $
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
import de.willuhn.jameica.Jameica;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.FileInput;
import de.willuhn.jameica.gui.parts.AbstractInput;
import de.willuhn.jameica.gui.parts.SelectInput;
import de.willuhn.jameica.gui.parts.Table;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.gui.views.ServiceSettings;
import de.willuhn.jameica.gui.views.Settings;
import de.willuhn.jameica.gui.views.Start;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * 
 */
public class SettingsControl extends AbstractControl
{

	private Config config = Application.getConfig();
	
	private AbstractInput logFile  = new FileInput(config.getLogFile());
	private AbstractInput logLevel = new SelectInput(Logger.LEVEL_TEXT,config.getLogLevel());
	private I18N i18n;

  /**
   * ct.
   * @param view
   */
  public SettingsControl(AbstractView view)
  {
    super(view);
    i18n = PluginLoader.getPlugin(Jameica.class).getResources().getI18N();
  }


	/**
	 * Liefert das Eingabe-Feld fuer das Logfile.
   * @return Eingabe-Feld fuer das Logfile.
   */
  public AbstractInput getLogFile()
	{
		return logFile;
	}

	/**
	 * Liefert das Eingabefeld fuer das Loglevel.
   * @return Eingabe-Feld fuer das Loglevel.
   */
  public AbstractInput getLoglevel()
	{
		return logLevel;
	}

	/**
	 * Liefert eine Tabelle mit den lokalen Services.
   * @return Tabelle mit den lokalen Services.
   */
  public Table getLocalServices()
	{
		Table t = new Table(config.getLocalServiceData(),this);
		t.addColumn(i18n.tr("Name"),null);
		return t;
	}

	/**
	 * Liefert eine Tabelle mit den remote Services.
	 * @return Tabelle mit den remote Services.
	 */
	public Table getRemoteServices()
	{
		Table t = new Table(config.getRemoteServiceData(),this);
		t.addColumn(i18n.tr("Name"),null);
		return t;
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleDelete()
   */
  public void handleDelete()
  {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCancel()
   */
  public void handleCancel()
  {
  	GUI.startView(Start.class.getName(),null);
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public void handleStore()
  {
  	Config config = Application.getConfig();

  	config.setLoglevel((String)logLevel.getValue());
  	Application.getLog().setLevel(config.getLogLevel()); // live umschaltung
  	config.setLogFile((String)logFile.getValue());

  	try
    {
      config.store();
      GUI.setActionText(i18n.tr("Konfiguaration gespeichert."));
    }
    catch (Exception e)
    {
    	Application.getLog().error("error while writing config",e);
    	GUI.setActionText(i18n.tr("Fehler beim Speichern der Konfiguration."));
    }
  	
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate()
  {
  }
  
  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleOpen(java.lang.Object)
   */
  public void handleOpen(Object o)
  {
  	GUI.startView(ServiceSettings.class.getName(),o);
  }
  
  /**
   * Setzt die Einstellungen zurueck.
   */
  public void handleRestore()
  {
  	try {
			Application.getConfig().restore();
			GUI.startView(Settings.class.getName(),null);
			GUI.setActionText(i18n.tr("letzte gespeicherte Konfiguaration wieder hergestellt."));
  	}
  	catch (Exception e)
  	{
  		Application.getLog().error("error while restoreing config",e);
  		GUI.setActionText(i18n.tr("Fehler beim Wiederherstellen der Konfiguration"));
  	}
  	
  }

}


/**********************************************************************
 * $Log: SettingsControl.java,v $
 * Revision 1.11  2004/03/11 08:56:56  willuhn
 * @C some refactoring
 *
 * Revision 1.10  2004/03/06 18:24:24  willuhn
 * @D javadoc
 *
 * Revision 1.9  2004/03/03 22:27:11  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.8  2004/02/24 22:46:53  willuhn
 * @N GUI refactoring
 *
 * Revision 1.7  2004/02/11 00:10:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/01/28 20:51:25  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
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