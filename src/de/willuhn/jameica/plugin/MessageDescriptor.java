/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/MessageDescriptor.java,v $
 * $Revision: 1.5 $
 * $Date: 2012/04/05 23:31:25 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.plugin;

import java.util.HashMap;
import java.util.Map;

import de.willuhn.jameica.services.VelocityService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
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
 * 
 */
public class MessageDescriptor
{
  private Manifest mf       = null;
	private IXMLElement root	= null;
	private String data       = null;
	private String queue      = null;

  /**
   * ct.
   * @param mf
   * @param root
   */
  public MessageDescriptor(Manifest mf, IXMLElement root)
  {
    this.mf = mf;
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
    if (this.data != null) // allready loaded?
      return this.data;
    
    this.data = root.getContent();
    
    if (this.data == null) // do we have content?
      return this.data;
    
    this.data = this.data.trim();
    try
    {
      VelocityService s = Application.getBootLoader().getBootable(VelocityService.class);
      Map<String,Object> ctx = new HashMap<String,Object>();
      ctx.put("manifest",this.mf);
      this.data = s.merge(this.data,ctx);
    }
    catch (Exception e)
    {
      Logger.error("unable to resolve " + this.data + " - leaving unchanged",e);
    }
    return this.data;
  }
}

/**********************************************************************
 * $Log: MessageDescriptor.java,v $
 * Revision 1.5  2012/04/05 23:31:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2012/04/05 23:31:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2012/04/05 23:30:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2012/04/05 23:25:46  willuhn
 * @N Support fuer das Senden von Messages direkt aus dem Manifest heraus (wurde zum Registrieren von Javascripts aus Java-losen Plugins heraus benoetigt)
 *
 * Revision 1.1  2012/04/04 20:43:37  willuhn
 * @R Ueberfluessige Interface+XMLImpl entfernt
 * @N MessageDescriptor
 *
 **********************************************************************/