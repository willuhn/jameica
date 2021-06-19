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

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.internal.buttons.Cancel;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Dialog zur Eingabe eines kurzen Textes.
 */
public class TextDialog extends SimpleDialog
{
  private static final int WINDOW_WIDTH = 500;
  private TextInput input = null;
  private Object value = null;
  
	/**
	 * Erzeugt einen neuen Text-Dialog.
	 * @param position Position des Dialogs.
	 * @see AbstractDialog#POSITION_MOUSE
	 * @see AbstractDialog#POSITION_CENTER
	 */
  public TextDialog(int position)
  {
    super(position);
    this.setSize(WINDOW_WIDTH,SWT.DEFAULT);
    this.setSideImage(SWTUtil.getImage("dialog-question-large.png"));
    this.input = new TextInput("");
    this.input.setName(Application.getI18n().tr("Ihre Eingabe"));
  }

	/**
	 * Speichert den Text, der links neben dem Eingabefeld fuer die
	 * Text-Eingabe angezeigt werden soll. Wird er nicht definiert,
	 * wird "Ihre Eingabe" angezeigt.
   * @param t anzuzeigender Text.
   */
  public void setLabelText(String t)
	{
    if (StringUtils.trimToNull(t) != null)
      this.input.setName(t);
	}

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void paint(Composite parent) throws Exception
	{
    Container c = new SimpleContainer(parent);
    
    String text = this.getText();
    if (StringUtils.trimToNull(text) != null)
      c.addText(text,true);
    
    c.addInput(this.input);
    
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(Application.getI18n().tr("Übernehmen"),new Action() {
      
      public void handleAction(Object context) throws ApplicationException
      {
        value = input.getValue();
        close();
      }
    },null,true,"ok.png");
    buttons.addButton(new Cancel());
    
    c.addButtonArea(buttons);
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
	}		

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return this.value;
  }
}
