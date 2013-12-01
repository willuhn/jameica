/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts;

import java.rmi.RemoteException;

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

/**
 * Kapselt eine einzeilige Meldung (Erfolg, Fehler, Hinweis), die farblich
 * gestaltet ist und ein Notification-Icon links enthaelt. 
 */
public class NotificationPanel implements Part
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
  
  private Type type       = null;
  private String text     = null;
  private Composite comp  = null;
  private CLabel label    = null;
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
    this.setText(type,text,false);
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
    gl.marginHeight = 1;
    gl.marginWidth = 1;
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

    GUI.getDisplay().asyncExec(new Runnable() {
      public void run()
      {
        if (label.isDisposed())
          return;
        
        label.setImage(type.icon != null ? SWTUtil.getImage(type.icon) : null);
        label.setForeground(type.fg.getSWTColor());
        label.setText(text);
        label.setBackground(type.bg.getSWTColor());
        comp.setBackground(type.fg.getSWTColor());
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
  
  /**
   * Resettet das Panel.
   */
  public void reset()
  {
    setText(Type.INVISIBLE,null,false);
  }

}


