/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.plugin;

import net.n3.nanoxml.IXMLElement;

/**
 * Implementierung fuer die Beschreibungen eines Message-Consumers im Manifest.
 * Wird in der plugin.xml definiert. Beispiel:
 * 
 * <messaging>
 *   <consumer class="mein.package.MyMessageConsumer" queue="meine.queue" />
 *   <consumer class="mein.package.MyMessageConsumer" />
 * </messaging>

 */
public class ConsumerDescriptor
{
	private IXMLElement root 	= null;
	private String className 	= null;
	private String queue      = null;

  /**
   * ct.
   * @param root
   */
  public ConsumerDescriptor(IXMLElement root)
  {
  	this.root = root;
  }

  /**
   * Liefert den Namen der Java-Klasse des Message-Consumers.
   * @return Name der Klasse.
   * Diese muss das Interface MessageConsumer implementieren.
   */
  public String getClassname()
  {
  	if (className == null)
  		className = root.getAttribute("class",null);
		return className;
  }

  /**
   * Liefert den Namen der Queue, in der der Message-Consumer registriert werden soll.
   * @return Name der Queue.
   */
  public String getQueue()
  {
    if (queue == null)
      queue = root.getAttribute("queue",null);
    return queue;
  }
}

/**********************************************************************
 * $Log: ConsumerDescriptor.java,v $
 * Revision 1.2  2012/04/04 20:43:37  willuhn
 * @R Ueberfluessige Interface+XMLImpl entfernt
 * @N MessageDescriptor
 *
 * Revision 1.1  2011-06-17 15:55:18  willuhn
 * @N Registrieren von Message-Consumern im Manifest
 *
 **********************************************************************/