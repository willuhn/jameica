/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/DelayedListener.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/04/26 18:20:22 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.util;

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
  public final static int TIMEOUT_DEFAULT = 800;

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
    GUI.getDisplay().timerExec(this.timeout,new Runnable()
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

}


/*********************************************************************
 * $Log: DelayedListener.java,v $
 * Revision 1.1  2007/04/26 18:20:22  willuhn
 * @N Ein verzoegernder Listener fuer Bulk-Updates
 *
 **********************************************************************/