/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Session.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/12/19 01:43:27 $
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
  
  // TODO: Noch plugin tauglich machen

  /**
   * Speichert einen Wert unter dem angegebenen Schluessel in der Session.
   * @param key Name des Schluessels.
   * @param value Wert des Schluessels.
   */
  public static void setAttribute(Object key, Object value)
  {
    data.put(key,value);
  }

  /**
   * Liefert Wert aus der Session, der unter dem angegebenen Namen gespeichert ist.
   * @param key Name des Schluessels in der Session.
   * @return Wert des Schluessels.
   */
  public static Object getAttribute(Object key)
  {
    return data.get(key);
  }
}

/*********************************************************************
 * $Log: Session.java,v $
 * Revision 1.1  2003/12/19 01:43:27  willuhn
 * @N added Tree
 *
 **********************************************************************/