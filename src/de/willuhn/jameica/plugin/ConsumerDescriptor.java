/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/ConsumerDescriptor.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/06/17 15:55:18 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.plugin;

/**
 * Interface fuer die Beschreibung eines Message-Consumers.
 * Wird in der plugin.xml definiert. Beispiel:
 * 
 * <messaging>
 *   <consumer class="mein.package.MyMessageConsumer" queue="meine.queue" />
 *   <consumer class="mein.package.MyMessageConsumer" />
 * </messaging>
 */
public interface ConsumerDescriptor
{
  /**
   * Liefert den Namen der Java-Klasse des Message-Consumers.
   * @return Name der Klasse.
   * Diese muss das Interface MessageConsumer implementieren.
   */
  public String getClassname();
  
  /**
   * Liefert den Namen der Queue, in der der Message-Consumer registriert werden soll.
   * @return Name der Queue.
   */
  public String getQueue();
}



/**********************************************************************
 * $Log: ConsumerDescriptor.java,v $
 * Revision 1.1  2011/06/17 15:55:18  willuhn
 * @N Registrieren von Message-Consumern im Manifest
 *
 **********************************************************************/