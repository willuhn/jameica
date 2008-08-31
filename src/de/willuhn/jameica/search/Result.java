/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/search/Result.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/08/31 14:08:45 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.search;

import java.io.Serializable;

import de.willuhn.jameica.gui.Action;


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
   * Action, die ausgefuehrt werden soll, wenn der User doppelt
   * auf das Suchergebnis klickt.
   * @return auszufuehrende Aktion bei Auswahl des Suchergebnisses.
   */
  public Action getAction();
}


/**********************************************************************
 * $Log: Result.java,v $
 * Revision 1.1  2008/08/31 14:08:45  willuhn
 * @N Erster Code fuer eine jameica-interne Suchmaschine
 *
 **********************************************************************/
