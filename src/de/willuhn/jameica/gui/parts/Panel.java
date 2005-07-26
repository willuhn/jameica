/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Panel.java,v $
 * $Revision: 1.7 $
 * $Date: 2005/07/26 22:58:34 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign 
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
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

    ///////////////////////////////
    // Eigenes Parent, damit wir ein GridLayout verwenden koennen
    myParent = new Composite(parent,this.border ? SWT.BORDER : SWT.NONE);
    //myParent.setBackground(de.willuhn.jameica.gui.util.Color.BACKGROUND.getSWTColor());
    GridLayout myLayout = new GridLayout();
    myLayout.horizontalSpacing = 0;
    myLayout.verticalSpacing = 0;
    myLayout.marginHeight = 0;
    myLayout.marginWidth = 0;
    myParent.setLayout(myLayout);
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

      title.addListener(SWT.Paint,new Listener()
      {
        public void handleEvent(Event event)
        {
          GC gc = event.gc;
          gc.setFont(Font.H2.getSWTFont());
          gc.drawText(titleText == null ? "" : titleText,8,1,true);
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