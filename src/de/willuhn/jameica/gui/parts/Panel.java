/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Panel.java,v $
 * $Revision: 1.11 $
 * $Date: 2007/11/02 01:19:38 $
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
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;
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

  private String titleText = "";
  private Part child       = null;
  
  private Composite myParent;
  private boolean border = true;
  
  private Vector minimizeListeners = new Vector();
  private int offset = 20;
  private boolean highlight = false;
  
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
  public void paint(final Composite parent) throws RemoteException
  {

    ///////////////////////////////
    // Eigenes Parent, damit wir ein GridLayout verwenden koennen
    myParent = new Composite(parent,this.border ? SWT.BORDER : SWT.NONE);
    myParent.setBackground(de.willuhn.jameica.gui.util.Color.BACKGROUND.getSWTColor());
    GridLayout myLayout = new GridLayout();
    myLayout.horizontalSpacing = 0;
    myLayout.verticalSpacing = 0;
    myLayout.marginHeight = 0;
    myLayout.marginWidth = 0;
    myParent.setLayout(myLayout);
    myParent.setLayoutData(new GridData(GridData.FILL_BOTH));
    //
    ///////////////////////////////
    
      ///////////////////////////////
      // Titelleiste
      Composite head = new Composite(myParent,SWT.NONE);
      GridLayout headLayout = new GridLayout();
      headLayout.horizontalSpacing = 0;
      headLayout.verticalSpacing = 0;
      headLayout.marginHeight = 0;
      headLayout.marginWidth = 0;
      head.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      head.setLayout(headLayout);
      head.setBackground(new Color(GUI.getDisplay(),255,255,255));

      //
      ///////////////////////////////
      

      ///////////////////////////////
      // Der Titel selbst
      title = SWTUtil.getCanvas(head,SWTUtil.getImage("panel-reverse.gif"), SWT.TOP | SWT.RIGHT);
      GridLayout layout2 = new GridLayout();
      layout2.marginHeight = 0;
      layout2.marginWidth = 0;
      layout2.horizontalSpacing = 0;
      layout2.verticalSpacing = 0;
      title.setLayout(layout2);

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
          gc.setFont(Font.H2.getSWTFont());
          gc.setForeground(highlight ? de.willuhn.jameica.gui.util.Color.COMMENT.getSWTColor() : de.willuhn.jameica.gui.util.Color.WIDGET_FG.getSWTColor());
          gc.drawText(titleText == null ? "" : titleText,8,1,true);
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
      
      // TODO: Hier weiter machen mit Drag&Drop fuer Panels
      int operations = DND.DROP_MOVE | DND.DROP_DEFAULT;
      DragSource dragSource = new DragSource(title, operations);

      Transfer[] transferTypes = new Transfer[] {TextTransfer.getInstance()};
      dragSource.setTransfer(transferTypes);

      dragSource.addDragListener(new DragSourceListener()
      {
        public void dragStart(DragSourceEvent dsEvent) {
          highlight = true;
          title.redraw();
        }
        public void dragSetData(DragSourceEvent dsEvent)
        {
        }
        public void dragFinished(DragSourceEvent dsEvent) {
          highlight = false;
          title.redraw();
        }
      });    

  }
}


/*********************************************************************
 * $Log: Panel.java,v $
 * Revision 1.11  2007/11/02 01:19:38  willuhn
 * @N Vorbereitungen fuer Drag&Drop von Panels
 * @N besserer Klick-Indikator in Statusleiste fuer Oeffnen des Logs
 *
 * Revision 1.10  2006/12/28 15:35:52  willuhn
 * @N Farbige Pflichtfelder
 *
 * Revision 1.9  2005/08/18 21:40:53  web0
 * @B layout bug wegen MacOS-Umstellung
 *
 * Revision 1.8  2005/07/29 15:10:16  web0
 * @N minimize/maximize icons
 *
 * Revision 1.7  2005/07/26 22:58:34  web0
 * @N background task refactoring
 *
 * Revision 1.6  2005/06/13 23:18:18  web0
 * *** empty log message ***
 *
 * Revision 1.5  2005/06/13 22:05:32  web0
 * *** empty log message ***
 *
 * Revision 1.4  2005/03/31 22:35:37  web0
 * @N flexible Actions fuer FormTexte
 *
 * Revision 1.3  2005/03/05 19:11:03  web0
 * *** empty log message ***
 *
 * Revision 1.2  2004/11/10 17:48:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/10 15:53:23  willuhn
 * @N Panel
 *
 **********************************************************************/