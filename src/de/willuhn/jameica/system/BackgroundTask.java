/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.system;

import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Klassen, die dieses Interface implementieren, koennen in
 * Jameica als Hintergrund-Task in einem separaten Thread ausgefuehrt werden.
 * Sie werden ueber die Funktion <code>Application.getController().start(BackgroundTask)</code> gestartet. 
 */
public interface BackgroundTask
{
  /**
   * Diese Methode wird von Jameica in einem separaten Thread
   * ausgefuehrt. Der Funktion wird ein Monitor uebergeben, ueber
   * den der Task Rueckmeldungen ueber seinen Verarbeitungszustand
   * ausgeben soll.
   * @param monitor
   * @throws ApplicationException
   */
  public void run(ProgressMonitor monitor) throws ApplicationException;
  
  /**
   * Bricht den Task ab.
   */
  public void interrupt();
  
  /**
   * Prueft, ob der Task abgebrochen wurde.
   * @return true, wenn er abgebrochen wurde.
   */
  public boolean isInterrupted();
}


/**********************************************************************
 * $Log: BackgroundTask.java,v $
 * Revision 1.6  2008/02/05 22:48:23  willuhn
 * @B BUGZILLA 547
 *
 * Revision 1.5  2006/01/18 18:40:21  web0
 * @N Redesign des Background-Task-Handlings
 *
 * Revision 1.4  2005/07/26 22:58:34  web0
 * @N background task refactoring
 *
 * Revision 1.4  2005/07/11 08:31:24  web0
 * *** empty log message ***
 *
 * Revision 1.3  2004/10/07 18:05:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/08/18 23:14:19  willuhn
 * @D Javadoc
 *
 * Revision 1.1  2004/08/11 00:39:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/08/09 22:24:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/08/09 21:03:15  willuhn
 * *** empty log message ***
 *
 **********************************************************************/