/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/PanelButton.java,v $
 * $Revision: 1.9 $
 * $Date: 2011/09/12 15:27:46 $
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
import org.eclipse.swt.widgets.Control;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
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
  // Die Icons muessen 16x16 Pixel gross sein.
  private final static int ICON_WIDTH  = 16;
  private final static int ICON_HEIGHT = 16;
  
  private final static int ALPHA_DEFAULT  = 190;
  private final static int ALPHA_DISABLED =  50;
  private final static int ALPHA_NONE     = 255;
  
  /**
   * Dateiname des Hintergrund-Bildes
   */
  public final static String BG_DEFAULT   = "panelbar-button-default.png";
  
  /**
   * Dateiname des Hintergrund-Bildes fuer den Hover-Effekt.
   */
  public final static String BG_HOVER     = "panelbar-button-hover.png";

  private Canvas canvas  = null;
  private String icon    = null;
  private Action action  = null;
  private String tooltip = null;
  
  private int height     = 0;
  private int width      = 0;
  
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
    // Hoehe und Breite festlegen
    Rectangle bounds = SWTUtil.getImage(BG_DEFAULT).getBounds();
    try
    {
      this.height = ((GridData)parent.getLayoutData()).heightHint;
    }
    catch (Exception e)
    {
      // Fallback: hatte wohl kein Grid-Layout. Dann nehmen wir die Hoehe des Images
      this.height = bounds.height;
    }
    this.width  = bounds.width; // So breit wie das Hintergrund-Bild
    
    GridData gd   = new GridData();
    gd.widthHint  = this.width;
    gd.heightHint = this.height;
    this.canvas = new Canvas(parent,SWT.NONE);
    this.canvas.setBackground(GUI.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    this.canvas.setLayoutData(gd);

    if (this.tooltip != null)
      this.canvas.setToolTipText(this.tooltip);
    
    // Default
    this.canvas.addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e)
      {
        e.gc.setAlpha(ALPHA_NONE);
        drawBackground(e.gc,BG_DEFAULT);

        e.gc.setAlpha(isEnabled() ? ALPHA_DEFAULT : ALPHA_DISABLED);
        drawIcon(e.gc,false);
      }
    });

    // Mausklick
    this.canvas.addMouseListener(new MouseAdapter() {

      // Maus runtergedrueckt
      public void mouseDown(MouseEvent e)
      {
        if (e.button != 1 || !isEnabled())
          return;
        
        GC gc = new GC(canvas);

        gc.setAlpha(ALPHA_NONE);
        drawBackground(gc,BG_HOVER);
        drawIcon(gc,true);
        gc.dispose();
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
        
        // Innerhalb des Hintergrundes?
        final Image background = SWTUtil.getImage(BG_DEFAULT);
        Rectangle rect = background.getBounds();
        if (e.x > rect.width || e.y > height)
          return;

        // Eigentlich waere es optisch schoener, wenn wir das Zuruecksetzen der Icons
        // NACH dem Ausfuehren der Aktion machen. Da die Aktion aber dazu fuehren kann,
        // dass eine neue Seite geoeffnet wird, kann "canvas" anschliessend disposed
        // sein. Also muessen wir es vorher machen.
        GC gc = new GC(canvas);

        gc.setAlpha(ALPHA_NONE);
        drawBackground(gc,BG_DEFAULT);
        drawIcon(gc,false);
        gc.dispose();

        handleClick();
      }
    });
    
    this.canvas.addMouseTrackListener(new MouseTrackAdapter() {
      public void mouseExit(MouseEvent e)
      {
        GC gc = new GC(canvas);

        gc.setAlpha(ALPHA_NONE);
        drawBackground(gc,BG_DEFAULT);

        gc.setAlpha(isEnabled() ? ALPHA_DEFAULT : ALPHA_DISABLED);
        drawIcon(gc,false);
        gc.dispose();
      }
      
      public void mouseEnter(MouseEvent e)
      {
        if (!isEnabled())
          return;
        GC gc = new GC(canvas);

        gc.setAlpha(ALPHA_NONE);
        drawBackground(gc,BG_DEFAULT);

        gc.setAlpha(isEnabled() ? ALPHA_NONE : ALPHA_DISABLED);
        drawIcon(gc,false);
        gc.dispose();
      }
    });
  }
  
  /**
   * Malt den angegebenen Hintergrund auf den GC.
   * @param gc der Graphics-Context.
   * @param imageName Name der Bild-Datei.
   */
  private void drawBackground(GC gc, String imageName)
  {
    Image image = SWTUtil.getImage(imageName);
    Rectangle size = image.getBounds();

    // Einmal mit weiss dahinter malen, weil das Bild Transparenz besitzt
    gc.setBackground(GUI.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    gc.fillRectangle(size);
    
    gc.drawImage(image,0,0,size.width,size.height,0,0,this.width,this.height);
  }
  
  /**
   * Malt das Icon auf den GC.
   * @param gc der Graphics-Context.
   * @param pressed true, wenn das Icon angeklickt gezeichnet werden soll.
   */
  private void drawIcon(GC gc, boolean pressed)
  {
    // im gedrueckten Zustand ein Pixel weiter unten - das gibt den Effekt des Reindrueckens
    int y = (this.height - ICON_HEIGHT) / 2;
    if (pressed) y++;
    gc.drawImage(SWTUtil.getImage(this.icon),(this.width - ICON_WIDTH) / 2,y);
  }
  
  /**
   * Speichert das Icon des Panel-Button.
   * @param icon das Icon des Panel-Button.
   */
  public void setIcon(String icon)
  {
    if (icon == null)
      return;
    
    this.icon = icon;
    
    if (this.canvas != null && !this.canvas.isDisposed())
      this.canvas.redraw();
  }
  
  /**
   * Speichert einen Tooltip fuer den Panel-Button.
   * @param tooltip der Tooltip.
   */
  public void setTooltip(String tooltip)
  {
    if (tooltip == null)
      return;
    
    this.tooltip = tooltip;
    
    if (this.canvas != null && !this.canvas.isDisposed())
      this.canvas.setToolTipText(this.tooltip);
  }
  
  /**
   * Speichert die auszufuehrende Action.
   * @param a die auszufuehrende Action.
   */
  public void setAction(Action a)
  {
    this.action = a;
  }
  
  
  
  /**
   * Liefert das Control des Buttons.
   * @return das Control des Buttons.
   * Ist null, wenn paint() noch nicht aufgerufen wurde.
   */
  protected Control getControl()
  {
    return this.canvas;
  }
  
  /**
   * Fuehrt den Click-Aktion aus.
   */
  protected void handleClick()
  {
    try
    {
      action.handleAction(null);
    }
    catch (OperationCanceledException oce)
    {
      Logger.debug("operation cancelled");
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
    if (this.canvas != null)
    {
      GUI.getDisplay().asyncExec(new Runnable() {
        public void run()
        {
          if (canvas != null && !canvas.isDisposed())
            canvas.redraw();
        }
      });
    }
  }
}



/**********************************************************************
 * $Log: PanelButton.java,v $
 * Revision 1.9  2011/09/12 15:27:46  willuhn
 * @N Hintergrund-Jobs koennen nur abgebrochen werden, wenn sie noch laufen
 * @N Enabled-State live uebernehmen
 *
 * Revision 1.8  2011-09-12 15:09:58  willuhn
 * @N Redraw ausloesen, wenn der Enabled-Status geaendert wird
 *
 * Revision 1.7  2011-08-18 16:03:38  willuhn
 * @N BUGZILLA 286 - Panel-Code komplett refactored und in eine gemeinsame neue Klasse "TitlePart" verschoben. Damit muss der Code (incl. Skalieren der Panel) nur noch an einer Stelle gewartet werden. Und wir haben automatisch Panelbutton-Support an allen Stellen - nicht nur in der View, sondern jetzt auch im Snapin, in der Navi und sogar in Dialogen ;)
 *
 * Revision 1.6  2011-05-03 11:56:48  willuhn
 * @B GC wurde nicht korrekt disposed - siehe http://www.eclipse.org/forums/index.php?t=msg&goto=528906
 *
 * Revision 1.5  2011-04-14 16:58:48  willuhn
 * @N Globaler Shortcut <CTRL><P> zum Drucken (falls PanelButtonPrint aktiv ist)
 *
 * Revision 1.4  2011-04-07 16:49:56  willuhn
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