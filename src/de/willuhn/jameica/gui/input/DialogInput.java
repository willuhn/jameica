/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/DialogInput.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/04/27 00:04:44 $
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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
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
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.util.Style;

/**
 * Text-Eingabefeld, welches jedoch noch einen Button hinten dran
 * besitzt (mit einer Lupe drauf). Klickt man auf den, wird der im Konstruktor
 * uebergeben AbstractDialog ausgefuehrt.
 * Achtung: Der Dialog liefert beim Schliessen ein lapidares <code>Object</code>
 * zurueckg. Da das Text-Eingabefeld natuerlich nicht wissen kann,
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
public class DialogInput extends AbstractInput
{

  private Composite comp;
  private AbstractDialog dialog;
  private Text text;
  private String value;
  private boolean enabled = true;

	private Button button;
	private String buttonText;
	private Image  buttonImage;

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
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#getControl()
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
    text.setEnabled(enabled);
    text.addFocusListener(new FocusAdapter(){
      public void focusGained(FocusEvent e){
        text.setSelection(0, text.getText().length());
      }
    });

    button = new Button(comp,SWT.NONE);
		if (this.buttonImage == null && this.buttonText == null)
	    button.setImage(Style.getImage("search.gif"));
	  else if (this.buttonImage != null)
			button.setImage(this.buttonImage);
		else if (this.buttonText != null)
			button.setText(this.buttonText);
    button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    button.setAlignment(SWT.RIGHT);
    button.setEnabled(enabled);
    button.addMouseListener(new MouseAdapter()
    {
      public void mouseUp(MouseEvent e)
      {
        Application.getLog().debug("starting dialog");
				try {
					choosen = dialog.open();
					text.redraw();
					text.forceFocus(); // das muessen wir machen, damit die Lister ausgeloest werden
				}
				catch (Exception e1)
				{
					Application.getLog().error("error while opening dialog",e1);
				}
      }
    });
 
    return comp;
  }

	/**
	 * Definiert den auf dem Button anzuzeigenden Text.
	 * Leider kann auf dem Button nicht Image <b>und</b> Text
	 * angezeigt werden. Wenn also sowohl <code>setButtonText</code> und
	 * <code>setButtonImage</code> gesetzt werden, wird nur das Image
	 * angezeigt.
	 * Wird nichts von beiden gesetzt, wird ein Image mit einer Lupe angezeigt.
   * @param text auf dem Button anzuzeigender Text.
   */
  public void setButtonText(String text)
	{
		this.buttonText = text;
	}

	/**
	 * Definiert das auf dem Button anzuzeigende Image.
	 * @see #setButtonText(String).
   * @param image anzuzeigendes Image.
   */
  public void setButtonImage(Image image)
	{
		this.buttonImage = image;
	}

  /**
   * Liefert das Objekt, welches in dem Dialog ausgewaehlt wurde.
   * Fuer gewoehnlich ist das ein Fach-Objekt.
   * @see de.willuhn.jameica.gui.input.AbstractInput#getValue()
   */
  public Object getValue()
  {
    return choosen;
  }

	/**
	 * Liefert den angezeigten Text zurueck.
	 * @return Text.
	 */
	public String getText()
	{
		return text.getText();
	}

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (value == null)
      return;
    this.text.setText(value.toString());
    this.text.redraw();
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#focus()
   */
  public void focus()
  {
    text.setFocus();
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#disable()
   */
  public void disable()
  {
  	enabled = false;
  	if (text != null && !text.isDisposed())
	    text.setEnabled(false);
	  if (button != null && !button.isDisposed())
	  	button.setEnabled(false);
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#enable()
   */
  public void enable()
  {
		enabled = true;
		if (text != null && !text.isDisposed())
	    text.setEnabled(true);
		if (button != null && !button.isDisposed())
			button.setEnabled(true);
  }

}

/*********************************************************************
 * $Log: DialogInput.java,v $
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