/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/rmi/Attic/RemoteServiceData.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/03 18:08:06 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 **********************************************************************/
package de.willuhn.jameica.rmi;

import net.n3.nanoxml.IXMLElement;

public class RemoteServiceData extends AbstractServiceData {

  private String host;

  public RemoteServiceData(IXMLElement xml)
	{
		super(xml);
    host = xml.getAttribute("host",null);
	}

  public String getUrl()
  {
    return ("//" + host + "/" + getClassName() + "." + getName());
  }

}


/*********************************************************************
 * $Log: RemoteServiceData.java,v $
 * Revision 1.2  2004/01/03 18:08:06  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.1  2003/10/29 00:41:26  willuhn
 * *** empty log message ***
 *
 **********************************************************************/