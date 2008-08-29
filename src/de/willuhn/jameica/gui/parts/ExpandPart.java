/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/ExpandPart.java,v $
 * $Revision: 1.6 $
 * $Date: 2008/08/29 14:38:43 $
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
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;

/**
 * Ein auf- und zuklappbarer Container.
 */
public class ExpandPart implements Part
{
  private ArrayList items = new ArrayList();
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
    this.items.add(new Item(box.getName(),box,box.getHeight()));
  }
  
  /**
   * Fuegt der Expand-Bar ein weiteres Kind-Element hinzu.
   * @param title Titel.
   * @param child Kind-Element.
   */
  public void add(String title, Part child)
  {
    this.items.add(new Item(title,child));
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    ExpandBar bar = new ExpandBar(parent, SWT.V_SCROLL);
    bar.setBackground(Color.BACKGROUND.getSWTColor());
    bar.setLayoutData(new GridData(GridData.FILL_BOTH));
    
    for (int i=0;i<this.items.size();++i)
    {
      final Item ei = (Item) this.items.get(i);

      try
      {
        final Composite composite = new Composite(bar, SWT.NONE);
        composite.setBackground(Color.BACKGROUND.getSWTColor());
        GridLayout layout = new GridLayout();
        layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
        layout.verticalSpacing = 10;
        composite.setLayout(layout);

        ei.part.paint(composite);

        final ExpandItem item = new ExpandItem(bar, SWT.NONE);
        item.setText(ei.title);
        item.setHeight(ei.height > 0 ? ei.height : composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        item.setControl(composite);
        item.setExpanded(settings.getBoolean(ei.part.getClass().getName() + ".expanded",true));
        item.addDisposeListener(new DisposeListener() {
          public void widgetDisposed(DisposeEvent e)
          {
            try
            {
              settings.setAttribute(ei.part.getClass().getName() + ".expanded",item.getExpanded());
            }
            catch (Exception e2)
            {
              Logger.error("unable to store expanded state for child " + ei.part.getClass().getName(),e2);
            }
          }
        
        });
      }
      catch (Exception e)
      {
        Logger.error("unable to paint box " + ei.title,e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Anzeigen der Box \"{0}\"",ei.title),StatusBarMessage.TYPE_ERROR));
      }
    }

  }
  
  /**
   * Hilfsklasse.
   */
  private class Item
  {
    private String title = null;
    private Part part    = null;
    private int height   = -1;
    
    /**
     * @param title
     * @param part
     * @param height
     */
    private Item(String title, Part part)
    {
      this(title,part,-1);
    }

    /**
     * @param title
     * @param part
     * @param height
     */
    private Item(String title, Part part, int height)
    {
      this.title  = title == null ? "" : title;
      this.part   = part;
      this.height = height;
    }
  }
}


/*********************************************************************
 * $Log: ExpandPart.java,v $
 * Revision 1.6  2008/08/29 14:38:43  willuhn
 * @N wen ein einzelnes Part beim Zeichnen einen Fehler wirft, dann ueberspringen und Fehler melden - fuehrt sonst dazu, dass ggf. die Box mit den Jameica-Startmeldungen nicht angezeigt wird
 *
 * Revision 1.5  2007/12/29 18:45:31  willuhn
 * @N Hoehe von Boxen explizit konfigurierbar
 *
 * Revision 1.4  2007/12/19 00:09:29  willuhn
 * @N Splashscreen nochmal ueberarbeitet
 *
 * Revision 1.3  2007/12/18 23:05:42  willuhn
 * @C Farben wieder explizit vorgegeben. Unter Windows XP sieht es so oder so (ob Expand-Bar mit oder ohne XP-Lookp) haesslich aus
 *
 * Revision 1.2  2007/12/18 17:50:12  willuhn
 * @R Background-Color nicht mehr aenderbar
 * @C Layout der Startseite
 *
 * Revision 1.1  2007/12/18 17:10:14  willuhn
 * @N Neues ExpandPart
 * @N Boxen auf der Startseite koennen jetzt zusammengeklappt werden
 *
 **********************************************************************/