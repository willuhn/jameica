/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/calendar/DayRendererImpl.java,v $
 * $Revision: 1.4 $
 * $Date: 2010/11/19 16:09:39 $
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Default-Implementierung des DayRenderer-Interfaces.
 */
public class DayRendererImpl implements DayRenderer
{
  // Cache fuer die Farb-Ressourcen
  private Map<RGB,Color> colorMap = new HashMap<RGB,Color>();
  
  private Composite comp    = null; // Unser Container-Composite
  private Label day         = null; // Label mit dem Tag des Monats
  private Composite content = null; // Das Composite fuer die Termine
  
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    this.comp = new Composite(parent,SWT.NONE);
    this.comp.setLayoutData(new GridData(GridData.FILL_BOTH));
    this.comp.setBackground(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    this.comp.setBackgroundMode(SWT.INHERIT_FORCE);
    this.comp.setLayout(SWTUtil.createGrid(1,false));
    this.comp.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        disposeColors();
      }
    });
    
    this.day = new Label(this.comp, SWT.RIGHT);
    this.day.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    this.content = new Composite(this.comp,SWT.NONE);
    this.content.setLayoutData(new GridData(GridData.FILL_BOTH));
    this.content.setLayout(new GridLayout(2,false));
  }
  
  /**
   * Disposed die Farben.
   */
  private void disposeColors()
  {
    synchronized (this.colorMap)
    {
      try
      {
        Iterator<Color> i = this.colorMap.values().iterator();
        while (i.hasNext())
        {
          Color c = i.next();
          if (c != null && !c.isDisposed())
            c.dispose();
        }
      }
      finally
      {
        this.colorMap.clear();
      }
    }
  }
  
  /**
   * @see de.willuhn.jameica.gui.calendar.DayRenderer#update(de.willuhn.jameica.gui.calendar.DayRenderer.Status, java.util.Date, java.util.List)
   */
  public void update(Status status, Date date, List<Appointment> appointments)
  {
    // Content entfernen
    SWTUtil.disposeChildren(this.content);
    

    // Tag ist nicht Bestandteil des Monats
    if (date == null || status == Status.OFF)
    {
      // Tag entfernen
      this.day.setText("");

      // Hintergrund grau
      this.comp.setBackground(de.willuhn.jameica.gui.util.Color.BACKGROUND.getSWTColor());
      this.content.setBackground(de.willuhn.jameica.gui.util.Color.BACKGROUND.getSWTColor());
      this.day.setBackground(de.willuhn.jameica.gui.util.Color.BACKGROUND.getSWTColor());

      // Content noch neu zeichnen
      this.content.layout();

      return;
    }

    // Tag rendern
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);

    this.day.setText(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)) + " ");
    
    // Farbe ermitteln
    int weekday = cal.get(Calendar.DAY_OF_WEEK);
    Color color = GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE);
    if (status == Status.CURRENT)
      color = getColor(250,250,183); // aktueller Tag
    else if (weekday == Calendar.SATURDAY || weekday == Calendar.SUNDAY)
      color = getColor(240,240,240);

    // Und uebernehmen
    this.comp.setBackground(color);
    this.content.setBackground(color);
    
    // Die Farbe vom Day-Label machen wir einen Tick dunkler
    RGB rgb = color.getRGB();
    this.day.setBackground(getColor(rgb.red - 15,rgb.green - 15, rgb.blue - 15));
    
    // Haben wir Termine an dem Tag?
    if (appointments != null && appointments.size() > 0)
    {
      // Wenn wir mehr als 1 Termin an dem Tag haben, verwenden
      // wir CLabel statt Label. Das verkuerzt den Text, damit alle
      // Eintraege reinpassen.
      boolean more = appointments.size() > 1;

      for (final Appointment a:appointments)
      {
        if (more)
        {
          CLabel label = new CLabel(this.content,SWT.LEFT);
          label.setMargins(0,0,0,0);
          label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
          
          String name = a.getName();
          label.setText(name);
          
          String desc = a.getDescription();
          if (desc == null || desc.length() == 0)
            desc = name;
          
          label.setToolTipText(desc);
        }
        else
        {
          Label label = new Label(this.content,SWT.LEFT | SWT.WRAP);
          label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

          String name = a.getName();
          label.setText(name);
          
          String desc = a.getDescription();
          if (desc == null || desc.length() == 0)
            desc = name;
          
          label.setToolTipText(desc);
        }

        Link link = new Link(this.content,SWT.WRAP);
        link.setText("<a>...</a>");
        link.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END | GridData.HORIZONTAL_ALIGN_BEGINNING));
        link.setToolTipText("Öffnen");
        link.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent e)
          {
            try
            {
              a.execute();
            }
            catch (ApplicationException ae)
            {
              Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
            }
          }
        });
      }
    }

    // Content noch neu zeichnen
    this.content.layout();
  }
  
  /**
   * Liefert ein Color-Objekt fuer den angegebenen Farb-Code.
   * @param red Rot-Wert.
   * @param green Gruen-Wert.
   * @param blue Blau-Wert.
   * @return das Farb-Objekt.
   */
  private Color getColor(int red, int green, int blue)
  {
    RGB rgb = new RGB(red,green,blue);
    Color color = this.colorMap.get(rgb);
    if (color != null)
      return color;
    
    color = new Color(GUI.getDisplay(),rgb);
    this.colorMap.put(rgb,color);
    return color;
  }
}



/**********************************************************************
 * $Log: DayRendererImpl.java,v $
 * Revision 1.4  2010/11/19 16:09:39  willuhn
 * @B Content-Composite wurde beim Neuladen nicht leer gemacht
 *
 * Revision 1.3  2010-11-19 15:46:21  willuhn
 * @B minor fixes
 *
 * Revision 1.2  2010-11-19 13:44:15  willuhn
 * @N Appointment-API zum Anzeigen von Terminen im Kalender.
 *
 * Revision 1.1  2010-11-17 16:59:56  willuhn
 * @N Erster Code fuer eine Kalender-Komponente, ueber die man z.Bsp. kommende Termine anzeigen kann
 *
 **********************************************************************/