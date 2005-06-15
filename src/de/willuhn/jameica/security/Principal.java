/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/Principal.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/06/15 16:10:57 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.security;

import java.util.HashMap;

import de.willuhn.logging.Logger;

/**
 * Kleine Hilfsklasse um die X.500-Attribute eine java.security.Principal
 * besser auslesen zu koennen.
 */
public class Principal
{

  /**
   * Attribut DN.
   */
  public final static String DISTINGUISHED_NAME  = "DN";
  
  /**
   * Attribut CN.
   */
  public final static String COMMON_NAME         = "CN";
  
  /**
   * Attribut O.
   */
  public final static String ORGANIZATION        = "O";

  /**
   * Attribut OU.
   */
  public final static String ORGANIZATIONAL_UNIT = "OU";
  
  /**
   * Attribut L.
   */
  public final static String LOCALITY            = "L";
  
  /**
   * Attribut ST.
   */
  public final static String STATE               = "ST";
  
  /**
   * Attribut C.
   */
  public final static String COUNTRY             = "C";

  private java.security.Principal p = null;

  // Hier drin speichern wir die Attribute.
  private HashMap attributes = new HashMap();

  /**
   * ct.
   * @param p
   */
  public Principal(java.security.Principal p)
  {
    this.p = p;
    String s = this.p.getName();
    if (s != null && s.indexOf(',') != -1)
    {
      // parsefaehiger Content da
      String[] list = s.split(",");
      for (int i=0;i<list.length;++i)
      {
        if (list[i] == null || list[i].indexOf('=') == -1)
        {
          Logger.info("unable to parse attribute " + list[i]);
          continue;
        }
        String[] pair = list[i].trim().split("=");
        if (pair[1] == null || pair[1].length() == 0)
          continue;
        attributes.put(pair[0].toUpperCase(),pair[1]);
      }
    }
    // und jetzt noch den DN
    attributes.put(DISTINGUISHED_NAME,this.p.getName());
  }

  /**
   * Liefert den Wert des Attributes.
   * @param name Name des Attributes. Siehe Konstanten in dieser Klasse.
   * @return Wert des Attributes. Es werden nie Leerstrings geliefert sondern hoechstens null.
   */
  public String getAttribute(String name)
  {
    return (String) this.attributes.get(name);
  }
}


/*********************************************************************
 * $Log: Principal.java,v $
 * Revision 1.2  2005/06/15 16:10:57  web0
 * @B javadoc fixes
 *
 * Revision 1.1  2005/06/10 10:12:26  web0
 * @N Zertifikats-Dialog ergonomischer gestaltet
 * @C TrustManager prueft nun zuerst im Java-eigenen Keystore
 *
 *********************************************************************/