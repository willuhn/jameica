/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

/**
 * Billiger Dialog, der nur einen Text und einen OK-Button enthaelt.
 */
public class SimpleDialog extends AbstractDialog
{
  private final static int WINDOW_WIDTH = 400;
	private String text = null;

	/**
	 * Erzeugt einen neuen simplen Dialog mit OK-Knopf.
	 * @param position Position des Dialogs.
	 * @see AbstractDialog#POSITION_MOUSE
	 * @see AbstractDialog#POSITION_CENTER
	 */
  public SimpleDialog(int position)
  {
    super(position);
    this.setSize(WINDOW_WIDTH,SWT.DEFAULT);
    this.setSideImage(SWTUtil.getImage("dialog-information-large.png"));
  }

	/**
	 * Speichert den anzuzeigenden Text.
	 * @param text anzuzeigender Text.
	 */
	public void setText(String text)
	{
		this.text = text;
	}

	/**
	 * Liefert den angezeigten Text.
	 * @return angezeigter Text.
	 */
	public String getText()
	{
		return text;
	}
	
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#onEscape()
   * Abbrechen nicht zulaessig.
   */
  protected void onEscape()
  {
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
	{
    if (this.text == null)
      throw new OperationCanceledException("no text specified to be displayed");
    
    Container c = new SimpleContainer(parent,true);
    c.addText(this.text,true);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("   " + Application.getI18n().tr("OK") + "   ",new Action() {
      
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,true,"ok.png");

    
    c.addButtonArea(buttons);
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
	}

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }
}
