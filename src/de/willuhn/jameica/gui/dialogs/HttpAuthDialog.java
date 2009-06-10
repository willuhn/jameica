/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/HttpAuthDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/06/10 11:25:53 $
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.security.JameicaAuthenticator;
import de.willuhn.jameica.security.Login;
import de.willuhn.jameica.security.Wallet;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;

/**
 * Erweitert den Login-Dialog fuer HTTP-Authentifizierung
 * und hierbei insb. das Speichern des Logins.
 */
public class HttpAuthDialog extends LoginDialog
{
  // Hier drin koennen wir die Login-Daten speichern
  private static Wallet wallet = null;
  private static Settings settings = new Settings(HttpAuthDialog.class);
  
  private CheckboxInput store = null;
  private String walletKey    = null;
  
  static
  {
    try
    {
      wallet = new Wallet(HttpAuthDialog.class);
    }
    catch (Exception e)
    {
      Logger.error("unable to create wallet, saving of logins disabled",e);
    }
  }

  /**
   * ct.
   * @param position
   */
  public HttpAuthDialog(int position)
  {
    this(position,null);
  }

  /**
   * ct.
   * @param position
   * @param auth der Jameica-Authenticator.
   */
  public HttpAuthDialog(int position, JameicaAuthenticator auth)
  {
    super(position);
    
    // Mal schauen, ob wir fuer den Authenticator ein Login haben
    if (auth != null)
    {
      // Der Prompt ist bei HTTP-Auth der Realm.
      // Und der kann zusammen mit dem Hostnamen
      // als Key genutzt werden. Webbrowser machen das
      // genauso.
      String prompt = (String) auth.getRequestParam(JameicaAuthenticator.RequestParam.PROMPT);
      String host   = (String) auth.getRequestParam(JameicaAuthenticator.RequestParam.HOST);
      
      this.walletKey = host + ":" + auth.getRequestParam(JameicaAuthenticator.RequestParam.PORT) + ":" + prompt;

      // Wenn ein Realm existiert, verwenden wir es auch als Text.
      if (prompt != null && prompt.length() > 0)
        this.setText(i18n.tr("Bitte geben Sie Benutzername und Passwort ein.\nSeite: {0}",prompt));

      // Wenn ein Host angegeben ist, schreiben wir ihn in den Titel.
      if (host != null && host.length() > 0)
        this.setTitle(i18n.tr("Login: {0}",host));
      // Login uebernehmen, falls wir eines haben
      this.setLogin((Login) wallet.get(this.walletKey));
    }
    
    this.addCloseListener(new Listener()
    {
    
      /**
       * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
       */
      public void handleEvent(Event event)
      {
        if (event.detail != SWT.OK)
          return; // Wenn der Dialog nicht mit OK geschlossen wurde, machen wir gar nichts
        
        // Speichern/Loeschen des Logins
        Login login = (Login) event.data;
        try
        {
          wallet.set(walletKey,settings.getBoolean("login.store",false) ? login : null);
        }
        catch (Exception e)
        {
          // Dann halt nicht
          Logger.error("unable to store login in wallet",e);
        }
      }
    });
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.LoginDialog#extend(de.willuhn.jameica.gui.util.Container)
   */
  protected void extend(Container container) throws Exception
  {
    // Ueberschrieben, um eine Checkbox zum Speichern des Logins anzubieten
    this.store = new CheckboxInput(settings.getBoolean("login.store",false));
    this.store.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        // Aktuellen Zustand der Checkbox sofort speichern
        settings.setAttribute("login.store",((Boolean)store.getValue()).booleanValue());
      }
    });
    container.addCheckbox(this.store,i18n.tr("Login speichern"));
  }

}


/**********************************************************************
 * $Log: HttpAuthDialog.java,v $
 * Revision 1.1  2009/06/10 11:25:53  willuhn
 * @N Transparente HTTP-Authentifizierung ueber Jameica (sowohl in GUI- als auch in Server-Mode) mittels ApplicationCallback
 *
 **********************************************************************/
