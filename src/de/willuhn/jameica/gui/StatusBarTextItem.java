/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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

import de.willuhn.jameica.gui.internal.action.LogExport;
import de.willuhn.jameica.gui.internal.parts.LogList;
import de.willuhn.jameica.gui.parts.Panel;
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Customizing;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Ein Statusbar-Element, welches einen Status-Text anzeigt.
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
    Application.getMessagingFactory().getMessagingQueue("jameica.statusbar").registerMessageConsumer(new StatusMessageConsumer());
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
          gc.drawImage(SWTUtil.getImage(snapIn ? "minimize.png" : "maximize.png"),size.width - 20,3);
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
            panel.addButton(new PanelButton("minimize.png",new Action() {
              public void handleAction(Object context) throws ApplicationException
              {
                if (GUI.getView().snappedIn())
                  GUI.getView().snapOut();
                snapIn = false;
                c.redraw();
              }
            },Application.getI18n().tr("Minimieren")));
            panel.addButton(new PanelButton("document-save.png",new LogExport(),Application.getI18n().tr("Log-Ausgaben in Datei speichern")));
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
                color = Color.FOREGROUND;
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
