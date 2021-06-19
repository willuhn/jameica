/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.system.ServiceFactory;


/**
 * Initialisiert die Services der Plugins.
 * PS: Das ist ein haesslicher Klassen-Name ;)
 */
public class PluginServiceService implements Bootable
{
  private ServiceFactory factory = null;

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class<Bootable>[] depends()
  {
    return new Class[]{PluginService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    try
    {
      this.factory = new ServiceFactory();
    }
    catch (RuntimeException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Liefert die aktuelle Service-Factory.
   * @return die Service-Factory.
   */
  public ServiceFactory getServiceFactory()
  {
    return this.factory;
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
    this.factory.shutDown();
  }

}


/**********************************************************************
 * $Log: PluginServiceService.java,v $
 * Revision 1.2  2008/04/10 13:36:14  willuhn
 * @N Reihenfolge beim Laden/Initialisieren der Plugins geaendert.
 *
 * Vorher:
 *
 * 1) Plugin A: Klassen laden
 * 2) Plugn A: init()
 * 3) Plugin B: Klassen laden
 * 4) Plugn B: init()
 * 5) Plugin A: Services starten
 * 6) Plugin B: Services starten
 *
 * Nun:
 *
 * 1) Plugin A: Klassen laden
 * 2) Plugin B: Klassen laden
 * 3) Plugn A: init()
 * 4) Plugin A: Services starten
 * 5) Plugn B: init()
 * 6) Plugin B: Services starten
 *
 *
 * Vorteile:
 *
 * 1) Wenn das erste Plugin initialisiert wird, sind bereits alle Klassen geladen und der Classfinder findet alles relevante
 * 2) Wenn Plugin B auf Services von Plugin A angewiesen ist, sind diese nun bereits in PluginB.init() verfuegbar
 *
 * Revision 1.1  2008/02/13 01:04:34  willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 *
 **********************************************************************/
