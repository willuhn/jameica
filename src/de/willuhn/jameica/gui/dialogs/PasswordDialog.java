/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/PasswordDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/02/20 20:45:24 $
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
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.util.Style;
import de.willuhn.util.I18N;

/**
 * Dialog zu Passwort-Eingabe.
 */
public class PasswordDialog extends SimpleDialog {

	private Composite comp = null;
	private Label label = null;
	private Label pLabel = null;
	private CLabel error = null;
	private Text password = null;
	private Button button = null;
	
	private String enteredPassword = "";

	/**
	 * Erzeugt einen neuen Passwort-Dialog.
	 * @param position Position des Dialogs.
	 * @see Dialog#POSITION_MOUSE
	 * @see Dialog#POSITION_CENTER
	 */
  public PasswordDialog(int position) {
    super(position);
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
		GridData grid = new GridData(GridData.FILL_BOTH);
		grid.horizontalSpan = 2;
		label.setLayoutData(grid);
		
		pLabel = new Label(comp,SWT.NONE);
		pLabel.setText(I18N.tr("Passwort"));
		pLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		password = new Text(comp,SWT.SINGLE | SWT.BORDER);
		password.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		password.setEchoChar('*');

		Composite bComp = new Composite(comp,SWT.NONE);
		GridData grid2 = new GridData(GridData.FILL_BOTH);
		grid2.horizontalSpan = 2;
		bComp.setLayoutData(grid2);
		GridLayout gl = new GridLayout(2,false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		bComp.setLayout(gl);

		error = new CLabel(bComp, SWT.WRAP);
		error.setLayoutData(new GridData(GridData.FILL_BOTH));
		error.setForeground(Style.COLOR_ERROR);
		
		button = new Button(bComp, SWT.NONE);
		button.setText("   OK   ");
		button.setLayoutData(new GridData(GridData.END));
		button.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				if (!checkPassword())
					return;
				enteredPassword = password.getText();
				close();
			}
		});

		// so und jetzt noch der Shell-Listener, damit der
		// User den Dialog nicht schliessen kann ohne was
		// einzugeben ;)
		addShellListener(new ShellListener() {
			public void shellClosed(ShellEvent e) {
				e.doit = false;
			}
      public void shellActivated(ShellEvent e) {};
      public void shellDeactivated(ShellEvent e) {};
      public void shellDeiconified(ShellEvent e) {};
      public void shellIconified(ShellEvent e) {};
    });
	}		

	/**
   * Prueft ob ein Passwort eingegeben wurde.
   */
  private boolean checkPassword()
	{
		if (password.getText() != null && password.getText().length() > 0)
			return true;
		error.setText(I18N.tr("Fehler: Bitte geben Sie ein Passwort ein"));
		error.layout();
		return false;
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
 * Revision 1.1  2004/02/20 20:45:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/20 01:25:06  willuhn
 * @N nice dialog
 * @N busy indicator
 * @N new status bar
 *
 * Revision 1.1  2004/02/17 00:53:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/12 23:46:27  willuhn
 * *** empty log message ***
 *
 **********************************************************************/