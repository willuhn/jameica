/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/PasswordDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/02/17 00:53:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.willuhn.util.I18N;

/**
 * Dialog zu Passwort-Eingabe.
 */
public class PasswordDialog extends Dialog {

	private Composite comp = null;
	private Label label = null;
	private Label pLabel = null;
	private Text password = null;
	private Button button = null;

	private String enteredPassword = "";

  /**
   * Erzeugt den Dialog.
   */
  public PasswordDialog() {
    super();
  }

	/**
	 * Speichert den anzuzeigenden Text.
   * @param text anzuzeigender Text.
   */
  public void setText(String text)
	{
		if (label != null && comp != null)
		{
			label.setText(text);
			return;
		}

		shell.setLayout(new GridLayout(1,false));
		shell.setSize(300,150);

		comp = new Composite(shell,SWT.NONE);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayout(new GridLayout(2,false));
		
		label = new Label(comp,SWT.WRAP);
		label.setText(text);
		GridData grid = new GridData(GridData.FILL_BOTH);
		grid.horizontalSpan = 2;
		label.setLayoutData(grid);
		
		pLabel = new Label(comp,SWT.NONE);
		pLabel.setText(I18N.tr("Passwort"));
		pLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		password = new Text(comp,SWT.SINGLE | SWT.BORDER);
		password.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		password.setEchoChar('*');

		button = new Button(comp, SWT.NONE);
		button.setText("    OK    ");
		GridData grid2 = new GridData(GridData.HORIZONTAL_ALIGN_END);
		grid2.horizontalSpan = 2;
		button.setLayoutData(grid2);
		button.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				enteredPassword = password.getText();
				close();
			}
		});
	}

	/**
	 * Oeffnet den Dialog und liefert das Passwort nachdem OK gedrueckt wurde.
   * @return
   */
  public String getPassword()
	{
		super.open();
		return enteredPassword;
	}
}


/**********************************************************************
 * $Log: PasswordDialog.java,v $
 * Revision 1.1  2004/02/17 00:53:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/12 23:46:27  willuhn
 * *** empty log message ***
 *
 **********************************************************************/