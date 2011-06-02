/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/PluginMessage.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/06/02 12:15:16 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import de.willuhn.jameica.plugin.Manifest;

/**
 * Message, die verschickt wird, wenn zur Laufzeit an einem Plugin etwas geaendert wurde.
 * Die kann z.Bsp. gesendet werden, wenn ein Plugin deinstalliert oder installiert wurde.
 */
public class PluginMessage implements Message
{
  /**
   * Die verschiedenen Events.
   */
  public static enum Event
  {
    /**
     * Plugin wurde deinstalliert.
     */
    UNINSTALLED,
    
    /**
     * Plugin wurde installiert.
     */
    INSTALLED,
    
    /**
     * Plugin wurde aktualisiert.
     */
    UPDATED,
  }
  
  private Manifest mf = null;
  private Event event = null;
  
  /**
   * ct.
   * @param mf das Manifest des Plugins.
   * @param event das ausgeloeste Event.
   */
  public PluginMessage(Manifest mf, Event event)
  {
    this.mf    = mf;
    this.event = event;
  }
  
  /**
   * Liefert das Manifest des Plugins, fuer das das Event ausgeloest wurde.
   * @return das Manifest des Plugins, fuer das das Event ausgeloest wurde.
   */
  public Manifest getManifest()
  {
    return this.mf;
  }
  
  /**
   * Liefert das ausgeloeste Event.
   * @return das ausgeloeste Event.
   */
  public Event getEvent()
  {
    return this.event;
  }
}



/**********************************************************************
 * $Log: PluginMessage.java,v $
 * Revision 1.2  2011/06/02 12:15:16  willuhn
 * @B Das Handling beim Update war noch nicht sauber
 *
 * Revision 1.1  2011-05-31 16:39:04  willuhn
 * @N Funktionen zum Installieren/Deinstallieren von Plugins direkt in der GUI unter Datei->Einstellungen->Plugins
 *
 **********************************************************************/