/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/StatusBarTextItem.java,v $
 * $Revision: 1.3 $
 * $Date: 2007/03/21 13:48:52 $
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.internal.parts.LogList;
import de.willuhn.jameica.gui.parts.Panel;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
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
  public void paint(Composite parent) throws RemoteException
  {
    text = new Label(parent, SWT.NONE);
    GridData at = new GridData(GridData.FILL_HORIZONTAL);
    at.verticalAlignment = GridData.CENTER;
    at.verticalIndent = 1;
    text.setAlignment(SWT.RIGHT);
    text.setLayoutData(at);
    text.setText("");


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
            }
          });
          panel.paint(GUI.getView().getSnapin());
          GUI.getView().snapIn();
          snapIn = true;
        }
        catch (RemoteException re)
        {
          Logger.error("unable to display log list",re);
        }
      }
    };

    String s = Application.getI18n().tr("Klicken Sie hier, um die letzten Zeilen des System-Logs anzuzeigen.");
    text.setToolTipText(s);
    text.addMouseListener(ma);
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

      final long currentClick = System.currentTimeMillis();
      GUI.getDisplay().asyncExec(new Runnable() {
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
            text.redraw();
            text.update();
            lastClick = currentClick;
          }
        }
      });
      GUI.getDisplay().asyncExec(new Runnable() {
        public void run()
        {
          GUI.getDisplay().timerExec(10000,new Runnable()
          {
            public void run()
            {
              if (currentClick == lastClick && !text.isDisposed())
              {
                // nur entfernen, wenn wir der letzte Klick waren
                text.setText("");
                text.redraw();
                text.update();
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
 * Revision 1.3  2007/03/21 13:48:52  willuhn
 * @N new abstract "WaitDialog"
 * @N force redraw in backgroundtask monitor/statusbar
 *
 * Revision 1.2  2006/04/18 16:49:46  web0
 * @C redesign in MessagingFactory
 *
 * Revision 1.1  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
 *********************************************************************/