/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/SimpleDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/02/23 20:30:33 $
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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

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
	 * @see Dialog#POSITION_MOUSE
	 * @see Dialog#POSITION_CENTER
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
  public void paint(Composite parent) throws Exception
	{
		comp = new Composite(parent,SWT.NONE);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayout(new GridLayout(1,false));
		
		label = new Label(comp,SWT.WRAP);
		label.setText(getText() + "\n");
		label.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		button = new Button(comp, SWT.FLAT);
		button.setText("    OK    ");
		button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		button.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				close();
			}
		});
	}

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  public Object getData() throws Exception {
    return null;
  }
}


/**********************************************************************
 * $Log: SimpleDialog.java,v $
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