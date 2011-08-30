/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/boxes/BoxRegistry.java,v $
 * $Revision: 1.7 $
 * $Date: 2011/08/30 16:02:23 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.boxes;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;

/**
 * Diese Klasse uebernimmt die Verwaltung der Boxen.
 */
public class BoxRegistry
{
  private static Class<Box>[] boxes = null;
  
  /**
   * Liefert eine Liste der verfuegbaren Boxen.
   * Die Liste enthaelt auch deaktivierte Boxen.
   * @return Liste der verfuegbaren Boxen.
   */
  public static synchronized List<Box> getBoxes()
  {
    List<Box> instances = new LinkedList<Box>();

    if (boxes == null)
    {
      // Klassen wurden noch nicht geladen. Das tun wir jetzt
      ClassFinder finder = Application.getClassLoader().getClassFinder();
      try
      {
        boxes = finder.findImplementors(Box.class);
        if (boxes == null || boxes.length == 0)
          throw new ClassNotFoundException();
      }
      catch (ClassNotFoundException ce)
      {
        Logger.warn("no boxes found, jameica welcome page will be empty");
        boxes = new Class[0];
      }
    }

    BeanService beanService = Application.getBootLoader().getBootable(BeanService.class);
    // Erzeugen der Instanzen.
    for (Class<Box> c:boxes)
    {
      try
      {
        instances.add(beanService.get(c));
      }
      catch (Exception e)
      {
        Logger.error("unable to load box " + c.getName() + ", skipping", e);
      }
    }
    
    // Boxen sortieren
    Collections.sort(instances);

    // Wir bereinigen die Indizes bei der Gelegenheit. Denn es kann
    // sein, dass Boxen identische Indizes haben. 
    for (int i=0;i<instances.size();++i)
    {
      instances.get(i).setIndex(i);
    }
    return instances;
  }
}


/*********************************************************************
 * $Log: BoxRegistry.java,v $
 * Revision 1.7  2011/08/30 16:02:23  willuhn
 * @N Alle restlichen Stellen, in denen Instanzen via Class#newInstance erzeugt wurden, gegen BeanService ersetzt. Damit kann jetzt quasi ueberall Dependency-Injection verwendet werden, wo Jameica selbst die Instanzen erzeugt
 *
 * Revision 1.6  2011-05-03 12:57:00  willuhn
 * @B Das komplette Ausblenden nicht-aktiver Boxen fuehrte zu ziemlichem Durcheinander in dem Dialog
 * @C Aendern der Sortier-Reihenfolge vereinfacht. Sie wird jetzt nicht mehr live sondern erst nach Klick auf "Uebernehmen" gespeichert - was fachlich ja auch richtiger ist
 *
 **********************************************************************/