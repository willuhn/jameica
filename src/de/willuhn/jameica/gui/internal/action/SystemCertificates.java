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
import de.willuhn.jameica.gui.internal.dialogs.SystemCertificatesDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Anzeigen der System-Zertifikate.
 */
public class SystemCertificates implements Action
{
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      SystemCertificatesDialog d = new SystemCertificatesDialog(SystemCertificatesDialog.POSITION_CENTER);
      d.open();
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled");
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to show system certificates",e);
      throw new ApplicationException(Application.getI18n().tr("Fehler beim Anzeigen: {0}",e.getMessage()));
    }
  }
}



/**********************************************************************
 * $Log: SystemCertificates.java,v $
 * Revision 1.1  2011/06/27 17:51:43  willuhn
 * @N Man kann sich jetzt die Liste der von Java bereits mitgelieferten Aussteller-Zertifikate unter Datei->Einstellungen anzeigen lassen - um mal einen Ueberblick zu kriegen, wem man so eigentlich alles blind vertraut ;)
 * @N Mit der neuen Option "Aussteller-Zertifikaten von Java vertrauen" kann man die Vertrauensstellung zu diesen Zertifikaten deaktivieren - dann muss der User jedes Zertifikate explizit bestaetigen - auch wenn Java die CA kennt
 *
 **********************************************************************/