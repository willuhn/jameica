/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/CertificateDetailDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/06/13 12:13:37 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.dialogs;

import java.security.cert.X509Certificate;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.util.ApplicationException;

/**
 * Ein Dialog, der ein X509-Zertifikat lediglich anzeigt.
 */
public class CertificateDetailDialog extends AbstractCertificateDialog
{

  /**
   * ct.
   * @param position
   * @param cert
   */
  public CertificateDetailDialog(int position, X509Certificate cert)
  {
    super(position, cert);
    setTitle(i18n.tr("Zertifikat"));
   
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractCertificateDialog#paintButtons(org.eclipse.swt.widgets.Composite)
   */
  protected void paintButtons(Composite parent)
  {
    ButtonArea buttons = new ButtonArea(parent,1);
    buttons.addButton("   " + i18n.tr("Schliessen") + "   ", new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,true);
  }
}


/**********************************************************************
 * $Log: CertificateDetailDialog.java,v $
 * Revision 1.1  2005/06/13 12:13:37  web0
 * @N Certificate-Code completed
 *
 **********************************************************************/