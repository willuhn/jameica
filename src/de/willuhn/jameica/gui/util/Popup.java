/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/Popup.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/07/18 13:01:08 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
 * TODO: Popup still under construction ;)
 */
public class Popup
{
  private final static int ALIGN_DEFAULT = SWT.BOTTOM | SWT.RIGHT; 
  private String title   = null;
  private String text    = null;
  private Point location = null;
  private int align      = ALIGN_DEFAULT;
  
  /**
   * ct
   * @param text anzuzeigender Text.
   */
  public Popup(String text)
  {
    this(text,null);
  }

  /**
   * ct
   * @param title der Titel.
   * @param text anzuzeigender Text.
   */
  public Popup(String title, String text)
  {
    this(text,null,null);
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
   * Oeffnet den Tooltip.
   */
  public void open()
  {
    Display display = GUI.getDisplay();
    
    final Shell shell = new Shell(GUI.getShell(), SWT.ON_TOP | SWT.TOOL);
    shell.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
  
    GridLayout gl = new GridLayout(2,false);
    gl.horizontalSpacing = 5;
    gl.verticalSpacing   = 5;
    shell.setLayout(gl);
    
    Composite comp = new Composite(shell,SWT.NONE);
    comp.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
    comp.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    comp.setBackgroundMode(SWT.INHERIT_FORCE);
    paint(comp);
  
    Button ok = new Button(shell,SWT.BORDER);
    ok.setLayoutData(new GridData(GridData.END));
    ok.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    ok.setText("  OK  ");
    ok.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e)
      {
        try
        {
          SWTUtil.disposeChildren(shell);
          shell.close();
          if (!shell.isDisposed())
            shell.dispose();
        }
        catch (Exception e2)
        {
          Logger.error("error while disposing popup",e2);
        }
      }
    });

    // Keine Position angegeben, dann nehmen wir die rechte untere Ecke des Jameica-Fensters.
    if (this.location == null)
    {
      Rectangle r = GUI.getShell().getBounds();
      this.location = new Point(r.x + r.width, r.y + r.height);
      this.align = SWT.RIGHT | SWT.BOTTOM;
    }

    final Point size = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    // Abhaengig von der Ausrichtung muessen wir die Groesse abziehen oder dazurechnen
    if ((this.align & SWT.RIGHT) != 0)
      this.location.x -= size.x;
    
    if ((this.align & SWT.BOTTOM) != 0)
      this.location.y -= size.y;

    shell.setBounds(this.location.x,this.location.y,size.x,size.y);
    shell.setVisible(true);
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
      label.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
      label.setBackground(GUI.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
      label.setText(this.title);
    }
    Label label = new Label(comp, SWT.NONE);
    label.setLayoutData(new RowData());
    label.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
    label.setBackground(GUI.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    label.setText(text);
  }
}


/*********************************************************************
 * $Log: Popup.java,v $
 * Revision 1.1  2008/07/18 13:01:08  willuhn
 * @N Popup
 *
 **********************************************************************/