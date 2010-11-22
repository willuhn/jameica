/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/PasswordDialog.java,v $
 * $Revision: 1.27 $
 * $Date: 2010/11/22 11:32:03 $
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
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

/**
 * Dialog zu Passwort-Eingabe.
 * Hinweis: Diese Klasse hat einen internen Zaehler, der die Anzahl
 * der fehlgeschlagenen Aufrufe von <code>checkPassword(String)</code>
 * zaehlt. Nach 3 Versuchen wird die Funktion <code>cancel()</code>
 * aufgerufen und der Dialog geschlossen.
 */
public class PasswordDialog extends AbstractDialog
{

  private final static int WINDOW_WIDTH = 420;
  
	/**
	 * Maximale Anzahl von Fehlversuchen.
	 */
	public final static int MAX_RETRIES = 3;

  private int retries                 = 0;
  private boolean showPassword        = false;
  
  private String text                 = null;
  private String userText             = null;
  private String labelText 					  = i18n.tr("Passwort");
  private String errorText            = null;

  private String enteredUsername      = null;
  private String enteredPassword      = null;
  
  private LabelInput error            = null;

	/**
	 * Erzeugt einen neuen Passwort-Dialog.
	 * @param position Position des Dialogs.
	 * @see AbstractDialog#POSITION_MOUSE
	 * @see AbstractDialog#POSITION_CENTER
	 */
  public PasswordDialog(int position)
  {
    super(position);
    this.setSize(WINDOW_WIDTH,SWT.DEFAULT);
    this.setTitle(i18n.tr("Passwort"));
    this.setSideImage(SWTUtil.getImage("dialog-password.png"));
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
   * Speichert ein optionales Label fuer die zusaetzliche Eingabe eines
   * Usernamens. Wird hier ein Wert uebergeben, zeigt der Dialog neben
   * den beiden Passwort-Feldern extra noch ein Feld fuer den Usernamen an.
   * Der da eingegebene Wert kann nach dem Aufruf von <code>open()</code>
   * mit <code>getUsername()</code> ermittelt werden.
   * Wenn das Eingabefeld fuer den Usernamen angezeigt wird, ist es auch Pflicht.
   * Es gibt also entweder gar keinen Usernamen oder ein verpflichtetenden.
   * Jedoch keinen optionalen Usernamen.
   * @param text das anzuzeigende Label vor dem Eingabefeld, insofern
   * es angezeigt werden soll.
   */
  public void setUsernameText(String text)
  {
    this.userText = text;
  }

  /**
	 * Speichert den Text, der links neben dem Eingabefeld fuer die
	 * Passwort-Eingabe angezeigt werden soll (Optional).
   * @param text anzuzeigender Text.
   */
  public void setLabelText(String text)
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
    if (this.error != null)
      this.error.setValue(this.errorText);
	}

  /**
   * Legt fest, ob das Passwort waehrend der Eingabe angezeigt werden soll.
   * @param show
   */
  public void setShowPassword(boolean show)
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
		
    final TextInput username = (this.userText != null && this.userText.length() > 0) ? new TextInput(null) : null;
    if (username != null)
    {
      username.setValue(Application.getStartupParams().getUsername());
      username.setName(this.userText);
      username.setMandatory(true);
      username.focus();
      container.addInput(username);
    }

    // das Passwort
    final PasswordInput passwordInput = new PasswordInput(this.enteredPassword);
    passwordInput.setName(this.labelText);
    passwordInput.setShowPassword(this.showPassword);
    if (username == null) passwordInput.focus();
    container.addInput(passwordInput);
    
    // Fehlertext
    this.error = new LabelInput(this.errorText);
    this.error.setName("");
    this.error.setColor(Color.ERROR);
    this.error.setValue(this.errorText);
    container.addInput(this.error);

    // Der Freiraum fuer Erweiterungen
    extend(container);

    // Listener, welcher passwordModified() aufruft wenn sich das
    // passwort aendert (fuer Erweiterungen).
    ((Text)passwordInput.getControl()).addModifyListener(
        new ModifyListener() {
          public void modifyText(ModifyEvent e)
          {
            passwordModified((String)passwordInput.getValue());
          }
        }
    );


    ButtonArea buttons = new ButtonArea();
    buttons.addButton("    " + i18n.tr("OK") + "    ",new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        retries++;

        if (username != null)
        {
          String u = (String) username.getValue();
          if (!checkUsername(u))
            return;
          enteredUsername = u;
        }

        String p = (String) passwordInput.getValue();
        if (!checkPassword(p))
        {
          passwordInput.setValue("");
          if (retries >= MAX_RETRIES)
          {
            // maximale Anzahl der Fehlversuche erreicht.
            throw new OperationCanceledException(i18n.tr("{0} falsche Passwort-Eingaben",Integer.toString(MAX_RETRIES)));
          }
          return;
        }
        enteredPassword = p;
        close();
      }
    },null,true,"ok.png");
    buttons.addButton(i18n.tr("Abbrechen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException("Dialog abgebrochen");
      }
    },null,false,"process-stop.png");
    
    container.addButtonArea(buttons);

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
		
    // BUGZILLA 828
		getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
	}

  /**
   * Prueft die Eingabe des Usernamens, insofern das Eingabefeld vorhanden ist. 
   * @param username der eingegebene Username.
   * @return true, wenn die Eingabe OK ist, andernfalls false.
   */
  protected boolean checkUsername(String username)
  {
    boolean set = username != null && username.length() > 0;
    if (!set)
    {
      setErrorText(i18n.tr("Bitte geben Sie einen Namen ein."));
      return false;
    }
    return true;
  }

  /**
   * Prueft die Eingabe des Passwortes.
   * Hinweis: Der Dialog wird erst geschlossen, wenn diese
   * Funktion <code>true</code> zurueckliefert.
   * Die Default-Implementierung liefert immer TRUE.
   * @param password das gerade eingegebene Passwort.
   * @return true, wenn die Eingabe OK ist, andernfalls false.
   */
  protected boolean checkPassword(String password)
  {
    return true;
  }
  
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
   * den Password-Dialog noch zu erweitern.
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
  protected Object getData() throws Exception
  {
    return this.enteredPassword;
  }

  /**
   * Liefert den eingegebenen Benutzernamen.
   * @return der eingegebene Benutzername.
   */
  public String getUsername()
  {
    return this.enteredUsername;
  }
}


/**********************************************************************
 * $Log: PasswordDialog.java,v $
 * Revision 1.27  2010/11/22 11:32:03  willuhn
 * @N Beim Start von Jameica kann nun neben dem Masterpasswort optional auch ein Benutzername abgefragt werden. Dieser kann auch ueber den neuen Kommandozeilen-Parameter "-u" uebergeben werden.
 *
 * Revision 1.26  2010-03-04 22:54:06  willuhn
 * @N BUGZILLA 828
 **********************************************************************/