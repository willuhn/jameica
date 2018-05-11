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

import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.LinkInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.internal.action.Program;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.security.Certificate;
import de.willuhn.jameica.security.Principal;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Abstrakter Basis-Dialog zur Anzeige von X.509-Zertifikaten
 */
public abstract class AbstractCertificateDialog extends AbstractDialog
{
  private String text = null;
  private List<X509Certificate> certs = new LinkedList<X509Certificate>();
  
  private Input cnIssuer      = null;
  private Input oIssuer       = null;
  private Input ouIssuer      = null;
  private Input cnSubject     = null;
  private Input oSubject      = null;
  private Input ouSubject     = null;
  private LabelInput validity = null;
  private Input serial        = null;
  private Input fingerprint   = null;
  
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
    
    this.cnSubject   = this.createLink(i18n.tr("Common Name (CN)"));
    this.oSubject    = this.createLabel(i18n.tr("Organisation (O)"));
    this.ouSubject   = this.createLabel(i18n.tr("Abteilung (OU)"));
    
    this.validity    = this.createLabel(i18n.tr("Gültigkeit"));
    this.serial      = this.createLabel(i18n.tr("Seriennummer"));
    this.fingerprint = new TextAreaInput("");
    this.fingerprint.setEnabled(false);
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
   * Erzeugt ein neues Label mit dem angegebenen Namen als anklickbarer Link.
   * @param name der Name des Labels.
   * @return das Label.
   */
  private LinkInput createLink(String name)
  {
    LinkInput link = new LinkInput("")
    {
      /**
       * @see de.willuhn.jameica.gui.input.LinkInput#setValue(java.lang.Object)
       */
      @Override
      public void setValue(Object value)
      {
        // Wenn es ein aufloesbarer Hostname ist, schreiben wir ein "https://" davor
        // und machen einen Link draus
        if (value != null)
        {
          try
          {
            InetAddress.getByName(value.toString());
            super.setValue("<a>https://" + value + "</a>");
            return;
          }
          catch (Exception e)
          {
            Logger.write(Level.DEBUG,"unable to resolve text as hostname: " + value,e);
          }
        }
        super.setValue(value);
      }
    };
    link.setName(name);
    link.addListener(new Listener() {
      
      public void handleEvent(Event event)
      {
        try
        {
          new Program().handleAction(event.text);
        }
        catch (ApplicationException e)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getMessage(),StatusBarMessage.TYPE_ERROR));
        }
      }
    });
    return link;
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
    sb.append(i18n.tr("SHA256:\n{0}",myCert.getSHA256Fingerprint().replaceAll("(.{48})","$1\n")));
    sb.append("\n\n");
    sb.append(i18n.tr("SHA1:\n{0}",myCert.getSHA1Fingerprint()));
    this.fingerprint.setValue(sb.toString());
    
    /////////////////////////////////////////////////////////////////////////////
    
    /////////////////////////////////////////////////////////////////////////////
    // Ggf. Warnhinweis
    try
    {
      cert.checkValidity();
    }
    catch (CertificateExpiredException expired)
    {
      this.validity.setComment(Application.getI18n().tr("Zertifikat abgelaufen"));
      this.validity.setColor(Color.ERROR);
    }
    catch (CertificateNotYetValidException notyet)
    {
      this.validity.setComment(Application.getI18n().tr("Noch nicht gültig"));
      this.validity.setColor(Color.ERROR);
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