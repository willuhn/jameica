/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Panel.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/11/10 17:48:18 $
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tracker;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;

/**
 * Das ist ein Container, der weitere Parts aufnehmen kann, jedoch
 * die Anzeige um einen Titel und Rahmen erweitert und via Drag&Drop
 * verschiebbar ist.
 * @author willuhn
 */
public class Panel implements Part
{

  private String titleText = "";
  private Part child       = null;
  
  private CLabel title;
  private Composite myParent;

  private Tracker tracker;
  
  /**
   * ct.
   * @param title anzuzeigender Titel.
   * @param child Kind-Part welches angezeigt werden soll.
   */
  public Panel(String title, Part child)
  {
    if (title != null)
      this.titleText = title;
    this.child = child;
  }

  /**
   * Setzt den anzuzeigenden Titel.
   * Dies kann auch nachtraeglich noch ausgefuehrt werden, wenn das
   * Panel schon angezeigt wird.
   * @param title
   */
  public void setTitle(String title)
  {
    if (title == null)
      return;
    this.titleText = title;
    if (this.title != null && !this.title.isDisposed())
      this.title.setText(title);
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {

    ///////////////////////////////
    // Eigenes Parent, damit wir ein GridLayout verwenden koennen
    myParent = new Composite(parent,SWT.BORDER);
    myParent.setBackground(de.willuhn.jameica.gui.util.Color.BACKGROUND.getSWTColor());
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
      GridLayout headLayout = new GridLayout(2,true);
      headLayout.horizontalSpacing = 0;
      headLayout.verticalSpacing = 0;
      headLayout.marginHeight = 0;
      headLayout.marginWidth = 0;
      head.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      head.setLayout(headLayout);
      head.setBackground(new Color(GUI.getDisplay(),255,255,255));

//      form.addListener(SWT.MouseDown, new Listener() {
//        public void handleEvent (Event e) {
//          Tracker tracker = new Tracker(myParent,0);
//          Point pt = myParent.getSize();
//          tracker.setRectangles(new Rectangle[]
//          {
//            new Rectangle(e.x,e.y,pt.x,pt.y)
//          });
//          tracker.open();
//        }
//      });

      //
      ///////////////////////////////
      
        ///////////////////////////////
        // Der Titel selbst
        title = new CLabel(head,SWT.NONE);
        title.setBackground(new Color(GUI.getDisplay(),255,255,255));
        title.setLayoutData(new GridData(GridData.FILL_BOTH));
        title.setFont(Font.H2.getSWTFont());
        title.setText(titleText);
    
        Label image = new Label(head,SWT.NONE);
        image.setImage(SWTUtil.getImage("gradient.gif"));
        title.setBackground(new Color(GUI.getDisplay(),255,255,255));
        image.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));


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
 * Revision 1.2  2004/11/10 17:48:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/10 15:53:23  willuhn
 * @N Panel
 *
 **********************************************************************/