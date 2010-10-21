/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/BackgroundTaskDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/10/21 10:48:45 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ProgressBar;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Platform;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Startet einen Background-Task in einem modalen Dialog.
 * Normalerweise werden solche Tasks in Jameica ueber
 * "Application.getController().start(task)" gestartet, was dazu fuehrt,
 * dass im GUI-Modus das Snapin unten aufklappt und den Fortschritt anzeigt
 * und im Server-Modus die Monitor-Ausgaben auf der Konsole erscheinen.
 * Unter Umstaenden kann es aber mal sinnvoll sein, einen solchen
 * Task mit einem modalen Dialog laufen zu lassen. Hierfuer kann dieser
 * Dialog verwendet werden.
 * Der Task wird gestartet, sowie der Dialog geoeffnet wird.
 */
public class BackgroundTaskDialog extends AbstractDialog
{
  private final static int WINDOW_WIDTH = 500;
  private BackgroundTask task = null;
  
  /**
   * ct.
   * @param position
   * @param task der auszufuehrende Task.
   */
  public BackgroundTaskDialog(int position, BackgroundTask task)
  {
    super(position);
    this.setSize(WINDOW_WIDTH,SWT.DEFAULT);
    this.task = task;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return task;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    if (task == null)
      throw new Exception("no task given");
    
    final ProgressBar monitor = new ProgressBar();
    monitor.showLogs(false);    // Log brauchen wir hier nicht.
    monitor.paint(parent);
    
    final Thread t = new Thread()
    {
      public void run()
      {
        GUI.getStatusBar().startProgress();
        try
        {
          task.run(monitor);
        }
        catch (OperationCanceledException oce)
        {
          if (monitor != null) 
          {
            monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
            monitor.setPercentComplete(100);
            monitor.setStatusText(oce.getMessage());
          }
        }
        catch (ApplicationException ae)
        {
          if (monitor != null) 
          {
            monitor.setStatus(ProgressMonitor.STATUS_ERROR);
            monitor.setPercentComplete(100);
            monitor.setStatusText(ae.getMessage());
          }
        }
        catch (Throwable t)
        {
          Logger.error("error while executing background task",t);
          if (monitor != null) 
          {
            monitor.setStatus(ProgressMonitor.STATUS_ERROR);
            monitor.setPercentComplete(100);
          }
        }
        finally
        {
          GUI.getStatusBar().stopProgress();
          
          // Dialog schliessen
          close();
        }
      }
    };
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));

    // BUGZILLA 359: Sonderbehandlung fuer MacOS
    Runnable job = null;
    if (Application.getPlatform().getOS() == Platform.OS_MAC)
    {
      job = t;
    }
    else
    {
      job = new Runnable() {
      
        public void run()
        {
          t.start();
        }
      };
    }

    getDisplay().asyncExec(job);
  }

}



/**********************************************************************
 * $Log: BackgroundTaskDialog.java,v $
 * Revision 1.1  2010/10/21 10:48:45  willuhn
 * @N BackgroundTaskDialog
 *
 **********************************************************************/