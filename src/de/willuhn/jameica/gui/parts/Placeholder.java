/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Grafisch gestaltete Platzhalter-Komponente.
 */
public class Placeholder implements Part
{
  private final static I18N i18n = Application.getI18n();
  
  /**
   * Vorkonfigurierte Styles.
   */
  public enum Style
  {
    /**
     * Vorkonfigurierter Style fuer "Keine Daten".
     */
    EMPTY("placeholder-empty.png",i18n.tr("Keine Daten")),

    /**
     * Vorkonfigurierter Style fuer "Lade...".
     */
    LOADING("placeholder-loading.png",i18n.tr("Lade...")),

    /**
     * Vorkonfigurierter Style fuer "Bitte warten...".
     */
    WAIT("placeholder-wait.png",i18n.tr("Bitte warten...")),

    /**
     * Vorkonfigurierter Style fuer "Fertig, nichts zu tun".
     */
    DONE("placeholder-done.png",i18n.tr("Fertig")),

    /**
     * Vorkonfigurierter Style fuer "Fehler".
     */
    ERROR("placeholder-error.png",i18n.tr("Fehler")),

    ;
    
    private String image = null;
    private String title = null;
    
    /**
     * ct.
     * @param image
     * @param title
     */
    private Style(String image, String title)
    {
      this.image = image;
      this.title = title;
    }
  }
  
  private String image = null;
  private String title = null;
  private String text = null;
  
  /**
   * ct.
   * @param style optionale Angabe des Style.
   */
  public Placeholder(Style style)
  {
    if (style != null)
    {
      this.image = style.image;
      this.title = style.title;
    }
  }
  
  /**
   * Legt das Bild fest.
   * @param image das Bild.
   */
  public void setImage(String image)
  {
    this.image = image;
  }
  
  /**
   * Legt den Titel fest.
   * @param title der Titel.
   */
  public void setTitle(String title)
  {
    this.title = title;
  }
  
  /**
   * Legt einen optionalen Beschreibungstext fest.
   * @param text optionaler Beschreibungstext.
   */
  public void setText(String text)
  {
    this.text = text;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    if (parent == null || parent.isDisposed())
      return;
    
    Composite comp = new Composite(parent,SWT.NO_BACKGROUND | SWT.TRANSPARENT);
    comp.setBackgroundMode(SWT.INHERIT_FORCE);
    comp.setBackground(parent.getBackground());
    comp.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
    
    final GridLayout gl = new GridLayout();
    gl.marginHeight = 40;
    gl.verticalSpacing = 10;
    comp.setLayout(gl);
    
    if (this.image != null && this.image.length() > 0)
    {
      
      Image i = SWTUtil.getImage(this.image);
      Label l = new Label(comp,SWT.NONE);
      l.setImage(i);
      l.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,true,false));
    }
    
    if (this.title != null && this.title.length() > 0)
    {
      Label l = new Label(comp,SWT.NONE);
      l.setFont(Font.H1.getSWTFont());
      l.setForeground(Color.COMMENT.getSWTColor());
      l.setText(this.title);
      l.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,true,false));
    }
    if (this.text != null && this.text.length() > 0)
    {
      Label l = new Label(comp,SWT.NONE);
      l.setFont(Font.ITALIC.getSWTFont());
      l.setForeground(Color.COMMENT.getSWTColor());
      l.setText(this.text);
      l.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,true,false));
    }
  }
}


