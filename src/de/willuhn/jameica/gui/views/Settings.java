/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/Settings.java,v $
 * $Revision: 1.16 $
 * $Date: 2004/04/12 19:15:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.SettingsControl;
import de.willuhn.jameica.gui.input.ColorInput;
import de.willuhn.jameica.gui.input.FontInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.Style;
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

		final I18N i18n = Application.getI18n();

		GUI.getView().setTitle(i18n.tr("Einstellungen"));
		final SettingsControl control = new SettingsControl(this);


		CTabFolder folder = new CTabFolder(getParent(),SWT.TOP);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		/////////////////////////////////////////////////////////////////
		// Grund-Einstellungen
		CTabItem main = new CTabItem(folder,SWT.NONE);
		main.setText(i18n.tr("Grundeinstellungen"));
		LabelGroup mainGroup = new LabelGroup(folder,"");

		mainGroup.addText("",false);
		mainGroup.addLabelPair("Log-Datei",control.getLogFile());
		mainGroup.addLabelPair("Log-Level", control.getLoglevel());

		ButtonArea mainButtons = mainGroup.createButtonArea(3);
		mainButtons.addCustomButton(i18n.tr("Zurücksetzen"),new MouseAdapter()
		{
			public void mouseUp(MouseEvent e)
			{
				control.handleRestore();
			}
		});
		mainButtons.addCancelButton(control);
		mainButtons.addStoreButton(control);

		main.setControl(mainGroup.getControl());
		//
		/////////////////////////////////////////////////////////////////

		/////////////////////////////////////////////////////////////////
		// Farb-Einstellungen
		CTabItem color = new CTabItem(folder,SWT.NONE);
		color.setText(i18n.tr("Style"));
		LabelGroup colorGroup = new LabelGroup(folder,"");

		colorGroup.addText("",false);

		colorGroup.addLabelPair("Hintergrundfarbe",new ColorInput(Style.COLOR_BG));
		colorGroup.addLabelPair("Überschriften",new FontInput(Style.FONT_H1));

		ButtonArea colorButtons = colorGroup.createButtonArea(3);
		colorButtons.addCustomButton(i18n.tr("Zurücksetzen"),new MouseAdapter()
		{
			public void mouseUp(MouseEvent e)
			{
				control.handleRestore();
			}
		});
		colorButtons.addCancelButton(control);
		colorButtons.addStoreButton(control);

		color.setControl(colorGroup.getControl());
		//
		/////////////////////////////////////////////////////////////////


		/////////////////////////////////////////////////////////////////
		// Service-Einstellungen
		CTabItem service = new CTabItem(folder,SWT.NONE);
		service.setText(i18n.tr("Services"));
		LabelGroup serviceGroup = new LabelGroup(folder,"");
		serviceGroup.addText("",false);
		serviceGroup.addHeadline(i18n.tr("Lokale Services"));
		serviceGroup.addTable(control.getLocalServices());
		
		serviceGroup.addText("",false);
		serviceGroup.addHeadline(i18n.tr("Netzwerkservices"));
		serviceGroup.addTable(control.getRemoteServices());

		ButtonArea serviceButtons = serviceGroup.createButtonArea(5);
		serviceButtons.addCustomButton(i18n.tr("lokalen Service erstellen"),new MouseAdapter()
		{
			public void mouseUp(MouseEvent e)
			{
				control.handleRestore();
			}
		});
		serviceButtons.addCustomButton(i18n.tr("Netzwerk-Service erstellen"),new MouseAdapter()
		{
			public void mouseUp(MouseEvent e)
			{
				control.handleRestore();
			}
		});
		serviceButtons.addCustomButton(i18n.tr("Zurücksetzen"),new MouseAdapter()
    {
      public void mouseUp(MouseEvent e)
      {
      	control.handleRestore();
      }
    });
		serviceButtons.addCancelButton(control);
		serviceButtons.addStoreButton(control);

		service.setControl(serviceGroup.getControl());
		//
		/////////////////////////////////////////////////////////////////



		folder.setSelection(main);

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