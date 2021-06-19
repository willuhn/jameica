/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;

/**
 * Kleines Tooltipp-Popup.
 */
public class Popup
{
  private static final int ALIGN_DEFAULT = SWT.BOTTOM | SWT.RIGHT;
  private static final int ALPHA = 180;
  
  private Shell shell    = null;
  private String title   = null;
  private String text    = null;
  private Point location = null;
  private int align      = ALIGN_DEFAULT;
  private int timeout    = -1;
  
  private Color bg = null;
  private Color fg = null;
  
  /**
   * ct
   * @param text anzuzeigender Text.
   */
  public Popup(String text)
  {
    this(null,text);
  }

  /**
   * ct
   * @param title der Titel.
   * @param text anzuzeigender Text.
   */
  public Popup(String title, String text)
  {
    this(title,text,null);
  }

  /**
   * ct
   * @param title der Titel.
   * @param text anzuzeigender Text.
   * @param location Position der linken oberen Ecke des Popups.
   */
  public Popup(String title, String text, Point location)
  {
    this(title,text,location,ALIGN_DEFAULT);
  }

  /**
   * ct
   * @param title der Titel.
   * @param text anzuzeigender Text.
   * @param location Position der linken oberen Ecke des Popups.
   * @param align Ausrichtung des Popups.
   * Welche Ecke des Popup-Fensters soll mit dem Parameter "location" gemeint.
   * Default-Wert ist "SWT.BOTTOM | SWT.RIGHT", also die rechte untere Ecke.
   * Moeglich ist z.Bsp. auf "SWT.TOP | SWT.LEFT".
   */
  public Popup(String title, String text, Point location, int align)
  {
    this.title = title;
    this.text = text;
    this.location = location;
    this.align = align;
  }

  /**
   * Legt ein Timeout in Sekunden fast, nachdem das Popup automatisch
   * ausgeblendet werden soll.
   * @param timeout Timeout in Sekunden.
   */
  public void setTimeout(int timeout)
  {
    this.timeout = timeout;
  }
  
  /**
   * Oeffnet den Tooltip.
   */
  public void open()
  {
    // Schliessen, wenn schon eines offen ist.
    if (this.shell != null)
      this.close();

    final Display display = GUI.getDisplay();

    // Wenn die konfigurierten Vorder- und Hintergrundfarben fehlen oder identisch sind,
    // verwenden wir eigene
    this.bg = display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
    this.fg = display.getSystemColor(SWT.COLOR_INFO_FOREGROUND);
    if (this.bg == null || this.fg == null || this.bg.equals(this.fg))
    {
      this.bg = de.willuhn.jameica.gui.util.Color.TOOLTIP_BG.getSWTColor();
      this.fg = de.willuhn.jameica.gui.util.Color.BLACK.getSWTColor();
    }
    
    this.shell = new Shell(GUI.getShell(), SWT.ON_TOP | SWT.TOOL);
    this.shell.setAlpha(ALPHA);
    
    if (this.bg != null)
      this.shell.setBackground(this.bg);
  
    GridLayout gl = new GridLayout(2,false);
    gl.horizontalSpacing = 5;
    gl.verticalSpacing   = 5;
    this.shell.setLayout(gl);
    
    Composite comp = new Composite(this.shell,SWT.NONE);
    comp.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
    if (this.bg != null) comp.setBackground(this.bg);
    comp.setBackgroundMode(SWT.INHERIT_FORCE);
    paint(comp);
  
    Button ok = new Button(this.shell,SWT.BORDER);
    ok.setLayoutData(new GridData(GridData.END));
    if (this.bg != null) ok.setBackground(this.bg);
    ok.setText("  OK  ");
    ok.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e)
      {
        close();
      }
    });

    // Keine Position angegeben, dann nehmen wir die rechte untere Ecke des Jameica-Fensters.
    if (this.location == null)
    {
      Rectangle r = GUI.getShell().getBounds();
      this.location = new Point(r.x + r.width, r.y + r.height);
      this.align = SWT.RIGHT | SWT.BOTTOM;
    }

    final Point size = this.shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    // Abhaengig von der Ausrichtung muessen wir die Groesse abziehen oder dazurechnen
    if ((this.align & SWT.RIGHT) != 0)
      this.location.x -= size.x;
    
    if ((this.align & SWT.BOTTOM) != 0)
      this.location.y -= size.y;

    this.shell.setBounds(this.location.x,this.location.y,size.x,size.y);
    this.shell.setVisible(true);
    
    if (this.timeout > 0)
    {
      display.timerExec(this.timeout * 1000, new Runnable()
      {
        public void run()
        {
          close();
        }
      });
    }
  }
  
  /**
   * Schliesst das Popup.
   * Muss normalerweise nicht manuell aufgerufen werden, weil
   * das entweder der User durch Klick auf "OK" macht oder das
   * Timeout (insofern eines definiert ist). Mit dieser Funktion
   * hier kann es jedoch programmatisch geschlossen werden.
   */
  public void close()
  {
    try
    {
      if (this.shell == null || this.shell.isDisposed())
        return;
      
      SWTUtil.disposeChildren(this.shell);
      this.shell.close();
      if (!this.shell.isDisposed())
        this.shell.dispose();
    }
    catch (Exception e2)
    {
      Logger.error("error while disposing popup",e2);
    }
  }
  
  /**
   * Malt den Text in den Tooltip.
   * Ist als separate Methode implementiert, damit sie in abgeleiteten Klassen
   * ueberschrieben werden kann.
   * @param comp das Composite, in das gezeichnet wird.
   */
  protected void paint(Composite comp)
  {
    comp.setLayout(new RowLayout(SWT.VERTICAL));
    if (this.title != null && this.title.length() > 0)
    {
      Label label = new Label(comp, SWT.NONE);
      label.setLayoutData(new RowData());
      label.setFont(Font.BOLD.getSWTFont());
      if (this.fg != null) label.setForeground(this.fg);
      if (this.bg != null) label.setBackground(this.bg);
      label.setText(this.title);
    }
    Label label = new Label(comp, SWT.NONE);
    label.setLayoutData(new RowData());
    if (this.fg != null) label.setForeground(this.fg);
    if (this.bg != null) label.setBackground(this.bg);
    label.setText(text == null ? "" : text);
  }
}
