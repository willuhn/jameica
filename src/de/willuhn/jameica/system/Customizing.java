/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Customizing.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/04/26 12:01:42 $
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
  /**
   * Die Customizing-Settings.
   */
  public final static Settings SETTINGS = new Settings(Customizing.class);
  
  static
  {
    SETTINGS.setStoreWhenRead(false);
  }
}


/**********************************************************************
 * $Log: Customizing.java,v $
 * Revision 1.2  2011/04/26 12:01:42  willuhn
 * @D javadoc Fixes
 *
 * Revision 1.1  2009/04/16 12:58:39  willuhn
 * @N BUGZILLA 722
 *
 **********************************************************************/
