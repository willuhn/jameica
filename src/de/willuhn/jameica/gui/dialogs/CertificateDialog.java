/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/Attic/CertificateDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/06/10 13:04:41 $
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
import java.text.DateFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.security.Certificate;
import de.willuhn.jameica.security.Principal;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

/**
 * Ein Dialog, der ein X509-Zertifikat anzeigt und den User fragt, ob er dem Zertifikat traut.
 */
public class CertificateDialog extends AbstractDialog
{

  private String text = null;

  private X509Certificate cert = null;

  private Boolean trust = Boolean.FALSE;

  /**
   * ct.
   * @param position
   * @param cert
   */
  public CertificateDialog(int position, X509Certificate cert)
  {
    super(position);
    setTitle(i18n.tr("Sicherheitsabfrage"));
    this.cert = cert;
    setSize(400,SWT.DEFAULT); // Breite legen wir fest, damit der Fingerprint hinpasst
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT, Application.getConfig().getLocale());

    LabelGroup group = new LabelGroup(parent,i18n.tr("Unbekanntes Zertifikat"));
    
    if (text != null && text.length() > 0)
    {
      group.addText(text,true);
    }
    else
    {
      group.addText(i18n.tr("Sie verbinden sich mit einem für Jameica unbekannten System. " +        "Bitte prüfen Sie die Eigenschaften des Zertifikats und entscheiden Sie, ob Sie ihm " +        "vertrauen möchten."),true);
    }

   

    /////////////////////////////////////////////////////////////////////////////
    // Aussteller
    group.addHeadline(Application.getI18n().tr("Ausgestellt von"));


    Certificate myCert = new Certificate(cert);
    Principal p = myCert.getIssuer();

    String cn = p.getAttribute(Principal.COMMON_NAME);
    String o  = p.getAttribute(Principal.ORGANIZATION);
    String ou = p.getAttribute(Principal.ORGANIZATIONAL_UNIT);

    if (cn != null) group.addLabelPair(i18n.tr("Common Name (CN)"), new LabelInput(cn));
    if (o  != null) group.addLabelPair(i18n.tr("Organisation (O)"), new LabelInput(o));
    if (ou != null) group.addLabelPair(i18n.tr("Abteilung (OU)"), new LabelInput(ou));
    /////////////////////////////////////////////////////////////////////////////
    

    /////////////////////////////////////////////////////////////////////////////
    // Subject
    group.addHeadline(Application.getI18n().tr("Ausgestellt für"));

    p = myCert.getSubject();

    cn = p.getAttribute(Principal.COMMON_NAME);
    o  = p.getAttribute(Principal.ORGANIZATION);
    ou = p.getAttribute(Principal.ORGANIZATIONAL_UNIT);

    if (cn != null) group.addLabelPair(i18n.tr("Common Name (CN)"), new LabelInput(cn));
    if (o  != null) group.addLabelPair(i18n.tr("Organisation (O)"), new LabelInput(o));
    if (ou != null) group.addLabelPair(i18n.tr("Abteilung (OU)"), new LabelInput(ou));
    /////////////////////////////////////////////////////////////////////////////



    /////////////////////////////////////////////////////////////////////////////
    // Details
    group.addHeadline(Application.getI18n().tr("Eigenschaften des Zertifikats"));

    group.addLabelPair(i18n.tr("Gültig von"), new LabelInput(df.format(cert.getNotBefore())));
    group.addLabelPair(i18n.tr("Gültig bis"), new LabelInput(df.format(cert.getNotAfter())));
    group.addLabelPair(i18n.tr("Seriennummer"), new LabelInput(cert.getSerialNumber().toString()));
    group.addLabelPair(i18n.tr("Typ"), new LabelInput(cert.getType()));
    /////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////
    // Fingerprint
    group.addLabelPair(i18n.tr("MD5-Fingerabdruck"), new LabelInput(myCert.getMD5Fingerprint()));
    /////////////////////////////////////////////////////////////////////////////

    try
    {
      cert.checkValidity();
    }
    catch (Exception e)
    {
      group.addHeadline(Application.getI18n().tr("WARNUNG"));
      group.addText(Application.getI18n().tr("Zertifikat abgelaufen oder noch nicht gültig!"),true,Color.ERROR);
    }
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

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return trust;
  }

  /**
   * Legt den anzuzeigenden Text auf dem Dialog fest.
   * @param text anzuzeigender Text.
   */
  public void setText(String text)
  {
    this.text = text;
  }
}


/**********************************************************************
 * $Log: CertificateDialog.java,v $
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