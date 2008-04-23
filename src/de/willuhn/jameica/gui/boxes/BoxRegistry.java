/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/boxes/BoxRegistry.java,v $
 * $Revision: 1.5 $
 * $Date: 2008/04/23 13:20:56 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.boxes;

import java.util.ArrayList;
import java.util.Collections;

import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;

/**
 * Diese Klasse uebernimmt die Verwaltung der Boxen.
 */
public class BoxRegistry
{
  private static Class[] boxes = null;
  
  /**
   * Liefert eine Liste der verfuegbaren Boxen.
   * Die Liste enthaelt auch deaktivierte Boxen.
   * @return Liste der verfuegbaren Boxen.
   */
  public static synchronized Box[] getBoxes()
  {
    if (boxes == null)
    {
      // Klassen wurden noch nicht geladen. Das tun wir jetzt
      ClassFinder finder = Application.getClassLoader().getClassFinder();
      try {
        boxes = finder.findImplementors(Box.class);
        if (boxes == null || boxes.length == 0)
          throw new ClassNotFoundException();
      }
      catch (ClassNotFoundException ce)
      {
        Logger.warn("no boxes found, jameica welcome page will be empty");
        boxes = new Class[0];
        return new Box[0];
      }
    }

    // Erzeugen der Instanzen.
    ArrayList instances = new ArrayList();
    for (int i=0;i<boxes.length;++i)
    {
      try
      {
        final Box current = (Box) boxes[i].newInstance();
        instances.add(current);
      }
      catch (Exception e)
      {
        Logger.error("unable to load box " + boxes[i].getName() + ", skipping", e);
      }
    }
    
    // Boxen sortieren
    Collections.sort(instances);
    return (Box[]) instances.toArray(new Box[instances.size()]);
  }
  
  /**
   * Schiebt die Box einen Index nach unten.
   * @param box die zu verschiebende Box.
   * @return true, wenn die Box verschoben wurde.
   */
  public static synchronized boolean up(Box box)
  {
    Box[] boxes = getBoxes();

    int index = box.getIndex();
    if (index <= 0)
      return false; // Die Box ist schon ganz oben.
      
    // Wir nehmen die Box oben drueber und tauschen die Positionen
    boxes[index]     = boxes[index - 1];
    boxes[index - 1] = box;
  
    // Indizes speichern
    for (int i=0;i<boxes.length;++i)
    {
      Box b = boxes[i];
      b.setIndex(i);
    }
    return true;
  }
  
  /**
   * Schiebt die Box einen Index nach oben.
   * @param box die zu verschiebende Box.
   * @return true, wenn die Box verschoben wurde.
   */
  public static synchronized boolean down(Box box)
  {
    Box[] boxes = getBoxes();

    int index = box.getIndex();
      
    if (index >= (boxes.length - 1))
      return false; // Die Box ist schon ganz unten

    // Wir nehmen die Box unten drunter und tauschen die Positionen
    boxes[index]     = boxes[index + 1];
    boxes[index + 1] = box;

    // Indizes speichern
    for (int i=0;i<boxes.length;++i)
    {
      Box b = boxes[i];
      b.setIndex(i);
    }
    return true;
  }
}


/*********************************************************************
 * $Log: BoxRegistry.java,v $
 * Revision 1.5  2008/04/23 13:20:56  willuhn
 * @B Bug 588
 *
 * Revision 1.4  2006/08/02 09:24:58  willuhn
 * @B Neusetzen der Indizes beim Laden der Boxen
 *
 * Revision 1.3  2006/08/02 09:12:02  willuhn
 * @B Sortierung der Boxen auf der Startseite
 *
 * Revision 1.2  2006/06/30 13:51:34  willuhn
 * @N Pluginloader Redesign in HEAD uebernommen
 *
 * Revision 1.1  2006/06/29 23:10:01  willuhn
 * @N Box-System aus Hibiscus in Jameica-Source verschoben
 *
 * Revision 1.1  2005/11/20 23:39:11  willuhn
 * @N box handling
 *
 **********************************************************************/