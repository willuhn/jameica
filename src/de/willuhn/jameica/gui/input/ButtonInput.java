/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/ButtonInput.java,v $
 * $Revision: 1.22 $
 * $Date: 2011/08/08 10:45:05 $
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.system.OperationCanceledException;

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
	private ArrayList<Listener> buttonListeners = new ArrayList<Listener>();
	private boolean focus = false;

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
    if (comp != null && !comp.isDisposed())
      return comp;

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
		if (this.focus)
  		clientControl.setFocus();
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
		button.addSelectionListener(new SelectionAdapter()
    {
      public void widgetSelected(SelectionEvent e)
      {
				Event event = new Event();
				event.data = e.data;
				event.detail = e.detail;
				event.display = e.display;
				event.doit = e.doit;
				event.item = e.item;
				event.width = e.width;
				event.height = e.height;
				event.x = e.x;
				event.y = e.y;
				for (int i=0;i<buttonListeners.size();++i)
				{
					buttonListeners.get(i).handleEvent(event);
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
  public final void setButtonText(String text)
	{
		this.buttonText = text;
    if (this.button != null && !this.button.isDisposed() && this.buttonImage == null)
      this.button.setText(this.buttonText);
	}

	/**
	 * Definiert das auf dem Button anzuzeigende Image.
   * @param image anzuzeigendes Image.
   */
  public final void setButtonImage(Image image)
	{
		this.buttonImage = image;
    if (this.button != null && !this.button.isDisposed() && this.buttonText == null)
      this.button.setImage(this.buttonImage);
	}

	/**
	 * Fuegt zum Button einen Listener hinzu.
   * @param l Listener.
   */
  protected final void addButtonListener(Listener l)
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
    this.focus = true;
    if (this.clientControl != null && !this.clientControl.isDisposed())
      clientControl.setFocus();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#disable()
   */
  public final void disable()
  {
    setEnabled(false);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#enable()
   */
  public final void enable()
  {
    setEnabled(true);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled)
  {
    if (enabled)
    {
      enableButton();
      enableClientControl();
    }
    else
    {
      disableButton();
      disableClientControl();
    }
    update();
  }

  /**
   * Aktiviert nur das ClientControl.
   */
  public final void enableClientControl()
	{
		clientControlEnabled = true;
		if (clientControl != null && !clientControl.isDisposed())
		{
      clientControl.setEnabled(true);
	    update();
		}
	}
	
	/**
   * Aktiviert nur den Button.
   */
  public final void enableButton()
	{
		buttonEnabled = true;
		if (button != null && !button.isDisposed())
		{
      button.setEnabled(true);
      update();
		}
	}
	
	/**
   * Deaktiviert nur das ClientControl.
   */
  public final void disableClientControl()
	{
		clientControlEnabled = false;
		if (clientControl != null && !clientControl.isDisposed())
		{
      clientControl.setEnabled(false);
      update();
		}
	}
	
	/**
   * Deaktiviert nur den Button.
   */
  public final void disableButton()
	{
		buttonEnabled = false;
		if (button != null && !button.isDisposed())
		{
      button.setEnabled(false);
      update();
		}
	}
  
  /**
   * @see de.willuhn.jameica.gui.input.Input#isEnabled()
   */
  public boolean isEnabled()
  {
    return buttonEnabled && clientControlEnabled;
  }
  
  private boolean inUpdate = false;

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#update()
   */
  protected void update() throws OperationCanceledException
  {
    if (inUpdate)
      throw new OperationCanceledException();

    try
    {
      inUpdate = true;

      if (this.comp == null || this.comp.isDisposed())
        return;

      if (!isEnabled())
        return;
      
      Object value = getValue();

      if (isMandatory() && (value == null || "".equals(value.toString())))
      {
        this.clientControl.setBackground(Color.MANDATORY_BG.getSWTColor());
        return;
      }
      this.clientControl.setBackground(null);
    }
    finally
    {
      inUpdate = false;
    }
  }

}
