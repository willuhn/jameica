/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/LocalServiceData.java,v $
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
public class LocalServiceData extends ServiceData
{
  private boolean share = false;

  public LocalServiceData(XmlFile xml, String key)
  {
    super(xml,key);
    String s = xml.getString(key,"share","false");
    share = ("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s));
  }

  public boolean isShared()
  {
    return share;
  }
}

/*********************************************************************
 * $Log: LocalServiceData.java,v $
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
