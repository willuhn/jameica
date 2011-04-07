/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/PanelButton.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/04/07 16:49:56 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Button, der oben rechts im Panel angezeigt wird.
 */
public class PanelButton implements Part
{
  private final static int POS_X = 12;
  private final static int POS_Y = 2;
  
  private final static int ALPHA_DEFAULT  = 150;
  private final static int ALPHA_DISABLED =  50;
  private final static int ALPHA_NONE     = 255;
  
  private final static String BG_DEFAULT   = "panelbar-button-default.png";
  private final static String BG_HOVER     = "panelbar-button-hover.png";

  private String icon    = null;
  private Action action  = null;
  private String tooltip = null;
  
  private boolean enabled = true;
  
  /**
   * ct.
   * @param icon Icon fuer den Button
   * @param action auszufuehrende Aktion beim Klick.
   * @param tooltip anzuzeigender Tooltop beim Ueberfahren mit der Maus.
   */
  public PanelButton(String icon, Action action, String tooltip)
  {
    this.icon    = icon;
    this.action  = action;
    this.tooltip = tooltip;
  }
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    final Image bgDefault = SWTUtil.getImage(BG_DEFAULT);
    
    final Canvas canvas = new Canvas(parent,SWT.NONE);

    GridData gd = new GridData();
    gd.widthHint = bgDefault.getBounds().width;
    canvas.setLayoutData(gd);

    if (this.tooltip != null)
      canvas.setToolTipText(this.tooltip);
    
    
    // Default
    canvas.addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e)
      {
        e.gc.setAlpha(ALPHA_NONE);
        e.gc.drawImage(bgDefault,0,0);

        e.gc.setAlpha(isEnabled() ? ALPHA_DEFAULT : ALPHA_DISABLED);
        e.gc.drawImage(SWTUtil.getImage(icon),POS_X,POS_Y);
      }
    });

    // Mausklick
    canvas.addMouseListener(new MouseAdapter() {

      // Maus runtergedrueckt
      public void mouseDown(MouseEvent e)
      {
        if (e.button != 1 || !isEnabled())
          return;
        
        GC gc = new GC(canvas);

        gc.setAlpha(ALPHA_NONE);
        gc.drawImage(SWTUtil.getImage(BG_HOVER),0,0);
        gc.drawImage(SWTUtil.getImage(icon),POS_X,POS_Y + 1);  // 1 Pixel weiter unten - gibt den Effekt des Reindrueckens
      }

      // Maus losgelassen
      public void mouseUp(MouseEvent e)
      {
        if (e.button != 1 || action == null || !isEnabled())
          return;

        // Wir checken, ob sich die Maus noch innerhalb des Buttons
        // befindet. Hintergrund: Bei normalen Buttons kann man
        // das Ausloesen der Aktion noch verhindern, wenn man vor
        // dem Loslassen des Buttons den Zeiger ausserhalb des Buttons
        // bewegt
        if (e.x < 0 || e.y < 0)
          return; // oben oder links raus bewegt
        
        final Image bgDefault = SWTUtil.getImage(BG_DEFAULT);

        Rectangle rect = bgDefault.getBounds();
        if (e.x > rect.width || e.y > rect.height)
          return;

        // Eigentlich waere es optisch schoener, wenn wir das Zuruecksetzen der Icons
        // NACH dem Ausfuehren der Aktion machen. Da die Aktion aber dazu fuehren kann,
        // dass eine neue Seite geoeffnet wird, kann "canvas" anschliessend disposed
        // sein. Also muessen wir es vorher machen.
        GC gc = new GC(canvas);

        gc.setAlpha(ALPHA_NONE);
        gc.drawImage(bgDefault,0,0);
        gc.drawImage(SWTUtil.getImage(icon),POS_X,POS_Y);

        try
        {
          action.handleAction(null);
        }
        catch (OperationCanceledException oce)
        {
          Logger.info("operation cancelled");
        }
        catch (ApplicationException ae)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
        }
        catch (Exception ex)
        {
          Logger.error("unable to execute action",ex);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler: {0}",ex.getMessage()),StatusBarMessage.TYPE_ERROR));
        }
      }
    });
    
    canvas.addMouseTrackListener(new MouseTrackAdapter() {
      public void mouseExit(MouseEvent e)
      {
        GC gc = new GC(canvas);

        gc.setAlpha(ALPHA_NONE);
        gc.drawImage(SWTUtil.getImage(BG_DEFAULT),0,0);

        gc.setAlpha(isEnabled() ? ALPHA_DEFAULT : ALPHA_DISABLED);
        gc.drawImage(SWTUtil.getImage(icon),POS_X,POS_Y);
      }
      
      public void mouseEnter(MouseEvent e)
      {
        if (!isEnabled())
          return;
        GC gc = new GC(canvas);

        gc.setAlpha(ALPHA_NONE);
        gc.drawImage(SWTUtil.getImage(BG_DEFAULT),0,0);

        gc.setAlpha(isEnabled() ? ALPHA_NONE : ALPHA_DISABLED);
        gc.drawImage(SWTUtil.getImage(icon),POS_X,POS_Y);
      }
    });
  }
  
  /**
   * Prueft, ob der Button derzeit anklickbar sein soll.
   * @return true, wenn er anklickbar sein soll.
   */
  public boolean isEnabled()
  {
    return this.enabled;
  }
  
  /**
   * Legt fest, ob der Button anklickbar sein soll.
   * @param b true, wenn er anklickbar sein soll, sonst false.
   */
  public void setEnabled(boolean b)
  {
    this.enabled = b;
  }
}



/**********************************************************************
 * $Log: PanelButton.java,v $
 * Revision 1.4  2011/04/07 16:49:56  willuhn
 * @N Rudimentaere GUI-Klassen fuer die Druck-Anbindung
 *
 * Revision 1.3  2011-04-07 15:09:15  willuhn
 * @N Exception-Handling
 *
 * Revision 1.2  2011-04-07 08:31:01  willuhn
 * @N Setter zum Deaktivieren des Panel-Buttons
 *
 * Revision 1.1  2011-04-06 16:13:16  willuhn
 * @N BUGZILLA 631
 *
 **********************************************************************/