/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Panel.java,v $
 * $Revision: 1.15 $
 * $Date: 2011/08/18 09:17:10 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign 
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts;

import java.rmi.RemoteException;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;

/**
 * Das ist ein Container, der weitere Parts aufnehmen kann, jedoch
 * die Anzeige um einen Titel und Rahmen erweitert.
 * @author willuhn
 */
public class Panel implements Part
{
  private final static Font FONT = Font.H2;
  private final static int TITLE_OFFSET_X = 8;
  private final static int TITLE_OFFSET_Y = 3;
  
  
  private String titleText = "";
  private Part child       = null;
  
  private Composite myParent;
  private boolean border = true;
  
  private Vector minimizeListeners = new Vector();
  private int offset = 20;
  
  private Canvas title;

  /**
   * ct.
   * @param title anzuzeigender Titel.
   * @param child Kind-Part welches angezeigt werden soll.
   */
  public Panel(String title, Part child)
  {
    this(title,child,true);
  }

  /**
   * ct.
   * @param title anzuzeigender Titel.
   * @param child Kind-Part welches angezeigt werden soll.
   * @param border legt fest, ob ein Rahmen um das Panel gezeichnet werden soll.
   */
  public Panel(String title, Part child, boolean border)
  {
    if (title != null)
      this.titleText = title;
    this.child = child;
    this.border = border;
  }

  /**
   * Fuegt dem Panel einen Listener zum Minimieren hinzu.
   * Wird ein solcher angegeben, wird automatisch ein Knopf zum
   * Minimieren angezeigt, der sonst ausgeblendet ist.
   * @param l der auszuloesende Listener.
   */
  public void addMinimizeListener(Listener l)
  {
    if (l == null)
      return;
    this.minimizeListeners.add(l);
  }

  /**
   * Setzt den anzuzeigenden Titel.
   * Dies kann auch nachtraeglich noch ausgefuehrt werden, wenn das
   * Panel schon angezeigt wird.
   * @param title
   */
  public void setTitle(String title)
  {
    this.titleText = title == null ? "" : title;
    if (this.title != null && !this.title.isDisposed())
        this.title.redraw();
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    // BUGZILLA 286 Wenn die Ueberschriftengroesse hoeher als die Bild-Groesse ist, dann strecken
    Image image = SWTUtil.getImage("panelbar.png");
    int imageHeight = image.getBounds().height;
    int fontHeight  = Font.getHeight(FONT) + (2 * TITLE_OFFSET_Y); // Abstand oben und unten brauchen wir auch etwas
    int height      = fontHeight > imageHeight ? fontHeight : imageHeight;
    
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
      head.setLayout(SWTUtil.createGrid(1,false));
      {
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = height;
        head.setLayoutData(gd);
      }

      //
      ///////////////////////////////
      

      ///////////////////////////////
      // Der Titel selbst
      title = SWTUtil.getCanvas(head,image, SWT.TOP | SWT.BOTTOM);
      title.setLayout(SWTUtil.createGrid(1,false));
      {
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = height;
        title.setLayoutData(gd);
      }

      final boolean mExists = minimizeListeners.size() > 0;

      
      if (mExists)
      {
        title.addMouseListener(new MouseAdapter()
        {
          public void mouseUp(MouseEvent e)
          {
            Rectangle size = title.getBounds();
            size.x += (size.width - offset);
            size.width = offset;
            if (size.contains(new Point(e.x,e.y)))
            {
              Event e1 = new Event();
              e1.data = e;
              for (int i=0;i<minimizeListeners.size();++i)
              {
                ((Listener)minimizeListeners.get(i)).handleEvent(e1);
              }
            }
          }
        });
      }
      
      title.addListener(SWT.Paint,new Listener()
      {
        public void handleEvent(Event event)
        {
          GC gc = event.gc;
          gc.setFont(FONT.getSWTFont());
          gc.drawText(titleText == null ? "" : titleText,TITLE_OFFSET_X,TITLE_OFFSET_Y,true);
          if (mExists)
          {
            Rectangle size = title.getBounds();
            gc.drawImage(SWTUtil.getImage("minimize.png"),size.width - 20,0);
          }
        }
      });
      //
      ///////////////////////////////
  
      ///////////////////////////////
      // Separator
      Label sep = new Label(myParent,SWT.SEPARATOR | SWT.HORIZONTAL);
      sep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      //
      ///////////////////////////////

      child.paint(myParent);
  }
}


/*********************************************************************
 * $Log: Panel.java,v $
 * Revision 1.15  2011/08/18 09:17:10  willuhn
 * @N BUGZILLA 286 - Testcode
 *
 * Revision 1.14  2011-05-03 10:13:10  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.13  2011-04-06 16:13:16  willuhn
 * @N BUGZILLA 631
 **********************************************************************/