/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/AbstractApplicationCallback.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/10/28 01:05:21 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.system;

import java.net.InetAddress;

import de.willuhn.logging.Logger;

/**
 * Abstrakte Basis-Implementierung des Application-Callback.
 */
public abstract class AbstractApplicationCallback implements ApplicationCallback
{
  protected Settings settings = new Settings(ApplicationCallback.class);

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#getHostname()
   */
  public String getHostname() throws Exception
  {
    // BUGZILLA 26 http://www.willuhn.de/bugzilla/show_bug.cgi?id=26
    String question =
      Application.getI18n().tr("Der Hostname Ihres Computers konnte nicht ermittelt werden.\n" +
                               "Bitte geben Sie ihn manuell ein. Sollten Sie ihn nicht kennen,\n" +
                               "dann wählen Sie einen beliebigen Namen. Verwenden Sie bitte\n" +
                               "ausschliesslich Buchstaben oder Zahlen und ggf. \".\" oder \"-\"");
    String label = Application.getI18n().tr("Hostname Ihres Computers");
    try
    {
      InetAddress a = InetAddress.getLocalHost();

      String host = a.getCanonicalHostName();

      if (host == null || host.length() == 0)
        host = a.getHostName();

      if (host == null || host.length() == 0)
        host = a.getHostAddress();

      if (host != null && host.length() > 0 && !host.equals("127.0.0.1"))
        return host;
      
      return Application.getCallback().askUser(question,label);
      
    }
    catch (Exception e)
    {
      Logger.error("unable to determine hostname, asking user",e);
      return Application.getCallback().askUser(question,label);
    }
  }
}


/*********************************************************************
 * $Log: AbstractApplicationCallback.java,v $
 * Revision 1.1  2006/10/28 01:05:21  willuhn
 * *** empty log message ***
 *
 **********************************************************************/