/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/ExpandPart.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/12/18 17:10:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;

import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.boxes.Box;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;

/**
 * Ein auf- und zuklappbarer Container.
 */
public class ExpandPart implements Part
{
  private ArrayList titles = new ArrayList();
  private ArrayList childs = new ArrayList();
  private Settings settings = new Settings(ExpandPart.class);
  
  /**
   * ct.
   */
  public ExpandPart()
  {
  }

  /**
   * ct.
   * @param title anzuzeigender Titel.
   * @param child Kind-Part welches angezeigt werden soll.
   */
  public ExpandPart(String title, Part child)
  {
    add(title,child);
  }
  
  /**
   * Fuegt der Expand-Bar ein weiteres Kind-Element hinzu.
   * @param box eine Box.
   */
  public void add(Box box)
  {
    if (!box.isEnabled() || !box.isActive())
      return;
    add(box.getName(),box);
  }
  
  /**
   * Fuegt der Expand-Bar ein weiteres Kind-Element hinzu.
   * @param title Titel.
   * @param child Kind-Element.
   */
  public void add(String title, Part child)
  {
    this.titles.add(title == null ? "" : title);
    this.childs.add(child);
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    ExpandBar bar = new ExpandBar(parent, SWT.V_SCROLL);
    bar.setBackground(Color.BACKGROUND.getSWTColor());
    bar.setLayoutData(new GridData(GridData.FILL_BOTH));
    
    for (int i=0;i<this.titles.size();++i)
    {
      final Composite composite = new Composite(bar, SWT.NONE);
      composite.setBackground(Color.BACKGROUND.getSWTColor());
      GridLayout layout = new GridLayout();
      layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
      layout.verticalSpacing = 10;
      composite.setLayout(layout);

      final String title = (String) this.titles.get(i);
      final Part child   = (Part) this.childs.get(i);
      child.paint(composite);
      final ExpandItem item = new ExpandItem(bar, SWT.NONE);
      item.setText(title);
      item.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
      item.setControl(composite);
      item.setExpanded(settings.getBoolean(child.getClass().getName() + ".expanded",true));
      item.addDisposeListener(new DisposeListener() {
        public void widgetDisposed(DisposeEvent e)
        {
          try
          {
            settings.setAttribute(child.getClass().getName() + ".expanded",item.getExpanded());
          }
          catch (Exception e2)
          {
            Logger.error("unable to store expanded state for child " + child.getClass().getName(),e2);
          }
        }
      
      });
    }

  }
}


/*********************************************************************
 * $Log: ExpandPart.java,v $
 * Revision 1.1  2007/12/18 17:10:14  willuhn
 * @N Neues ExpandPart
 * @N Boxen auf der Startseite koennen jetzt zusammengeklappt werden
 *
 **********************************************************************/