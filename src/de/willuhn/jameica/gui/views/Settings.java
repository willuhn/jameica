/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/Settings.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/01/08 20:50:32 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.views;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.Config;
import de.willuhn.jameica.gui.controller.SettingsControl;
import de.willuhn.jameica.gui.views.parts.*;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Dialog fuer die Programm-Einstellungen.
 */
public class Settings extends AbstractView
{

  /**
   * Erstellt den Einstellungs-Dialog.
   * @param o Dummy.
   */
  public Settings(Object o)
  {
    super(o);
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind()
  {

		final SettingsControl control = new SettingsControl(null);

  	new Headline(getParent(),I18N.tr("Einstellungen"));
  	
		Config config = Application.getConfig();

  	LabelGroup mainSettings = new LabelGroup(getParent(),I18N.tr("Grundeinstellungen"));

  	FileInput fi = new FileInput(config.getLogFile());
		mainSettings.addLabelPair("Log-Datei",fi);
		control.register("logfile",fi);		

		SelectInput loglevel = new SelectInput(Logger.LEVEL_TEXT,config.getLogLevel());
		mainSettings.addLabelPair("Log-Level", loglevel);
		control.register("loglevel",loglevel);		

//		LabelGroup services = new LabelGroup(getParent(),I18N.tr("Services"));
//
//		services.addText(I18N.tr("Lokale Services"),false);
//		Table lt = new Table(config.getLocalServiceNames(),control);
//		lt.addColumn(I18N.tr("Name"),null);
//		services.addTable(lt);
//		
//		services.addText(I18N.tr("Netzwerk-Services"),false);
//		Table rt = new Table(config.getRemoteServiceNames(),control);
//		rt.addColumn(I18N.tr("Name"),null);
//		services.addTable(rt);
//
//		ButtonArea sbuttons = services.createButtonArea(2);
//		sbuttons.addCustomButton(I18N.tr("lokalen Service erstellen"),new MouseAdapter()
//		{
//			public void mouseUp(MouseEvent e)
//			{
//				control.handleRestore();
//			}
//		});
//		sbuttons.addCustomButton(I18N.tr("Netzwerk-Service erstellen"),new MouseAdapter()
//		{
//			public void mouseUp(MouseEvent e)
//			{
//				control.handleRestore();
//			}
//		});

		ButtonArea buttons = new ButtonArea(getParent(),3);
		buttons.addCancelButton(control);
		buttons.addCustomButton(I18N.tr("Zurücksetzen"),new MouseAdapter()
    {
      public void mouseUp(MouseEvent e)
      {
      	control.handleRestore();
      }
    });
		buttons.addStoreButton(control);
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
  }

}


/**********************************************************************
 * $Log: Settings.java,v $
 * Revision 1.4  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.3  2004/01/06 20:11:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/01/06 01:27:30  willuhn
 * @N table order
 *
 * Revision 1.1  2004/01/04 19:51:01  willuhn
 * *** empty log message ***
 *
 **********************************************************************/