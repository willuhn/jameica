/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/FileInput.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/05/23 15:30:52 $
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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.FileDialog;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;

/**
 * @author willuhn
 * Ist zustaendig fuer Text-Eingabefelder, hinter denen sich jedoch noch ein
 * zusaetzlicher Button fuer eine Dateisuche befindet.
 */
public class FileInput extends ButtonInput
{

  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param value der initial einzufuegende Wert fuer das Eingabefeld.
   */
  public FileInput(String value)
  {
  	super(value);
		addButtonListener(new MouseAdapter()
		{
			public void mouseUp(MouseEvent e)
			{
				Application.getLog().debug("starting file dialog");
				FileDialog dialog = new FileDialog(GUI.getShell(),SWT.OPEN);
				String s = dialog.open();
				if (s != null && !"".equals(s))
					text.setText(s); // wir schreiben den Wert nur rein, wenn etwas uebergeben wurde
				text.redraw();
				text.forceFocus(); // das muessen wir machen, damit die CommentLister ausgeloest werden
			}
		});
  }


  /**
   * Liefert ein Objekt des Typs java.lang.String.
   * @see de.willuhn.jameica.gui.input.AbstractInput#getValue()
   */
  public Object getValue()
  {
    return text.getText();
  }

  /**
   * Erwartet ein Objekt des Typs java.lang.String.
   * @see de.willuhn.jameica.gui.input.AbstractInput#setValue(java.lang.Object)
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

}

/*********************************************************************
 * $Log: FileInput.java,v $
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