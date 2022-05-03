/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Default-Implementierung des DayRenderer-Interfaces.
 */
public class DayRendererImpl implements DayRenderer
{
  private final static int[] BACKGROUNDS = new int[]{SWT.COLOR_LIST_BACKGROUND,SWT.COLOR_TITLE_BACKGROUND, SWT.COLOR_WIDGET_BACKGROUND, SWT.COLOR_WHITE};
  
  // Cache fuer die Farb-Ressourcen
  private Map<RGB,Color> colorMap = new HashMap<RGB,Color>();
  
  private Composite comp           = null; // Unser Container-Composite
  private Label day                = null; // Label mit dem Tag des Monats
  private ScrolledComposite scroll = null; // Der Scroll-Container
  private Composite content        = null; // Das Composite fuer die Termine
  
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    // Alle Tage in gleicher Hoehe zeichnen
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.heightHint = parent.getBounds().height / 6;
    
    this.comp = new Composite(parent,SWT.NONE);
    this.comp.setLayoutData(gd);
    this.comp.setBackground(this.getBackground());
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

    this.scroll = new ScrolledComposite(this.comp,SWT.V_SCROLL);
    this.scroll.setLayoutData(new GridData(GridData.FILL_BOTH));
    this.scroll.setLayout(new GridLayout(1,false));
    this.scroll.addListener(SWT.Resize, new Listener() {
      public void handleEvent(Event event) {
        content.setSize(content.computeSize(scroll.getClientArea().width,SWT.DEFAULT));
      }
    });
    
