/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 * 
 * GPLv2
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.X509TrustManager;

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
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.internal.action.CertificateImport;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
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
   * @throws Exception
   */
  public CertificateList() throws Exception
  {
    this(Application.getSSLFactory().getTrustManager(),true);
  }

  /**
   * ct.
   * @param trustManager der TrustManager, aus dem die Zertifikate geladen werden sollen.
   * @param changable true, wenn Zertifikate importiert/exportiert werden koennen.
   */
  public CertificateList(X509TrustManager trustManager, final boolean changable)
  {
    super(init(trustManager),new Open());
    
    addColumn(Application.getI18n().tr("Ausgestellt für"),"name");
    addColumn(Application.getI18n().tr("Organisation"),"organization");
    addColumn(Application.getI18n().tr("OU"),"ou");
    addColumn(Application.getI18n().tr("Aussteller"),"issuer");
    addColumn(Application.getI18n().tr("Gültig von"),"datefrom",new DateFormatter());
    addColumn(Application.getI18n().tr("Gültig bis"),"dateto",new DateFormatter());
    addColumn(Application.getI18n().tr("Seriennummer"),"serial");
    this.setMulti(false);
    this.removeFeature(FeatureSummary.class);
    
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
            item.setForeground(Color.FOREGROUND.getSWTColor());
        }
        catch (Exception e)
        {
          Logger.error("unable to check for system certificate",e);
        }
      }
    
    });
    
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
            removeItem(context);
          }
        }
        catch (OperationCanceledException oce)
        {
          Logger.info(Application.getI18n().tr("Vorgang abgebrochen"));
          return;
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
        if (!changable)
          return false;
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
        catch (OperationCanceledException oce)
        {
          Logger.info(oce.getMessage());
          return;
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
    },"document-open.png")
    {
      public boolean isEnabledFor(Object o)
      {
        return changable && super.isEnabledFor(o);
      }
    });
    this.setContextMenu(menu);
  }

  /**
   * @return
   * @param trustManager der Trustmanager mit den Zertifikaten.
   */
  private static GenericIterator init(X509TrustManager trustManager)
  {
    try
    {
      X509Certificate[] list = trustManager.getAcceptedIssuers();
      List<CertObject> al = new ArrayList<CertObject>();
      for (int i=0;i<list.length;++i)
      {
        al.add(new CertObject(list[i]));
      }
      
      if (trustManager == Application.getSSLFactory().getTrustManager())
      {
        // System-Zertifikat noch hinzufuegen
        al.add(new CertObject(Application.getSSLFactory().getSystemCertificate()));
      }
      Collections.sort(al);
      return PseudoIterator.fromArray(al.toArray(new CertObject[al.size()]));
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

  private static class CertObject implements GenericObject, Comparable
  {

    private X509Certificate cert = null;
    private Certificate myCert = null;
    
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
          s = myCert.getIssuer().getAttribute(Principal.ORGANIZATION);
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
        return cert.getNotBefore();
      if ("dateto".equals(arg0))
        return cert.getNotAfter();

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

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o)
    {
      if (!(o instanceof CertObject))
        return -1;

      try
      {
        CertObject other = (CertObject) o;
        String n1 = (String) this.getAttribute("name");
        String n2 = (String) other.getAttribute("name");
        return n1.compareTo(n2);
      }
      catch (Exception e)
      {
        Logger.error("unable to compare certs",e);
      }
      return 0;
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
      catch (OperationCanceledException oce)
      {
        Logger.info(oce.getMessage());
        return;
      }
      catch (Exception e)
      {
        Logger.error("error while displaying certificate",e);
      }
    }
  }
}
