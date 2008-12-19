/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/CertificateList.java,v $
 * $Revision: 1.17 $
 * $Date: 2008/12/19 12:16:02 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.CertificateDetailDialog;
import de.willuhn.jameica.gui.dialogs.CertificateTrustDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.internal.action.CertificateImport;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.security.Certificate;
import de.willuhn.jameica.security.Principal;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Eine vorkonfektionierte Tabelle, welche die Liste der installierten Zertifikate anzeigt.
 */
public class CertificateList extends TablePart
{
  private final static Settings mySettings = new Settings(CertificateList.class);

  /**
   * ct.
   */
  public CertificateList()
  {
    super(init(),new Open());
    addColumn(Application.getI18n().tr("Ausgestellt für"),"name");
    addColumn(Application.getI18n().tr("Organisation"),"organization");
    addColumn(Application.getI18n().tr("OU"),"ou");
    addColumn(Application.getI18n().tr("Aussteller"),"issuer");
    addColumn(Application.getI18n().tr("Gültig von"),"datefrom");
    addColumn(Application.getI18n().tr("Gültig bis"),"dateto");
    addColumn(Application.getI18n().tr("Seriennummer"),"serial");
    this.setMulti(false);
    this.setSummary(false);
    ContextMenu menu = new ContextMenu();
    menu.addItem(new CheckedContextMenuItem(Application.getI18n().tr("Öffnen..."),new Open(),"document-open.png"));
    menu.addItem(new CheckedContextMenuItem(Application.getI18n().tr("Löschen..."), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null || !(context instanceof CertObject))
          throw new ApplicationException(Application.getI18n().tr("Bitte wählen Sie das zu löschende Zertifikat aus"));
        
        final X509Certificate c = ((CertObject)context).cert;
        CertificateTrustDialog d = new CertificateTrustDialog(CertificateTrustDialog.POSITION_CENTER,c);
        d.setText(Application.getI18n().tr("Sind Sie sicher, dass Sie dieses Zertifikat aus dem Stammspeicher löschen wollen?"));
        try
        {
          Boolean b = (Boolean) d.open();
          if (b != null && b.booleanValue())
          {
            Application.getSSLFactory().removeTrustedCertificate(c);
            
            // jetzt noch aus der Tabelle loeschen
            removeItem((CertObject)context);
          }
        }
        catch (OperationCanceledException oce)
        {
          Logger.info(Application.getI18n().tr("Vorgang abgebrochen"));
          // ignore
        }
        catch (Exception e)
        {
          Logger.error("error while deleting certificate",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Löschen des Zertifikats."),StatusBarMessage.TYPE_ERROR));
        }
      }
    },"user-trash-full.png")
    {
      /**
       * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
       */
      public boolean isEnabledFor(Object o)
      {
        try
        {
          if (Application.getSSLFactory().getSystemCertificate().equals(((CertObject)o).cert))
            return false;
        }
        catch (Exception e)
        {
          Logger.error("unable to check for system certificate",e);
        }
        return super.isEnabledFor(o);
      }
    });
    menu.addItem(ContextMenuItem.SEPARATOR);
    menu.addItem(new CheckedContextMenuItem(Application.getI18n().tr("Exportieren..."),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          X509Certificate cert = ((CertObject) context).cert;

          Certificate myCert = new Certificate(cert);
          String s = myCert.getSubject().getAttribute(Principal.COMMON_NAME);
          String s2 = myCert.getSubject().getAttribute(Principal.ORGANIZATIONAL_UNIT);

          FileDialog fd = new FileDialog(GUI.getShell(),SWT.SAVE);
          fd.setText(Application.getI18n().tr("Bitte geben Sie das Verzeichnis an, in dem Sie das Zertifikat speichern möchten"));
          if (s2 != null && s2.length() > 0)
            fd.setFileName(s + "-" + s2 + ".crt");
          else
            fd.setFileName(s + ".crt");
          fd.setFilterPath(mySettings.getString("lastdir",System.getProperty("user.home")));
          String target = fd.open();
          if (target == null)
            return;
          File f = new File(target);
          if (f.exists())
          {
            YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
            d.setTitle(Application.getI18n().tr("Überschreiben?"));
            d.setText(Application.getI18n().tr("Datei existiert bereits. Überschreiben?"));
            Boolean b = (Boolean) d.open();
            if (!b.booleanValue())
              return;
          }
          OutputStream os = null;
          try
          {
            os = new FileOutputStream(f);
            os.write(cert.getEncoded());
            os.flush();
            mySettings.setAttribute("lastdir",f.getParent());
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Zertifikate exportiert"), StatusBarMessage.TYPE_SUCCESS));
          }
          finally
          {
            if (os != null)
              os.close();
          }
        }
        catch (Exception e)
        {
          Logger.error("unable to export certificate",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Export des Zertifikates"), StatusBarMessage.TYPE_ERROR));
        }
      }
    },"document-save.png"));
    menu.addItem(new ContextMenuItem(Application.getI18n().tr("Importieren..."),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new CertificateImport().handleAction(context);
        GUI.startView(GUI.getCurrentView().getClass(),GUI.getCurrentView().getCurrentObject());
      }
    },"document-open.png"));

    setFormatter(new TableFormatter() {
    
      public void format(TableItem item)
      {
        if (item == null || item.getData() == null)
          return;
        try
        {
          CertObject o = (CertObject) item.getData();
          if (Application.getSSLFactory().getSystemCertificate().equals(o.cert))
            item.setForeground(Color.COMMENT.getSWTColor());
          else
            item.setForeground(Color.WIDGET_FG.getSWTColor());
        }
        catch (Exception e)
        {
          Logger.error("unable to check for system certificate",e);
        }
      }
    
    });
    this.setContextMenu(menu);
  }

  private static GenericIterator init()
  {
    try
    {
      X509Certificate[] list = Application.getSSLFactory().getTrustedCertificates();
      ArrayList al = new ArrayList();
      for (int i=0;i<list.length;++i)
      {
        al.add(new CertObject(list[i]));
      }
      
      // System-Zertifikat noch hinzufuegen
      al.add(new CertObject(Application.getSSLFactory().getSystemCertificate()));
      return PseudoIterator.fromArray((CertObject[])al.toArray(new CertObject[al.size()]));
    }
    catch (Exception e)
    {
      Logger.error("error while loading certificate list",e);
      try
      {
        return PseudoIterator.fromArray(new GenericObject[]{});
      }
      catch (Exception e2)
      {
        Logger.error("error while loading dummy list, useless",e2);
        return null;
      }
    }
  }

  private static class CertObject implements GenericObject
  {

    private X509Certificate cert = null;
    private Certificate myCert = null;
    
    private DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT,Application.getConfig().getLocale());

    private CertObject(X509Certificate cert)
    {
      this.cert = cert;
      this.myCert = new Certificate(cert);
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      
      if ("name".equals(arg0))
      {
        String s = myCert.getSubject().getAttribute(Principal.COMMON_NAME);
        if (s == null || s.length() == 0)
        {
          s = this.cert.getSubjectDN().getName();
          if (s != null && s.length() > 40)
            s = s.substring(0,39) + "...";
          return s;
        }
        return s;
      }
      if ("issuer".equals(arg0))
      {
        String s = myCert.getIssuer().getAttribute(Principal.COMMON_NAME);
        if (s == null || s.length() == 0)
        {
          s = this.cert.getIssuerDN().getName();
          if (s != null && s.length() > 40)
            s = s.substring(0,39) + "...";
          
        }
        return s;
      }
      if ("serial".equals(arg0))
        return cert.getSerialNumber().toString();
      if ("organization".equals(arg0))
        return myCert.getSubject().getAttribute(Principal.ORGANIZATION);
      if ("ou".equals(arg0))
        return myCert.getSubject().getAttribute(Principal.ORGANIZATIONAL_UNIT);
      if ("datefrom".equals(arg0))
        return df.format(cert.getNotBefore());
      if ("dateto".equals(arg0))
        return df.format(cert.getNotAfter());
      if ("fingerprint".equals(arg0))
      {
        try
        {
          return myCert.getMD5Fingerprint();
        }
        catch (Exception e)
        {
          Logger.error("error while reading certificate fingerprint from certficate " + cert.getSubjectDN().getName());
          return Application.getI18n().tr("MD5-Fingerprint nicht lesbar");
        }
      }

      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]
      {
        "name",
        "issuer",
        "serial",
        "datefrom",
        "dateto",
        "fingerprint",
        "organization",
        "ou"
      };
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return (String) getAttribute("serial") + (String) getAttribute("fingerprint");
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "name";
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject arg0) throws RemoteException
    {
      if (arg0 == null)
        return false;
      return this.getID().equals(arg0.getID());
    }
  }
  
  /**
   * Action zum Oeffnen des Zertifikats.
   */
  private static class Open implements Action
  {
    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    public void handleAction(Object context) throws ApplicationException
    {
      if (context == null || !(context instanceof CertObject))
        return;
      try
      {
        CertObject c = (CertObject) context;
        CertificateDetailDialog d = new CertificateDetailDialog(CertificateDetailDialog.POSITION_CENTER,c.cert);
        d.open();
      }
      catch (Exception e)
      {
        Logger.error("error while displaying certificate",e);
      }
    }
  }
}


