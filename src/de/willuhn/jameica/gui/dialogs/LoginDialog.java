/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/LoginDialog.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/05/06 12:32:04 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
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
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.security.Login;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

/**
 * Ein vorkonfigurierter Login-Dialog zur Abfrage von Username und Passwort.
 */
public class LoginDialog extends AbstractDialog
{
  private Login login = null;
  
  // Die Beschriftungen der Felder fuer Username und Passwort.
  private String labelUser     = null;
  private String labelPassword = null;
  
  // Label fuer die Fehlermeldung.
  private LabelInput error     = null;
  
  // Ueber Username und Passwort angezeigter Text.
  private String text          = null;
  
  // Anzuzeigender Fehlertext.
  private String errorText     = null;
  
  /**
   * ct.
   * @param position
   */
  public LoginDialog(int position)
  {
    super(position);
    this.setSize(400,SWT.DEFAULT);
    this.setTitle(i18n.tr("Login"));
    this.setSideImage(SWTUtil.getImage("dialog-password.png"));
    
    this.labelUser     = i18n.tr("Benutzername");
    this.labelPassword = i18n.tr("Passwort");
    this.text          = i18n.tr("Bitte geben Sie Benutzername und Passwort ein.");
  }
  
  /**
   * Speichert das vorausgefuellte Login.
   * @param login das vorausgefuellte Login.
   */
  public void setLogin(Login login)
  {
    this.login = login;
  }

  /**
   * Speichert den Text, der links neben dem Eingabefeld fuer die
   * Benutzername-Eingabe angezeigt werden soll (Optional).
   * @param text anzuzeigender Text.
   */
  public void setUsernameLabelText(String text)
  {
    if (text != null && text.length() > 0)
      this.labelUser = text;
  }

  /**
   * Speichert den Text, der links neben dem Eingabefeld fuer die
   * Passwort-Eingabe angezeigt werden soll (Optional).
   * @param text anzuzeigender Text.
   */
  public void setPasswordLabelText(String text)
  {
    if (text != null && text.length() > 0)
      this.labelPassword = text;
  }
  
  /**
   * Speichert den anzuzeigenden Text.
   * @param text anzuzeigender Text.
   */
  public void setText(String text)
  {
    if (text != null)
      this.text = text;
  }
  
  /**
   * Aktualisiert den Fehlertext.
   * @param text der Fehlertext.
   */
  public void setErrorText(String text)
  {
    this.errorText = text;
    if (this.error != null)
      this.error.setValue(this.errorText == null ? "" : this.errorText);
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
    
    // Username
    final TextInput username = new TextInput(null);
    username.setName(this.labelUser);
    username.setMaxLength(100); // Das sollte lang genug fuer jeden Usernamen sein ;)
    container.addInput(username);

    // Passwort.
    final PasswordInput password = new PasswordInput(null);
    password.setName(this.labelPassword);
    password.setMaxLength(100);
    container.addInput(password);
    
    // Falls ein Login angegeben ist, uebernehmen wir es und platzieren
    // den Focus gleich im Passwortfeld.
    if (this.login != null)
    {
      username.setValue(this.login.getUsername());
      char[] pw = this.login.getPassword();
      if (pw != null)
        password.setValue(new String(pw));
      else
        password.focus();
    }
    
    // Fehlertext
    this.error = new LabelInput(this.errorText);
    this.error.setColor(Color.ERROR);
    this.error.setName("");
    container.addInput(this.error);


    // Ggf. erweitern.
    extend(container);

    ButtonArea buttons = container.createButtonArea(2);
    
    buttons.addButton("    " + i18n.tr("OK") + "    ",new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (login == null)
          login = new Login();
        
        login.setUsername((String)username.getValue());
        login.setPassword((String)password.getValue());
        
        if (checkLogin(login))
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
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception 
  {
    return this.login;
  }

  /**
   * Kann von abgeleiteten Dialogen ueberschrieben werden, um
   * den Login-Dialog noch zu erweitern. 
   * Angezeigt wird die Erweiterung dann direkt ueber den beiden
   * Buttons.
   * @param container der Container.
   * @throws Exception
   */
  protected void extend(Container container) throws Exception
  {
  }

  /**
   * Kann ueberschrieben werden, wenn das Passwort geprueft werden soll.
   * @param login das eingegebene Login.
   * @return true, wenn das Login ok ist und der Dialog geschlosssen werden kann.
   * Die Funktion liefert per Default true zurueck.
   */
  protected boolean checkLogin(Login login)
  {
    return true;
  }
}


/*********************************************************************
 * $Log: LoginDialog.java,v $
 * Revision 1.5  2011/05/06 12:32:04  willuhn
 * @R Nicht mehr noetig - macht AbstractDialog jetzt selbst
 *
 * Revision 1.4  2009/06/10 11:25:54  willuhn
 * @N Transparente HTTP-Authentifizierung ueber Jameica (sowohl in GUI- als auch in Server-Mode) mittels ApplicationCallback
 *
 * Revision 1.3  2008/12/18 23:21:13  willuhn
 * @N GUI-Polishing: Neue Icons in Hibiscus und Jameica aus dem Tango-Projekt (http://tango.freedesktop.org/)
 * @R Nicht mehr benoetigte Grafiken entfernt
 * @C Anordnung des SideImages in AbstractDialog etwas geaendert (ein paar Pixel Abstand des Images vom Rand)
 *
 * Revision 1.2  2007/01/31 16:11:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2007/01/31 13:07:52  willuhn
 * @N Login-Dialog
 * @N SystemMessage
 *
 **********************************************************************/