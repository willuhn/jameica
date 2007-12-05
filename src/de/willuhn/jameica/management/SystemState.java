/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/management/Attic/SystemState.java,v $
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

import java.util.Date;

import de.willuhn.jameica.system.Application;

/**
 * Implementierung der SystemState-Bean.
 */
public class SystemState implements SystemStateMBean
{
  /**
   * @see de.willuhn.jameica.management.SystemStateMBean#getStartDate()
   */
  public Date getStartDate()
  {
    return Application.getStartDate();
  }

  /**
   * @see de.willuhn.jameica.management.JameicaMBean#getType()
   */
  public String getType()
  {
    return "System-Status";
  }
}


/*********************************************************************
 * $Log: SystemState.java,v $
 * Revision 1.1  2007/12/05 13:35:30  willuhn
 * @N Unterstuetzung fuer JMX
 *
 **********************************************************************/