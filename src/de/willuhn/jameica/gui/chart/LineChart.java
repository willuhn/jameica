/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/chart/Attic/LineChart.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/07/11 14:30:06 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.chart;

import java.rmi.RemoteException;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Line-Charts.
 */
public class LineChart implements Chart
{

  private String title = null;
  private int border = 20;
  private String xFrom = null;
  private String xTo = null;
  private Vector data = new Vector();
  private Formatter format = null;
  
  /**
   * @see de.willuhn.jameica.gui.chart.Chart#setTitle(java.lang.String)
   */
  public void setTitle(String title)
  {
    this.title = title;
  }

  /**
   * Legt die Rahmenbreite fest.
   * @param border
   */
  public void setBorder(int border)
  {
    if (border < 0)
      return;
    this.border = border;
  }
  
  /**
   * Legt einen Formatter fuer die Y-Skala fest.
   * @param format
   */
  public void setDecimalFormatter(Formatter format)
  {
    this.format = format;
  }
  
  /**
   * Speichert zwei Label entlang der X-Koordinate.
   * @param from Label am Koordinatenursprung
   * @param to Label am Koordinaten-Ende.
   */
  public void setXLabel(String from, String to)
  {
    this.xFrom = from;
    this.xTo = to;
  }

  /**
   * @see de.willuhn.jameica.gui.chart.Chart#addData(de.willuhn.datasource.GenericIterator, java.lang.String, org.eclipse.swt.graphics.Color, org.eclipse.swt.graphics.Color)
   */
  public void addData(GenericIterator items, String attribute, Color lineColor, Color fillColor) throws RemoteException
  {
    data.add(new Data(items,attribute,lineColor,fillColor));
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(final Composite parent) throws RemoteException
  {
    final Display d = GUI.getDisplay();

    parent.setBackground(new Color(d,255,255,255));

    int max = 0;
    int min = 0;
    int num = 0;

    for (int j=0;j<data.size();++j)
    {
      Data line = (Data) data.get(j);
      int[] values = line.getValues();
      for (int i=0;i<values.length;++i)
      {
        if (values.length > num) num = values.length;
        if (values[i] > max) max = values[i];
        if (values[i] < min) min = values[i];
      }
    }
    
    final int shift = min < 0 ? Math.abs(min) : 0;
    final int scale = max + shift;
    final int maxValue = max;
    final int minValue = min;
    final int maxNum = num;
    
    parent.addListener(SWT.Paint, new Listener()
    {
      public void handleEvent(Event event)
      {
        Rectangle area = parent.getBounds();
        
        GC gc = event.gc;
        gc.setLineWidth(1);
       
        gc.setForeground(new Color(d,150,150,150));
        gc.drawLine(border,border,border,area.height - border);
        gc.drawLine(border,area.height - border,area.width - border,area.height - border);
        
        int height = area.height - (2*border);
        double xfactor = (double) (area.width - (2*border)) / (double) maxNum;
        double yfactor = (double) height / (double) scale;
        double yShift  = (double) shift * yfactor;

        if (xFrom != null) gc.drawText(xFrom,border,area.height - border + 3,true);
        if (xTo != null)   gc.drawText(xTo,area.width - (2 * border) - 30,area.height - border + 3,true);
        if (title != null) gc.drawText(title,4,4,true);

        gc.setLineWidth(2);
        for (int j=0;j<data.size();++j)
        {
          Data line = (Data) data.get(j);
          try
          {
            int[] values = line.getValues();
            gc.setForeground(line.lineColor);
            
            int[] poly = new int[2 * (values.length + 2)];

            int count=0;
            poly[count++] = border;
            poly[count++] = height + border;
            int lastX = 0;
            for (int i=0;i<values.length-1;++i)
            {
              int x1 = (int) (i * xfactor) + border + 1;
              int x2 = (int) ((i+1) * xfactor) + border + 1;
              int y1 = height + border - ((int) (values[i] * yfactor)) - (int) yShift;
              int y2 = height + border - ((int) (values[i+1] * yfactor)) - (int) yShift;
              lastX = x2;
              gc.drawLine(x1,y1,x2,y2);
              poly[count++] = x1;
              poly[count++] = y1;
              if (i == values.length-2)
              {
                poly[count++] = x2;
                poly[count++] = y2;
              }
            }
            poly[count++] = lastX;
            poly[count++] = height + border;
            gc.setBackground(line.fillColor);
            gc.fillPolygon(poly);
          }
          catch (RemoteException e)
          {
            Logger.error("unable to draw line",e);
            continue;
          }
        }

        if (shift != 0)
        {
          gc.setLineWidth(1);
          gc.setForeground(new Color(d,210,210,210));
          int y = area.height - border - (int)yShift;
          gc.setLineStyle(SWT.LINE_DASHDOT);
          gc.drawLine(border,y,area.width - border,y);
          gc.setForeground(new Color(d,150,150,150));
          gc.drawText("0",5,y-6,true);
        }

        gc.setBackground(new Color(d,255,255,255));
        gc.setForeground(new Color(d,150,150,150));
        gc.drawText(format != null ? format.format(new Double(maxValue)) : maxValue+"",5,border);
        gc.drawText(format != null ? format.format(new Double(minValue)) : minValue+"",5,height + border - 14);

      }
    
    });
  }

  private class Data
  {
    private int[] values         = null;
    private GenericIterator list = null;
    private String attribute     = null;
    private Color lineColor      = null;
    private Color fillColor      = null;
    
    private Data(GenericIterator values, String attribute, Color lineColor, Color fillColor)
    {
      this.lineColor = lineColor;
      this.fillColor = fillColor;
      this.list = values;
      this.attribute = attribute;
    }
    
    private int[] getValues() throws RemoteException
    {
      if (this.values == null)
      {
        int count = 0;
        this.values = new int[list.size()];
        GenericObject object = null;
        while (list.hasNext())
        {
          object = list.next();
          Object o = object.getAttribute(attribute);
          if (o == null || !(o instanceof Number))
            continue;
          this.values[count++] = ((Number)o).intValue();
        }
        
      }
      return this.values;
    }
  }
}


/*********************************************************************
 * $Log: LineChart.java,v $
 * Revision 1.2  2005/07/11 14:30:06  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/07/11 13:50:11  web0
 * @N Linecharts
 *
 **********************************************************************/