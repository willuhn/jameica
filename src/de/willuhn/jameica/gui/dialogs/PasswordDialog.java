/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/PasswordDialog.java,v $
 * $Revision: 1.7 $
 * $Date: 2004/03/06 18:24:24 $
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
	private CLabel label = null;
	private CLabel pLabel = null;
	private CLabel error = null;
	private Text password = null;
	private Button button = null;
	private Button cancel = null;
	
	private String labelText 					= "";
	
	private String enteredPassword = "";

	/**
	 * Erzeugt einen neuen Passwort-Dialog.
	 * @param position Position des Dialogs.
	 * @see AbstractDialog#POSITION_MOUSE
	 * @see AbstractDialog#POSITION_CENTER
	 */
  public PasswordDialog(int position) {
    super(position);
    labelText = i18n.tr("Passwort");
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
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
	{
		// Composite um alles drumrum.
		comp = new Composite(parent,SWT.NONE);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayout(new GridLayout(3,false));
		
		// Text
		label = new CLabel(comp,SWT.WRAP);
		label.setText(getText());
		GridData grid = new GridData(GridData.FILL_HORIZONTAL);
		grid.horizontalSpan = 3;
		label.setLayoutData(grid);
		
		// Fehlertext 
		error = new CLabel(comp, SWT.WRAP);
		error.setForeground(Style.COLOR_ERROR);
		GridData grid2 = new GridData(GridData.FILL_HORIZONTAL);
		grid2.horizontalSpan = 3;
		grid2.horizontalIndent = 0;
		error.setLayoutData(grid2);

		// Label vor Eingabefeld
		pLabel = new CLabel(comp,SWT.NONE);
		pLabel.setText(labelText);
		pLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		// Eingabe-Feld. Das packen wir wegen des FLAT-Styles noch
		// in ein Composite
		Composite border = new Composite(comp,SWT.NONE);
		GridData grid3 = new GridData(GridData.FILL_HORIZONTAL);
		grid3.horizontalSpan = 2;
		border.setLayoutData(grid3);
		border.setBackground(Style.COLOR_BORDER);
		GridLayout l = new GridLayout();
		l.horizontalSpacing = 0;
		l.verticalSpacing = 0;
		l.marginHeight = 1;
		l.marginWidth = 1;
		border.setLayout(l);
		password = new Text(border,SWT.SINGLE);
		password.setBackground(Style.COLOR_WHITE);
		password.setEchoChar('*');
		password.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Dummy-Label damit die Buttons buendig unter dem Eingabefeld stehen
		Label dummy = new Label(comp,SWT.NONE);
		dummy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// OK-Button
		button = new Button(comp, SWT.FLAT);
		button.setText(i18n.tr("OK"));
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				String p = password.getText();
				retries++;

				if (!checkPassword(p))
				{
					if (retries >= MAX_RETRIES)
					{
						// maximale Anzahl der Fehlversuche erreicht.
						throw new RuntimeException(MAX_RETRIES + " falsche Passwort-Eingaben");
					}
					return;
				}
				enteredPassword = p;
				close();
			}
		});

		// Abbrechen-Button
		cancel = new Button(comp,SWT.FLAT);
		cancel.setText(i18n.tr("Abbrechen"));
		cancel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		cancel.addMouseListener(new MouseAdapter() {
      public void mouseUp(MouseEvent e) {
				throw new RuntimeException("Dialog abgebrochen");
      }
    });

		// so und jetzt noch der Shell-Listener, damit der
		// User den Dialog nicht schliessen kann ohne was
		// einzugeben ;)
		addShellListener(new ShellListener() {
			public void shellClosed(ShellEvent e) {
				throw new RuntimeException("dialog cancelled via close button");
			}
      public void shellActivated(ShellEvent e) {}
      public void shellDeactivated(ShellEvent e) {}
      public void shellDeiconified(ShellEvent e) {}
      public void shellIconified(ShellEvent e) {}
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
	 * Liefert die Anzahl der moeglichen Rest-Versuche zur
	 * Eingabe bevor der Dialog abgebrochen wird.
   * @return Anzahl der Restversuche.
   */
  protected int getRemainingRetries()
	{
		return (MAX_RETRIES - retries);
	}

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception {
    return enteredPassword;
  }

}


/**********************************************************************
 * $Log: PasswordDialog.java,v $
 * Revision 1.7  2004/03/06 18:24:24  willuhn
 * @D javadoc
 *
 * Revision 1.6  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.5  2004/02/24 22:46:53  willuhn
 * @N GUI refactoring
 *
 * Revision 1.4  2004/02/23 20:30:34  willuhn
 * @C refactoring in AbstractDialog
 *
 * Revision 1.3  2004/02/22 20:05:21  willuhn
 * @N new Logo panel
 *
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