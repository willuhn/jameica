/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/AbstractCertificateDialog.java,v $
 * $Revision: 1.9 $
 * $Date: 2012/01/28 00:06:07 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.dialogs;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.security.Certificate;
import de.willuhn.jameica.security.Principal;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;

/**
 * Abstrakter Basis-Dialog zur Anzeige von X.509-Zertifikaten
 */
public abstract class AbstractCertificateDialog extends AbstractDialog
{
  private String text = null;
  private List<X509Certificate> certs = new LinkedList<X509Certificate>();
  
  private Input cnIssuer    = null;
  private Input oIssuer     = null;
  private Input ouIssuer    = null;
  private Input cnSubject   = null;
  private Input oSubject    = null;
  private Input ouSubject   = null;
  private Input validity    = null;
  private Input serial      = null;
  private Input fingerprint = null;
  
  private LabelInput warning = null;

  /**
   * ct.
   * @param position Position des Dialogs.
   * @param cert Zertifikat.
   */
  public AbstractCertificateDialog(int position, X509Certificate cert)
  {
    this(position,Arrays.asList(cert));
  }
  
  /**
   * ct.
   * @param position Position des Dialogs.
   * @param certs Liste der Zertifikate.
   */
  public AbstractCertificateDialog(int position, List<X509Certificate> certs)
  {
    super(position);
    this.setSize(440,SWT.DEFAULT);
    this.certs.addAll(certs);
    
    this.cnIssuer    = this.createLabel(i18n.tr("Common Name (CN)"));
    this.oIssuer     = this.createLabel(i18n.tr("Organisation (O)"));
    this.ouIssuer    = this.createLabel(i18n.tr("Abteilung (OU)"));
    
    this.cnSubject   = this.createLabel(i18n.tr("Common Name (CN)"));
    this.oSubject    = this.createLabel(i18n.tr("Organisation (O)"));
    this.ouSubject   = this.createLabel(i18n.tr("Abteilung (OU)"));
    
    this.validity    = this.createLabel(i18n.tr("Gültigkeit"));
    this.serial      = this.createLabel(i18n.tr("Seriennummer"));
    this.fingerprint = new TextAreaInput("");
    this.fingerprint.setEnabled(false);
    
    this.warning     = this.createLabel("");
    this.warning.setColor(Color.ERROR);
  }
  
  /**
   * Erzeugt ein neues Label mit dem angegebenen Namen.
   * @param name der Name des Labels.
   * @return das Label.
   */
  private LabelInput createLabel(String name)
  {
    LabelInput label = new LabelInput("");
    label.setName(name);
    return label;
  }

  /**
   * Befuellt die Labels mit den Werten des Zertifikates.
   * @param cert das Zertifikat.
   * @throws NoSuchAlgorithmException 
   * @throws CertificateEncodingException 
   */
  private void fill(X509Certificate cert) throws CertificateEncodingException, NoSuchAlgorithmException
  {
    Certificate myCert = new Certificate(cert);
    
    /////////////////////////////////////////////////////////////////////////////
    // Aussteller
    {
      Principal p = myCert.getIssuer();

      String cn = p.getAttribute(Principal.COMMON_NAME);
      String o  = p.getAttribute(Principal.ORGANIZATION);
      String ou = p.getAttribute(Principal.ORGANIZATIONAL_UNIT);

      this.cnIssuer.setValue(format(cn));
      this.oIssuer.setValue(format(o));
      this.ouIssuer.setValue(format(ou));
    }
    //
    /////////////////////////////////////////////////////////////////////////////
    
    /////////////////////////////////////////////////////////////////////////////
    // Subject
    {
      Principal p = myCert.getSubject();

      String cn = p.getAttribute(Principal.COMMON_NAME);
      String o  = p.getAttribute(Principal.ORGANIZATION);
      String ou = p.getAttribute(Principal.ORGANIZATIONAL_UNIT);

      this.cnSubject.setValue(format(cn));
      this.oSubject.setValue(format(o));
      this.ouSubject.setValue(format(ou));
    }
    //
    /////////////////////////////////////////////////////////////////////////////
    
    /////////////////////////////////////////////////////////////////////////////
    // Details
    DateFormat df = DateUtil.DEFAULT_FORMAT;
    this.validity.setValue(i18n.tr("{0} - {1}",df.format(cert.getNotBefore()),df.format(cert.getNotAfter())));
    this.serial.setValue(cert.getSerialNumber().toString());
    /////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////
    // Fingerprint
    StringBuffer sb = new StringBuffer();
    sb.append(i18n.tr("MD5: {0}",myCert.getMD5Fingerprint()));
    sb.append("\n");
    sb.append(i18n.tr("SHA1: {0}",myCert.getSHA1Fingerprint()));
    this.fingerprint.setValue(sb.toString());
    /////////////////////////////////////////////////////////////////////////////
    
    /////////////////////////////////////////////////////////////////////////////
    // Ggf. Warnhinweis
    try
    {
      cert.checkValidity();
      this.warning.setValue("");
    }
    catch (CertificateException e)
    {
      this.warning.setValue(Application.getI18n().tr("Zertifikat abgelaufen oder noch nicht gültig!"));
    }
    //
    /////////////////////////////////////////////////////////////////////////////
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    // Per Default zeigen wir das erste Zertifikat an
    X509Certificate cert = this.certs.get(0);
    this.fill(cert);

    Container group = new SimpleContainer(parent,true);
    
    if (text != null && text.length() > 0)
      group.addText(text,true);

    /////////////////////////////////////////////////////////////////////////////
    // Aussteller
    group.addHeadline(Application.getI18n().tr("Ausgestellt von"));
    group.addInput(this.cnIssuer);
    group.addInput(this.oIssuer);
    group.addInput(this.ouIssuer);
    /////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////
    // Subject
    group.addHeadline(Application.getI18n().tr("Ausgestellt für"));
    group.addInput(this.cnSubject);
    group.addInput(this.oSubject);
    group.addInput(this.ouSubject);
    /////////////////////////////////////////////////////////////////////////////


    /////////////////////////////////////////////////////////////////////////////
    // Details
    group.addHeadline(Application.getI18n().tr("Eigenschaften des Zertifikats"));
    group.addInput(this.validity);
    group.addInput(this.serial);
    /////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////
    // Fingerprint
    group.addHeadline(Application.getI18n().tr("Fingerabdrücke"));
    group.addPart(this.fingerprint);
    /////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////
    // Warnung, falls abgelaufen
    Container c = new SimpleContainer(parent,false,1);
    this.warning.paint(c.getComposite());
    // group.addInput(this.warning);
    /////////////////////////////////////////////////////////////////////////////

    Container cb = new SimpleContainer(parent);
    ButtonArea buttons = new ButtonArea();
    paintButtons(buttons);
    cb.addButtonArea(buttons);
  }

  /**
   * Formatiert den Text.
   * @param s
   * @return
   */
  private String format(String s)
  {
    if (s == null)
      return "";
    
    if (s.length() < 40)
      return s;

    // Am Komma umbrechen
    return s.replaceAll(",","\n");
  }

  /**
   * Muss von der ableitenden Klasse implementiert werden,
   * um die Buttons am Ende des Dialogs zu zeichnen.
   * @param area die Button-Area.
   */
  protected abstract void paintButtons(ButtonArea area);
  

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
 * Revision 1.9  2012/01/28 00:06:07  willuhn
 * @N BUGZILLA 1179 - in progress
 *
 * Revision 1.8  2011-05-03 10:13:11  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 **********************************************************************/