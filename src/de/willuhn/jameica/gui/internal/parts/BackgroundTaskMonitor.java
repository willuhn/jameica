/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.View;
import de.willuhn.jameica.gui.parts.Panel;
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.gui.parts.ProgressBar;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Implementierung eines Progress-Monitors fuer Hintergrund-Jobs in Jameica.
 */
public class BackgroundTaskMonitor extends ProgressBar
{
  /**
   * Datums-Format dd.MM.yyyy HH:mm:ss.
   */
  private final static DateFormat DF  = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

  private final static AtomicBoolean DEFAULT_LOCKED = new AtomicBoolean(false);
  private final static AtomicLong USAGES = new AtomicLong(0);

  private DelayedListener delay     = null;
  private AtomicBoolean started     = new AtomicBoolean(false);
  private AtomicBoolean panelLocked = DEFAULT_LOCKED;
  private AtomicLong usage          = new AtomicLong(USAGES.incrementAndGet());
  
  private BackgroundTask task       = null;
  private PanelButton cancel        = null;
  
  /**
   * ct.
   */
  public BackgroundTaskMonitor()
  {
    this(null);
  }
  
  /**
   * ct.
   * @param task der auszufuehrende Task.
   * Kann hier uebergeben werden, um den Task abbrechen zu koennen.
   */
  public BackgroundTaskMonitor(BackgroundTask task)
  {
    this.task = task;
  }
  
  /**
   * Aktualisiert den Status.
   * @param status Aktueller Status-Code.
   */
  private synchronized void check()
  {
    if (delay == null)
    {
      delay = new DelayedListener(30 * 1000,new Listener() {
        public void handleEvent(Event event)
        {
          // Egal, welche sonstigen Zustaende existieren. Wenn es gelockt ist, bleibt es offen
          if (panelLocked.get())
            return;
          
          // Wenn wir hier sind, muss das timeout abgelaufen und niemand
          // sonst an dem Progress etwas aktualisiert haben
          // BUGZILLA 179 + 432
          if (started.get() && isFinalState(event.detail))
          {
            GUI.getDisplay().asyncExec(new Runnable() {
              public void run()
              {
                // Wir machen das Fenster nicht zu, wenn:
                // a) wir wurden noch nicht gestartet
                // b) es ist gar nicht offen
                // c) wir waren nicht die letzte Instanz.
                //    Das kann passieren, wenn das Snapin von einem Folgeprozess wiederverwendet wurde, bevor es
                //    von unserer Instanz geschlossen wurde. Beispiel: User synchronisiert die Konten, wartet
                //    nicht, bis das Snapin automatisch zugegangen ist und schliesst es auch nicht manuell. Dann
                //    startet er eine erneute Synchronisation - noch bevor das vorherige Snapin geschlossen wurde.
                //    Da der vorherige Monitor auf einen finalen State ging, wird der nach 30 Sekunden versuchen,
                //    das Snapin zu schliessen. Das hierfuer gefeuerte Event laesst sich nachtraeglich auch nicht
                //    mehr aufhalten. Es ist per Display#timerExec bereits an SWT uebergeben worden. Stattdessen
                //    muessen wir hier bei der Ausfuehrung des Events erkennen, ob wir die letzten waren. Sind
                //    wir es nicht mehr, ist der Nachfolger fuer das Schliessen zustaendig.
                if (!started.get() || !GUI.getView().snappedIn() || usage.get() != USAGES.get())
                  return;
                try
                {
                  Logger.info("auto closing monitor snapin");
                  GUI.getView().snapOut();
                }
                finally
                {
                  started.set(false);
                }
              }
            });
          }
        }
      });
    }
    
    // Ping
    Event e = new Event();
    e.detail = getStatus();
    delay.handleEvent(e);

    // Wird schon angezeigt.
    if (started.get())
      return;

    Logger.info("creating progress monitor for GUI");
    GUI.getDisplay().syncExec(new Runnable() {
      public void run()
      {
        final View view = GUI.getView();
        if (view.snappedIn())
          view.snapOut();
        
        try
        {
          I18N i18n = Application.getI18n();
          Panel panel = new Panel(i18n.tr("Status"), BackgroundTaskMonitor.this, false);
          panel.addButton(new PanelButton("minimize.png",new Action() {
            public void handleAction(Object context) throws ApplicationException
            {
              Logger.info("closing background task monitor snapin");
              view.snapOut();
              started.set(false);
            }
          },Application.getI18n().tr("Minimieren")));
         
          panel.addButton(getLockButton());
          
          // Abbrechen-Button einblenden, wenn wir einen Task haben
          if (task != null)
            panel.addButton(getCancelButton());
          
          panel.paint(view.getSnapin());
          Logger.info("activating progress monitor");
          view.snapIn();
          started.set(true);
        }
        catch (RemoteException e)
        {
          Logger.error("unable to snapin progress monitor",e);
        }
      }
    });
  }
  
