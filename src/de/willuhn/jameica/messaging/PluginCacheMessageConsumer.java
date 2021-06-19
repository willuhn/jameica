/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.willuhn.jameica.messaging.PluginMessage.Event;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Wird ueber die Installation und Deinstallation von Plugins benachrichtigt und
 * haelt eine Liste mit dem aktuellen Stand der Plugins.
 */
public class PluginCacheMessageConsumer implements MessageConsumer
{
  // Wir cachen die Plugins hier statisch, damit auch die frisch installierten aber noch
  // nicht aktivierten hier angezeigt werden - auch dann, wenn wir die Seite mal verlassen
  // LinkedHashMap, damit die Reihenfolge erhalten bleibt
  private static Map<String,Manifest> cache = null;
  
  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{PluginMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    PluginMessage m = (PluginMessage) message;

    Manifest mf = m.getManifest();
    Event event = m.getEvent();
    
    // Neu einfuegen. Aber nur, wenn es installiert/aktualisiert wurde
    switch (event)
    {
      case INSTALLED:
      case UPDATED:
        // Zum Cache tun, ggf. ueberschreiben
        getCache().put(mf.getName(),mf);
        
        break;
        
      case UNINSTALLED:
        // Aus dem Cache werfen
        getCache().remove(mf.getName());
        
        break;
        
      default:
        Logger.warn("unknown event " + event);
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return true;
  }
  
  /**
   * Liefert den Cache der aktuell installierten Plugins.
   * @return der Cache der aktuell installierten Plugins.
   */
  public static synchronized Map<String,Manifest> getCache()
  {
    // initial fuellen. Alles andere sind dann nur noch Updates
    if (cache == null)
    {
      cache = new LinkedHashMap<>();
      List<Manifest> mfs = Application.getPluginLoader().getManifests();
      for (Manifest m:mfs)
      {
        cache.put(m.getName(),m);
      }
    }
    return cache;
  }
}



/**********************************************************************
 * $Log: PluginCacheMessageConsumer.java,v $
 * Revision 1.2  2011/07/13 14:04:28  willuhn
 * @C Auch inaktive Plugins anzeigen - dann koennen sie wenigstens deinstalliert werden
 *
 * Revision 1.1  2011-06-02 13:02:26  willuhn
 * @N MessageConsumer fuer die Plugin-Install-Benachrichtigungen ausgelagert, damit die Messages auch von jameica.update empfangen werden koennen
 *
 **********************************************************************/