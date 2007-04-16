/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/ServiceDescriptorXml.java,v $
 * $Revision: 1.4 $
 * $Date: 2007/04/16 13:19:33 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.plugin;

import java.util.ArrayList;
import java.util.StringTokenizer;

import net.n3.nanoxml.IXMLElement;

/**
 * XML-basierte Implementierung eines Service-Deskriptors.
 */
public class ServiceDescriptorXml implements ServiceDescriptor
{

	private IXMLElement root 	= null;
	private String name 			= null;
	private String className 	= null;
	private String[] depends	= null;

  /**
   * ct.
   * @param root
   */
  public ServiceDescriptorXml(IXMLElement root)
  {
  	this.root = root;
  }

  /**
   * @see de.willuhn.jameica.plugin.ServiceDescriptor#getName()
   */
  public String getName()
  {
  	if (name != null)
  		return name;
    name = root.getAttribute("name",null);
    return name;
  }

  /**
   * @see de.willuhn.jameica.plugin.ServiceDescriptor#getClassname()
   */
  public String getClassname()
  {
  	if (className != null)
  		return className;
		className = root.getAttribute("class",null);
		return className;
  }

  /**
   * @see de.willuhn.jameica.plugin.ServiceDescriptor#autostart()
   */
  public boolean autostart()
  {
  	String s = root.getAttribute("autostart","true");
  	return "true".equalsIgnoreCase(s);
  }

  /**
   * @see de.willuhn.jameica.plugin.ServiceDescriptor#depends()
   */
  public String[] depends()
  {
  	if (depends != null)
  		return depends;

		String s = root.getAttribute("depends",null);
		if (s == null || s.length() == 0)
			return new String[0];

    s = s.replaceAll("\n|\r","");
		StringTokenizer st = new StringTokenizer(s,",");
    ArrayList l = new ArrayList();
		while (st.hasMoreTokens())
		{
		  s = st.nextToken();
      if (s == null || s.length() == 0)
        continue;
			l.add(s.trim());
		}
    this.depends = (String[]) l.toArray(new String[l.size()]);
		return depends;
  }

  /**
   * @see de.willuhn.jameica.plugin.ServiceDescriptor#share()
   */
  public boolean share()
  {
    String s = root.getAttribute("share","true");
    return "true".equalsIgnoreCase(s);
  }

}


/**********************************************************************
 * $Log: ServiceDescriptorXml.java,v $
 * Revision 1.4  2007/04/16 13:19:33  willuhn
 * @C return empty list instead of null
 *
 * Revision 1.3  2005/08/18 23:57:20  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/07/14 22:58:36  web0
 * *** empty log message ***
 *
 * Revision 1.1  2004/12/21 01:08:01  willuhn
 * @N new service configuration system in plugin.xml with auostart and dependencies
 *
 **********************************************************************/