  /**
   * Prueft, ob es sich um einen finalen Status handelt.
   * @param state der Status.
   * @return true, wenn es ein finaler Status ist.
   */
  private boolean isFinalState(int state)
  {
    return state == STATUS_CANCEL ||
           state == STATUS_DONE ||
           state == STATUS_ERROR;
  }

  /**
   * Erzeugt den Lock-Button.
   * @return der Lock-Button.
   */
  private PanelButton getLockButton()
  {
    final PanelButton lockButton=new PanelButton(getLockedIcon(), null, Application.getI18n().tr("Fixieren"));
    lockButton.setAction(new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        panelLocked.set(!panelLocked.get());
        DEFAULT_LOCKED.set(panelLocked.get());
        lockButton.setIcon(getLockedIcon());
        
        // Wenn das Panel nicht mehr gesperrt ist, lassen wir den Timer neu loslaufen,
        // damit es nach dem Timeout wieder von allein verschwinden kann
        if (!panelLocked.get())
        {
          check();
          Event e = new Event();
          e.detail = STATUS_DONE;
          delay.handleEvent(e);
        }
          
      }
    });
    return lockButton;
  }

  /**
   * Liefert das Lock-Icon.
   * @return das Lock-Icon.
   */
  private String getLockedIcon()
  {
    return panelLocked.get() ? "locked.png" : "unlocked.png";
  }
  
  /**
   * Liefert einen Button zum Abbrechen.
   * @return Button zum Abbrechen.
   */
  private PanelButton getCancelButton()
  {
    if (this.cancel != null)
      return this.cancel;

    this.cancel = new PanelButton("process-stop.png",new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (task == null || task.isInterrupted())
          return;
        
        Logger.info("cancel background task");
        task.interrupt();
        cancel.setEnabled(false); // nur einmal erlauben
      }
    },Application.getI18n().tr("Abbrechen"));
    this.cancel.setEnabled(this.task != null && !this.task.isInterrupted());
    return this.cancel;
  }
  
  /**
   * @see de.willuhn.util.ProgressMonitor#setPercentComplete(int)
   */
  public void setPercentComplete(int arg0)
  {
    check();
    super.setPercentComplete(arg0);
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#addPercentComplete(int)
   */
  public void addPercentComplete(int arg0)
  {
    check();
    super.addPercentComplete(arg0);
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#getPercentComplete()
   */
  public int getPercentComplete()
  {
    check();
    return super.getPercentComplete();
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#setStatus(int)
   */
  public void setStatus(int status)
  {
    // Wenn wir in einem finalen Zustand sind, kann der Cancel-Button nicht mehr gedrueckt werden
    boolean finalState = (status == ProgressMonitor.STATUS_CANCEL) ||
                         (status == ProgressMonitor.STATUS_DONE) ||
                         (status == ProgressMonitor.STATUS_ERROR);
    if (finalState)
      this.getCancelButton().setEnabled(false);
    
    super.setStatus(status);
    check();
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#setStatusText(java.lang.String)
   */
  public void setStatusText(String arg0)
  {
    check();
    super.setStatusText(arg0);
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#log(java.lang.String)
   */
  public void log(String arg0)
  {
    check();
    super.log("[" + DF.format(new Date()) + "] " + arg0);
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.ProgressBar#clearLog()
   */
  public void clearLog()
  {
    check();
    super.clearLog();
  }
}
