/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/ExtensionDescriptorXml.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/05/27 17:31:46 $
 * $Author: web0 $
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

		String s = root.getAttribute("extends",null);
		if (s == null || s.length() == 0)
			return null;

		StringTokenizer st = new StringTokenizer(s,",");
		ids = new String[st.countTokens()];
		for (int i=0;i<ids.length;++i)
		{
			ids[i] = st.nextToken();
		}
		return ids;
  }

}

/**********************************************************************
 * $Log: ExtensionDescriptorXml.java,v $
 * Revision 1.1  2005/05/27 17:31:46  web0
 * @N extension system
 *
 **********************************************************************/