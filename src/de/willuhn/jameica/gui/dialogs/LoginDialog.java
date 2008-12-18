/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/LoginDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2008/12/18 23:21:13 $
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.security.Login;
import de.willuhn.jameica.system.OperationCanceledException;

/**
 * Ein vorkonfigurierter Login-Dialog zur Abfrage von Username und Passwort.
 */
public class LoginDialog extends SimpleDialog
{
  private int retries    = 0;

  private Composite comp = null;
  private Label label    = null;

  private Label userLabel     = null;
  private Label passwordLabel = null;

  private Label error   = null;

  private Text user     = null;
  private Text password = null;

  private Button button = null;
  private Button cancel = null;
  
  private String labelUser       = "";
  private String labelPassword   = "";
  
  private Login login = null;


  /**
   * ct.
   * @param position
   */
  public LoginDialog(int position)
  {
    super(position);
    labelUser     = i18n.tr("Benutzername");
    labelPassword = i18n.tr("Passwort");
    setSideImage(SWTUtil.getImage("dialog-password.png"));
  }

  /**
   * Speichert den Text, der links neben dem Eingabefeld fuer die
   * Benutzername-Eingabe angezeigt werden soll (Optional).
   * @param text anzuzeigender Text.
   */
  public void setUsernameLabelText(String text)
  {
    if (text == null || text.length() == 0)
      return;
    labelUser = text;
  }

  /**
   * Speichert den Text, der links neben dem Eingabefeld fuer die
   * Passwort-Eingabe angezeigt werden soll (Optional).
   * @param text anzuzeigender Text.
   */
  public void setPasswordLabelText(String text)
  {
    if (text == null || text.length() == 0)
      return;
    labelPassword = text;
  }
  
  /**
   * Legt den standardmaessig anzuzeigenden Usernamen fest.
   * @param username
   */
  public void setDefaultUsername(String username)
  {
    if (username != null)
      this.login = new Login(username,null);
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    // Composite um alles drumrum.
    comp = new Composite(parent,SWT.NONE);
    comp.setBackground(Color.BACKGROUND.getSWTColor());
    comp.setLayoutData(new GridData(GridData.FILL_BOTH));
    GridLayout gl = new GridLayout(3,false);
    comp.setLayout(gl);
    
    // Text
    label = GUI.getStyleFactory().createLabel(comp,SWT.WRAP);
    label.setText(getText());
    GridData grid = new GridData(GridData.FILL_HORIZONTAL);
    grid.horizontalSpan = 3;
    label.setLayoutData(grid);
    
    // Fehlertext 
    error = GUI.getStyleFactory().createLabel(comp,SWT.WRAP);
    error.setForeground(Color.ERROR.getSWTColor());
    GridData grid2 = new GridData(GridData.FILL_HORIZONTAL);
    grid2.horizontalSpan = 3;
    grid2.horizontalIndent = 0;
    error.setLayoutData(grid2);

    // Label vor User-Eingabefeld
    userLabel = GUI.getStyleFactory().createLabel(comp,SWT.NONE);
    userLabel.setText(labelUser);
    userLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

    user = GUI.getStyleFactory().createText(comp);
    GridData grid3 = new GridData(GridData.FILL_HORIZONTAL);
    grid3.horizontalSpan = 2;
    user.setLayoutData(grid3);

    // Label vor Passwort-Eingabe
    passwordLabel = GUI.getStyleFactory().createLabel(comp,SWT.NONE);
    passwordLabel.setText(labelPassword);
    passwordLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

    password = GUI.getStyleFactory().createText(comp);
    password.setEchoChar('*');
    GridData grid4 = new GridData(GridData.FILL_HORIZONTAL);
    grid4.horizontalSpan = 2;
    password.setLayoutData(grid4);

    if (login != null && login.getUsername() != null)
    {
      user.setText(login.getUsername());
      password.setFocus();
    }

    // Dummy-Label damit die Buttons buendig unter dem Eingabefeld stehen
    Label dummy = GUI.getStyleFactory().createLabel(comp,SWT.NONE);
    dummy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    // OK-Button
    button = GUI.getStyleFactory().createButton(comp);
    button.setText("    " + i18n.tr("OK") + "    ");
    button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    button.addSelectionListener(new SelectionAdapter()
    {
      public void widgetSelected(SelectionEvent e)
      {
        retries++;
        login = new Login(user.getText(),password.getText() == null ? null : password.getText().toCharArray());

        if (!checkLogin(login))
        {
          password.setText("");
          if (retries >= PasswordDialog.MAX_RETRIES)
          {
            // maximale Anzahl der Fehlversuche erreicht.
            throw new OperationCanceledException(i18n.tr("{0} falsche Login-Versuche",""+PasswordDialog.MAX_RETRIES));
          }
          
          if (error != null && !error.isDisposed())
          {
            int remaining = PasswordDialog.MAX_RETRIES - retries;
            String s = remaining > 1 ? i18n.tr("Versuche") : i18n.tr("Versuch");
            error.setText(i18n.tr("Login fehlgeschlagen. Noch {0} {1}.", new String[]{""+remaining,s}));
          }
          return;
        }
        close();
      }
    });
    getShell().setDefaultButton(button);

    // Abbrechen-Button
    cancel = GUI.getStyleFactory().createButton(comp);
    cancel.setText(i18n.tr("Abbrechen"));
    cancel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    cancel.addSelectionListener(new SelectionAdapter()
    {
      public void widgetSelected(SelectionEvent e)
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
    return login;
  }

  /**
   * Kann ueberschrieben werden, wenn das Passwort geprueft werden soll.
   * @param login das eingegebene Login.
   * @return true, wenn das Login i.o. ist.
   */
  protected boolean checkLogin(Login login)
  {
    return true;
  }

}


/*********************************************************************
 * $Log: LoginDialog.java,v $
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