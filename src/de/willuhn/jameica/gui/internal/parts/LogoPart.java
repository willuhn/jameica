/**********************************************************************
 *
 * Copyright (c) 2022 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.Customizing;

/**
 * Optionale anpassbarer Bereich oben in der Anwendung für ein Logo.
 */
public class LogoPart implements Part, Extendable
{
  private Composite space = null;
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void paint(Composite parent)
  {
    final String logo = StringUtils.trimToNull(Customizing.SETTINGS.getString("application.view.logo",null));
    if (logo == null)
      return;
    
    final Composite comp = new Composite(parent,SWT.NONE);
    comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    comp.setLayout(SWTUtil.createGrid(2,false));
    
    // Freier Bereich links
    this.space = new Composite(comp,SWT.NONE);
    this.space.setLayoutData(new GridData(GridData.FILL_BOTH));
    this.space.setLayout(SWTUtil.createGrid(1,false));
    
    // Logo rechts
    final Image image = SWTUtil.getImage(logo);
    final Canvas logoBg = SWTUtil.getCanvas(comp,image, SWT.TOP | SWT.LEFT);
    final GridData gd = new GridData(GridData.END);
    gd.widthHint = image.getBounds().width;
    logoBg.setLayoutData(gd);
    
    ExtensionRegistry.extend(this);
  }

  /**
   * @see de.willuhn.jameica.gui.extension.Extendable#getExtendableID()
   */
  @Override
  public String getExtendableID()
  {
    return LogoPart.class.getName();
  }
  
  /**
   * Liefert das Composite für den Gestaltungsfreiraum.
   * @return das Composite für den Gestaltungsfreiraum.
   */
  public Composite getSpace()
  {
    return this.space;
  }
}
