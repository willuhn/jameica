/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/Settings.java,v $
 * $Revision: 1.13 $
 * $Date: 2004/03/24 00:46:02 $
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

import de.willuhn.jameica.Jameica;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.SettingsControl;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.LabelGroup;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog fuer die Programm-Einstellungen.
 * TODO: Plugins via GUI installier- und deinstallierbar machen.
 */
public class Settings extends AbstractView
{

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind()
  {

		final I18N i18n = PluginLoader.getPlugin(Jameica.class).getResources().getI18N();
		GUI.getView().setTitle(i18n.tr("Einstellungen"));

		final SettingsControl control = new SettingsControl(this);

  	LabelGroup mainSettings = new LabelGroup(getParent(),i18n.tr("Grundeinstellungen"));

		mainSettings.addLabelPair("Log-Datei",control.getLogFile());
		mainSettings.addLabelPair("Log-Level", control.getLoglevel());

		mainSettings.addText("",false);
		mainSettings.addHeadline(i18n.tr("Lokale Services"));
		mainSettings.addTable(control.getLocalServices());
		
		mainSettings.addText("",false);
		mainSettings.addHeadline(i18n.tr("Netzwerkservices"));
		mainSettings.addTable(control.getRemoteServices());


		ButtonArea buttons = new ButtonArea(getParent(),5);
		buttons.addCustomButton(i18n.tr("lokalen Service erstellen"),new MouseAdapter()
		{
			public void mouseUp(MouseEvent e)
			{
				control.handleRestore();
			}
		});
		buttons.addCustomButton(i18n.tr("Netzwerk-Service erstellen"),new MouseAdapter()
		{
			public void mouseUp(MouseEvent e)
			{
				control.handleRestore();
			}
		});
		buttons.addCustomButton(i18n.tr("Zurücksetzen"),new MouseAdapter()
    {
      public void mouseUp(MouseEvent e)
      {
      	control.handleRestore();
      }
    });
		buttons.addCancelButton(control);
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
 * Revision 1.13  2004/03/24 00:46:02  willuhn
 * @C refactoring
 *
 * Revision 1.12  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.11  2004/02/22 20:05:21  willuhn
 * @N new Logo panel
 *
 * Revision 1.10  2004/02/20 20:45:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/02/18 01:40:29  willuhn
 * @N new white style
 *
 * Revision 1.8  2004/02/11 00:10:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/01/29 00:45:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/01/28 20:51:25  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.5  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
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