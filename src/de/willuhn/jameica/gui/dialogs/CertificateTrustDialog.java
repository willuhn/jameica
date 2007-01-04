/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/CertificateTrustDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/01/04 15:34:26 $
 * $Author: willuhn $
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
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

/**
 * Ein Dialog, der ein X509-Zertifikat anzeigt und den User fragt, ob er dem Zertifikat traut.
 */
public class CertificateTrustDialog extends AbstractCertificateDialog
{

  private Boolean trust = Boolean.FALSE;

  /**
   * ct.
   * @param position
   * @param cert
   */
  public CertificateTrustDialog(int position, X509Certificate cert)
  {
    super(position, cert);
    setTitle(i18n.tr("Sicherheitsabfrage"));
    
    // Wir speichern vorab schonmal einen eigenen Text. Falls
    // der Benutzer "setText(String)" nicht aufruft, wird dann
    // dieser hier angezeigt.
    setText(i18n.tr("Sie verbinden sich mit einem für Jameica unbekannten System.\n" +
    "Bitte prüfen Sie die Eigenschaften des Zertifikats und entscheiden Sie,\nob Sie ihm " +
    "vertrauen möchten."));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return trust;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractCertificateDialog#paintButtons(org.eclipse.swt.widgets.Composite)
   */
  protected void paintButtons(Composite parent)
  {
    ButtonArea buttons = new ButtonArea(parent,3);
    buttons.addButton("   " + i18n.tr("Ja") + "   ", new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        trust = Boolean.TRUE;
        close();
      }
    });
    buttons.addButton("   " + i18n.tr("Nein") + "   ", new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        trust = Boolean.FALSE;
        close();
      }
    },null,true);
    buttons.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    });
  }

}


/**********************************************************************
 * $Log: CertificateTrustDialog.java,v $
 * Revision 1.2  2007/01/04 15:34:26  willuhn
 * @N linebreak
 *
 * Revision 1.1  2005/06/13 12:13:37  web0
 * @N Certificate-Code completed
 *
 * Revision 1.3  2005/06/10 13:04:41  web0
 * @N non-interactive Mode
 * @N automatisches Abspeichern eingehender Zertifikate im nicht-interaktiven Mode
 *
 * Revision 1.2  2005/06/10 10:12:26  web0
 * @N Zertifikats-Dialog ergonomischer gestaltet
 * @C TrustManager prueft nun zuerst im Java-eigenen Keystore
 *
 * Revision 1.1  2005/06/09 23:07:47  web0
 * @N certificate checking activated
 *
 **********************************************************************/