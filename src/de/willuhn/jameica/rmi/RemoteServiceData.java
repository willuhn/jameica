/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/rmi/Attic/RemoteServiceData.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/01/04 18:48:36 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 **********************************************************************/
package de.willuhn.jameica.rmi;

import net.n3.nanoxml.IXMLElement;

/**
 * Datencontainer zur Konfiguration eines Remote-Service.
 */
public class RemoteServiceData extends AbstractServiceData {

  private String host;

  /**
   * Erzeugt einen neuen Datencontainer fuer einen Remoteservice.
   * @param xml xml-Element mit den Config-Daten.
   */
  public RemoteServiceData(IXMLElement xml)
	{
		super(xml);
    host = xml.getAttribute("host",null);
	}

  /**
   * Liefert die URL, unter der der Service verfuegbar ist.
   * @return RMI URL des Services.
   */
  public String getUrl()
  {
    return ("//" + host + "/" + getClassName() + "." + getName());
  }

	/**
	 * Liefert den Hostnamen, auf dem der Service verfuegbar ist.
   * @return Hostname des Providers.
   */
  public String getHost()
	{
		return host;
	}
}


/*********************************************************************
 * $Log: RemoteServiceData.java,v $
 * Revision 1.3  2004/01/04 18:48:36  willuhn
 * @N config store support
 *
 * Revision 1.2  2004/01/03 18:08:06  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.1  2003/10/29 00:41:26  willuhn
 * *** empty log message ***
 *
 **********************************************************************/