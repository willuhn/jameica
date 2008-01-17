/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/DialogInput.java,v $
 * $Revision: 1.21 $
 * $Date: 2008/01/17 23:46:05 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.input;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;

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
  private final static Object PLACEHOLDER = new Object();

	private Text text;
  private AbstractDialog dialog;
  private Object choosen;

  private Object oldValue = PLACEHOLDER;

  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param value der initial einzufuegende Wert fuer das Eingabefeld.
   * @param d der Dialog.
   */
  public DialogInput(String value,AbstractDialog d)
  {
  	this.value = value;
  	this.dialog = d;
    addButtonListener(new Listener()
    {
      public void handleEvent(Event event)
      {
				Logger.debug("starting dialog");
				try {
					choosen = dialog.open();
					text.redraw();
					text.forceFocus(); // das muessen wir machen, damit die Listener ausgeloest werden
				}
        catch (OperationCanceledException oce)
        {
          Logger.info("operation cancelled");
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
	 * Speichert den anzuzeigenden Text.
   * @param text anzuzeigender Text.
   */
  public void setText(String text)
	{
		if (text == null)
			return;
    this.value = text; // BUGZILLA 541
		if (this.text != null && !this.text.isDisposed())
			this.text.setText(text);
	}

  /**
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   * Speichert jedoch nicht den anzuzeigenden Text sondern das FachObjekt.
   * Sprich: Das Objekt, welches auch geliefert wird, wenn der Dialog
   * zur Auswahl des Objektes verwendet werden wuerde.
   * Soll der anzuzeigende Text geaendert werden, dann bitte die
   * Funktion <code>setText(String)</code> verwenden.
   */
  public void setValue(Object value)
  {
  	this.choosen = value;
  }

  /**
   * @see de.willuhn.jameica.gui.input.ButtonInput#getClientControl(org.eclipse.swt.widgets.Composite)
   */
  public Control getClientControl(Composite parent) {
//  	text = GUI.getStyleFactory().createText(parent);
    text = new Text(parent,SWT.NONE | SWT.SINGLE);
    text.setForeground(Color.WIDGET_FG.getSWTColor());
  	if (value != null)
  		text.setText(value);
  	return text;
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#update()
   */
  void update() throws OperationCanceledException
  {
    super.update();

    // ueberschrieben, weil getValue() das Objekt zurueckliefert.
    // Wir pruefen hier aber auch, ob lediglich ein Text drin steht.
    if (text == null || text.isDisposed())
      return;
    
    String s = text.getText();

    org.eclipse.swt.graphics.Color color = null;
    
    if (!isEnabled())
      color = Color.BACKGROUND.getSWTColor();
    else if (isMandatory() && (s == null || s.length() == 0))
      color = Color.MANDATORY_BG.getSWTColor();
    else
      color = Color.WIDGET_BG.getSWTColor();

    text.setBackground(color);

    // Das ist der Rahmen vom ButtonInput, den muessen wir auch noch anpassen
    Composite comp = text.getParent();
    if (comp != null && !comp.isDisposed())
      comp.setBackground(color);
  }

  /**
   * Ueberschrieben, weil nur der angezeigte Text interessiert.
   * @see de.willuhn.jameica.gui.input.Input#hasChanged()
   */
  public boolean hasChanged()
  {
    Object newValue = getText();

    try
    {
      // Wir wurden noch nie aufgerufen
      if (oldValue == PLACEHOLDER || oldValue == newValue)
        return false;

      return newValue == null || !newValue.equals(oldValue);
    }
    finally
    {
      oldValue = newValue;
    }
  }
}

/*********************************************************************
 * $Log: DialogInput.java,v $
 * Revision 1.21  2008/01/17 23:46:05  willuhn
 * @B Bug 541
 *
 * Revision 1.20  2007/04/26 14:38:11  willuhn
 * @B Manuell eingegebenes Datum bei anschliessendem Oeffnen des Kalender-Dialogs uebernehmen
 * @N hasChanged in DialogInput ueberschrieben
 *
 * Revision 1.19  2007/01/23 15:52:10  willuhn
 * @C update() check for recursion
 * @N mandatoryCheck configurable
 *
 * Revision 1.18  2007/01/05 10:36:49  willuhn
 * @C Farbhandling - Jetzt aber!
 *
 * Revision 1.17  2007/01/05 09:41:53  willuhn
 * @C change color in DialogInput too
 *
 * Revision 1.16  2007/01/05 09:31:40  willuhn
 * @C change color in DialogInput too
 *
 * Revision 1.15  2006/12/28 15:35:52  willuhn
 * @N Farbige Pflichtfelder
 *
 * Revision 1.14  2005/06/23 23:03:25  web0
 * *** empty log message ***
 *
 * Revision 1.13  2005/03/09 01:06:37  web0
 * @D javadoc fixes
 *
 * Revision 1.12  2004/11/12 18:23:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/10/08 00:19:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/07/27 23:41:30  willuhn
 * *** empty log message ***
 *
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