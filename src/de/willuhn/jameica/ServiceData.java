/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/ServiceData.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/10/23 21:49:46 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica;

import de.bb.util.XmlFile;

/**
 * @author willuhn
 */
public abstract class ServiceData
{

  private String name;
  private String clazz;

  ServiceData(XmlFile xml,String key)
  {
    name  = xml.getString(key,"name",null);
    clazz = xml.getString(key,"class",null);
  }

  public String getClassName()
  {
    return clazz;
  }
  
  public String getName()
  {
    return name;
  }
}

/*********************************************************************
 * $Log: ServiceData.java,v $
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
