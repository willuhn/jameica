/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/PasswordDialog.java,v $
 * $Revision: 1.20 $
 * $Date: 2007/01/31 13:07:52 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.dialogs;

import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Composite;

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
    this.labelText = i18n.tr("Passwort");
    this.setSideImage(SWTUtil.getImage("password.gif"));
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
    if (text == null || text.length() == 0)
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
    container.addLabelPair(labelText,this.passwordInput);
    // Fehlertext
    this.errorTextInput = new LabelInput(this.errorText);
    this.errorTextInput.setColor(Color.ERROR);
    container.addLabelPair("",this.errorTextInput);

    // Der Freiraum fuer Erweiterungen
    extend(container);


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
 * Revision 1.20  2007/01/31 13:07:52  willuhn
 * @N Login-Dialog
 * @N SystemMessage
 *
 * Revision 1.19  2006/07/05 23:29:15  willuhn
 * @B Bug 174
 *
 * Revision 1.18  2005/06/06 09:54:30  web0
 * *** empty log message ***
 *
 * Revision 1.17  2005/06/02 22:57:42  web0
 * *** empty log message ***
 *
 * Revision 1.16  2005/02/01 17:15:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2005/01/30 20:47:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/10/19 23:33:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/10/18 23:37:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/08/15 17:55:17  willuhn
 * @C sync handling
 *
 * Revision 1.11  2004/07/27 23:41:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.9  2004/05/23 16:34:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
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