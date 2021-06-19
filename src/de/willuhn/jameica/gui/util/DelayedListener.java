/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.util;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;

/**
 * Implementiert die verzoegerte Ausloesung einer Aktion, um Bundle-Updates durchzufuehren.
 * Szenario: In einer Tabelle muessen Inhalte aktualisiert werden.
 * Der Aktualisierungsvorgang kann laenger dauern (mehrere Sekunden), dennoch
 * soll das Aktualisieren bereits bei Eingabe eines Buchstabens in einem
 * Suchfeld ausgeloest werden. Damit nun die Eingabe des Suchtextes
 * nicht dauernd blockiert, da jeder Tastenanschlag das Event ausloest,
 * kann es mit dieser Action hier kurz verzoegert werden. Das eigentliche
 * Update wird erst dann durchgefuehrt, wenn innerhalb eines definierten
 * Zeitraumens (per Default 300ms) kein weiterer Aufruf hinzukam. Somit wird
 * das Update erst dann durchgefuehrt, wenn z.Bsp. der komplette Begriff
 * als Suchwort eingegeben wurde.
 * Der Listener dient als Proxy fuer den eigentlichen Listener.
 * Ist die angegebene Zeit abgelaufen und wurde das Event
 * zwischenzeitlich nicht nochmal aufegerufen, dann wird das
 * Event ausgefuehrt. Andernfalls wird weiter gewartet, bis
 * innerhalb des angegebenen Zeitraumes kein weiteres Event
 * hinzugekommen ist.
 */
public class DelayedListener implements Listener
{
  /**
   * Das Default-Timeout.
   */
  public static final int TIMEOUT_DEFAULT = 300;

  private long count         = 0;
  private int timeout        = TIMEOUT_DEFAULT;
  private Listener listener  = null;

  /**
   * ct.
   * @param listener der eigentlich auszufuehrende Listener.
   */
  public DelayedListener(Listener listener)
  {
    this(TIMEOUT_DEFAULT,listener);
  }

  /**
   * ct.
   * @param millis Anzahl der Millisekunden, die gewartet werden soll.
   * @param listener der eigentlich auszufuehrende Listener.
   */
  public DelayedListener(int millis, Listener listener)
  {
    this.timeout = millis;
    this.listener = listener;
  }

  /**
   * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
   */
  public final synchronized void handleEvent(final Event event)
  {
    count++;

    // Muss ich leider doppelt verpacken, da timerExec keinen Zugriff
    // aus Fremdthreads erlaubt
    Display display = GUI.getDisplay();
    display.asyncExec(new Runnable()
    {
      public void run()
      {
        GUI.getDisplay().timerExec(DelayedListener.this.timeout,new Runnable()
        {
          private long myCount = count;

          public void run()
          {
            if (listener == null)
              return;

            // count steht immer noch dort, wo wir ihn
            // hinterlassen haben, also kam nach uns
            // keiner mehr.
            if (count <= myCount)
              listener.handleEvent(event);
          }
        });
      }
    });
  }

}


/*********************************************************************
 * $Log: DelayedListener.java,v $
 * Revision 1.3  2007/06/04 23:24:26  willuhn
 * @B delayedListener occurs SWTException (invalid thread access) when not wrapped in syncExec/asyncExec
 *
 * Revision 1.2  2007/04/26 18:21:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2007/04/26 18:20:22  willuhn
 * @N Ein verzoegernder Listener fuer Bulk-Updates
 *
 **********************************************************************/