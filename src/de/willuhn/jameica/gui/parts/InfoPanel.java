/**********************************************************************
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.internal.action.Program;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Platform;
import de.willuhn.util.ApplicationException;

/**
 * Kapselt ein UI-Element, welches links ein Icon und rechts daneben
 * Ueberschrift, Text und Buttons anzeigt. Das wird z.Bsp. in der Liste
 * der installierten Plugins verwendet.
 */
public class InfoPanel implements Part
{
  private String icon      = null;
  private String url       = null;
  private String title     = null;
  private String text      = null;
  private String comment   = null;
  private String tooltip   = null;
  private Color fgColor    = null;
  private Composite comp   = null;
  private List<Button> buttons = new LinkedList<Button>();
  
  /**
   * Legt das anzuzeigende Icon fest.
   * Sollte mindestens 64x64 Pixel gross sein, damit es nicht pixelig wird.
   * @param icon das anzuzeigende Icon.
   */
  public void setIcon(String icon)
  {
    this.icon = icon;
  }
  
  /**
   * Eine optional anzuzeigende URL.
   * @param url URL.
   */
  public void setUrl(String url)
  {
    this.url = url;
  }
  
  /**
   * Der anzuzeigende Titel.
   * @param title der Titel.
   */
  public void setTitle(String title)
  {
    this.title = title;
  }
  
  /**
   * Der anzuzeigende Text.
   * @param text der Text.
   */
  public void setText(String text)
  {
    this.text = text;
  }
  
  /**
   * Zeigt einen optionalen Kommentar an.
   * @param comment optionaler Kommentar.
   */
  public void setComment(String comment)
  {
    this.comment = comment;
  }
  
  /**
   * Zeigt einen optionalen Tooltip an. 
   * @param tooltip optionaler Tooltip.
   */
  public void setTooltip(String tooltip)
  {
    this.tooltip = tooltip;
  }
  
  /**
   * Fuegt einen Button hinzu.
   * @param button Button.
   */
  public void addButton(Button button)
  {
    this.buttons.add(button);
  }
  
  /**
   * Optionale Angabe der Textfarbe.
   * Per Default wird die Standard-Schrift-Farbe verwendet.
   * @param color die Schrift-Farbe.
   */
  public void setForeground(Color color)
  {
    this.fgColor = color;
  }
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    // Wir wurden schon gezeichnet.
    if (this.comp != null)
      return;
    
    // Wir unterscheiden hier beim Layout nach Windows/OSX und Rest.
    // Unter Windows und OSX sieht es ohne Rahmen und ohne Hintergrund besser aus
    org.eclipse.swt.graphics.Color bg = null;
    int border = SWT.NONE;
    
    int os = Application.getPlatform().getOS();
    if (os != Platform.OS_WINDOWS && os != Platform.OS_WINDOWS_64 && os != Platform.OS_MAC)
    {
      bg = GUI.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
      border = SWT.BORDER;
    }

    // 2-spaltige Anzeige. Links das Icon, rechts Eigenschaften und Buttons
    this.comp = new Composite(parent,border);
    this.comp.setBackground(bg);
    this.comp.setBackgroundMode(SWT.INHERIT_FORCE);
    this.comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    this.comp.setLayout(new GridLayout(2,false));
    
    // Linke Spalte mit dem Icon
    {
      int rows = 4;
      if (this.url != null && this.url.length() > 0) rows++;
      GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
      gd.verticalSpan = rows;
      Label icon = new Label(this.comp,SWT.NONE);
      icon.setBackground(bg);
      icon.setLayoutData(gd);
      icon.setImage(SWTUtil.getImage((this.icon != null && this.icon.length() > 0) ? this.icon : "package-x-generic-medium.png"));
    }
    
    // Rechte Spalte mit den Eigenschaften
    {
      // Name
      {
        Label title = new Label(this.comp,SWT.NONE);
        title.setBackground(bg);
        title.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        title.setFont(Font.H2.getSWTFont());
        if (this.fgColor != null)
          title.setForeground(this.fgColor.getSWTColor());
        title.setText(this.title != null ? this.title : "");
      }
      
      // Beschreibung
      {
        Label desc = new Label(this.comp,SWT.WRAP);
        desc.setBackground(bg);
        
        if (this.fgColor != null)
          desc.setForeground(this.fgColor.getSWTColor());
        
        desc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        desc.setText(this.text != null ? this.text : "");
      }
      
      // Link
      if (this.url != null && this.url.length() > 0)
      {
        Link l = new Link(this.comp,SWT.NONE);
        l.setBackground(bg);
        l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        if (this.fgColor != null)
          l.setForeground(this.fgColor.getSWTColor());
        
        l.setText("<A>" + this.url + "</A>");
        l.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent e)
          {
            try
            {
              new Program().handleAction(url);
            }
            catch (ApplicationException ae)
            {
              Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
            }
          }
        });
      }

      // Versionsnummer
      {
        Label comment = new Label(this.comp,SWT.NONE);
        comment.setBackground(bg);
        comment.setFont(Font.SMALL.getSWTFont());
        comment.setForeground(Color.COMMENT.getSWTColor());
        comment.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        comment.setText(this.comment != null ? this.comment : "");
        if (this.tooltip != null)
          comment.setToolTipText(this.tooltip);
      }
      
      ButtonArea buttons = new ButtonArea();
      for (Button b:this.buttons)
        buttons.addButton(b);

      buttons.paint(this.comp);

    }
  }
  
  /**
   * Disposed das Panel.
   */
  public void dispose()
  {
    try
    {
      if (this.comp == null || this.comp.isDisposed())
        return;
      SWTUtil.disposeChildren(this.comp);
      this.comp.dispose();
    }
    finally
    {
      this.comp = null;
    }
  }
}
