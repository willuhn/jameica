/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Config.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/10/23 21:49:46 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import de.bb.util.XmlFile;

public class Config
{

  private XmlFile xml = new XmlFile();

  private Hashtable remoteServices  = new Hashtable();
  private Hashtable localServices   = new Hashtable();

  private int rmiPort;

  protected Config()
  {
    init();
  }

  private void init()
  {
    InputStream file = getClass().getResourceAsStream("/config.xml");
    xml.read(file);
    
    readServices();
  }

  private void readServices()
  {
    Enumeration e = xml.getSections("/config/services/").elements();

    Application.getLog().info("loading services configuration");

    while (e.hasMoreElements())
    {
      String key = (String) e.nextElement();
      String name = xml.getString(key,"name",null);
      
      if (key.startsWith("/config/services/remoteservice"))
      {
        remoteServices.put(name, new RemoteServiceData(xml,key));
      }
      else if (key.startsWith("/config/services/localservice"))
      {
        localServices.put(name, new LocalServiceData(xml,key));
      }

    }
    Application.getLog().info("done");

		try {
			rmiPort = Integer.parseInt(xml.getContent("/config/services/localport"));
		}
		catch (NumberFormatException nfe)
		{
			rmiPort = 1099;
  	}
  }

  public LocalServiceData getLocalService(String name)
  {
    return (LocalServiceData) localServices.get(name);
  }
  
  public RemoteServiceData getRemoteService(String name)
  {
    return (RemoteServiceData) remoteServices.get(name);
  }


  public Enumeration getLocalServiceNames()
  {
    return localServices.keys();
  }

  public Enumeration getRemoteServiceNames()
  {
    return remoteServices.keys();
  }

  public int getLocalRmiPort()
  {
  	return rmiPort;
  }
}


/*********************************************************************
 * $Log: Config.java,v $
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
