/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/BackgroundTaskMonitor.java,v $
 * $Revision: 1.9 $
 * $Date: 2011/08/18 16:55:24 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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

/**
 * Implementierung eines Progress-Monitors fuer Hintergrund-Jobs in Jameica.
 */
public class BackgroundTaskMonitor extends ProgressBar
{
  /**
   * Datums-Format dd.MM.yyyy HH:mm:ss.
   */
  private final static DateFormat DF  = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

  private static DelayedListener delay = null;
  private boolean started              = false;
  
  private BackgroundTask task          = null;
  private PanelButton cancel           = null;
  
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
          // Wenn wir hier sind, muss das timeout abgelaufen und niemand
          // sonst an dem Progress etwas aktualisiert haben
          // BUGZILLA 179 + 432
          int status = event.detail;
          if (started && (status == STATUS_CANCEL || status == STATUS_DONE || status == STATUS_ERROR))
          {
            GUI.getDisplay().asyncExec(new Runnable() {
              public void run()
              {
                if (!started || !GUI.getView().snappedIn())
                  return;
                try
                {
                  Logger.info("auto closing monitor snapin");
                  GUI.getView().snapOut();
                }
                finally
                {
                  started = false;
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
    if (started)
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
              started = false;
            }
          },Application.getI18n().tr("Minimieren")));
          
          // Abbrechen-Button einblenden, wenn wir einen Task haben
          if (task != null)
            panel.addButton(getCancelButton());
          
          panel.paint(view.getSnapin());
          Logger.info("activating progress monitor");
          view.snapIn();
          started = true;
        }
        catch (RemoteException e)
        {
          Logger.error("unable to snapin progress monitor",e);
        }
      }
    });
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
    check();
    super.setStatus(status);
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


/*********************************************************************
 * $Log: BackgroundTaskMonitor.java,v $
 * Revision 1.9  2011/08/18 16:55:24  willuhn
 * @N Button zum Abbrechen von Background-Tasks. Ob die den Request dann auch beachten, ist aber deren Sache ;)
 *
 * Revision 1.8  2009/03/11 23:09:56  willuhn
 * @C unnuetzes Ueberschreiben der paint()-Methode
 *
 * Revision 1.7  2008/04/23 11:43:40  willuhn
 * @B 432+179 Snapin nach 30 Sekunden automatisch ausblenden (jetzt einfacher via DelayedListener geloest)
 *
 * Revision 1.6  2006/06/19 11:50:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2006/06/08 17:40:17  willuhn
 * @C Ausblende-Timeout verlaengert
 *
 * Revision 1.4  2006/02/20 23:30:29  web0
 * *** empty log message ***
 *
 * Revision 1.3  2006/02/20 22:29:07  web0
 * @N longer timeout
 *
 * Revision 1.2  2006/02/06 17:15:49  web0
 * @B bug 179
 *
 * Revision 1.1  2006/01/18 18:40:21  web0
 * @N Redesign des Background-Task-Handlings
 *
 * Revision 1.3  2006/01/11 00:29:21  willuhn
 * @C HBCISynchronizer nach gui.action verschoben
 * @R undo bug 179 (blendet zu zeitig aus, wenn mehrere Jobs (Synchronize) laufen)
 *
 * Revision 1.2  2005/07/29 15:10:32  web0
 * @N minimize hbci progress dialog
 *
 * Revision 1.1  2005/07/26 23:00:03  web0
 * @N Multithreading-Support fuer HBCI-Jobs
 *
 **********************************************************************/