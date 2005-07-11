/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/chart/Attic/Chart.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/07/11 13:50:11 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.chart;

import java.rmi.RemoteException;

import org.eclipse.swt.graphics.Color;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Part;

/**
 * Basis-Interface aller Charts.
 */
public interface Chart extends Part
{
  /**
   * Definiert den Titel des Charts.
   * @param title
   */
  public void setTitle(String title);
  
  /**
   * Fuegt eine Zahlenreihe hinzu.
   * @param items Iterator mit den Objekten.
   * @param attribute Name des Attributs der Objekte, welches den Wert enthaelt.
   * @param lineColor
   * @param fillColor
   * @throws RemoteException
   */
  public void addData(GenericIterator items, String attribute, Color lineColor, Color fillColor) throws RemoteException;
}


/*********************************************************************
 * $Log: Chart.java,v $
 * Revision 1.1  2005/07/11 13:50:11  web0
 * @N Linecharts
 *
 **********************************************************************/