    this.content = new Composite(scroll,SWT.NONE);
    this.content.setLayoutData(new GridData(GridData.FILL_BOTH));
    this.content.setLayout(new GridLayout(1,false));
    this.scroll.setContent(this.content);
  }
  
  /**
   * @see de.willuhn.jameica.gui.calendar.DayRenderer#update(de.willuhn.jameica.gui.calendar.DayRenderer.Status, java.util.Date, java.util.List)
   */
  public void update(Status status, Date date, List<Appointment> appointments)
  {
    // Content leeren
    SWTUtil.disposeChildren(this.content);
    
    try
    {
      ////////////////////////////////////////////////////////////////////////////
      // Tag ist nicht Bestandteil des Monats
      if (date == null || status == Status.OFF)
      {
        renderNone();
        return;
      }
      //
      ////////////////////////////////////////////////////////////////////////////

      ////////////////////////////////////////////////////////////////////////////
      // Tag rendern
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      this.day.setText(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)) + " ");
      //
      ////////////////////////////////////////////////////////////////////////////
      
      ////////////////////////////////////////////////////////////////////////////
      // Farbe ermitteln
      int weekday = cal.get(Calendar.DAY_OF_WEEK);
      final Color color = (status == Status.CURRENT || weekday == Calendar.SATURDAY || weekday == Calendar.SUNDAY) ? this.getHighlightBackground() : this.getBackground();

      // Und uebernehmen
      this.comp.setBackground(color);
      this.content.setBackground(color);
      
      // Die Farbe vom Day-Label machen wir einen Tick dunkler
      RGB rgb = color.getRGB();
      this.day.setBackground(getColor(new RGB(rgb.red - 15,rgb.green - 15, rgb.blue - 15)));
      //
      ////////////////////////////////////////////////////////////////////////////

      
      ////////////////////////////////////////////////////////////////////////////
      // Haben wir Termine an dem Tag?
      if (appointments != null && appointments.size() > 0)
      {
        // Wenn wir mehr als 2 Termine an dem Tag haben, verwenden
        // wir CLabel statt Label. Das verkuerzt den Text, damit alle
        // Eintraege reinpassen.
        boolean more = appointments.size() > 2;

        for (final Appointment a:appointments)
        {
          if (more)
            renderLong(a);
          else
            renderShort(a);
        }
      }
      //
      ////////////////////////////////////////////////////////////////////////////
    }
    finally
    {
      // Content noch neu zeichnen
      this.content.layout();
      
      // Scrolled-Composite neu resizen
      this.content.setSize(this.content.computeSize(SWT.DEFAULT,SWT.DEFAULT));
      
    }
  }
  
  /**
   * Rendert den Tag als nicht vorhanden.
   */
  private void renderNone()
  {
    // Tag entfernen
    this.day.setText("");

    // Hintergrund grau
    Color bg = de.willuhn.jameica.gui.util.Color.BACKGROUND.getSWTColor();
    this.comp.setBackground(bg);
    this.content.setBackground(bg);
    this.day.setBackground(bg);
  }

  /**
   * Erzeugt den Mouselistener fuer den Termin.
   * @param a der Termin.
   * @return der Listener.
   */
  private MouseListener createListener(final Appointment a)
  {
    return new MouseAdapter() {
      public void mouseDoubleClick(MouseEvent e)
      {
        if (e.button != 1)
          return;
        
        try
        {
          a.execute();
        }
        catch (ApplicationException ae)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
        }
      }
    };
  }
  
  // Die folgenden beiden Funktionen sehen ziemlich aehnlich aus
  // und man moechte meinen, der Code koenne auch gemeinsam verwendet
  // werden. Geht aber leider nicht. CLabel kann nettes Text-Shortening
  // fuer einzeilige Darstellung, hat jedoch keinen Mehrzeilen-Support.
  // Label kann zwar Mehrzeilen, jedoch kein Text-Shortening.
  // Und beide Klassen sind nicht von einander abgeleitet, haben
  // also keine gemeinsame Basisklasse. Die Funktionen heissen nur gleich.
  /**
   * Rendert die ausfuehrliche Ansicht eines Termins. 
   * @param a der Termin.
   */
  private void renderLong(final Appointment a)
  {
    CLabel label = new CLabel(this.content,SWT.LEFT);
    label.setFont(Font.SMALL.getSWTFont());

    try
    {
      label.setMargins(0,0,0,0);
    }
    catch (NoSuchMethodError e)
    {
      Logger.write(Level.DEBUG,"unable to set margins, SWT version probably too old",e);
    }
    
    label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    label.addMouseListener(createListener(a));

    // Farbe des Textes
    RGB fg = a.getColor();
    if (fg != null)
      label.setForeground(getColor(fg));

    // Name und Beschreibung
    String name = a.getName();
    label.setText(name);
    String desc = a.getDescription();
    if (desc == null || desc.length() == 0)
      desc = name;
    label.setToolTipText(desc);
  }
  
  /**
   * Rendert die gekuerzte Ansicht eines Termins.
   * Das ist sinnvoll, wenn wir an dem Tag mehrere Termine haben.
   * @param a der Termin.
   */
  private void renderShort(final Appointment a)
  {
    Label label = new Label(this.content,SWT.LEFT | SWT.WRAP);
    label.setFont(Font.SMALL.getSWTFont());
    label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    label.addMouseListener(createListener(a));

    // Farbe des Textes.
    RGB fg = a.getColor();
    if (fg != null)
      label.setForeground(getColor(fg));

    // Name und Beschreibung
    String name = a.getName();
    label.setText(name == null ? "" : name);
    String desc = a.getDescription();
    if (desc == null || desc.length() == 0)
      desc = name;
    label.setToolTipText(desc);
  }
  
  /**
   * Liefert die zu verwendende Hintergrundfarbe.
   * @return die zu verwendende Hintergrundfarbe.
   */
  protected Color getBackground()
  {
    final Display display = GUI.getDisplay();
    
    // Wir nehmen die erste, die existiert
    for (int i:BACKGROUNDS)
    {
      // Display#getSystemColor kann NULL liefern, wenn auf dem OS fuer diesen Code keine Farbe definiert ist
      final Color c = display.getSystemColor(i);
      if (c != null)
        return c;
    }
    
    // Wir verwenden unsere eigene Hintergrund-Farbe
    return de.willuhn.jameica.gui.util.Color.WHITE.getSWTColor();
  }
  
  /**
   * Liefert die Hintergrundfarbe der hervorgehobenen Tage - passend zur restlichen Hintergrundfarbe.
   * @return die Hintergrundfarbe der hervorgehobenen Tage.
   */
  protected Color getHighlightBackground()
  {
    final Color bg = this.getBackground();
    
    final int r = bg.getRed();
    final int g = bg.getGreen();
    final int b = bg.getBlue();
    
    
    // Mal schauen, ob das eine eher helle oder dunkle Farbe ist
    double avg = (r + g + b) / 3;
    
    // Wenn es eine eher hellere Farbe, machen wir die Highlight-Farbe etwas dunkler
    // Wenn es eine dunklere Farbe ist, dann etwas heller
    // Bei dunklen Themes sieht staerkere Aufhellung besser aus
    final int offset = (avg > 127) ? -15 : 40;
    
    return this.getColor(new RGB(r + offset,g + offset,b + offset));
  }
  
  /**
   * Liefert ein Color-Objekt fuer den angegebenen Farb-Code.
   * @param rgb der Farbcode.
   * @return das Farb-Objekt.
   */
  private Color getColor(RGB rgb)
  {
    Color color = this.colorMap.get(rgb);
    if (color != null && !color.isDisposed())
      return color;
    
    color = new Color(GUI.getDisplay(),rgb);
    this.colorMap.put(rgb,color);
    return color;
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
}
