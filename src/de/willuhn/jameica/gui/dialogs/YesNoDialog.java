/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/YesNoDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/02/22 20:05:21 $
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

import de.willuhn.util.I18N;

/**
 * Dialog, der nur einen Text und einen Ja/Nein-Button enthaelt.
 */
public class YesNoDialog extends AbstractDialog {

	private Composite comp = null;
	private Label label = null;
	private Button yes = null;
	private Button no = null;
	
	private String text = null;

	private boolean choice = false;

	/**
	 * Erzeugt einen neuen Dialog.
	 * @param position Position des Dialogs.
	 * @see Dialog#POSITION_MOUSE
	 * @see Dialog#POSITION_CENTER
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
   * Oeffnet den Dialog und liefert ein Boolean - je nach Auswahl von Ja/Nein.
   * @return true, wenn Ja gedrueckt wurde, sonst false.
   * @throws Exception
   */
  public boolean getChoice() throws Exception
	{
		super.open();
		return choice;
	}

	/**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint()
   */
  public void paint() throws Exception
	{
		comp = new Composite(getParent(),SWT.NONE);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayout(new GridLayout(2,false));
		
		label = new Label(comp,SWT.WRAP);
		label.setText(getText() + "\n");
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		
		yes = new Button(comp, SWT.FLAT);
		yes.setText("   " + I18N.tr("Ja") + "   ");
		yes.setLayoutData(new GridData(GridData.BEGINNING));
		yes.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				choice = true;
				close();
			}
		});

		no = new Button(comp, SWT.FLAT);
		no.setText("   " + I18N.tr("Nein") + "   ");
		no.setLayoutData(new GridData(GridData.BEGINNING));
		no.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				choice = false;
				close();
			}
		});
	}
}


/**********************************************************************
 * $Log: YesNoDialog.java,v $
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