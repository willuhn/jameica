/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/ButtonInput.java,v $
 * $Revision: 1.1 $
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

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Color;

/**
 * Text-Eingabefeld, welches jedoch noch einen Button hinten dran
 * besitzt.
 * @author willuhn
 */
public abstract class ButtonInput extends AbstractInput
{

  private Composite comp;
  protected Text text;
  protected String value;
	protected boolean textEnabled = true;
  protected boolean buttonEnabled = true;

	private Button button;
	private String buttonText;
	private Image  buttonImage;
	private ArrayList buttonListeners = new ArrayList();

  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param value der initial einzufuegende Wert fuer das Eingabefeld.
   * @param d der Dialog.
   */
  public ButtonInput(String value)
  {
    this.value = value;
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#getControl()
   */
  public final Control getControl()
  {

		comp = new Composite(getParent(),SWT.NONE);
		comp.setBackground(Color.BACKGROUND.getSWTColor());
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight=0;
		layout.marginWidth=0;
		layout.horizontalSpacing = 5;
		layout.verticalSpacing = 0;
		comp.setLayout(layout);
  
		text = GUI.getStyleFactory().createText(comp);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = 1;
		text.setLayoutData(gd);
		text.setText((value == null ? "" : value));
		text.setEnabled(textEnabled);
		text.addFocusListener(new FocusAdapter(){
			public void focusGained(FocusEvent e){
				text.selectAll();
			}
		});
  
    button = GUI.getStyleFactory().createButton(comp);
		if (this.buttonImage == null && this.buttonText == null)
	    button.setText("...");
	  else if (this.buttonImage != null)
			button.setImage(this.buttonImage);
		else if (this.buttonText != null)
			button.setText(this.buttonText);
    button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    button.setAlignment(SWT.RIGHT);
    button.setEnabled(buttonEnabled);
		for (int i=0;i<buttonListeners.size();++i)
		{
			button.addMouseListener((MouseListener) buttonListeners.get(i));
		}
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
  public final void setButtonText(String text)
	{
		this.buttonText = text;
	}

	/**
	 * Definiert das auf dem Button anzuzeigende Image.
	 * @see #setButtonText(String).
   * @param image anzuzeigendes Image.
   */
  public final void setButtonImage(Image image)
	{
		this.buttonImage = image;
	}

	/**
	 * Fuegt zum Button einen Listener hinzu.
   * @param l Listener.
   */
  protected final void addButtonListener(MouseListener l)
	{
		if (l == null)
			return;
		buttonListeners.add(l);
	}

	/**
	 * Liefert den angezeigten Text zurueck.
	 * @return Text.
	 */
	public final String getText()
	{
		return text.getText();
	}

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#focus()
   */
  public final void focus()
  {
    text.setFocus();
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#disable()
   */
  public final void disable()
  {
  	disableButton();
  	disableText();
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#enable()
   */
  public final void enable()
  {
  	enableButton();
  	enableText();
  }

	/**
   * Aktiviert nur den Text.
   */
  public final void enableText()
	{
		textEnabled = true;
		if (text != null && !text.isDisposed())
			text.setEnabled(true);
	}
	
	/**
   * Aktiviert nur den Button.
   */
  public final void enableButton()
	{
		buttonEnabled = true;
		if (button != null && !button.isDisposed())
			button.setEnabled(true);
	}
	
	/**
   * Deaktiviert nur den Text.
   */
  public final void disableText()
	{
		textEnabled = false;
		if (text != null && !text.isDisposed())
			text.setEnabled(false);
	}
	
	/**
   * Deaktiviert nur den Button.
   */
  public final void disableButton()
	{
		buttonEnabled = false;
		if (button != null && !button.isDisposed())
			button.setEnabled(false);
	}
}

/*********************************************************************
 * $Log: ButtonInput.java,v $
 * Revision 1.1  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 **********************************************************************/