/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/YesNoDialog.java,v $
 * $Revision: 1.11 $
 * $Date: 2011/03/07 10:33:51 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
	{
    // Bei Druck auf ESC interpretieren wir das als NEIN.
    parent.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        if (e.keyCode == SWT.ESC)
        {
          choice = false;
          close();
        }
      }
    });

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


/**********************************************************************
 * $Log: YesNoDialog.java,v $
 * Revision 1.11  2011/03/07 10:33:51  willuhn
 * @N BUGZILLA 999
 *
 * Revision 1.10  2005/08/25 21:18:24  web0
 * @C changes accoring to findbugs eclipse plugin
 *
 * Revision 1.9  2005/02/01 17:15:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/08/15 17:55:17  willuhn
 * @C sync handling
 *
 * Revision 1.7  2004/07/27 23:41:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.5  2004/03/06 18:24:23  willuhn
 * @D javadoc
 *
 * Revision 1.4  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.3  2004/02/24 22:46:53  willuhn
 * @N GUI refactoring
 *
 * Revision 1.2  2004/02/23 20:30:34  willuhn
 * @C refactoring in AbstractDialog
 *
 * Revision 1.1  2004/02/22 20:05:21  willuhn
 * @N new Logo panel
 *
 * Revision 1.1  2004/02/20 20:45:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/02/20 01:25:06  willuhn
 * @N nice dialog
 * @N busy indicator
 * @N new status bar
 *
 * Revision 1.2  2004/02/17 00:53:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/12 23:46:27  willuhn
 * *** empty log message ***
 *
 **********************************************************************/