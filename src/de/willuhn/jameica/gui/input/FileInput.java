/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/FileInput.java,v $
 * $Revision: 1.16 $
 * $Date: 2008/07/17 08:47:12 $
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
   * @param file der initial einzufuegende Wert fuer das Eingabefeld.
   */
  public FileInput(final String file)
  {
    this(file,false);
  }
  

  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param file der initial einzufuegende Wert fuer das Eingabefeld.
   * @param save legt fest, ob es ein Speichern-Dialog sein soll.
   */
  public FileInput(String file, boolean save)
  {
    this(file, save, null);
  }

  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param file der initial einzufuegende Wert fuer das Eingabefeld.
   * @param save legt fest, ob es ein Speichern-Dialog sein soll.
   * @param extensions legt die zulaessigen Datei-Endungen fest, die vom Dialog angezeigt werden sollen.
   */
  public FileInput(String file, final boolean save, final String[] extensions)
  {
    this.value = file;
    addButtonListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        Logger.debug("starting file dialog");
        FileDialog dialog = new FileDialog(GUI.getShell(), save ? SWT.SAVE : SWT.OPEN);
        if (extensions != null && extensions.length > 0)
          dialog.setFilterExtensions(extensions);
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
    if (text == null || text.isDisposed())
      return this.value;
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

    this.value = (String) value;
    if (this.text != null && !this.text.isDisposed())
    {
      this.text.setText((String) value);
      this.text.redraw();
    }
  }

  /**
	 * @see de.willuhn.jameica.gui.input.ButtonInput#getClientControl(org.eclipse.swt.widgets.Composite)
	 */
  public Control getClientControl(Composite parent) {
    text = GUI.getStyleFactory().createText(parent);
    text.setText(this.value == null ? "" : this.value);
  	return text;
  }

}

/*********************************************************************
 * $Log: FileInput.java,v $
 * Revision 1.16  2008/07/17 08:47:12  willuhn
 * @N Heiners Patch zum expliziten Vorgeben von Dateiendungen im Dialog
 *
 * Revision 1.15  2008/02/29 01:12:30  willuhn
 * @N Erster Code fuer neues Backup-System
 * @N DirectoryInput
 * @B Fixes an FileInput, TextInput
 *
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
 **********************************************************************/