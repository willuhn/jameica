/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Plugin.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/11/13 00:37:35 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica;

/**
 * Basisklasse aller Plugins.
 * Jedes Plugin muss diese Klasse erweitern, damit es beim Start erkannt wird. 
 * @author willuhn
 */
public abstract class Plugin
{

  /**
   * Diese Funktion wird beim Start der Anwendung ausgefuehrt. Hier kann die Plugin-
   * Implementierung also diverse Dinge durchfuehren, die es beim Start gern
   * automatisch durchgefuehrt haben moechte ;)
   */
  public abstract void init();

  /**
   * Diese Funktion wird beim Beenden der Anwendung ausgefuehrt.
   */
  public abstract void shutDown();

}

/*********************************************************************
 * $Log: Plugin.java,v $
 * Revision 1.1  2003/11/13 00:37:35  willuhn
 * *** empty log message ***
 *
 **********************************************************************/