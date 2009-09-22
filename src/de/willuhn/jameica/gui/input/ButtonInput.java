/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/ButtonInput.java,v $
 * $Revision: 1.17 $
 * $Date: 2009/09/22 11:14:51 $
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
	private ArrayList buttonListeners = new ArrayList();
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

		comp = new Composite(getParent(),SWT.BORDER);
		comp.setBackground(Color.WIDGET_BG.getSWTColor());
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
					((Listener)buttonListeners.get(i)).handleEvent(event);
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
  void update() throws OperationCanceledException
  {
    if (inUpdate)
      throw new OperationCanceledException();

    try
    {
      inUpdate = true;

      if (this.comp == null || this.comp.isDisposed())
        return;

      if (!isEnabled())
      {
        this.comp.setBackground(Color.BACKGROUND.getSWTColor());
        return;
      }
      
      Object value = getValue();

      if (isMandatory() && (value == null || "".equals(value.toString())))
      {
        this.comp.setBackground(Color.MANDATORY_BG.getSWTColor());
        return;
      }
      this.comp.setBackground(Color.WIDGET_BG.getSWTColor());
    }
    finally
    {
      inUpdate = false;
    }
  }

}

/*********************************************************************
 * $Log: ButtonInput.java,v $
 * Revision 1.17  2009/09/22 11:14:51  willuhn
 * @B Hintergrundfarbe wurde im Composite nicht gesetzt - fuehrte zu einem grauen Rand
 *
 * Revision 1.16  2009/07/26 22:40:35  willuhn
 * @B BUGZILLA 744
 *
 * Revision 1.15  2008/05/30 10:16:25  willuhn
 * @N Text/Image des Button on-the-fly aktualisierbar
 *
 * Revision 1.14  2007/07/17 14:34:23  willuhn
 * @B Updates nichts bei Buttons und Checkboxen durchfuehren
 *
 * Revision 1.13  2007/04/26 13:27:24  willuhn
 * @R undo
 *
 * Revision 1.11  2007/01/05 10:36:49  willuhn
 * @C Farbhandling - Jetzt aber!
 *
 * Revision 1.10  2006/12/28 15:35:52  willuhn
 * @N Farbige Pflichtfelder
 *
 * Revision 1.9  2006/06/19 10:54:24  willuhn
 * @N neue Methode setEnabled(boolean) in Input
 * @N neue de_willuhn_util lib
 *
 * Revision 1.8  2005/08/22 13:31:52  web0
 * *** empty log message ***
 *
 * Revision 1.7  2004/09/15 22:31:53  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/07/27 23:41:30  willuhn
 * *** empty log message ***
 *
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