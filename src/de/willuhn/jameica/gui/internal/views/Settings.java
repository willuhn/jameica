/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/views/Settings.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/10/14 23:15:05 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.views;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.controller.SettingsControl;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Dialog fuer die Programm-Einstellungen.
 */
public class Settings extends AbstractView
{

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind()
  {

		final I18N i18n = Application.getI18n();

		GUI.getView().setTitle(i18n.tr("Einstellungen"));
		final SettingsControl control = new SettingsControl(this);

		/////////////////////////////////////////////////////////////////
		// Farb-Einstellungen
		LabelGroup colorGroup = new LabelGroup(getParent(),i18n.tr("Look and Feel"));

		try
		{
			colorGroup.addLabelPair(i18n.tr("installierte Sprache"), control.getLocale());
		}
		catch (RemoteException e)
		{
			Logger.error("error while reading locale settings",e);
			GUI.getStatusBar().setErrorText(Application.getI18n().tr("Fehler beim Laden der Sprach-Einstellungen"));
		}
		colorGroup.addLabelPair(i18n.tr("Style"), control.getStyleFactory());
		colorGroup.addLabelPair(i18n.tr("Hintergrund von Eingabefeldern"),control.getColorWidgetBG());
		colorGroup.addLabelPair(i18n.tr("Textfarbe von Eingabefeldern"),control.getColorWidgetFG());
		colorGroup.addLabelPair(i18n.tr("Hintergrundfarbe"),control.getColorBackground());

		colorGroup.addLabelPair(i18n.tr("Farbe von Kommentaren"),control.getColorComment());
		colorGroup.addLabelPair(i18n.tr("Fehlermeldungen"),control.getColorError());
		colorGroup.addLabelPair(i18n.tr("Erfolgsmeldungen"),control.getColorSuccess());
		colorGroup.addLabelPair(i18n.tr("Links"),control.getColorLink());
		colorGroup.addLabelPair(i18n.tr("Aktive Links"),control.getColorLinkActive());

		ButtonArea colorButtons = colorGroup.createButtonArea(3);
		colorButtons.addCustomButton(i18n.tr("Zurücksetzen"),new Listener()
    {
      public void handleEvent(Event event)
      {
				control.handleRestore();
      }
    });
		colorButtons.addCancelButton(control);
		colorButtons.addStoreButton(control);

		//
		/////////////////////////////////////////////////////////////////

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
 * Revision 1.2  2004/10/14 23:15:05  willuhn
 * @N maded locale configurable via GUI
 * @B fixed locale handling
 * @B DecimalInput now honors locale
 *
 * Revision 1.1  2004/10/08 13:38:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.25  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.24  2004/07/20 22:52:49  willuhn
 * @C Refactoring
 *
 * Revision 1.23  2004/06/03 00:24:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.22  2004/06/02 21:15:15  willuhn
 * @B win32 fixes in flat style
 * @C made ButtonInput more abstract
 *
 * Revision 1.21  2004/05/27 21:44:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.20  2004/05/23 18:15:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2004/05/23 16:34:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.17  2004/04/19 22:05:27  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.15  2004/04/01 00:23:24  willuhn
 * @N FontInput
 * @N ColorInput
 * @C improved ClassLoader
 * @N Tabs in Settings
 *
 * Revision 1.14  2004/03/30 22:08:26  willuhn
 * *** empty log message ***
 *
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