/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Plugin.java,v $
 * $Revision: 1.2 $
 * $Date: 2003/11/14 00:49:46 $
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
 * Interface fuer alle Plugins.
 * Jedes Plugin muss diese Klasse implementieren, damit es beim Start erkannt wird. 
 * @author willuhn
 */
public interface Plugin
{

  /**
   * Diese Funktion wird beim Start der Anwendung ausgefuehrt. Hier kann die Plugin-
   * Implementierung also diverse Dinge durchfuehren, die es beim Start gern
   * automatisch durchgefuehrt haben moechte ;)
   */
  public void init();

  /**
   * Diese Funktion wird beim Beenden der Anwendung ausgefuehrt.
   */
  public void shutDown();

}

/*********************************************************************
 * $Log: Plugin.java,v $
 * Revision 1.2  2003/11/14 00:49:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/11/13 00:37:35  willuhn
 * *** empty log message ***
 *
 **********************************************************************/