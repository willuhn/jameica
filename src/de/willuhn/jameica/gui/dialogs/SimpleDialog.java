/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/SimpleDialog.java,v $
 * $Revision: 1.7 $
 * $Date: 2004/07/27 23:41:30 $
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.gui.GUI;

/**
 * Billiger Dialog, der nur einen Text und einen OK-Button enthaelt.
 */
public class SimpleDialog extends AbstractDialog {

	private Composite comp = null;
	private Label label = null;
	private Button button = null;
	
	private String text = null;

	/**
	 * Erzeugt einen neuen simplen Dialog mit OK-Knopf.
	 * @param position Position des Dialogs.
	 * @see AbstractDialog#POSITION_MOUSE
	 * @see AbstractDialog#POSITION_CENTER
	 */
  public SimpleDialog(int position) {
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
		comp = new Composite(parent,SWT.NONE);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayout(new GridLayout(1,false));
		
		label = new Label(comp,SWT.WRAP);
		label.setText(getText() + "\n");
		label.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		button = GUI.getStyleFactory().createButton(comp);
		button.setText("    OK    ");
		button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		GUI.getShell().setDefaultButton(button);
		button.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				close();
			}
		});
	}

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception {
    return null;
  }
}


/**********************************************************************
 * $Log: SimpleDialog.java,v $
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
 * Revision 1.4  2004/02/24 22:46:53  willuhn
 * @N GUI refactoring
 *
 * Revision 1.3  2004/02/23 20:30:33  willuhn
 * @C refactoring in AbstractDialog
 *
 * Revision 1.2  2004/02/22 20:05:21  willuhn
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