/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import java.net.URL;

import de.willuhn.jameica.gui.dialogs.CertificateTrustDialog;
import de.willuhn.jameica.services.RepositoryService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Wird verwendet, um beim ersten Oeffnen des System-Plugin-Repository einen Custom-Dialog
 * anzuzeigen.
 */
public class SystemRepositoryTrustMessageConsumer implements MessageConsumer
{
  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{CheckTrustMessage.class};
  }
  
  /**
   * Oeffnet das System-Repository und stellt die Vertrauensstellung her.
   * @throws ApplicationException
   */
  public void performTrust() throws ApplicationException
  {
    // Wir oeffnen einmal das System-Repository, um die Anzeige des Zertifikates zu triggern, falls
    // es vom User noch nicht importiert wurde.
    try
    {
      Logger.info("open system plugin repository to verify trust state");
      Application.getMessagingFactory().registerMessageConsumer(this);
      RepositoryService rs = Application.getBootLoader().getBootable(RepositoryService.class);
      rs.open(new URL(RepositoryService.SYSTEM_REPOSITORY));
      Logger.info("trust to system plugin repository granted");
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      throw new ApplicationException(Application.getI18n().tr("Öffnen des System-Plugin-Repository fehlgeschlagen: {0}",e.getMessage()),e);
    }
    finally
    {
      Application.getMessagingFactory().unRegisterMessageConsumer(this);
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    CheckTrustMessage msg = (CheckTrustMessage) message;
    
    if (Application.inServerMode() || Application.inNonInteractiveMode())
      return;
    
    CertificateTrustDialog d = new CertificateTrustDialog(CertificateTrustDialog.POSITION_CENTER,msg.getCertificate());
    d.setText(Application.getI18n().tr("Bitte prüfen Sie, ob das angezeigte Zertifikat mit dem Server-Zertifikat des\n" +
                                       "Webservers www.willuhn.de übereinstimmt."));

    // Wir muessen hier die Exception hier fangen und per Message zurueckreichen
    try
    {
      Boolean b = (Boolean) d.open();
      msg.setTrusted(b.booleanValue(),"user via certificate dialog");
    }
    catch (Exception e)
    {
      msg.setException(e);
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    // Nur manuell
    return false;
  }

}


