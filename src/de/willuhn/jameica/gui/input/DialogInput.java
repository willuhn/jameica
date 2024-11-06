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

import java.util.ArrayList;

import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyListener;
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
 *
 * <p>Achtung: Der Dialog liefert beim Schliessen ein lapidares {@link Object}
 * zurueck. Da das Text-Eingabefeld natuerlich nicht wissen kann,
 * wie es das anzeigen soll, wird der Rueckgabewert des Dialogs
 * nicht ausgewertet. Stattdessen muss an den Dialog via {@link #addListener(Listener)}
 * ein Listener angehangen werden, der beim Schliessen des Dialogs ausgeloest
 * wird. In dessen {@code event.data} befindet sich dann das jeweilige
 * Objekt. Das ist z.Bsp. bei {@link de.willuhn.jameica.gui.dialogs.ListDialog} ein Fachobjekt aus der
 * Datenbank oder bei {@link de.willuhn.jameica.gui.dialogs.CalendarDialog} ein {@link java.util.Date}-Objekt.
 *
 * <p>Hinweis: Der Listener darf nicht an das Input-Feld selbst angehangen werden,
 * denn die werden bei jedem Focus-Wechsel ausgeloest.
 *
 * @author willuhn
 */
public class DialogInput extends ButtonInput
{
  private static final Object PLACEHOLDER = new Object();

	private Text text;
  private AbstractDialog dialog;
  private Object choosen;
  private int maxlength = 0;
  
  private ArrayList<FocusListener> focusListeners = new ArrayList<>();
  private ArrayList<KeyListener> keyListeners = new ArrayList<>();

  private Object oldValue = PLACEHOLDER;

  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * Der Dialog sollte anschliessend noch mittels {@link #setDialog(AbstractDialog)} gesetzt werden.
   * Dieser Konstruktor ist nur eine Convenience-Funktion, um den Dialog
   * auch nach der Erzeugung des Input-Objektes setzen zu koennen.
   *
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
   */
  @Override
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
   * Wie {@link de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)},
   * speichert jedoch nicht den anzuzeigenden Text sondern das FachObjekt.
   * Sprich: Das Objekt, welches auch geliefert wird, wenn der Dialog
   * zur Auswahl des Objektes verwendet werden wuerde.
   *
   * <p>Soll der anzuzeigende Text geaendert werden, dann bitte die
   * Funktion {@link #setText(String)} verwenden.
   */
  @Override
  public void setValue(Object value)
  {
  	this.choosen = value;
  }

  public void setSelection(int start, int end) {
    this.text.setSelection(start, end);
  }

  public void addFocusListener(FocusListener listener) {
    this.focusListeners.add(listener);
  }

  public void addKeyListener(KeyListener listener) {
    this.keyListeners.add(listener);
  }

  @Override
  public Control getClientControl(Composite parent) {
    if (text != null) {
      return text;
    }
    text = GUI.getStyleFactory().createText(parent);
  	if (value != null)
  		text.setText(value);

    if (this.maxlength > 0)
      text.setTextLimit(this.maxlength);

    for (FocusListener l: focusListeners) {
      this.text.addFocusListener(l);
    }
    for (KeyListener l: keyListeners) {
      this.text.addKeyListener(l);
    }

  	return text;
  }

  @Override
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

  @Override
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
