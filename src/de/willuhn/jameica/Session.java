/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Session.java,v $
 * $Revision: 1.2 $
 * $Date: 2003/12/19 19:45:02 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica;

import java.util.HashMap;

/**
 * @author willuhn
 */
public class Session
{

  private static HashMap data = new HashMap();

  private Class clazz;  
  
  private Session()
  {
    // disable
  }

  /**
   * Erzeugt eine neue Session fuer die uebergebene Klasse.
   * Diese enthaelt dann exklusiv nur fuer diese Klasse Daten. 
   * @param clazz
   */
  public Session(Class clazz)
  {
    this.clazz = clazz;
    data.put(clazz,new HashMap());
  }

  /**
   * Speichert einen Wert unter dem angegebenen Schluessel in der Session.
   * @param key Name des Schluessels.
   * @param value Wert des Schluessels.
   */
  public void setAttribute(Object key, Object value)
  {
    HashMap h = (HashMap) data.get(clazz);
    if (h == null) return;

    h.put(key,value);
  }

  /**
   * Liefert Wert aus der Session, der unter dem angegebenen Namen gespeichert ist.
   * @param key Name des Schluessels in der Session.
   * @return Wert des Schluessels.
   */
  public Object getAttribute(Object key)
  {
    HashMap h = (HashMap) data.get(clazz);
    if (h == null) return null;

    return h.get(key);
  }
}

/*********************************************************************
 * $Log: Session.java,v $
 * Revision 1.2  2003/12/19 19:45:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/12/19 01:43:27  willuhn
 * @N added Tree
 *
 **********************************************************************/