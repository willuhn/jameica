/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.search;

import java.io.Serializable;
import java.rmi.RemoteException;

import de.willuhn.util.ApplicationException;


/**
 * Interface fuer ein einzelnes Suchergebnis.
 */
public interface Result extends Serializable
{
  /**
   * Liefert einen sprechenden Namen/Anzeigetext fuer das Suchergebnis.
   * @return sprechender Name/Anzeigetext fuer das Suchergebnis.
   */
  public String getName();
  
  /**
   * Wird ausgefuehrt, wenn der User doppelt auf das Suchergebnis klickt.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public void execute() throws RemoteException, ApplicationException;
}


/**********************************************************************
 * $Log: Result.java,v $
 * Revision 1.2  2008/08/31 23:07:10  willuhn
 * @N Erster GUI-Code fuer die Suche
 *
 * Revision 1.1  2008/08/31 14:08:45  willuhn
 * @N Erster Code fuer eine jameica-interne Suchmaschine
 *
 **********************************************************************/
