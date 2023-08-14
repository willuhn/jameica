/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.action.FileClose;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Zeigt das Systray an.
 */
@Lifecycle(Type.CONTEXT)
public class SysTray
{
  public final static String KEY_MINIMIZED = "__systray-minimize";
  
  private TrayItem item = null;
  private boolean activity = false;

  /**
   * Startet das Systray.
   */
  public void start()
  {
    if (this.item != null)
      return;

    final Tray sysTray = GUI.getDisplay().getSystemTray();
    if (sysTray == null)
      return;
    
    this.item = new TrayItem(sysTray,SWT.NONE);

    // Menu aufklappen bei Klick mit der rechten Maustaste
    this.item.addListener (SWT.MenuDetect, new Listener()
    {
      public void handleEvent(Event event)
      {
        getMenu();
      }
    });
    
    // Minimieren/Maximieren beim Klick mit der linken Maustaste.
    this.item.addSelectionListener(new SelectionAdapter() {
    
      public void widgetSelected(SelectionEvent e)
      {
        toggle();
      }
    });

    this.refresh();
  }
  
  /**
   * Legt fest, ob das Symbol neue Aktivität anzeigen soll.
   * @param b true, wenn neue Aktivität angezeigt werden soll.
   */
  public void setNewActivity(boolean b)
  {
    this.activity = b;
    this.refresh();
  }
  
  /**
   * Wechselt beim Klick auf das Systray-Symbol zwischen Minimierung und Nicht-Minimierung hin und her.
   */
  private void toggle()
  {
    try
    {
      final Shell shell = GUI.getShell();
      if (shell == null || shell.isDisposed())
        return;
      
      final boolean minimized = (shell.getData(KEY_MINIMIZED) != null);
      Logger.info("shell minimized: " + minimized);
      if (!minimized)
      {
        Logger.info("minimize to systray");
        shell.setData(KEY_MINIMIZED,Boolean.TRUE);
        shell.setVisible(false);
        shell.setMinimized(true);
        return;
      }
      else
      {
        Logger.info("restore from systray");
        shell.setVisible(true);
        shell.setMinimized(false);
        shell.setData(KEY_MINIMIZED,null);
      }
    }
    catch (Exception e2)
    {
      Logger.error("unable to restore",e2);
    }
  }
  
  /**
   * Beendet das Systray.
   */
  public void stop()
  {
    try
    {
      if (this.item != null && !this.item.isDisposed())
        this.item.dispose();
    }
    finally
    {
      this.item = null;
    }
  }

  /**
   * Oeffnet das ContextMenu.
   */
  private void getMenu()
  {
    try
    {
      final I18N i18n = Application.getI18n();
      final Menu menu = new Menu(GUI.getShell(), SWT.POP_UP);

      ///////////////////////////////////////////////////////////////
      // Fenster wiederherstellen
      {
        final MenuItem restore = new MenuItem(menu, SWT.PUSH);
        restore.setText(i18n.tr("Fenster wiederherstellen"));
        restore.addListener(SWT.Selection, new Listener()
        {
          public void handleEvent (Event e)
          {
            toggle();
          }
        });
      }
      //
      ///////////////////////////////////////////////////////////////

      ///////////////////////////////////////////////////////////////
      // Programm beenden
      {
        final MenuItem shutdown = new MenuItem(menu, SWT.PUSH);
        shutdown.setText(i18n.tr("Programm beenden"));
        shutdown.addListener(SWT.Selection, new Listener()
        {
          public void handleEvent (Event e)
          {
            try
            {
              if (Application.getCallback().askUser(i18n.tr("Jameica wirklich beenden?")))
                new FileClose().handleAction(null);
            }
            catch (OperationCanceledException oce)
            {
              // ignore
            }
            catch (Exception e2)
            {
              Logger.error("unable to shutdown",e2);
            }
          }
        });
      }
      //
      ///////////////////////////////////////////////////////////////

//      new MenuItem(menu, SWT.SEPARATOR);

      // Menu anzeigen
      menu.setVisible(true);
    }
//    catch (ApplicationException ae)
//    {
//      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
//      if (GUI.getShell().getMinimized())
//      {
//        try
//        {
//          Application.getCallback().notifyUser(ae.getMessage());
//        }
//        catch (Exception e)
//        {
//          Logger.error("unable to notify user",e);
//        }
//      }
//    }
    catch (Exception e)
    {
      Logger.error("unable to display menu",e);
    }
  }
  
  /**
   * Aktualisiert das Icon des Systray.
   */
  private void refresh()
  {
    GUI.getDisplay().asyncExec(new Runnable() {
      
      public void run()
      {
        if (item == null || item.isDisposed())
          return;
        
        try
        {
          String s = "Jameica";
          if (activity)
            s += (":" + Application.getI18n().tr("Neue Aktivität"));
          
          item.setToolTipText(s);
          item.setImage(SWTUtil.getImage(activity ? "jameica-icon-notify.png" : "jameica-icon.png"));
        }
        catch (Exception e)
        {
          Logger.error("unable to refresh icon",e);
        }
      }
    });
  }
}
