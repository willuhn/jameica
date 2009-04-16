/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Customizing.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/04/16 12:58:39 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.system;

/**
 * Enthaelt Parameter-Daten fuer das Customizing von Jameica.
 * BUGZILLA 722
 */
public class Customizing
{
  public final static Settings SETTINGS = new Settings(Customizing.class);
  
  static
  {
    SETTINGS.setStoreWhenRead(false);
  }
}


/**********************************************************************
 * $Log: Customizing.java,v $
 * Revision 1.1  2009/04/16 12:58:39  willuhn
 * @N BUGZILLA 722
 *
 **********************************************************************/
