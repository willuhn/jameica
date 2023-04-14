/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;

/**
 * Kapselt eine einzeilige Meldung (Erfolg, Fehler, Hinweis), die farblich
 * gestaltet ist und ein Notification-Icon links enthaelt. 
 */
public class NotificationPanel implements Part, MessageConsumer
{
  /**
   * Der Typ der Notification.
   */
  public static enum Type
  {
    /**
     * Erfolgsmeldung.
     */
    SUCCESS("ok.png",new Color(null,new RGB(216,244,178)), new Color(null,new RGB(56,75,30))),
    
    /**
     * Fehler.
     */
    ERROR("dialog-error.png",new Color(null,new RGB(255,216,185)), new Color(null,new RGB(111,41,35))),
    
    /**
     * Hinweis-Meldung.
     */
    INFO("gtk-info.png",new Color(null,new RGB(178,216,244)), new Color(null,new RGB(30,56,75))),
    
    /**
     * Unsichtbar.
     */
    INVISIBLE(null,Color.BACKGROUND,Color.BACKGROUND),

    ;

    private String icon = null;
    private Color fg    = null;
    private Color bg    = null;
    
    /**
     * ct.
     * @param icon Icon.
     * @param bg Hintergrundfarbe.
     * @param fg Vordergrundfarbe.
     */
    private Type(String icon, Color bg, Color fg)
    {
      this.icon = icon;
      this.bg   = bg;
      this.fg   = fg;
    }
  }

  // Mappt die Status-Codes auf die Enums des Notification-Panel.
  private final static Map<Integer,Type> TYPEMAP = new HashMap<Integer,Type>();
  
  static
  {
    if (!Application.inServerMode())
    {
      TYPEMAP.put(StatusBarMessage.TYPE_ERROR,  Type.ERROR);
      TYPEMAP.put(StatusBarMessage.TYPE_SUCCESS,Type.SUCCESS);
      TYPEMAP.put(StatusBarMessage.TYPE_INFO,   Type.INFO);
    }
  }

  private boolean receiveMessages = false;
  
  private Type type          = null;
  private String text        = null;
  private Composite comp     = null;
  private CLabel label       = null;
  private int border         = 1;
  private boolean background = true;
  private long lastUpdate = 0L;

  /**
   * ct.
   */
  public NotificationPanel()
  {
  }

  /**
   * ct.
   * @param type der Typ.
   * @param text der Text.
   */
  public NotificationPanel(Type type, String text)
  {
    this();
    this.setText(type,text,false);
  }
  
  /**
   * Legt fest, ob das Notification-Panel Statusbar-Messages empfangen und anzeigen soll.
   * @param b true, wenn das Notification-Panel Statusbar-Messages empfangen und anzeigen soll.
   */
  public void setReceiveMessages(boolean b)
  {
    if (b && !this.receiveMessages)
      Application.getMessagingFactory().registerMessageConsumer(this);
    if (!b && this.receiveMessages)
      Application.getMessagingFactory().unRegisterMessageConsumer(this);

    this.receiveMessages = b;
  }
  
  /**
   * Legt fest, ob der farbige Hintergrund gezeichnet werden soll.
   * @param background true, wenn der farbige Hintergrund gezeichnet werden soll.
   */
  public void setBackground(boolean background)
  {
    this.background = background;
  }
  
  /**
   * Legt die Rahmendicke fest.
   * @param border die Rahmendicke.
   */
  public void setBorder(int border)
  {
    this.border = border;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    if (this.comp != null || this.label != null)
      return;
    
    this.comp = new Composite(parent,SWT.NONE);
    this.comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    GridLayout gl = new GridLayout();
    gl.marginHeight = this.border;
    gl.marginWidth = this.border;
    gl.horizontalSpacing = 3;
    gl.verticalSpacing = 3;
    this.comp.setLayout(gl);
    
    this.label = new CLabel(comp,SWT.NONE);
    this.label.setLayoutData(new GridData(GridData.FILL_BOTH));
  }
  
  /**
   * Aktualisiert den Text mit dem angegebenen Typ an.
   * Der Text wird nach 10 Sekunden automatisch ausgeblendet.
   * @param t der Typ.
   * @param s der Text.
   */
  public void setText(Type t, String s)
  {
    this.setText(t,s,true);
  }

  /**
   * Aktualisiert den Text mit dem angegebenen Typ an.
   * @param t der Typ.
   * @param s der Text.
   * @param autoHide true, wenn der Text nach 10 Sekunden automatisch ausgeblendet werden soll.
   */
  private void setText(Type t, String s, final boolean autoHide)
  {
    this.type = t != null ? t : Type.INFO;
    this.text = s != null ? s : "";

    if (this.comp == null || this.label == null)
      return;
    
    final long currentUpdate = System.currentTimeMillis();

    try
    {
      
      GUI.getDisplay().asyncExec(new Runnable() {
        public void run()
        {
          if (label.isDisposed())
            return;
          
          label.setImage(type.icon != null ? SWTUtil.getImage(type.icon) : null);
          label.setForeground(type.fg.getSWTColor());
          
          // BUGZILLA 1623 - siehe auch
          // http://help.eclipse.org/indigo/index.jsp?topic=/org.eclipse.platform.doc.isv/reference/api/org/eclipse/swt/widgets/Label.html
          label.setText(text.replaceAll("&","&&"));
          label.setToolTipText(label.getText());
          
          if (background)
          {
            label.setBackground(type.bg.getSWTColor());
            comp.setBackground(type.fg.getSWTColor());
          }
          lastUpdate = currentUpdate;
          
          if (autoHide)
          {
            GUI.getDisplay().timerExec(7000,new Runnable()
            {
              public void run()
              {
                // nur entfernen, wenn wir der letzte Klick waren
                if (currentUpdate == lastUpdate)
                {
                  reset();
                }
              }
            });
          }
        }
      });
    }
    catch (OperationCanceledException oce) // passiert, wenn die GUI beendet wird (siehe GUI.getDisplay)
    {
      Logger.debug(oce.getMessage());
    }
  }
  
  /**
   * Resettet das Panel.
   */
  public void reset()
  {
    setText(Type.INVISIBLE,null,false);
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{StatusBarMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    if (message == null)
      return;
    
    StatusBarMessage m = (StatusBarMessage) message;
    final Type type = TYPEMAP.get(m.getType());
    this.setText(type,m.getText());
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  @Override
  public boolean autoRegister()
  {
    return false;
  }
}
