/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/ConsumerDescriptorXml.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/06/17 15:55:18 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.plugin;

import net.n3.nanoxml.IXMLElement;

/**
 * XML-basierte Implementierung eines Consumer-Deskriptors.
 */
public class ConsumerDescriptorXml implements ConsumerDescriptor
{
	private IXMLElement root 	= null;
	private String className 	= null;
	private String queue      = null;

  /**
   * ct.
   * @param root
   */
  public ConsumerDescriptorXml(IXMLElement root)
  {
  	this.root = root;
  }

  /**
   * @see de.willuhn.jameica.plugin.ConsumerDescriptor#getClassname()
   */
  public String getClassname()
  {
  	if (className == null)
  		className = root.getAttribute("class",null);
		return className;
  }

  /**
   * @see de.willuhn.jameica.plugin.ConsumerDescriptor#getQueue()
   */
  public String getQueue()
  {
    if (queue == null)
      queue = root.getAttribute("queue",null);
    return queue;
  }
}

/**********************************************************************
 * $Log: ConsumerDescriptorXml.java,v $
 * Revision 1.1  2011/06/17 15:55:18  willuhn
 * @N Registrieren von Message-Consumern im Manifest
 *
 **********************************************************************/