/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/AbstractCertificateDialog.java,v $
 * $Revision: 1.7 $
 * $Date: 2007/01/04 15:24:21 $
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
import java.text.DateFormat;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.TextPart;
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
    //setSize(405,550); // Breite legen wir fest, damit der Fingerprint hinpasst
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

    if (cn == null && o == null && ou == null)
    {
      String s = cert.getIssuerDN().getName();
      group.addLabelPair(i18n.tr("Name"), new LabelInput(format(s)));
    }

    if (cn != null) group.addLabelPair(i18n.tr("Common Name (CN)"), new LabelInput(format(cn)));
    if (o  != null) group.addLabelPair(i18n.tr("Organisation (O)"), new LabelInput(format(o)));
    if (ou != null) group.addLabelPair(i18n.tr("Abteilung (OU)"), new LabelInput(format(ou)));
    /////////////////////////////////////////////////////////////////////////////
    

    /////////////////////////////////////////////////////////////////////////////
    // Subject
    group.addHeadline(Application.getI18n().tr("Ausgestellt für"));

    p = myCert.getSubject();

    cn = p.getAttribute(Principal.COMMON_NAME);
    o  = p.getAttribute(Principal.ORGANIZATION);
    ou = p.getAttribute(Principal.ORGANIZATIONAL_UNIT);

    if (cn == null && o == null && ou == null)
    {
      String s = cert.getSubjectDN().getName();
      group.addLabelPair(i18n.tr("Name"), new LabelInput(format(s)));
    }

    if (cn != null) group.addLabelPair(i18n.tr("Common Name (CN)"), new LabelInput(format(cn)));
    if (o  != null) group.addLabelPair(i18n.tr("Organisation (O)"), new LabelInput(format(o)));
    if (ou != null) group.addLabelPair(i18n.tr("Abteilung (OU)"), new LabelInput(format(ou)));
    /////////////////////////////////////////////////////////////////////////////



    /////////////////////////////////////////////////////////////////////////////
    // Details
    group.addHeadline(Application.getI18n().tr("Eigenschaften des Zertifikats"));

    group.addLabelPair(i18n.tr("Gültigkeit"), new LabelInput(df.format(cert.getNotBefore()) + " - " + df.format(cert.getNotAfter())));
    group.addLabelPair(i18n.tr("Seriennummer"), new LabelInput(cert.getSerialNumber().toString()));
    /////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////
    // Fingerprint
    TextPart fingerprints = new TextPart();
    fingerprints.setBackground(Color.WIDGET_BG);
    fingerprints.appendText(i18n.tr("MD5-Fingerabdruck:\n{0}",myCert.getMD5Fingerprint()));
    fingerprints.appendText("\n");
    fingerprints.appendText(i18n.tr("SHA1-Fingerabdruck:\n{0}",myCert.getSHA1Fingerprint()));
    group.addPart(fingerprints);
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

  private String format(String s)
  {
    if (s == null || s.length() < 40)
      return s;
    return s.replaceAll(",","\n");
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
 * Revision 1.7  2007/01/04 15:24:21  willuhn
 * @C certificate import handling
 * @B Bug 330
 *
 * Revision 1.6  2006/11/15 00:12:35  willuhn
 * @B Bug 329
 *
 * Revision 1.5  2005/11/22 07:38:32  web0
 * *** empty log message ***
 *
 * Revision 1.4  2005/11/22 00:47:23  web0
 * @B buttons not visible
 *
 * Revision 1.3  2005/07/20 16:23:10  web0
 * @B splitting x.500 name
 *
 * Revision 1.2  2005/06/15 17:51:31  web0
 * @N Code zum Konfigurieren der Service-Bindings
 *
 * Revision 1.1  2005/06/13 12:13:37  web0
 * @N Certificate-Code completed
 *
 **********************************************************************/