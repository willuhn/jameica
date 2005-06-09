/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/Attic/CertificateDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/06/09 23:07:47 $
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

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
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
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent,i18n.tr("Unbekanntes Zertifikat"));
    
    if (text != null && text.length() > 0)
    {
      group.addText(text,true);
    }
    else
    {
      group.addText(i18n.tr("Sie verbinden sich mit einem für Jameica unbekannten System." +        "Möchten Sie diesem Zertifikat vertrauen?."),true);

      group.addSeparator();
      
      DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT, Application.getConfig().getLocale());

      String issuer = cert.getIssuerDN().getName();
      if (issuer != null && issuer.length() > 0)
      {
        if (issuer.indexOf(",") != -1)
        {
          String[] items = issuer.split(",");
          group.addLabelPair(i18n.tr("Ausgestellt von"), new LabelInput(items[0]));
          for (int i=1;i<items.length;++i)
          {
            group.addLabelPair("", new LabelInput(items[i]));
          }
        }
        else
        {
          group.addLabelPair(i18n.tr("Ausgestellt von"), new LabelInput(issuer));
        }
      }
      
      String subject = cert.getSubjectDN().getName();
      if (subject != null && subject.length() > 0)
      {
        if (subject.indexOf(",") != -1)
        {
          String[] items = subject.split(",");
          group.addLabelPair(i18n.tr("Ausgestellt für"), new LabelInput(items[0]));
          for (int i=1;i<items.length;++i)
          {
            group.addLabelPair("", new LabelInput(items[i]));
          }
        }
        else
        {
          group.addLabelPair(i18n.tr("Ausgestellt für"), new LabelInput(subject));
        }
      }

      group.addLabelPair(i18n.tr("Gültig von"), new LabelInput(df.format(cert.getNotBefore())));
      group.addLabelPair(i18n.tr("Gültig bis"), new LabelInput(df.format(cert.getNotAfter())));
      group.addLabelPair(i18n.tr("Seriennummer"), new LabelInput(cert.getSerialNumber().toString()));
      group.addLabelPair(i18n.tr("Typ"), new LabelInput(cert.getType()));

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
 * Revision 1.1  2005/06/09 23:07:47  web0
 * @N certificate checking activated
 *
 **********************************************************************/