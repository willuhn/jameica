/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/RemoteServiceData.java,v $
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
public class RemoteServiceData extends ServiceData
{

  private String host;

  public RemoteServiceData(XmlFile xml, String key)
  {
    super(xml,key);
    host = xml.getString(key,"host",null);
    
  }
  
  public String getUrl()
  {
    return ("//" + host + "/" + getClassName() + "/" + getName());
  }
}

/*********************************************************************
 * $Log: RemoteServiceData.java,v $
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
