/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/ButtonArea.java,v $
 * $Revision: 1.11 $
 * $Date: 2005/03/09 01:06:37 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * Diese Klasse erzeugt standardisierte Bereiche fuer die Dialog-Buttons.
 * @author willuhn
 */
public class ButtonArea
{
  private Composite buttonArea;

  /**
   * Erzeugt einen neuen Standard-Button-Bereich.
   * @param parent Composite, in dem die Buttons gezeichnet werden sollen.
   * @param numButtons Anzahl der Buttons, die hier drin gespeichert werden sollen.
   */
  public ButtonArea(Composite parent, int numButtons)
  {
    GridLayout layout = new GridLayout();
    layout.marginHeight=0;
    layout.marginWidth=0;
    layout.numColumns = numButtons;

    buttonArea = new Composite(parent, SWT.NONE);
		buttonArea.setBackground(Color.BACKGROUND.getSWTColor());
    buttonArea.setLayout(layout);
    buttonArea.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
  }

  /**
   * Fuegt der Area einen Button hinzu.
   * Beim Klick wird die Action ausgeloest.
   * @param name Bezeichnung des Buttons.
   * @param action auszuloesende Action.
   */
  public void addButton(String name, final Action action)
  {
		addButton(name,action,null,false);
  }


	/**
   * Fuegt der Area einen Button hinzu.
   * Beim Klick wird die Action ausgeloest.
   * @param name Bezeichnung des Buttons.
   * @param action auszuloesende Action.
   * @param context Optionaler Context, der der Action mitgegeben wird.
   */
  public void addButton(String name, final Action action, final Object context)
	{
		addButton(name,action,context,false);
	}

	/**
   * Fuegt der Area einen Button hinzu.
   * Beim Klick wird die Action ausgeloest.
   * @param name Bezeichnung des Buttons.
   * @param action auszuloesende Action.
   * @param context Optionaler Context, der der Action mitgegeben wird.
   * @param isDefault markiert den per Default aktiven Button.
   */
  public void addButton(String name, final Action action, final Object context, boolean isDefault)
	{
		final Button button = GUI.getStyleFactory().createButton(buttonArea);
		button.setText(name);
		button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		if (isDefault)
			GUI.getShell().setDefaultButton(button);

		button.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e) {
				GUI.startSync(new Runnable()
				{
					public void run()
					{
						try
						{
							action.handleAction(context);
						}
						catch (ApplicationException e)
						{
							GUI.getStatusBar().setErrorText(e.getMessage());
						}
					}
				});
			}
		});
	}
}

/*********************************************************************
 * $Log: ButtonArea.java,v $
 * Revision 1.11  2005/03/09 01:06:37  web0
 * @D javadoc fixes
 *
 * Revision 1.10  2004/10/29 16:16:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/10/19 23:33:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/10/08 13:38:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/09/17 14:40:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/07/27 23:41:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.4  2004/07/20 22:52:49  willuhn
 * @C Refactoring
 *
 * Revision 1.3  2004/06/30 20:58:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.1  2004/04/12 19:15:59  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.7  2004/03/30 22:08:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.5  2004/02/20 01:25:06  willuhn
 * @N nice dialog
 * @N busy indicator
 * @N new status bar
 *
 * Revision 1.4  2004/02/18 20:28:45  willuhn
 * @N jameica now stores window position and size
 *
 * Revision 1.3  2004/02/18 11:38:49  willuhn
 * @N flat style
 *
 * Revision 1.2  2004/02/18 01:40:29  willuhn
 * @N new white style
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.12  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.10  2004/01/04 18:48:36  willuhn
 * @N config store support
 *
 * Revision 1.9  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.8  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.6  2003/12/10 01:12:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2003/12/10 00:47:12  willuhn
 * @N SearchDialog done
 * @N FatalErrorView
 *
 * Revision 1.4  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
 * Revision 1.3  2003/11/24 17:27:50  willuhn
 * @N Context menu in table
 *
 * Revision 1.2  2003/11/22 20:43:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 **********************************************************************/