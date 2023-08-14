/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Application;

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
  private final static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  
  /**
   * Das Default-Timeout.
   */
  public final static int TIMEOUT_DEFAULT = 300;

  private AtomicInteger count = new AtomicInteger();
  private int timeout         = TIMEOUT_DEFAULT;
  private Listener listener   = null;

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
    final int myCount = this.count.incrementAndGet();
    
    final Runnable r = new Runnable() {
      
      @Override
      public void run()
      {
        // Wenn der aktuelle Counter größer ist, kam nach uns noch ein Aufruf. Dann
        // muss der sich drum kümmern
        if (myCount < count.get())
          return;

        if (Application.inServerMode())
        {
          listener.handleEvent(event);
        }
        else
        {
          GUI.getDisplay().asyncExec(new Runnable() {
            
            @Override
            public void run()
            {
              listener.handleEvent(event);
            }
          });
        }
          
      }
    };

    executorService.schedule(r,timeout,TimeUnit.MILLISECONDS);
  }
}
