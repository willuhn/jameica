/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Attic/ProgressMonitor.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/08/09 22:24:16 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.system;

/**
 * Ein Monitor, der ueber alle Aktivitaeten eines Background-Tasks
 * benachrichtigt wird. Eine implementierende Klasse kann diesen
 * beispielsweise in Form eines SWT-Progress-Bars darstellen.
 */
public interface ProgressMonitor
{
	/**
	 * Teilt dem Monitor mit, wieviel Prozent der Aufgabe bereits abgearbeitet sind.
   * @param percent prozentualer Fortschritt (muss zwischen 0 und 100 liegen).
   */
  public void percentComplete(int percent);

	/**
	 * Teilt dem Monitor den aktuellen Status des Tasks mit.
	 * Siehe dazu die Konstanten in <code>BackgroundTask</code>.
   * @param status der aktuelle Status.
   */
  public void setStatus(int status);

	/**
	 * Teilt dem Monitor einen sprechenden Status-Text mit.
   * @param text Status-Text.
   */
  public void setStatusText(String text);

	/**
	 * Teilt dem Monitor mit, dass der angegebene Text protokolliert werden soll.
   * @param msg die zur protokollierende Nachricht.
   */
  public void log(String msg);
}


/**********************************************************************
 * $Log: ProgressMonitor.java,v $
 * Revision 1.1  2004/08/09 22:24:16  willuhn
 * *** empty log message ***
 *
 **********************************************************************/