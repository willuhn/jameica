/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/PasswordDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/02/21 19:49:41 $
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
 * Die Klasse ist deshalb abstract, damit sie bei der konkreten
 * Verwendung abgeleitet wird und dort via <code>checkPassword(String)</code>
 * die Eingabe geprueft werden kann.
 * Hinweis: Diese Klasse hat einen internen Zaehler, der die Anzahl
 * der fehlgeschlagenen Aufrufe von <code>checkPassword(String)</code>
 * zaehlt. Nach 3 Versuchen wird die Funktion <code>cancel()</code>
 * aufgerufen und der Dialog geschlossen.
 */
public abstract class PasswordDialog extends SimpleDialog {

	private final static int MAX_RETRIES = 3;
	private int retries = 0;

	private Composite comp = null;
	private Label label = null;
	private Label pLabel = null;
	private CLabel error = null;
	private Text password = null;
	private Button button = null;
	
	private String labelText 					= I18N.tr("Passwort");
	private String errorText					= "";
	
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
	 * Speichert den Text, der links neben dem Eingabefeld fuer die
	 * Passwort-Eingabe angezeigt werden soll (Optional).
   * @param text anzuzeigender Text.
   */
  protected void setLabelText(String text)
	{
		if (text == null || text.length() == 0)
			return;
		labelText = text;
	}

	/**
	 * Zeigt den uebergebenen Text rot markiert links neben dem OK-Button an.
	 * Diese Funktion sollte aus <code>checkPassword(String)</code> heraus
	 * aufgerufen werden, um dem benutzer zu zeigen, <b>warum</b> seine
	 * Passwort-Eingabe falsch war. 
   */
  protected final void setErrorText(String text)
	{
		if (text == null || text.length() == 0)
			return;
		error.setText(text);
		error.layout();
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
		pLabel.setText(labelText);
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
				String p = password.getText();
				retries++;
				if (!checkPassword(p))
				{
					if (retries >= MAX_RETRIES)
					{
						// maximale Anzahl der Fehlversuche erreicht.
						// Wir schliessen den Dialog und rufen vorher
						// noch cancel() auf.
						cancel();
						close();
					}
					return;
				}
				enteredPassword = p;
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
   * Prueft die Eingabe des Passwortes.
   * Hinweis: Der Dialog wird erst geschlossen, wenn diese
   * Funktion <code>true</code> zurueckliefert.
   * @param password das gerade eingegebene Passwort.
   * @return true, wenn die Eingabe OK ist, andernfalls false.
   */
  protected abstract boolean checkPassword(String password);
	
	/**
   * Wird aufgerufen, wenn das Passwort 3 mal falsch eingegeben wurde.
   */
  protected abstract void cancel();
	
	/**
	 * Liefert die Anzahl der moeglichen Rest-Versuche zur
	 * Eingabe bevor der Dialog abgebrochen wird.
   * @return Anzahl der Restversuche.
   */
  protected int getRemainingRetries()
	{
		return (MAX_RETRIES - retries);
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
 * Revision 1.2  2004/02/21 19:49:41  willuhn
 * *** empty log message ***
 *
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