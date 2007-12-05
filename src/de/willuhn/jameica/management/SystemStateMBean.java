/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/management/Attic/SystemStateMBean.java,v $
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

/**
 * Interface der SystemState-Bean.
 */
public interface SystemStateMBean extends JameicaMBean
{

  /**
   * Liefert das Start-Datum der Jameica-Instanz.
   * @return Start-Datum.
   */
  public Date getStartDate();
}


/*********************************************************************
 * $Log: SystemStateMBean.java,v $
 * Revision 1.1  2007/12/05 13:35:30  willuhn
 * @N Unterstuetzung fuer JMX
 *
 **********************************************************************/