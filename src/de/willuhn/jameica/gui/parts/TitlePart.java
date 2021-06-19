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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Platform;
import de.willuhn.logging.Logger;

/**
 * Eine Komponente, die einen Titel mit Farberverlauf im Hintergrund anzeigt.
 * Auf der rechten Seite koennen Buttons mit Aktionen hinzugefuegt werden.
 */
public class TitlePart implements Part
{
  private static final Font FONT          = Font.H2;
  private static final int TITLE_OFFSET_X = 8;
  private static final int TITLE_OFFSET_Y = 3;
  
  private Composite myParent  = null;
  private Canvas title        = null;

  private String titleText    = null;
  private boolean border      = true;

  private Composite panelButtons    = null;
  private List<PanelButton> buttons = new LinkedList<PanelButton>();
  

  /**
   * ct.
   * @param title anzuzeigender Titel.
   */
  public TitlePart(String title)
  {
    this(title,true);
  }

  /**
   * ct.
   * @param title anzuzeigender Titel.
   * @param border legt fest, ob ein Rahmen um das Composite gezeichnet werden soll.
   */
  public TitlePart(String title, boolean border)
  {
    this.titleText = title;
    this.border = border;
  }

  /**
   * Setzt den anzuzeigenden Titel.
   * @param text der Titel.
   */
  public void setTitle(String text)
  {
    this.titleText = text;
    if (this.title != null)
    {
      GUI.getDisplay().syncExec(new Runnable() {
        public void run()
        {
          if (title.isDisposed())
            return;
          title.redraw();
        }
      });
    }
  }

  /**
   * Fuegt einen Button hinzu.
   * @param b der Button.
   */
  public void addButton(PanelButton b)
  {
    // Button zur Liste hinzufuegen
    this.buttons.add(b);
    updateButtons();
  }
  
  /**
   * Entfernt alle Buttons wieder.
   */
  public void clearButtons()
  {
    for (PanelButton b:this.buttons)
    {
      try
      {
        Composite c = (Composite) b.getControl();
        if (c == null || c.isDisposed())
          return;
        SWTUtil.disposeChildren(c);
        c.dispose();
      }
      catch (Exception e)
      {
        Logger.error("error while disposing panel-button",e);
      }
    }
    this.buttons.clear();
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent)
  {
    if (myParent != null)
      return;
    
    // BUGZILLA 286 Wenn die Ueberschriftengroesse hoeher als die Bild-Groesse ist, dann strecken
    Image image = SWTUtil.getImage("panelbar.png");
    int imageHeight = image.getBounds().height;
    int fontHeight  = Font.getHeight(FONT) + (2 * TITLE_OFFSET_Y); // Abstand oben und unten brauchen wir auch etwas
    int height      = Math.max(imageHeight,fontHeight);
    
    ///////////////////////////////
    // Eigenes Parent, damit wir ein GridLayout verwenden koennen
    myParent = new Composite(parent,this.border ? SWT.BORDER : SWT.NONE);
    myParent.setLayout(SWTUtil.createGrid(1,false));
    myParent.setLayoutData(new GridData(GridData.FILL_BOTH));
    //
    ///////////////////////////////
    
    ///////////////////////////////
    // Titelleiste
    Composite head = new Composite(myParent,SWT.NONE);
    head.setLayout(SWTUtil.createGrid(2,false));
    {
      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.heightHint = height;
      head.setLayoutData(gd);
    }

    //
    ///////////////////////////////
      

    ///////////////////////////////
    // Der Titel selbst
    title = SWTUtil.getCanvas(head,image, SWT.BOTTOM | SWT.RIGHT);
    title.setBackground(GUI.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    title.setLayout(SWTUtil.createGrid(1,false));
    {
      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.heightHint = height;
      title.setLayoutData(gd);
    }
    title.addListener(SWT.Paint,new Listener()
    {
      public void handleEvent(Event event)
      {
        GC gc = event.gc;
        gc.setFont(FONT.getSWTFont());
        // kein Hintergrund hinter dem Text malen
        // Ist zumindest unter Linux nicht noetig. Windows und OSX muesste man mal noch testen
        gc.setBackground(GUI.getDisplay().getSystemColor(SWT.TRANSPARENT));
        
        // Das Skalieren ist unter Windows nicht noetig
        int osx = TITLE_OFFSET_X;
        int osy = TITLE_OFFSET_Y;
        int os = Application.getPlatform().getOS();
        if (os != Platform.OS_WINDOWS && os != Platform.OS_WINDOWS_64)
        {
          osx = SWTUtil.scaledPx(osx);
          osy = SWTUtil.scaledPx(osy);
        }
        gc.drawText(titleText == null ? "" : titleText,osx,osy,true);
      }
    });
    //
    ///////////////////////////////

    ///////////////////////////////
    // Bereich fuer die Buttons
    this.panelButtons = new Composite(head,SWT.NONE);
    {
      GridData gd = new GridData();
      gd.heightHint = height;
      gd.widthHint = 0; // Erstmal ohne Breitenangabe - gibts erst, wenn wirklich Buttons da sind
      this.panelButtons.setLayoutData(gd);
    }
    //
    ///////////////////////////////

    ///////////////////////////////
    // Separator
    Label sep = new Label(myParent,SWT.SEPARATOR | SWT.HORIZONTAL);
    sep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    //
    ///////////////////////////////
    
    // Einmal initial die Buttons zeichnen
    updateButtons();
  }
  
  /**
   * Aktualisiert die Buttons.
   */
  private void updateButtons()
  {
    if (panelButtons == null || panelButtons.isDisposed())
      return;

    GUI.getDisplay().asyncExec(new Runnable() {
      public void run()
      {
        if (panelButtons.isDisposed())
          return;
        
        try
        {
          // Kurz ausblenden - sieht beim Aufbau sauberer aus
          panelButtons.setVisible(false);

          int size = buttons.size();
          
          // Aktuelle Buttons disposen
          SWTUtil.disposeChildren(panelButtons);

          
          // Neues Layout basierend auf der Anzahl der Buttons anlegen
          panelButtons.setLayout(SWTUtil.createGrid(size,false));
          
          // Breite errechnen
          Image background = SWTUtil.getImage(PanelButton.BG_DEFAULT);
          GridData gd = (GridData) panelButtons.getLayoutData();
          gd.widthHint = background.getBounds().width * size;
          

          // Alle Buttons zeichnen - von rechts nach links
          for (int i=size-1;i>=0;i--)
          {
            buttons.get(i).paint(panelButtons);
          }
          
          // Das Neuberechnen des Parent fuehrt dazu, dass wir mehr Breite fuer die neuen Buttons kriegen
          panelButtons.getParent().layout(); 
          
          // Und wir zeichnen uns selbst neu
          panelButtons.layout();
        }
        catch (Exception e)
        {
          Logger.error("unable to paint panel buttons",e);
        }
        finally
        {
          panelButtons.setVisible(true);
        }
      }
    });
  }
  
  /**
   * Liefert das Composite, in dem dann weiterer Inhalt folgen kann.
   * @return Composite.
   */
  public Composite getComposite()
  {
    return this.myParent;
  }
} 
