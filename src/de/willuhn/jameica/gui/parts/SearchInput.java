/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Attic/SearchInput.java,v $
 * $Revision: 1.7 $
 * $Date: 2004/03/06 18:24:23 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.parts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.dialogs.ListDialog;
import de.willuhn.jameica.gui.util.Style;

/**
 * Text-Eingabefeld, welches jedoch noch einen Button hinten dran
 * besitzt (mit einer Lupe drauf). Klickt man auf den, wird der im Konstruktor
 * uebergeben ListDialog ausgefuehrt.
 * Achtung: Beim Schliessen des Dialogs wird ein lapidares <code>Object</code>
 * zurueckgeliefert. Da das Eingabe-Feld natuerlich nicht wissen kann,
 * wie es das in sich reinschreiben soll, wird der Rueckgabewert des Dialogs
 * nicht ausgewertet. Es ist Sache des Benutzers, an den ListDialog einen
 * Listener dranzuhaengen, in dessen <code>handleEvent</code> das ausgewaehlte
 * Objekt auszulesen und die Objekt-Werte in die Eingabe-Felder zu schreiben.
 * @author willuhn
 */
public class SearchInput extends Input
{

  private Composite comp;
  private ListDialog dialog;
  private Text text;
  private Button button;
  private String value;

  private Object choosen;

  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param value der initial einzufuegende Wert fuer das Eingabefeld.
   * @param d der ListDialog.
   */
  public SearchInput(String value,ListDialog d)
  {
    this.value = value;
    this.dialog = d;
  }

  /**
   * @see de.willuhn.jameica.gui.parts.Input#getControl()
   */
  public Control getControl()
  {

		comp = new Composite(getParent(),SWT.NONE);
		comp.setBackground(Style.COLOR_BG);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight=0;
		layout.marginWidth=0;
		comp.setLayout(layout);
  
		Composite around = new Composite(comp,SWT.NONE);
		around.setBackground(Style.COLOR_BORDER);
		around.setLayout(new FormLayout());
		around.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		FormData comboFD = new FormData();
		comboFD.left = new FormAttachment(0, 1);
		comboFD.top = new FormAttachment(0, 1);
		comboFD.right = new FormAttachment(100, -1);
		comboFD.bottom = new FormAttachment(100, -1);
    
		Composite around2 = new Composite(around,SWT.NONE);
		around2.setBackground(Style.COLOR_WHITE);
		around2.setLayout(new FormLayout());
		around2.setLayoutData(comboFD);

		FormData comboFD2 = new FormData();
		comboFD2.left = new FormAttachment(0, 2);
		comboFD2.top = new FormAttachment(0, 2);
		comboFD2.right = new FormAttachment(100, -2);
		comboFD2.bottom = new FormAttachment(100, -2);
  
    text = new Text(around2, SWT.NONE);
		text.setLayoutData(comboFD2);
		text.setBackground(Style.COLOR_WHITE);
    text.setText((value == null ? "" : value));
    text.addFocusListener(new FocusAdapter(){
      public void focusGained(FocusEvent e){
        text.setSelection(0, text.getText().length());
      }
    });

    button = new Button(comp,SWT.NONE);
    button.setImage(Style.getImage("search.gif"));
    button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    button.setAlignment(SWT.RIGHT);
    button.addMouseListener(new MouseAdapter()
    {
      public void mouseUp(MouseEvent e)
      {
        Application.getLog().debug("starting list dialog");
				try {
					choosen = dialog.open();
					text.redraw();
					text.forceFocus(); // das muessen wir machen, damit die CommentLister ausgeloest werden
				}
				catch (Exception e1)
				{
					Application.getLog().error("error while opening listDialog",e1);
				}
      }
    });
 
    return comp;
  }

  /**
   * Liefert das Objekt, welches in dem ListDialog ausgewaehlt wurde.
   * Fuer gewoehnlich ist das ein Fach-Objekt.
   * @see de.willuhn.jameica.gui.parts.Input#getValue()
   */
  public Object getValue()
  {
    return choosen;
  }

  /**
   * @see de.willuhn.jameica.gui.parts.Input#setValue(java.lang.String)
   */
  public void setValue(Object value)
  {
    if (value == null)
      return;
    this.text.setText(value.toString());
    this.text.redraw();
  }

  /**
   * @see de.willuhn.jameica.gui.parts.Input#focus()
   */
  public void focus()
  {
    text.setFocus();
  }

  /**
   * @see de.willuhn.jameica.gui.parts.Input#disable()
   */
  public void disable()
  {
    text.setEnabled(false);
  }

  /**
   * @see de.willuhn.jameica.gui.parts.Input#enable()
   */
  public void enable()
  {
    text.setEnabled(true);
  }

}

/*********************************************************************
 * $Log: SearchInput.java,v $
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