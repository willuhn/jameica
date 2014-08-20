/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/DialogInput.java,v $
 * $Revision: 1.28 $
 * $Date: 2011/10/20 16:16:53 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.input;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.GUI;
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
  private int maxlength = 0;

  private Object oldValue = PLACEHOLDER;

  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * Der dialog sollte anschliessend noch mittels setDialog() gesetzt werden.
   * Dieser Konstruktor ist nur eine Convenience-Funktion, um den Dialog
   * auch nach der Erzeugung des Input-Objektes setzen zu koennen.
   * @param value der initial einzufuegende Wert fuer das Eingabefeld.
   */
  public DialogInput(String value)
  {
    this(value,null);
  }

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
          Logger.debug("operation cancelled");
        }
				catch (Exception e1)
				{
					Logger.error("error while opening dialog",e1);
				}
      }
    });
  }
  
  /**
   * Speichert den anzuzeigenden Dialog.
   * @param d der anzuzeigende Dialog.
   */
  public void setDialog(AbstractDialog d)
  {
    this.dialog = d;
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
   * Legt die maximale Anzahl eingebbarer Zeichen fest.
   * @param length Maximal-Anzahl.
   * Zulaessig sind nur Werte, die groesser als "0" sind.
   */
  public void setMaxLength(int length)
  {
    if (length > 0)
      this.maxlength = length;
    if (this.text != null && !this.text.isDisposed())
      this.text.setTextLimit(this.maxlength);
  }
  
  /**
   * Liefert die maximale Anzahl eingebbarer Zeichen.
   * @return Anzahl der maximal eingebbaren Zeichen oder "0", wenn kein Limit definiert ist.
   */
  public int getMaxLength()
  {
    return this.maxlength;
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
    text = GUI.getStyleFactory().createText(parent);
  	if (value != null)
  		text.setText(value);
    if (this.maxlength > 0)
      text.setTextLimit(this.maxlength);

  	return text;
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#update()
   */
  protected void update() throws OperationCanceledException
  {
    super.update();

    // ueberschrieben, weil getValue() das Objekt zurueckliefert.
    // Wir pruefen hier aber auch, ob lediglich ein Text drin steht.
    if (text == null || text.isDisposed())
      return;
    
    String s = text.getText();

    org.eclipse.swt.graphics.Color color = null;
    
    if (isMandatory() && (s == null || s.length() == 0))
      color = Color.MANDATORY_BG.getSWTColor();

    text.setBackground(color);
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
