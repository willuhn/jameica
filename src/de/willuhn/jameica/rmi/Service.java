/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/rmi/Attic/Service.java,v $
 * $Revision: 1.3 $
 * $Date: 2003/12/12 21:11:29 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Basis-Interface aller Services, die via RMI genutzt werden koennen.
 * @author willuhn
 */
public interface Service extends Remote 
{

  /**
   * Oeffnet den Service. (Initialisiert ihn).
   * @throws RemoteException
   */
  public void open() throws RemoteException;
	
  /**
   * Schliesst den Service.
   * @throws RemoteException
   */
  public void close() throws RemoteException;

  /**
   * Prueft, ob dieser Service auf dem Server noch verfuegbar ist.
   * Wenn diese Funktion true liefert, kann der Service auch via open()
   * nicht mehr geoeffnet werden. Heisst: Der Service ist nicht
   * nur geschlossen worden sondern der gesamte Server ist heruntergefahren
   * worden.
   * @return true wenn er verfuegbar ist, sonst false.
   * @throws RemoteException
   */
  public boolean isAvailable() throws RemoteException;


  /**
   * Faehrt den Service herunter. Er kann danach nicht mehr genutzt werden.
   * Diese Funktion wird vom Shutdown-Prozess des Servers aufgerufen.
   * @throws RemoteException
   */
  public void shutDown() throws RemoteException;
}

/*********************************************************************
 * $Log: Service.java,v $
 * Revision 1.3  2003/12/12 21:11:29  willuhn
 * @N ObjectMetaCache
 *
 * Revision 1.2  2003/10/29 00:41:27  willuhn
 * *** empty log message ***
 *
 **********************************************************************/