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

import java.io.File;
import java.io.FileInputStream;
import java.security.cert.X509Certificate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.parts.CertificateList;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Importieren eines neuen Zertifikates.
 */
public class CertificateImport implements Action
{
  private final static Settings settings = new Settings(CertificateList.class);
  

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    FileDialog d = new FileDialog(GUI.getShell(),SWT.OPEN);
    d.setText(Application.getI18n().tr("Bitte wählen Sie das zu importierende Zertifikat aus"));
    d.setFilterPath(settings.getString("lastdir", System.getProperty("user.home")));
    d.setFilterExtensions(new String[]{"*.pem;*.crt;*.cer"});
    String s = d.open();
    if (s == null || s.length() == 0)
      return;
    File f = new File(s);
    if (!f.exists() || !f.isFile() || !f.canRead())
      throw new ApplicationException(Application.getI18n().tr("Zertifikat {0} nicht lesbar",s));

    settings.setAttribute("lastdir",f.getParent());
    try
    {
      final X509Certificate c = Application.getSSLFactory().loadCertificate(new FileInputStream(f));
      if (c == null)
        throw new ApplicationException(Application.getI18n().tr("Zertifikat nicht lesbar"));

      Application.getSSLFactory().addTrustedCertificate(c);
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (OperationCanceledException oce)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr(oce.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
    catch (Exception e)
    {
      Logger.error("error while importing certificate",e);
      throw new ApplicationException(Application.getI18n().tr("Fehler beim Importieren des Zertifikats"));
    }
  
  }

}


/*********************************************************************
 * $Log: CertificateImport.java,v $
 * Revision 1.7  2010/11/02 00:10:54  willuhn
 * @C FilterExtension
 *
 * Revision 1.6  2009/01/18 00:03:46  willuhn
 * @N SSLFactory#addTrustedCertificate() liefert jetzt den erzeugten Alias-Namen des Keystore-Entries
 * @N SSLFactory#getTrustedCertificate(String) zum Abrufen eines konkreten Zertifikates
 *
 * Revision 1.5  2007/01/04 15:24:21  willuhn
 * @C certificate import handling
 * @B Bug 330
 *
 * Revision 1.4  2006/11/13 00:56:54  willuhn
 * @C store last import dir
 *
 * Revision 1.3  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
 * Revision 1.2  2005/12/13 19:49:24  web0
 * @B wrong filename extension in file dialog
 *
 * Revision 1.1  2005/07/24 17:00:21  web0
 * *** empty log message ***
 *
 **********************************************************************/