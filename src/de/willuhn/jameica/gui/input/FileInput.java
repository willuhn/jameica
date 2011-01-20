/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/FileInput.java,v $
 * $Revision: 1.18 $
 * $Date: 2011/01/20 17:10:39 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.input;

import java.io.File;

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
 * Input-Feld fuer die Auswahl von Dateien.
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

        try
        {
          File f = new File(value);
          dialog.setFileName(f.getName());
          dialog.setFilterPath(f.getParent());
        }
        catch (Exception e)
        {
          Logger.error("unable to parse path",e);
          dialog.setFileName(value);
        }
        
        customize(dialog);
        setValue(dialog.open());
        text.forceFocus(); // das muessen wir machen, damit die Listener ausgeloest werden
      }
    });
  }

  /**
   * Erlaubt benutzerdefinierte Anpassungen am Dialog in abgeleiteten Klassen.
   * @param fd der Datei-Dialog.
   */
  protected void customize(FileDialog fd)
  {
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
   * Erwartet ein Objekt des Typs String oder File.
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (value == null)
      return;

    if (!(value instanceof String) && !(value instanceof File))
			return;

    this.value = value.toString();
    if (this.text != null && !this.text.isDisposed())
    {
      this.text.setText((String) value);
      this.text.redraw();
    }
  }

  /**
	 * @see de.willuhn.jameica.gui.input.ButtonInput#getClientControl(org.eclipse.swt.widgets.Composite)
	 */
  public Control getClientControl(Composite parent)
  {
    if (this.text != null && !this.text.isDisposed())
      return this.text;
    
    this.text = GUI.getStyleFactory().createText(parent);
    this.text.setText(this.value == null ? "" : this.value);
  	return this.text;
  }

}

/*********************************************************************
 * $Log: FileInput.java,v $
 * Revision 1.18  2011/01/20 17:10:39  willuhn
 * @B Dateiname und Pfad getrennt dem Dialog uebergeben. Sonst kann man keine sauberen Datei->Speichern-Dialoge machen
 *
 * Revision 1.17  2010-12-31 01:01:05  willuhn
 * @N BUGZILLA 969
 *
 * Revision 1.16  2008/07/17 08:47:12  willuhn
 * @N Heiners Patch zum expliziten Vorgeben von Dateiendungen im Dialog
 *
 * Revision 1.15  2008/02/29 01:12:30  willuhn
 * @N Erster Code fuer neues Backup-System
 * @N DirectoryInput
 * @B Fixes an FileInput, TextInput
 **********************************************************************/