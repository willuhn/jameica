/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/Settings.java,v $
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

package de.willuhn.jameica.gui.views;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.ApplicationException;
import de.willuhn.jameica.Config;
import de.willuhn.jameica.I18N;
import de.willuhn.jameica.gui.controller.SettingsControl;
import de.willuhn.jameica.gui.views.parts.*;
import de.willuhn.jameica.gui.views.parts.CheckboxInput;
import de.willuhn.jameica.gui.views.parts.FileInput;
import de.willuhn.jameica.gui.views.parts.Headline;
import de.willuhn.jameica.gui.views.parts.LabelGroup;

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

		CheckboxInput debug = new CheckboxInput(config.debug());
		mainSettings.addLabelPair("Debug-Modus aktivieren", debug);
		
		
		LabelGroup localServices = new LabelGroup(getParent(),I18N.tr("Lokale Services"));
		Table lt = new Table(config.getLocalServiceNames(),control);
		lt.addColumn(I18N.tr("Name"),null);
		localServices.addTable(lt);
		
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
 * Revision 1.1  2004/01/04 19:51:01  willuhn
 * *** empty log message ***
 *
 **********************************************************************/