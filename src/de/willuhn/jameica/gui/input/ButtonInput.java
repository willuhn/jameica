/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/ButtonInput.java,v $
 * $Revision: 1.5 $
 * $Date: 2004/07/09 00:12:47 $
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
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.willuhn.jameica.gui.GUI;

/**
 * Eingabefeld, welches jedoch noch einen Button hinten dran
 * besitzt.
 * @author willuhn
 */
public abstract class ButtonInput extends AbstractInput
{

  private Composite comp;
  protected Control clientControl;
  protected String value;
	protected boolean clientControlEnabled = true;
  protected boolean buttonEnabled = true;

	private Button button;
	private String buttonText;
	private Image  buttonImage;
	private ArrayList buttonListeners = new ArrayList();

  /**
   * Liefert das einzubettende Eingabefeld.
   * @param parent Composite, in dem sich das ClientControl malen soll.
   * @return das fertig gemalte Control.
   */
  public abstract Control getClientControl(Composite parent);
  
  /**
   * @see de.willuhn.jameica.gui.input.Input#getControl()
   */
  public final Control getControl()
  {

		comp = new Composite(getParent(),SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight=0;
		layout.marginWidth=0;
		layout.horizontalSpacing = 5;
		layout.verticalSpacing = 0;
		comp.setLayout(layout);
  
		clientControl = getClientControl(comp);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = 1;
		clientControl.setLayoutData(gd);
		clientControl.setEnabled(clientControlEnabled);
  
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
   * @see de.willuhn.jameica.gui.input.Input#focus()
   */
  public final void focus()
  {
    clientControl.setFocus();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#disable()
   */
  public final void disable()
  {
  	disableButton();
  	disableClientControl();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#enable()
   */
  public final void enable()
  {
  	enableButton();
  	enableClientControl();
  }

	/**
   * Aktiviert nur das ClientControl.
   */
  public final void enableClientControl()
	{
		clientControlEnabled = true;
		if (clientControl != null && !clientControl.isDisposed())
			clientControl.setEnabled(true);
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
   * Deaktiviert nur das ClientControl.
   */
  public final void disableClientControl()
	{
		clientControlEnabled = false;
		if (clientControl != null && !clientControl.isDisposed())
			clientControl.setEnabled(false);
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
 * Revision 1.5  2004/07/09 00:12:47  willuhn
 * @C Redesign
 *
 * Revision 1.4  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.3  2004/06/02 21:15:15  willuhn
 * @B win32 fixes in flat style
 * @C made ButtonInput more abstract
 *
 * Revision 1.2  2004/05/26 23:23:23  willuhn
 * @N Timeout fuer Messages in Statusbars
 *
 * Revision 1.1  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 **********************************************************************/