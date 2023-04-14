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
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

/**
 * Dialog, der nur einen Text und einen Ja/Nein-Button enthaelt.
 */
public class YesNoDialog extends AbstractDialog
{
	private String text    = null;
	private boolean choice = false;

	/**
	 * Erzeugt einen neuen Dialog.
	 * @param position Position des Dialogs.
	 * @see AbstractDialog#POSITION_MOUSE
	 * @see AbstractDialog#POSITION_CENTER
	 */
  public YesNoDialog(int position) {
    super(position);
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
   */
  protected void onEscape()
  {
    // Bei Druck auf ESC interpretieren wir das als NEIN und schliessen
    // den Dialog OHNE Exception (das entspricht dem bisherige Verhalten,
    // wir lassen das daher lieber so. Sonst muesste bei allen Verwendern
    // geprueft werden, ob die eine OCE korrekt fangen)
    choice = false;
    close();
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
	{
    Container container = new SimpleContainer(parent);
    container.addText(this.text,true);
		
    extend(container);
    
    ButtonArea buttons = new ButtonArea();
    
    buttons.addButton("   " + i18n.tr("Ja") + "   ",new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        choice = true;
        close();
      }
    },null,false,"ok.png");

    buttons.addButton("   " + i18n.tr("Nein") + "   ", new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        choice = false;
        close();
      }
    },null,false,"process-stop.png");
    
    container.addButtonArea(buttons);

    getShell().setMinimumSize(400,SWT.DEFAULT);
    getShell().setSize(getShell().computeSize(400,SWT.DEFAULT));

	}

  /**
   * Kann von abgeleiteten Dialogen ueberschrieben werden, um
   * den Dialog noch zu erweitern.
   * Angezeigt wird die Erweiterung dann direkt ueber den Buttons.
   * @param container der Container.
   * @throws Exception
   */
  protected void extend(Container container) throws Exception
  {
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception {
    return Boolean.valueOf(choice);
  }
}
