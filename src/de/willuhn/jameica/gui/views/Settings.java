/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/Settings.java,v $
 * $Revision: 1.9 $
 * $Date: 2004/02/18 01:40:29 $
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
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.controller.SettingsControl;
import de.willuhn.jameica.gui.parts.*;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog fuer die Programm-Einstellungen.
 * TODO: Plugins via GUI installier- und deinstallierbar machen.
 */
public class Settings extends AbstractView
{

  /**
   * ct.
   * @param parent
   */
  public Settings(Composite parent)
  {
    super(parent);
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind()
  {

		addHeadline("Einstellungen");

		final SettingsControl control = new SettingsControl(this);

  	LabelGroup mainSettings = new LabelGroup(getParent(),I18N.tr("Grundeinstellungen"));

		mainSettings.addLabelPair("Log-Datei",control.getLogFile());
		mainSettings.addLabelPair("Log-Level", control.getLoglevel());

		mainSettings.addText("",false);
		mainSettings.addHeadline(I18N.tr("Lokale Services"));
		mainSettings.addTable(control.getLocalServices());
		
		mainSettings.addText("",false);
		mainSettings.addHeadline(I18N.tr("Netzwerkservices"));
		mainSettings.addTable(control.getRemoteServices());


		ButtonArea buttons = new ButtonArea(getParent(),5);
		buttons.addCustomButton(I18N.tr("lokalen Service erstellen"),new MouseAdapter()
		{
			public void mouseUp(MouseEvent e)
			{
				control.handleRestore();
			}
		});
		buttons.addCustomButton(I18N.tr("Netzwerk-Service erstellen"),new MouseAdapter()
		{
			public void mouseUp(MouseEvent e)
			{
				control.handleRestore();
			}
		});
		buttons.addCustomButton(I18N.tr("Zurücksetzen"),new MouseAdapter()
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