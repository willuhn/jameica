/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/DialogInput.java,v $
 * $Revision: 1.9 $
 * $Date: 2004/07/20 21:47:44 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.input;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.util.Logger;

/**
 * Eingabe-Feld, welches beim Klick auf den Button einen Dialog zur Auswahl
 * eines Objektes oeffnet.
 * Achtung: Der Dialog liefert beim Schliessen ein lapidares <code>Object</code>
 * zurueck. Da das Text-Eingabefeld natuerlich nicht wissen kann,
 * wie es das anzeigen soll, wird der Rueckgabewert des Dialogs
 * nicht ausgewertet. Stattdessen muss an den Dialog via <code>addCloseListener</code>
 * ein Listener angehangen werden, der beim Schliessen des Dialogs ausgeloest
 * wird. In dessen <code>event.data</code> befindet sich dann das jeweilige
 * Objekt. Das ist z.Bsp. bei <code>ListDialog</code> ein Fachobjekt aus der
 * Datenbank oder bei <code>CalendarDialog</code> ein <code>java.util.Date</code>-Objekt.
 * <p>
 * Hinweis: Der Listener darf nicht an das Input-Feld selbst angehangen werden,
 * denn die werden bei jedem Focus-Wechsel ausgeloest.
 * </p>
 * @author willuhn
 */
public class DialogInput extends ButtonInput
{

	private String value;
	private Text text;
  private AbstractDialog dialog;
  private Object choosen;

  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param value der initial einzufuegende Wert fuer das Eingabefeld.
   * @param d der Dialog.
   */
  public DialogInput(String value,AbstractDialog d)
  {
  	this.value = value;
  	this.dialog = d;
    addButtonListener(new MouseAdapter()
		{
			public void mouseUp(MouseEvent e)
			{
				Logger.debug("starting dialog");
				try {
					choosen = dialog.open();
					text.redraw();
					text.forceFocus(); // das muessen wir machen, damit die Listener ausgeloest werden
				}
				catch (Exception e1)
				{
					Logger.error("error while opening dialog",e1);
				}
			}
		});
  }

  /**
   * Liefert das Objekt, welches in dem Dialog ausgewaehlt wurde.
   * Fuer gewoehnlich ist das ein Fach-Objekt.
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    return choosen;
  }
  
  /**
   * Liefert den derzeit angezeigten Text.
   * @return angezeigter Text.
   */
  public String getText()
  {
		if (text != null && !text.isDisposed())
			return text.getText();
  	return value;
  }


  /**
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (value == null)
      return;
    this.value = value.toString();
    if (this.text != null && !this.text.isDisposed())
	    this.text.setText(value.toString());
  }

  /**
   * @see de.willuhn.jameica.gui.input.ButtonInput#getClientControl(org.eclipse.swt.widgets.Composite)
   */
  public Control getClientControl(Composite parent) {
  	text = GUI.getStyleFactory().createText(parent);
  	if (value != null)
  		text.setText(value);
  	return text;
  }
  
}

/*********************************************************************
 * $Log: DialogInput.java,v $
 * Revision 1.9  2004/07/20 21:47:44  willuhn
 * @N ContextMenu
 *
 * Revision 1.8  2004/07/09 00:12:47  willuhn
 * @C Redesign
 *
 * Revision 1.7  2004/06/30 20:58:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/06/02 21:15:15  willuhn
 * @B win32 fixes in flat style
 * @C made ButtonInput more abstract
 *
 * Revision 1.5  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.4  2004/05/11 23:32:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/04/29 19:15:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/04/27 00:04:44  willuhn
 * @D javadoc
 *
 * Revision 1.1  2004/04/21 22:28:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.11  2004/04/05 23:29:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/03/25 00:45:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/03/24 00:46:03  willuhn
 * @C refactoring
 *
 * Revision 1.8  2004/03/11 08:56:55  willuhn
 * @C some refactoring
 *
 * Revision 1.7  2004/03/06 18:24:23  willuhn
 * @D javadoc
 *
 * Revision 1.6  2004/02/24 22:46:53  willuhn
 * @N GUI refactoring
 *
 * Revision 1.5  2004/02/23 20:30:34  willuhn
 * @C refactoring in AbstractDialog
 *
 * Revision 1.4  2004/02/22 20:05:21  willuhn
 * @N new Logo panel
 *
 * Revision 1.3  2004/02/18 20:28:45  willuhn
 * @N jameica now stores window position and size
 *
 * Revision 1.2  2004/02/18 01:40:30  willuhn
 * @N new white style
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.9  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.7  2003/12/20 16:52:45  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2003/12/16 02:27:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.4  2003/12/10 00:47:12  willuhn
 * @N SearchDialog done
 * @N FatalErrorView
 *
 * Revision 1.3  2003/12/08 16:19:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/12/08 15:41:09  willuhn
 * @N searchInput
 *
 * Revision 1.1  2003/12/05 18:43:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2003/12/01 21:22:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2003/12/01 20:28:58  willuhn
 * @B filter in DBIteratorImpl
 * @N InputFelder generalisiert
 *
 * Revision 1.5  2003/11/24 14:21:53  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2003/11/24 11:51:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/11/22 20:43:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 * Revision 1.1  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 **********************************************************************/