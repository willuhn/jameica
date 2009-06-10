/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/PasswordDialog.java,v $
 * $Revision: 1.24 $
 * $Date: 2009/06/10 11:25:53 $
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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.PasswordInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

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
public abstract class PasswordDialog extends AbstractDialog {

	/**
	 * Maximale Anzahl von Fehlversuchen.
	 */
	public final static int MAX_RETRIES = 3;

  private int retries                 = 0;
  private String text                 = "";
	private String labelText 					  = "";
  private String errorText            = "";
	private String enteredPassword      = "";
  private boolean showPassword        = false;
  
  private LabelInput errorTextInput   = null;
  private PasswordInput passwordInput = null;

	/**
	 * Erzeugt einen neuen Passwort-Dialog.
	 * @param position Position des Dialogs.
	 * @see AbstractDialog#POSITION_MOUSE
	 * @see AbstractDialog#POSITION_CENTER
	 */
  public PasswordDialog(int position) {
    super(position);
    this.setSize(400,SWT.DEFAULT);
    this.setTitle(i18n.tr("Passwort"));
    this.setSideImage(SWTUtil.getImage("dialog-password.png"));

    this.labelText = i18n.tr("Passwort");
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
		this.labelText = text;
	}

	/**
	 * Zeigt den uebergebenen Text rot markiert an.
	 * Diese Funktion sollte aus <code>checkPassword(String)</code> heraus
	 * aufgerufen werden, um dem benutzer zu zeigen, <b>warum</b> seine
	 * Passwort-Eingabe falsch war. 
   * @param text Der anzuzeigende Fehlertext.
   */
  protected final void setErrorText(String text)
	{
		if (text == null || text.length() == 0)
			return;
		this.errorText = text;
    if (this.errorTextInput != null)
      this.errorTextInput.setValue(this.errorText);
	}

  /**
   * Speichert den anzuzeigenden Text.
   * @param text anzuzeigender Text.
   */
  public void setText(String text)
  {
    if (text == null)
      return;
    this.text = text;
  }

  /**
   * Legt fest, ob das Passwort waehrend der Eingabe angezeigt werden soll.
   * @param show
   */
  protected void setShowPassword(boolean show)
  {
    this.showPassword = show;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
	{
    Container container = new SimpleContainer(parent);

		// Text
    if (this.text != null && this.text.length() > 0)
      container.addText(this.text,true);
		
    // das Passwort
    this.passwordInput = new PasswordInput(this.enteredPassword);
    this.passwordInput.setShowPassword(this.showPassword);
    this.passwordInput.focus();
    container.addLabelPair(labelText,this.passwordInput);
    // Fehlertext
    this.errorTextInput = new LabelInput(this.errorText);
    this.errorTextInput.setColor(Color.ERROR);
    container.addLabelPair("",this.errorTextInput);

    // Der Freiraum fuer Erweiterungen
    extend(container);

    // Listener, welcher passwordModified() aufruft wenn sich das
    // passwort aendert (fuer Erweiterungen).
    ((Text)this.passwordInput.getControl()).addModifyListener(
        new ModifyListener() {
          public void modifyText(ModifyEvent e)
          {
            passwordModified((String)passwordInput.getValue());
          }
        }
    );


    ButtonArea buttons = container.createButtonArea(2);
    
    buttons.addButton("    " + i18n.tr("OK") + "    ",new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        String p = (String) passwordInput.getValue();
        retries++;

        if (!checkPassword(p))
        {
          passwordInput.setValue("");
          if (retries >= MAX_RETRIES)
          {
            // maximale Anzahl der Fehlversuche erreicht.
            throw new OperationCanceledException(MAX_RETRIES + " falsche Passwort-Eingaben");
          }
          return;
        }
        enteredPassword = p;
        close();
      }
    },null,true);

    buttons.addButton(i18n.tr("Abbrechen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException("Dialog abgebrochen");
      }
    });

		// so und jetzt noch der Shell-Listener, damit der
		// User den Dialog nicht schliessen kann ohne was
		// einzugeben ;)
		addShellListener(new ShellListener() {
			public void shellClosed(ShellEvent e) {
				throw new OperationCanceledException("dialog cancelled via close button");
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
   * Kann von abgeleiteten Dialogen ueberschrieben werden, um
   * den Password-Dialog noch zu erweitern. Sie erhalten ein
   * Composite mit einem einspaltigen Grid-Layout.
   * Angezeigt wird die Erweiterung dann direkt ueber den beiden
   * Buttons.
   * @param container der Container.
   * @throws Exception
   */
  protected void extend(Container container) throws Exception
  {
  }
	
  /**
   * Kann von abgeleiteten Dialogen ueberschrieben werden, um
   * denPassword-Dialog noch zu erweitern.
   * Wird jedes mal aufgerufen, wenn die Eingabe im Passwort-Feld
   * sich aendert. Kann z. B. benutzt werden, um das Passwort
   * noch vor dem Klick auf OK zu pruefen.
   * @param password
   */
  protected void passwordModified(String password)
  {
  }

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
 * Revision 1.24  2009/06/10 11:25:53  willuhn
 * @N Transparente HTTP-Authentifizierung ueber Jameica (sowohl in GUI- als auch in Server-Mode) mittels ApplicationCallback
 *
 * Revision 1.23  2009/06/09 12:43:01  willuhn
 * @N Erster Code fuer Jameica Authenticator
 *
 * Revision 1.22  2009/04/30 13:20:09  willuhn
 * @N Jan's Patch, welches
 *
 * Revision 1.21  2008/12/18 23:21:13  willuhn
 * @N GUI-Polishing: Neue Icons in Hibiscus und Jameica aus dem Tango-Projekt (http://tango.freedesktop.org/)
 * @R Nicht mehr benoetigte Grafiken entfernt
 * @C Anordnung des SideImages in AbstractDialog etwas geaendert (ein paar Pixel Abstand des Images vom Rand)
 **********************************************************************/