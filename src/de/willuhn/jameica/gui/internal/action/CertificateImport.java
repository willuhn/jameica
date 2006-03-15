/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/action/CertificateImport.java,v $
 * $Revision: 1.3 $
 * $Date: 2006/03/15 16:25:32 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
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
import de.willuhn.jameica.gui.dialogs.CertificateTrustDialog;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Importieren eines neuen Zertifikates.
 */
public class CertificateImport implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    FileDialog d = new FileDialog(GUI.getShell(),SWT.OPEN);
    d.setText(Application.getI18n().tr("Bitte wählen Sie das zu importierende Zertifikat aus"));
    d.setFilterPath(Application.getConfig().getWorkDir());
    d.setFilterExtensions(new String[]{"*.pem","*.crt","*.cer"});
    String s = d.open();
    if (s == null || s.length() == 0)
      return;
    File f = new File(s);
    if (!f.exists() || !f.isFile() || !f.canRead())
      throw new ApplicationException(Application.getI18n().tr("Zertifikat {0} nicht lesbar",s));
    
    try
    {
      final X509Certificate c = Application.getSSLFactory().loadCertificate(new FileInputStream(f));
      CertificateTrustDialog d2 = new CertificateTrustDialog(CertificateTrustDialog.POSITION_CENTER,c);
      d2.setText(Application.getI18n().tr("Sind Sie sicher, dass Sie dieses Zertifikat importieren wollen?"));

      Boolean b = (Boolean) d2.open();
      if (b != null && b.booleanValue())
      {
        Application.getSSLFactory().addTrustedCertificate(c);
      }
    }
    catch (OperationCanceledException oce)
    {
      Logger.info(Application.getI18n().tr("Vorgang abgebrochen"));
      // ignore
    }
    catch (Exception e)
    {
      Logger.error("error while importing certificate",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Importieren des Zertifikats"),StatusBarMessage.TYPE_ERROR));
    }
  
  }

}


/*********************************************************************
 * $Log: CertificateImport.java,v $
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