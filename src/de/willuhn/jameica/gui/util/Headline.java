/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/Headline.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/05/23 15:30:52 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.gui.GUI;

/**
 * Malt eine Standard-Ueberschrift in den Dialog.
 * @author willuhn
 */
public class Headline
{

  private Label dotLine;

  /**
   * Erzeugt eine neue Standardueberschrift im angegebenen Composite mit dem uebergebenen Namen.
   * @param parent das Composite in dem die Ueberschrift gemalt werden soll.
   * @param headline Name der Ueberschrift.
   */
  public Headline(Composite parent, String headline)
  {
    Composite comp = new Composite(parent, SWT.NONE);
		comp.setBackground(Color.BACKGROUND.getSWTColor());
    comp.setLayout(new GridLayout());
    comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Label title = new Label(comp, SWT.NONE);
    title.setText(headline);
    title.setLayoutData(new GridData());
    title.setFont(Font.H1.getSWTFont());
		title.setBackground(Color.BACKGROUND.getSWTColor());

    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    data.heightHint = 3;
    dotLine = new Label(comp, SWT.NONE);
    dotLine.setText("");
		dotLine.setBackground(Color.BACKGROUND.getSWTColor());
    dotLine.setLayoutData(data);

    dotLine.addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        drawDottedLine(e);
      }
    });
  }
  
  /**
   * Zeichnet eine gepunktete Linie unter die Ueberschrift.
   * @param e PaintEvent
   */
  private void drawDottedLine(PaintEvent e)
  {
    Point p = dotLine.getSize();
    e.gc.setLineWidth(1); 
    e.gc.setLineStyle(SWT.LINE_SOLID); 
    e.gc.setForeground(new org.eclipse.swt.graphics.Color(GUI.getDisplay(), 125, 125, 125)); 
    for (int i=0; i<p.x;) {
      e.gc.drawLine(i,0,i,0);
      i=i+3;
    }
  }
}

/*********************************************************************
 * $Log: Headline.java,v $
 * Revision 1.2  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.1  2004/04/12 19:15:59  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.2  2004/02/18 01:40:29  willuhn
 * @N new white style
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.4  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.2  2003/12/05 18:43:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 **********************************************************************/