/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.gui.internal.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Ueber diese Action kann das Master-Passwort von Jameica geaendert werden.
 */
public class ChangePassword implements Action
{

  /**
   * Hier kann {@code null} uebergeben werden.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	try
  	{
  		Application.getSSLFactory().changePassword();
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Master-Passwort erfolgreich geändert."),StatusBarMessage.TYPE_SUCCESS));
  	}
  	catch (OperationCanceledException oe)
  	{
			Logger.warn("changing of master password interrupted");
  	}
  	catch (Exception e)
  	{
  		Logger.error("error while changing master password",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Ändern des Master-Passwortes."),StatusBarMessage.TYPE_ERROR));
  	}
  }
}


/**********************************************************************
 * $Log: ChangePassword.java,v $
 * Revision 1.4  2011/02/23 11:50:20  willuhn
 * @R redundante Log-Ausgaben
 *
 * Revision 1.3  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
 * Revision 1.2  2005/03/03 23:47:51  web0
 * @B Bugzilla http://www.willuhn.de/bugzilla/show_bug.cgi?id=17
 *
 * Revision 1.1  2005/03/01 22:56:48  web0
 * @N master password can now be changed
 *
 **********************************************************************/