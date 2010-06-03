/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/ExtensionDescriptorXml.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/06/03 13:52:55 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.plugin;

import java.util.StringTokenizer;

import net.n3.nanoxml.IXMLElement;

/**
 * XML-basierte Implementierung eines Extension-Deskriptors.
 */
public class ExtensionDescriptorXml implements ExtensionDescriptor
{

	private IXMLElement root 	= null;
	private String className 	= null;
	private String[] ids    	= null;
	private String[] plugins  = null;

  /**
   * ct.
   * @param root
   */
  public ExtensionDescriptorXml(IXMLElement root)
  {
  	this.root = root;
  }

  /**
   * @see de.willuhn.jameica.plugin.ExtensionDescriptor#getClassname()
   */
  public String getClassname()
  {
  	if (className != null)
  		return className;
		className = root.getAttribute("class",null);
		return className;
  }

  /**
   * @see de.willuhn.jameica.plugin.ExtensionDescriptor#getExtendableIDs()
   */
  public String[] getExtendableIDs()
  {
  	if (ids != null)
  		return ids;

  	ids = parseArray("extends");
		return ids;
  }

  /**
   * @see de.willuhn.jameica.plugin.ExtensionDescriptor#getRequiredPlugins()
   */
  public String[] getRequiredPlugins()
  {
    if (plugins != null)
      return plugins;
    
    plugins = parseArray("requires");
    return plugins;
  }
  
  /**
   * Parst den Wert des genannten XML-Elements als Array aus einer
   * kommaseparierten Liste von Werten.
   * @param element Name des XML-Elements.
   * @return Liste der Werte oder NULL.
   */
  private String[] parseArray(String element)
  {
    if (element == null || element.length() == 0)
      return null;

    String s = root.getAttribute(element,null);
    if (s == null || s.length() == 0)
      return null;

    StringTokenizer st = new StringTokenizer(s,",");
    String[] values = new String[st.countTokens()];
    for (int i=0;i<values.length;++i)
    {
      values[i] = st.nextToken();
    }
    return values;
  }

}

/**********************************************************************
 * $Log: ExtensionDescriptorXml.java,v $
 * Revision 1.2  2010/06/03 13:52:55  willuhn
 * @N Neues optionales Attribut "requires", damit Extensions nur dann registriert werden, wenn ein benoetigtes Plugin installiert ist
 *
 * Revision 1.1  2005/05/27 17:31:46  web0
 * @N extension system
 *
 **********************************************************************/