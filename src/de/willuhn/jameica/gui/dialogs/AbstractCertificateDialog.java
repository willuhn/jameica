/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/AbstractCertificateDialog.java,v $
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
import java.text.DateFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.security.Certificate;
import de.willuhn.jameica.security.Principal;
import de.willuhn.jameica.system.Application;

/**
 * Abstrakter Basis-Dialog zur Anzeige von X.509-Zertifikaten
 */
public abstract class AbstractCertificateDialog extends AbstractDialog
{

  private String text = null;

  private X509Certificate cert = null;

  /**
   * ct.
   * @param position
   * @param cert
   */
  public AbstractCertificateDialog(int position, X509Certificate cert)
  {
    super(position);
    this.cert = cert;
    setSize(400,SWT.DEFAULT); // Breite legen wir fest, damit der Fingerprint hinpasst
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT, Application.getConfig().getLocale());

    LabelGroup group = new LabelGroup(parent,i18n.tr("Details des Zertifikats"));
    
    group.addText(text == null ? "" : text,true);

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

    paintButtons(parent);

  }

  /**
   * Muss von der ableitenden Klasse implementiert werden,
   * um die Buttons am Ende des Dialogs zu zeichnen.
   * @param parent Parent, in das die Buttons gemalt werden muessen.
   */
  protected abstract void paintButtons(Composite parent);
  

  /**
   * Liefert <code>null</code> zurueck.
   * Bitte ggf. ueberschreiben, um einen anderen Wert zurueckzuliefern.
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
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
 * $Log: AbstractCertificateDialog.java,v $
 * Revision 1.1  2005/06/13 12:13:37  web0
 * @N Certificate-Code completed
 *
 **********************************************************************/