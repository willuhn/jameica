/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/BeanService.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/06/28 09:57:39 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;

/**
 * Ein Service, ueber den Beans instanziiert werden.
 * Der Service ueberwacht selbst den Lifecycle der Beans ueber Annotations.
 */
public class BeanService implements Bootable
{

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class<Bootable>[] depends()
  {
    // TODO Auto-generated
    return null;
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader arg0, Bootable arg1) throws SkipServiceException
  {
    // TODO Auto-generated

  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
    // TODO Auto-generated

  }

}



/**********************************************************************
 * $Log: BeanService.java,v $
 * Revision 1.1  2011/06/28 09:57:39  willuhn
 * @N Lifecycle-Annotation aus jameica.webadmin in util verschoben
 *
 **********************************************************************/