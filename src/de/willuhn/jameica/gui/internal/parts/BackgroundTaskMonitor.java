/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/BackgroundTaskMonitor.java,v $
 * $Revision: 1.3 $
 * $Date: 2006/02/20 22:29:07 $
 * $Author: web0 $
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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.View;
import de.willuhn.jameica.gui.parts.Panel;
import de.willuhn.jameica.gui.parts.ProgressBar;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
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

  private final static int TIMEOUT = 30 * 1000;
  private static boolean CANCEL_TIMEOUT = false;
  
  private boolean started = false;
  
  private void check()
  {
    // Wenn die Check-Funktion aufgerufen wird, brechen wir einen ggf. laufenden
    // Auto-Schliesser immer ab
    CANCEL_TIMEOUT = true;
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
          panel.addMinimizeListener(new Listener()
          {
            public void handleEvent(Event event)
            {
              Logger.info("closing background task monitor snapin");
              view.snapOut();
              started = false;
            }
          });
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
    // BUGZILLA 179
    if (started && status == STATUS_CANCEL || status == STATUS_DONE || status == STATUS_ERROR)
    {
      // Wir sind fertig. Dann starten wir einen Timeout und schliessen das
      // Fenster selbst nach ein paar Sekunden.
      GUI.getDisplay().asyncExec(new Runnable() {
        public void run()
        {
          CANCEL_TIMEOUT = false;
          GUI.getDisplay().timerExec(TIMEOUT,new Runnable() {
            public void run()
            {
              if (!started || CANCEL_TIMEOUT)
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
      });
    }
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#setStatusText(java.lang.String)
   */
  public void setStatusText(String arg0)
  {
    check();
    super.setStatusText(arg0);
    log(arg0);
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
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);
  }
}


/*********************************************************************
 * $Log: BackgroundTaskMonitor.java,v $
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