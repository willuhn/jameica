/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Composite;

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
 * Dialog zur Neuvergabe von Passworten.
 *
 * <p>Die Klasse implementiert bereits die Funktion {@link #checkPassword(String, String)}
 * und prueft dort, ob ueberhaupt Passworter eingegeben wurden und ob beide
 * uebereinstimmen. Sollen weitere Pruefungen vorgenommen werden, dann bitte
 * einfach diese Funktion ueberschreiben.
 */
public class NewPasswordDialog extends AbstractDialog
{
  private final static int WINDOW_WIDTH = 460;

  private String text               = null;
  private String userText           = null;
  private String labelText          = i18n.tr("Neues Passwort");
  private String labelText2         = i18n.tr("Passwort-Wiederholung");
  private String errorText          = null;
  
  private String enteredUsername    = null;
  private String enteredPassword    = null;

  private LabelInput error = null;

	/**
	 * Erzeugt einen neuen Passwort-Dialog.
	 * @param position Position des Dialogs.
	 * @see AbstractDialog#POSITION_MOUSE
	 * @see AbstractDialog#POSITION_CENTER
	 */
  public NewPasswordDialog(int position)
  {
    super(position);
    this.setSize(WINDOW_WIDTH,SWT.DEFAULT);
		setSideImage(SWTUtil.getImage("dialog-password.png"));
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
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#onEscape()
   */
  protected void onEscape()
  {
    // Kein Escape hier. Der User soll explizit auf "Abbrechen" klicken
  }

  /**
   * Speichert ein optionales Label fuer die zusaetzliche Eingabe eines
   * Usernamens. Wird hier ein Wert uebergeben, zeigt der Dialog neben
   * den beiden Passwort-Feldern extra noch ein Feld fuer den Usernamen an.
   *
   * <p>Der eingegebene Wert kann nach dem Aufruf von {@code open()}
   * mit {@link #getUsername()} ermittelt werden.
   *
   * <p>Wenn das Eingabefeld fuer den Usernamen angezeigt wird, ist es auch Pflicht.
   * Es gibt also entweder gar keinen Usernamen oder ein verpflichtetenden.
   * Jedoch keinen optionalen Usernamen.
   *
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
		labelText = text;
	}

	/**
	 * Speichert den Text, der links neben dem Eingabefeld fuer die
	 * Passwort-Wiederholung angezeigt werden soll (Optional).
	 * @param text anzuzeigender Text.
	 */
	public void setLabel2Text(String text)
	{
		if (text == null || text.length() == 0)
			return;
		labelText2 = text;
	}

	/**
	 * Zeigt den uebergebenen Text rot markiert links neben dem OK-Button an.
	 * Diese Funktion sollte aus {@link #checkPassword(String, String)} heraus
	 * aufgerufen werden, um dem Benutzer zu zeigen, <b>warum</b> seine
	 * Passwort-Eingabe falsch war. 
   * @param text Der anzuzeigende Fehlertext.
   */
  public final void setErrorText(String text)
	{
		if (text == null || text.length() == 0)
			return;
		
		this.errorText = text;
		
		if (error != null)
  		error.setValue(this.errorText);
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
    
    final PasswordInput password = new PasswordInput(null);
    password.setName(this.labelText);
    password.setMandatory(true);
    if (username == null) password.focus();
    container.addInput(password);
    
    final PasswordInput password2 = new PasswordInput(null);
    password2.setName(this.labelText2);
    password2.setMandatory(true);
    container.addInput(password2);

    this.error = new LabelInput(null);
    this.error.setColor(Color.ERROR);
    this.error.setName("");
    this.error.setValue(this.errorText);
    container.addInput(this.error);

    ButtonArea buttons = new ButtonArea();
    
    buttons.addButton("    " + i18n.tr("OK") + "    ",new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (username != null)
        {
          String u = (String) username.getValue();
          if (!checkUsername(u))
          {
            username.focus();
            return;
          }
          enteredUsername = u;
        }
        
        String p = (String) password.getValue();
        String p2 = (String) password2.getValue();

        if (!checkPassword(p,p2))
        {
          password.focus();
          return;
        }

        enteredPassword = p;
        close();
      }
    },null,true,"ok.png");
    buttons.addButton(i18n.tr("Abbrechen"),new Action() {
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

	   getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }
  
  /**
   * Prueft die Eingabe des Usernamens, insofern das Eingabefeld vorhanden ist. 
   * @param username der eingegebene Username.
   * @return true, wenn die Eingabe OK ist, andernfalls false.
   */
  protected boolean checkUsername(String username)
  {
    if (username == null || username.trim().length() == 0)
    {
      setErrorText(i18n.tr("Bitte geben Sie einen Namen ein."));
      return false;
    }
    if (username.trim().length() < 3)
    {
      setErrorText(i18n.tr("Bitte geben Sie als Name mindestens 3 Zeichen ein."));
      return false;
    }
    return true;
  }

  /**
   * Prueft die Eingabe der Passworte.
   * @param password das gerade eingegebene Passwort.
   * @param password2 die Passwort-Wiederholung.
   * @return true, wenn die Eingabe OK ist, andernfalls false.
   */
  protected boolean checkPassword(String password, String password2)
  {
    boolean set = (password != null && password.length() > 0 &&
                   password2 != null && password2.length() > 0);
    
    if (!set)
    {
      setErrorText(i18n.tr("Bitte geben Sie ein Passwort ein."));
      return false;
    }

    if (password != null && !password.equals(password2))
    {
      setErrorText(i18n.tr("Die eingegebenen Passworte stimmen nicht überein."));
      return false;
    }
    return true;
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
 * $Log: NewPasswordDialog.java,v $
 * Revision 1.9  2011/05/24 09:03:45  willuhn
 * @C Passwort-Dialoge koennen nicht mit Escape abgebrochen werden. Hier muss "Abbrechen" geklickt werden
 *
 * Revision 1.8  2011-04-26 12:09:18  willuhn
 * @B Potentielle Bugs gemaess Code-Checker
 *
 * Revision 1.7  2010-11-25 16:01:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2010-11-25 14:24:30  willuhn
 * @N Im Usernamen muessen mindestens 3 Zeichen eingegeben werden
 * @N Automatisch Focus im betreffenden Feld
 *
 * Revision 1.5  2010-11-22 11:32:03  willuhn
 * @N Beim Start von Jameica kann nun neben dem Masterpasswort optional auch ein Benutzername abgefragt werden. Dieser kann auch ueber den neuen Kommandozeilen-Parameter "-u" uebergeben werden.
 *
 **********************************************************************/