/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Plugin.java,v $
 * $Revision: 1.4 $
 * $Date: 2003/12/29 16:29:47 $
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
   * Liefert den Namen des Plugins.
   * @return Name des Plugins.
   */
  public String getName();

  /**
   * Liefert die Versionsnummer des Plugins.
   * @return Versionsnummer des Plugins.
   */
  public double getVersion();

  /**
   * Diese Funktion wird beim Beenden der Anwendung ausgefuehrt.
   */
  public void shutDown();
}

/*********************************************************************
 * $Log: Plugin.java,v $
 * Revision 1.4  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.3  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
 * Revision 1.2  2003/11/14 00:49:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/11/13 00:37:35  willuhn
 * *** empty log message ***
 *
 **********************************************************************/