/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/server/Attic/ObjectMetaData.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/12/12 21:11:28 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.server;

import java.util.HashMap;

/**
 * Diese Klasse haelt nur die Meta-Informationen eines Objektes.
 * Das sind quasi all die notwendigen Daten, die der Cache braucht,
 * um ein neues Objekt dieses 
 * @author willuhn
 */
public class ObjectMetaData
{

  private HashMap fields = new HashMap();

  /**
   * Erzeugt ein neues Objekt mit Metadaten.
   * @param fields HashMap mit der Feld-Definition (name=Feldname,value=Typ).
   */
  ObjectMetaData(HashMap fields)
  {
    this.fields = fields;
  }
  
  /**
   * Liefert eine Mappingtabelle mit den Feld-Definitionen.
   * @return HashMap mit der Feld-Definition (name=Feldname,value=Typ).
   */
  HashMap getFields()
  {
    return fields;
  }
}

/*********************************************************************
 * $Log: ObjectMetaData.java,v $
 * Revision 1.1  2003/12/12 21:11:28  willuhn
 * @N ObjectMetaCache
 *
 **********************************************************************/