/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.gui.input;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * @author willuhn
 * Ist zustaendig fuer Text-Eingabefelder, hinter denen sich jedoch noch ein
 * zusaetzlicher Button fuer eine Verzeichnisauswahl befindet.
 */
public class DirectoryInput extends ButtonInput
{

	private Text text;
	
  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param dir der initial einzufuegende Wert fuer das Eingabefeld.
   */
  public DirectoryInput(String dir)
  {
    this.value = dir;
    this.setButtonImage(SWTUtil.getImage("folder.png"));
    addButtonListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        Logger.debug("starting dir dialog");
        DirectoryDialog dialog = new DirectoryDialog(GUI.getShell());
        dialog.setText(Application.getI18n().tr("Bitte w�hlen Sie ein Verzeichnis aus"));
        dialog.setFilterPath(value);
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
      checkValidity();
      this.text.redraw();
    }
  }

  /**
	 * @see de.willuhn.jameica.gui.input.ButtonInput#getClientControl(org.eclipse.swt.widgets.Composite)
	 */
  public Control getClientControl(Composite parent) {
    if (text != null && !text.isDisposed())
      return text;
    text = GUI.getStyleFactory().createText(parent);
  	text.setText(this.value == null ? "" : this.value);
  	text.addListener(SWT.Modify, new Listener() {
      @Override
      public void handleEvent(Event event)
      {
        checkValidity();
      }
    });
  	checkValidity();
  	return text;
  }

  /**
   * Pruefen ob eingegebenes Verzeichnis existiert.
   * Wenn Verzeichnis nicht existiert Eingabe mit Fehlerfarbe markieren.
   */
  private void checkValidity() {
    if(this.text == null || this.text.isDisposed())
      return;
    
    if(this.text.getText().trim().isEmpty() || new File(this.text.getText()).isDirectory())
      this.text.setForeground(Color.FOREGROUND.getSWTColor());
    else
      this.text.setForeground(Color.ERROR.getSWTColor());
  }
}

/*********************************************************************
 * $Log: DirectoryInput.java,v $
 * Revision 1.3  2011/08/01 12:09:31  willuhn
 * @C Existierendes Control zurueckliefern, falls bereits vorhanden
 *
 * Revision 1.2  2011-03-04 16:30:51  willuhn
 * @C Folder-Icon statt "..."
 *
 * Revision 1.1  2008/02/29 01:12:30  willuhn
 * @N Erster Code fuer neues Backup-System
 * @N DirectoryInput
 * @B Fixes an FileInput, TextInput
 *
 **********************************************************************/