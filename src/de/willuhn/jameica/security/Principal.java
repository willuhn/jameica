/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/Principal.java,v $
 * $Revision: 1.6 $
 * $Date: 2009/10/15 16:01:11 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
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
  private HashMap<String, String> attributes = new HashMap<String, String>();

  /**
   * ct.
   * @param p
   */
  public Principal(java.security.Principal p)
  {
    this.p = p;

    // Erstmal den DN.
    attributes.put(DISTINGUISHED_NAME,this.p.getName());

    String s = this.p.getName();
    if (s == null || s.length() == 0)
      return;
    
    String[] items = s.split(",(?=([^\"]*\"[^\"]*\")*(?![^\"]*\"))");
    if (items == null || items.length < 1)
    {
      Logger.warn("this seems not to be a valid X.500 name: " + s);
      return;
    }
    for (int i=0;i<items.length;++i)
    {
      String token = items[i];
      if (token == null || token.indexOf('=') == -1)
      {
        Logger.info("unable to parse attribute " + token + ", skipping");
        continue;
      }
      String[] pair = token.trim().split("=");
      if (pair.length != 2)
        continue;
      if (pair[1] == null || pair[1].length() == 0)
        continue;
      attributes.put(pair[0].toUpperCase(),pair[1]);
    }
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
 * Revision 1.6  2009/10/15 16:01:11  willuhn
 * @N setList()/setRootObject() in TreePart
 * @C leere X.500-Attribute tolerieren
 *
 * Revision 1.5  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
 * Revision 1.4  2005/07/20 16:23:10  web0
 * @B splitting x.500 name
 *
 * Revision 1.3  2005/06/16 13:29:20  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/06/15 16:10:57  web0
 * @B javadoc fixes
 *
 * Revision 1.1  2005/06/10 10:12:26  web0
 * @N Zertifikats-Dialog ergonomischer gestaltet
 * @C TrustManager prueft nun zuerst im Java-eigenen Keystore
 *
 *********************************************************************/