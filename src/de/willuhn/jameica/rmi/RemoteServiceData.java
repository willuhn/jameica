/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/rmi/Attic/RemoteServiceData.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/10/29 00:41:26 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 **********************************************************************/
package de.willuhn.jameica.rmi;

import de.bb.util.XmlFile;

public class RemoteServiceData extends AbstractServiceData {

  private String host;

  public RemoteServiceData(XmlFile xml, String key)
	{
		super(xml,key);
    host = xml.getString(key,"host",null);
	}

  public String getUrl()
  {
    return ("//" + host + "/" + getClassName() + "." + getName());
  }

}


/*********************************************************************
 * $Log: RemoteServiceData.java,v $
 * Revision 1.1  2003/10/29 00:41:26  willuhn
 * *** empty log message ***
 *
 **********************************************************************/