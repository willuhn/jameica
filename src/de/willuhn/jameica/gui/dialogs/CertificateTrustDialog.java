/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.dialogs;

import java.security.cert.X509Certificate;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.ButtonArea;
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
    setText(i18n.tr("Bitte prüfen Sie die Eigenschaften des Server-Zertifikates und " +
                    "entscheiden Sie,\nob Sie ihm vertrauen möchten."));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return trust;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractCertificateDialog#paintButtons(de.willuhn.jameica.gui.parts.ButtonArea)
   */
  protected void paintButtons(ButtonArea buttons)
  {
    buttons.addButton("   " + i18n.tr("Ja") + "   ", new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        trust = Boolean.TRUE;
        close();
      }
    },null,false,"ok.png");
    buttons.addButton("   " + i18n.tr("Nein") + "   ", new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        trust = Boolean.FALSE;
        close();
      }
    },null,false,"window-close.png");
    buttons.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    },null,false,"process-stop.png");
  }

}


/**********************************************************************
 * $Log: CertificateTrustDialog.java,v $
 * Revision 1.4  2012/01/25 21:36:24  willuhn
 * @C BUGZILLA 1178 - geaenderter Text in Trust-Dialog
 *
 * Revision 1.3  2011-05-03 10:13:11  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
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