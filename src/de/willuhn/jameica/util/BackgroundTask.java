/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/util/Attic/BackgroundTask.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/10/07 18:05:26 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.util;

import de.willuhn.util.ProgressMonitor;

/**
 * Klassen, die von dieser ableiten, koennen in
 * Jameica als Hintergrund-Task in einem separaten Thread ausgefuehrt werden.
 * Sie werden ueber die Funktion <code>Application.start(BackgroundTask)</code> gestartet. 
 */
public abstract class BackgroundTask implements Runnable
{
	private ProgressMonitor monitor;

	/**
	 * Konstante fuer Status "Task noch nicht gestartet oder noch kein Status gesetzt".
	 */
	public final static int STATUS_NONE 		= 1;

  /**
   * Konstante fuer Status "Task laeuft gerade".
	 */
	public final static int STATUS_RUNNING	= 2;

  /**
   * Konstante fuer Status "Task mit Fehlern beendet".
	 */
	public final static int STATUS_ERROR		= 3;

  /**
   * Konstante fuer Status "Task erfolgreich beendet".
	 */
	public final static int STATUS_DONE			= 4;

  /**
   * Konstante fuer Status "Task durch Benutzer abgebrochen".
	 */
	public final static int STATUS_CANCEL		= 5;

	/**
	 * Erzeugt einen neuen Background-Task.
   * @param monitor dieser Monitor wird ueber alle Aktivitaeten des Tasks benachrichtigt.
   */
  public BackgroundTask(ProgressMonitor monitor)
	{
		this.monitor = monitor;
	}

	/**
	 * Liefert den Progress-Monitor.
   * @return Progress-Monitor.
   */
  public ProgressMonitor getMonitor()
	{
		return monitor;
	}
}


/**********************************************************************
 * $Log: BackgroundTask.java,v $
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