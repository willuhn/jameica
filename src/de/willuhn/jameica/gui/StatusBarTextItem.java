/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/StatusBarTextItem.java,v $
 * $Revision: 1.8 $
 * $Date: 2010/11/02 22:33:07 $
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.internal.parts.LogList;
import de.willuhn.jameica.gui.parts.Panel;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Customizing;
import de.willuhn.logging.Logger;

/**
 * Ein Statusbar-Element, welches einen Status-Text anzeigt.
 * @author willuhn
 */
public class StatusBarTextItem implements StatusBarItem
{

  private Label text = null;
  private boolean snapIn = false;
  
  /**
   * ct.
   */
  public StatusBarTextItem()
  {
    Application.getMessagingFactory().registerMessageConsumer(new StatusMessageConsumer());
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(final Composite parent) throws RemoteException
  {
    Composite comp = new Composite(parent, SWT.NONE);
    comp.setLayoutData(new GridData(GridData.FILL_BOTH));
    GridLayout gl = new GridLayout(2,false);
    gl.horizontalSpacing = 0;
    gl.verticalSpacing   = 0;
    gl.marginHeight      = 0;
    gl.marginWidth       = 0;
    comp.setLayout(gl);
    
    text = GUI.getStyleFactory().createLabel(comp, SWT.NONE);
    GridData at = new GridData(GridData.FILL_BOTH);
    at.verticalAlignment = GridData.CENTER;
    at.verticalIndent = 1;
    at.widthHint = 600;
    text.setAlignment(SWT.RIGHT);
    text.setLayoutData(at);
    text.setText("");
    
    if (!Customizing.SETTINGS.getBoolean("application.statusbar.hidelog",false))
    {
      final Canvas c = new Canvas(comp,SWT.NONE);
      GridData gd = new GridData(GridData.FILL_BOTH);
      at.verticalAlignment = GridData.END;
      gd.widthHint = 20;
      c.setLayoutData(gd);
      c.addListener(SWT.Paint,new Listener()
      {
        public void handleEvent(Event event)
        {
          GC gc = event.gc;
          Rectangle size = c.getBounds();
          gc.drawImage(SWTUtil.getImage(snapIn ? "minimize.png" : "maximize.png"),size.width - 20,0);
        }
      });

      MouseAdapter ma = new MouseAdapter()
      {
        public void mouseUp(MouseEvent e)
        {
          if (GUI.getView().snappedIn())
            GUI.getView().snapOut();

          if (snapIn)
          {
            // wir werden schon angezeigt, dann zoomen wir uns wieder raus
            snapIn = false;
            c.redraw();
            return;
          }
          try
          {
            Panel panel = new Panel(Application.getI18n().tr("System-Meldungen"),new LogList(),false);
            panel.addMinimizeListener(new Listener() {
              public void handleEvent(Event event)
              {
                if (GUI.getView().snappedIn())
                  GUI.getView().snapOut();
                snapIn = false;
                c.redraw();
              }
            });
            panel.paint(GUI.getView().getSnapin());
            GUI.getView().snapIn();
            snapIn = true;
            c.redraw();
          }
          catch (RemoteException re)
          {
            Logger.error("unable to display log list",re);
          }
        }
      };

      String s = Application.getI18n().tr("Klicken Sie hier, um die letzten Zeilen des System-Logs anzuzeigen.");
      c.setToolTipText(s);
      c.addMouseListener(ma);
    }
  }

  /**
   * Message-Consumer, ueber den wir die Status-Nachrichten erhalten.
   * @author willuhn
   */
  private class StatusMessageConsumer implements MessageConsumer
  {

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[] {StatusBarMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      if (message == null)
        return;

      final StatusBarMessage m = (StatusBarMessage) message;

      final Display d = GUI.getDisplay();
      if (d == null || d.isDisposed())
        return;
      
      final long currentClick = System.currentTimeMillis();
      d.asyncExec(new Runnable() {
        public void run() {
          if (text != null && !text.isDisposed())
          {
            Color color = null;
            switch (m.getType())
            {
              case StatusBarMessage.TYPE_ERROR:
                color = Color.ERROR;
                break;
              case StatusBarMessage.TYPE_SUCCESS:
                color = Color.SUCCESS;
                break;
              default:
                color = Color.WIDGET_FG;
            }
            text.setForeground(color.getSWTColor());
            text.setText(m.getText() + " ");
            text.update();
            text.redraw();
            lastClick = currentClick;
          }
        }
      });
      d.asyncExec(new Runnable() {
        public void run()
        {
          d.timerExec(10000,new Runnable()
          {
            public void run()
            {
              if (currentClick == lastClick && !text.isDisposed())
              {
                // nur entfernen, wenn wir der letzte Klick waren
                text.setText("");
                text.update();
                text.redraw();
              }
            }
          });
        }
      });
    }
    private long lastClick;

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }
  }
}


/*********************************************************************
 * $Log: StatusBarTextItem.java,v $
 * Revision 1.8  2010/11/02 22:33:07  willuhn
 * @B Kann beim Shutdown eine Racecondition mit "SWTException: Invalid thread access" ausloesen - stoert zwar nicht, sieht aber unschoen im Log aus
 *
 * Revision 1.7  2010-10-19 15:33:21  willuhn
 * @N Statusbar via Customizing anpassbar
 *
 * Revision 1.6  2007/11/02 01:19:38  willuhn
 * @N Vorbereitungen fuer Drag&Drop von Panels
 * @N besserer Klick-Indikator in Statusleiste fuer Oeffnen des Logs
 *
 * Revision 1.5  2007/05/14 11:18:09  willuhn
 * @N Hoehe der Statusleiste abhaengig von DPI-Zahl und Schriftgroesse
 * @N Default-Schrift konfigurierbar und Beruecksichtigung dieser an mehr Stellen
 *
 * Revision 1.4  2007/04/01 22:15:22  willuhn
 * @B Breite des Statusbarlabels
 * @B Redraw der Statusleiste
 *
 * Revision 1.2  2006/04/18 16:49:46  web0
 * @C redesign in MessagingFactory
 *
 * Revision 1.1  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
 *********************************************************************/