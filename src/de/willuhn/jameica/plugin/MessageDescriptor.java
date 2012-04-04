/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/MessageDescriptor.java,v $
 * $Revision: 1.1 $
 * $Date: 2012/04/04 20:43:37 $
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
 * Implementierung fuer die Beschreibungen einer Message im Manifest.
 * Wird in der plugin.xml definiert. Beispiel:
 * 
 * <messaging>
 *   <message queue="meine.queue">
 *     Text der Message
 *   </message>
 * </messaging>

 */
public class MessageDescriptor
{
	private IXMLElement root	= null;
	private String data       = null;
	private String queue      = null;

  /**
   * ct.
   * @param root
   */
  public MessageDescriptor(IXMLElement root)
  {
  	this.root = root;
  }

  /**
   * Liefert den Namen der Queue, in die die Message geschickt werden soll.
   * @return Name der Queue.
   */
  public String getQueue()
  {
    if (this.queue == null)
      this.queue = this.root.getAttribute("queue",null);
    return this.queue;
  }
  
  /**
   * Liefert den zu sendenden Text.
   * @return der zu sendende Text.
   */
  public String getData()
  {
    if (this.data == null)
      this.data = root.getContent();
    return this.data;
  }
}

/**********************************************************************
 * $Log: MessageDescriptor.java,v $
 * Revision 1.1  2012/04/04 20:43:37  willuhn
 * @R Ueberfluessige Interface+XMLImpl entfernt
 * @N MessageDescriptor
 *
 **********************************************************************/