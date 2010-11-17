/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/calendar/DayRendererImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/11/17 16:59:56 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.calendar;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.GUI;

/**
 * Default-Implementierung des DayRenderer-Interfaces.
 */
public class DayRendererImpl implements DayRenderer
{
  private Color colorCurrent = null;
  private Color colorWeekend = null;
  
  private StyledText text = null;
  
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    this.colorCurrent = new Color(GUI.getDisplay(),new RGB(250,250,183));
    this.colorWeekend = new Color(GUI.getDisplay(),new RGB(240,240,240));
    this.text = new StyledText(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
    this.text.setLayoutData(new GridData(GridData.FILL_BOTH));
    this.text.setMargins(4,4,4,4);
    this.text.setBackground(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    this.text.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        disposeColors();
      }
    });
  }
  
  /**
   * Disposed die Farben.
   */
  private void disposeColors()
  {
    if (colorCurrent != null && !colorCurrent.isDisposed()) colorCurrent.dispose();
    if (colorWeekend != null && !colorWeekend.isDisposed()) colorWeekend.dispose();
  }
  
  /**
   * @see de.willuhn.jameica.gui.calendar.DayRenderer#update(de.willuhn.jameica.gui.calendar.DayRenderer.Status, java.util.Date)
   */
  public void update(Status status, Date date)
  {
    if (status == Status.OFF)
    {
      this.text.setText("");
      this.text.setBackground(de.willuhn.jameica.gui.util.Color.BACKGROUND.getSWTColor());
      return;
    }
    
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    int weekday = cal.get(Calendar.DAY_OF_WEEK);
    
    this.text.setText(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
    
    if (status == Status.CURRENT)
      this.text.setBackground(colorCurrent);
    else if (weekday == Calendar.SATURDAY || weekday == Calendar.SUNDAY)
      this.text.setBackground(colorWeekend);
    else
      this.text.setBackground(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE));
  }
}



/**********************************************************************
 * $Log: DayRendererImpl.java,v $
 * Revision 1.1  2010/11/17 16:59:56  willuhn
 * @N Erster Code fuer eine Kalender-Komponente, ueber die man z.Bsp. kommende Termine anzeigen kann
 *
 **********************************************************************/