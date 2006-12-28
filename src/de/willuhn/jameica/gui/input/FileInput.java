/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/FileInput.java,v $
 * $Revision: 1.14 $
 * $Date: 2006/12/28 15:35:52 $
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * @author willuhn
 * Ist zustaendig fuer Text-Eingabefelder, hinter denen sich jedoch noch ein
 * zusaetzlicher Button fuer eine Dateisuche befindet.
 */
public class FileInput extends ButtonInput
{

	private Text text;
	
  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param value der initial einzufuegende Wert fuer das Eingabefeld.
   */
  public FileInput(final String value)
  {
    this(value,false);
  }

  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param value der initial einzufuegende Wert fuer das Eingabefeld.
   * @param save legt fest, ob es ein Speichern-Dialog sein soll.
   */
  public FileInput(final String value, final boolean save)
  {
    this.value = value;
    addButtonListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        Logger.debug("starting file dialog");
        FileDialog dialog = new FileDialog(GUI.getShell(), save ? SWT.SAVE : SWT.OPEN);
        dialog.setText(Application.getI18n().tr("Bitte wählen Sie die Datei aus"));
        dialog.setFileName(value);
        setValue(dialog.open());
        text.forceFocus(); // das muessen wir machen, damit die Listener ausgeloest werden
      }
    });
  }

  /**
   * Liefert ein Objekt des Typs java.lang.String.
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    return text.getText();
  }

  /**
   * Erwartet ein Objekt des Typs java.lang.String.
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (value == null)
      return;

    if (!(value instanceof String))
			return;

		this.text.setText((String) value);
    this.text.redraw();
  }

  /**
	 * @see de.willuhn.jameica.gui.input.ButtonInput#getClientControl(org.eclipse.swt.widgets.Composite)
	 */
  public Control getClientControl(Composite parent) {
//  	text = GUI.getStyleFactory().createText(parent);
    text = new Text(parent,SWT.NONE | SWT.SINGLE);
    text.setForeground(Color.WIDGET_FG.getSWTColor());
    text.setBackground(Color.WIDGET_BG.getSWTColor());
  	text.setText(value == null ? "" : value);
  	return text;
  }

}

/*********************************************************************
 * $Log: FileInput.java,v $
 * Revision 1.14  2006/12/28 15:35:52  willuhn
 * @N Farbige Pflichtfelder
 *
 * Revision 1.13  2006/04/20 08:44:03  web0
 * @C s/Childs/Children/
 *
 * Revision 1.12  2005/11/14 11:36:23  web0
 * *** empty log message ***
 *
 * Revision 1.11  2005/03/09 01:06:36  web0
 * @D javadoc fixes
 *
 * Revision 1.10  2005/01/18 23:00:32  willuhn
 * @C Default focus to actual file
 *
 * Revision 1.9  2004/11/12 18:23:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/07/27 23:41:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/07/09 00:12:47  willuhn
 * @C Redesign
 *
 * Revision 1.6  2004/06/30 20:58:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/06/08 22:54:00  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/06/02 21:15:15  willuhn
 * @B win32 fixes in flat style
 * @C made ButtonInput more abstract
 *
 * Revision 1.3  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.2  2004/04/27 00:04:44  willuhn
 * @D javadoc
 *
 * Revision 1.1  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.8  2004/03/11 08:56:55  willuhn
 * @C some refactoring
 *
 * Revision 1.7  2004/03/06 18:24:23  willuhn
 * @D javadoc
 *
 * Revision 1.6  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.5  2004/02/18 20:28:45  willuhn
 * @N jameica now stores window position and size
 *
 * Revision 1.4  2004/02/18 17:14:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/02/18 01:40:30  willuhn
 * @N new white style
 *
 * Revision 1.2  2004/01/29 00:07:24  willuhn
 * @N Text widget
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.3  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.2  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.1  2003/12/21 20:59:00  willuhn
 * @N added internal SSH tunnel
 *
 **********************************************************************/