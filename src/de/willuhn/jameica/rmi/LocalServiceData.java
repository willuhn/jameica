/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/rmi/Attic/LocalServiceData.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/10/29 00:41:27 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 **********************************************************************/
package de.willuhn.jameica.rmi;

import de.bb.util.XmlFile;

public class LocalServiceData extends AbstractServiceData {

	private boolean shared = false;
  private String param;

  public LocalServiceData(XmlFile xml, String key)
	{
		super(xml,key);
    param     = xml.getString(key,"param",null);

		String s  = xml.getString(key,"shared","false");
		shared    = ("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s));
	}

  public boolean isShared()
	{
		return shared;
	}
  
  public String getInitParam()
  {
    return param;
  }

  public String getUrl()
	{
		return ("//127.0.0.1/" + getClassName() + "." + getName());
	}

}


/*********************************************************************
 * $Log: LocalServiceData.java,v $
 * Revision 1.1  2003/10/29 00:41:27  willuhn
 * *** empty log message ***
 *
 **********************************************************************/