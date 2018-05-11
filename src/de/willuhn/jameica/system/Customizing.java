/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
