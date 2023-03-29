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
  public final static String KEY_SYSTRAY_DATA = "__systray-minimize";
  
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
        restore();
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
   * Stellt das Anwendungsfenster wieder her.
   */
  private void restore()
  {
    try
    {
      final Shell shell = GUI.getShell();
      
      // Unter Linux (zumindest KDE Plasma) behauptete die Shell, nicht minimiert zu sein.
      // Daher klappte dort das Restore nicht. Wir führen das "setMinimized" daher hier
      // nochmal zusätzlich aus, damit das Boolean-Flag in der Shell gesetzt ist
      // Zusätzlich setzen wir ein "data"-Flag, damit der SystrayService weiss, dass
      // er auf dieses minimize-Event im iconify-Listener nicht reagieren muss.
      shell.setData(KEY_SYSTRAY_DATA, Boolean.TRUE);
      shell.setMinimized(true);

      // Jetzt können wir die Shell wieder sichtbar machen
      shell.setMinimized(false);
      shell.setVisible(true);
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
            restore();
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
