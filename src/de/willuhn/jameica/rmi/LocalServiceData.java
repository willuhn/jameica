/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/rmi/Attic/LocalServiceData.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/01/03 18:08:06 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 **********************************************************************/
package de.willuhn.jameica.rmi;

import java.util.Enumeration;
import java.util.HashMap;

import net.n3.nanoxml.IXMLElement;


/**
 * Haelt die Konfigurationsdaten von lokalen Services vor.
 * @author willuhn
 */
public class LocalServiceData extends AbstractServiceData {

	private boolean shared = false;
  private HashMap initParams = new HashMap();


  /**
   * Erzeugt einen neuen Datencontainer fuer lokale Services.
   * @param xml der XML-Key.
   */
  public LocalServiceData(IXMLElement xml)
	{
		super(xml);
		Enumeration e = xml.enumerateChildren();
    while (e.hasMoreElements())
    {
      IXMLElement xe = (IXMLElement) e.nextElement();
      initParams.put(xe.getAttribute("name",""),xe.getAttribute("value",""));
    }

		String s  = xml.getAttribute("shared","false");
		shared    = ("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s));
	}

  /**
   * Prueft, ob der Service im Netzwerk freigegeben werden soll.
   * @return true, wenn er freigegeben wird, andernfalls false.
   */
  public boolean isShared()
	{
		return shared;
	}
  
  /**
   * Liefert den Wert des genannten Init-Params.
   * @param name Name des Init-Params.
   * @return Wert des Init-Params.
   */
  public String getInitParam(String name)
  {
    return (String) initParams.get(name);
  }

  /**
   * Liefert eine Hashmap mit allen Init-Params.
   * @return hashMap mit allen Init-Params.
   */
  public HashMap getInitParams()
  {
    return initParams;
  }

  /**
   * Liefert die URL unter der dieser Service via RMI freigegeben wird.
   * @return RMI URL des Services.
   */
  public String getUrl()
	{
		return ("//127.0.0.1/" + getClassName() + "." + getName());
	}

}


/*********************************************************************
 * $Log: LocalServiceData.java,v $
 * Revision 1.4  2004/01/03 18:08:06  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.3  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 * Revision 1.2  2003/11/12 00:58:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/29 00:41:27  willuhn
 * *** empty log message ***
 *
 **********************************************************************/