/**********************************************************************
 * $Log: CertificateList.java,v $
 * Revision 1.17  2008/12/19 12:16:02  willuhn
 * @N Mehr Icons
 * @C Reihenfolge der Contextmenu-Eintraege vereinheitlicht
 *
 * Revision 1.16  2007/06/21 18:32:54  willuhn
 * @B ClassCastException
 *
 * Revision 1.15  2007/04/02 12:43:03  willuhn
 * @B CertificateList.settings hides TablePart.settings
 *
 * Revision 1.14  2006/11/16 23:46:03  willuhn
 * @N launch type in cert creation
 * @N new row in cert list
 *
 * Revision 1.13  2006/11/15 00:30:44  willuhn
 * @C Bug 326
 *
 * Revision 1.12  2006/11/13 00:40:24  willuhn
 * @N Anzeige des System-Zertifikates
 * @N Export von Zertifikaten
 *
 * Revision 1.11  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
 * Revision 1.10  2005/07/24 17:00:20  web0
 * *** empty log message ***
 *
 * Revision 1.9  2005/07/20 16:23:10  web0
 * @B splitting x.500 name
 *
 * Revision 1.8  2005/06/27 21:53:51  web0
 * @N ability to import own certifcates
 *
 * Revision 1.7  2005/06/27 15:35:51  web0
 * @N ability to store last table order
 *
 * Revision 1.6  2005/06/24 14:55:56  web0
 * *** empty log message ***
 *
 * Revision 1.5  2005/06/15 17:51:31  web0
 * @N Code zum Konfigurieren der Service-Bindings
 *
 * Revision 1.4  2005/06/15 16:10:57  web0
 * @B javadoc fixes
 *
 * Revision 1.3  2005/06/13 12:13:37  web0
 * @N Certificate-Code completed
 *
 * Revision 1.2  2005/06/10 22:59:35  web0
 * @N Loeschen von Zertifikaten
 *
 * Revision 1.1  2005/06/10 22:13:09  web0
 * @N new TabGroup
 * @N extended Settings
 *
 **********************************************************************/