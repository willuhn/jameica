/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/StatusBarCalendarItem.java,v $
 * $Revision: 1.5 $
 * $Date: 2009/06/04 10:50:11 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.gui.dialogs.CalendarDialog;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;

/**
 * Statusbar-Item, welches einen Kalender anzeigt.
 * @author willuhn
 */
public class StatusBarCalendarItem implements StatusBarItem
{
  private Label text    = null;
  private DateFormat df = null;
  
  /**
   * ct.
   */
  public StatusBarCalendarItem()
  {
    this.df = new SimpleDateFormat("EEEE, dd.MM.yyyy HH:mm", Application.getConfig().getLocale());
    new Worker().start();
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {

    text = GUI.getStyleFactory().createLabel(parent, SWT.NONE);
    GridData at = new GridData(GridData.FILL_HORIZONTAL);
    at.verticalAlignment = GridData.CENTER;
    at.widthHint = 200;
    at.horizontalIndent = 5;
    at.verticalIndent = 1;
    text.setAlignment(SWT.LEFT);
    text.setLayoutData(at);
    text.setText(df.format(new Date()));
    text.addMouseListener(new MouseAdapter()
    {
      
      /**
       * @see org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt.events.MouseEvent)
       */
      public void mouseDown(MouseEvent e)
      {
        // wir heben den Text beim Klick hervor, damit man
        // ein optisches Feedback beim Klick kriegt
        text.setForeground(Color.WIDGET_BG.getSWTColor());
      }

      public void mouseUp(MouseEvent e)
      {
        text.setForeground(Color.WIDGET_FG.getSWTColor());
        GUI.getDisplay().asyncExec(new Runnable()
        {
          public void run()
          {
            try
            {
              CalendarDialog d = new CalendarDialog(CalendarDialog.POSITION_MOUSE);
              d.setTitle(Application.getI18n().tr("Kalender"));
              d.open();
            }
            catch (OperationCanceledException oce)
            {
              // ignore
            }
            catch (Exception ex)
            {
              Logger.error("unable to open calendar dialog",ex);
            }
          }
        });
      }
    });
  }

  /**
   * Hilfsklasse zur Aktualisierung des Datums.
   * @author willuhn
   */
  private class Worker extends Thread
  {
    /**
     * ct.
     */
    public Worker()
    {
      super("date watcher for statusbar calendar");
    }
    
    /**
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
      while (true)
      {
        Display display = GUI.getDisplay();
        if (display == null || display.isDisposed())
          return;
        display.syncExec(new Runnable()
        {
          public void run()
          {
            if (text != null && !text.isDisposed())
              text.setText(df.format(new Date()));
          }
        });
        try
        {
          sleep(10000l);
        }
        catch (InterruptedException e)
        {
          Logger.info("statusbar calendar interrupted");
          return;
        }
      }
    }
  }
}


/*********************************************************************
 * $Log: StatusBarCalendarItem.java,v $
 * Revision 1.5  2009/06/04 10:50:11  willuhn
 * @N Optisches Feedback beim Klick aufs Datum
 *
 * Revision 1.4  2007/05/14 11:18:09  willuhn
 * @N Hoehe der Statusleiste abhaengig von DPI-Zahl und Schriftgroesse
 * @N Default-Schrift konfigurierbar und Beruecksichtigung dieser an mehr Stellen
 *
 * Revision 1.3  2007/04/01 22:15:22  willuhn
 * @B Breite des Statusbarlabels
 * @B Redraw der Statusleiste
 *
 * Revision 1.2  2006/08/02 09:12:02  willuhn
 * @B Sortierung der Boxen auf der Startseite
 *
 * Revision 1.1  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
 *********************************************************************/