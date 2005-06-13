/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/action/Attic/CertificateDelete.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/06/13 12:13:37 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import java.security.cert.X509Certificate;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.CertificateTrustDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Diese Aktion loescht ein Zertifikat aus dem Keystore.
 */
public class CertificateDelete implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>java.security.cert.X509Certificate</code>
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof X509Certificate))
      throw new ApplicationException(Application.getI18n().tr("Bitte wählen Sie das zu löschende Zertifikat aus"));
    
    CertificateTrustDialog d = new CertificateTrustDialog(CertificateTrustDialog.POSITION_CENTER,(X509Certificate)context);
    d.setText(Application.getI18n().tr("Sind Sie sicher, dass Sie dieses Zertifikat aus dem Stammspeicher löschen wollen?"));
    try
    {
      Boolean b = (Boolean) d.open();
      if (b == null || !b.booleanValue())
        return;
      Application.getSSLFactory().removeTrustedCertificate((X509Certificate) context);
    }
    catch (OperationCanceledException oce)
    {
      Logger.info(Application.getI18n().tr("Vorgang abgebrochen"));
      // ignore
    }
    catch (Exception e)
    {
      Logger.error("error while deleting certificate",e);
      GUI.getStatusBar().setErrorText(Application.getI18n().tr("Fehler beim Löschen des Zertifikats"));
    }
  }

}


/**********************************************************************
 * $Log: CertificateDelete.java,v $
 * Revision 1.2  2005/06/13 12:13:37  web0
 * @N Certificate-Code completed
 *
 * Revision 1.1  2005/06/10 22:59:35  web0
 * @N Loeschen von Zertifikaten
 *
 **********************************************************************/