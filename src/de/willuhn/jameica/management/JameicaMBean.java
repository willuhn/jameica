/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/management/Attic/JameicaMBean.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/12/05 13:35:30 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.management;

/**
 * Basis-Interface, welches von MBeans implementiert werden muss,
 * wenn sie automatisch erkannt werden wollen.
 * Jede MBean muss ausserdem noch ein eigenes Interface implementieren,
 * welches als Klassenname den Namen der Implementierung + "MBean" traegt.
 * Das schreibt die JMX-Spec so vor.
 */
public interface JameicaMBean
{
  /**
   * Liefert den Wert des Attributes "type", mit dem die MBean registriert werden soll.
   * @return Wert des Attributes "type".
   */
  public String getType();
}


/*********************************************************************
 * $Log: JameicaMBean.java,v $
 * Revision 1.1  2007/12/05 13:35:30  willuhn
 * @N Unterstuetzung fuer JMX
 *
 **********************************************************************/