/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/CertificateList.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/06/10 22:13:09 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.rmi.RemoteException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.security.Certificate;
import de.willuhn.jameica.security.Principal;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Eine vorkonfektionierte Tabelle, welche die Liste der installierten Zertifikate anzeigt.
 */
public class CertificateList extends TablePart
{

  /**
   * @param list
   * @param action
   */
  public CertificateList()
  {
    super(init(),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null || !(context instanceof CertObject))
          return;
        // TODO hier weiter
      }
    });
    addColumn(Application.getI18n().tr("Ausgestellt für"),"name");
    addColumn(Application.getI18n().tr("Aussteller"),"issuer");
    addColumn(Application.getI18n().tr("Gültig von"),"datefrom");
    addColumn(Application.getI18n().tr("Gültig bis"),"dateto");
    addColumn(Application.getI18n().tr("Seriennummer"),"serial");
    this.setMulti(true);
    this.disableSummary();
  }

  private static GenericIterator init()
  {
    try
    {
      X509Certificate[] list = Application.getSSLFactory().getInstalledCertificates();
      GenericObject[] objects = new GenericObject[list.length];
      for (int i=0;i<list.length;++i)
      {
        objects[i] = new CertObject(list[i]);
      }
      return PseudoIterator.fromArray(objects);
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
        return myCert.getSubject().getAttribute(Principal.COMMON_NAME);
      if ("issuer".equals(arg0))
        return myCert.getIssuer().getAttribute(Principal.COMMON_NAME);
      if ("serial".equals(arg0))
        return cert.getSerialNumber().toString();
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
        "fingerprint"
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
}


/**********************************************************************
 * $Log: CertificateList.java,v $
 * Revision 1.1  2005/06/10 22:13:09  web0
 * @N new TabGroup
 * @N extended Settings
 *
 **********************************************************************/