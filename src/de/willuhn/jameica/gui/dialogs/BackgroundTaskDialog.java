/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.ProgressBar;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
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
  private BackgroundTask task           = null;
  private boolean interruptible         = false;
  private Button cancel                 = null;
  
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
   * Legt fest, ob der Task vom User abgebrochen werden koennen soll.
   * @param b true, wenn er abbrechbar sein soll.
   * Per Default ist er es nicht.
   */
  public void setInterruptible(boolean b)
  {
    this.interruptible = b;
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
    
    final Thread t = new Thread("[Jameica Backgroundtask] " + task.getClass().getName())
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
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
        }
        catch (Throwable t)
        {
          Logger.error("error while executing background task",t);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler: {0}",t.getMessage()),StatusBarMessage.TYPE_ERROR));
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
    
    if (this.interruptible)
    {
      ButtonArea buttons = new ButtonArea();
      this.cancel = new Button(Application.getI18n().tr("Abbrechen"),new Action() {
        public void handleAction(Object context) throws ApplicationException
        {
          // GUI Bescheid geben
          GUI.getDisplay().syncExec(new Runnable() {
            public void run()
            {
              // Button deaktivieren, damit der User nicht mehrfach draufklickt.
              cancel.setEnabled(false);

              monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
              monitor.setStatusText(Application.getI18n().tr("Breche ab ..."));
            }
          });
          
          // Das muessen wir in einem extra Thread machen, weil wir weder den Worker-Thread
          // noch den GUI-Thread blockieren wollen
          Thread shutdown = new Thread("[Shutdown Backgroundtask] " + task.getClass().getName())
          {
            public void run()
            {
              try
              {
                Logger.info("interrupting background task");
                
                
                ////////////////////////////////////////////////////////////////
                // 1. Task beenden
                // Wir markieren den Task erst als abgebrochen.
                task.interrupt();
                
                // Wir geben dem Task jetzt 60 Sekunden Zeit, aufzuraeumen
                Logger.info("waiting for task to finish");
                long timeout = System.currentTimeMillis() + (60 * 1000L);
                while (!task.isInterrupted() && (System.currentTimeMillis() < timeout))
                {
                  try
                  {
                    Thread.sleep(1000L);
                  }
                  catch (Exception e)
                  {
                    Logger.error("error while waiting for task",e);
                    break;
                  }
                }
                //
                ////////////////////////////////////////////////////////////////
                
                ////////////////////////////////////////////////////////////////
                // 2. Thread beenden
                //
                // OK, der Task muss jetzt fertig sein. Wir geben dem Thread Bescheid,
                // dass Feierabend ist. Ist aber nur noetig, wenn er wirklich noch laeuft.
                if (t.isAlive() && !t.isInterrupted())
                {
                  Logger.info("interrupting thread");
                  try
                  {
                    t.interrupt();
                  }
                  catch (Exception e)
                  {
                    Logger.error("unable to interrupt thread",e);
                  }

                  // Wir geben dem Thread jetzt auch nochmal ne Minute Zeit.
                  Logger.info("waiting for thread to finish");
                  t.join(60 * 1000L);
                }
                //
                ////////////////////////////////////////////////////////////////
              }
              catch (Exception e)
              {
                Logger.error("unable to stop background task",e);
              }
              finally
              {
                Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Abgebrochen"),StatusBarMessage.TYPE_ERROR));
              }
            }
          };
          shutdown.start();
          
        }
      },null,false,"process-stop.png");
      buttons.addButton(this.cancel);
      Container c = new SimpleContainer(parent);
      c.addButtonArea(buttons);
    }
    
    
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));

    Runnable job = new Runnable() {
      public void run()
      {
        t.start();
      }
    };
    getDisplay().asyncExec(job);
  }

}



/**********************************************************************
 * $Log: BackgroundTaskDialog.java,v $
 * Revision 1.4  2011/07/22 09:11:27  willuhn
 * @N Support zum Abbrechen von Background-Tasks
 *
 * Revision 1.3  2011-06-02 10:55:32  willuhn
 * @R Die nicht mehr noetige Mac-Sonderbehandlung entfernt (in GUI ist es auch nicht mehr drin)
 *
 * Revision 1.2  2011-03-17 11:01:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2010-10-21 10:48:45  willuhn
 * @N BackgroundTaskDialog
 *
 **********************************************************************/