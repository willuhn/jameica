/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/Menu.java,v $
 * $Revision: 1.2 $
 * $Date: 2003/10/23 23:31:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica;

import java.util.Enumeration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;

import de.bb.util.XmlFile;

/**
 * @author willuhn
 */
public class Menu
{

  private XmlFile xml;
  private org.eclipse.swt.widgets.Menu bar;


  protected Menu()
  {

    bar = new org.eclipse.swt.widgets.Menu(Application.shell,SWT.BAR);
    Application.shell.setMenuBar(bar);

    xml  = new XmlFile();
    xml.read(getClass().getResourceAsStream("/menu.xml"));

    // add elements
    Enumeration e = xml.getSections("/menu/").elements();
    while (e.hasMoreElements())
    {
      String key = (String) e.nextElement();
      new MenuCascade(key);
    }
  }

  class MenuCascade {

    private String path;

    MenuCascade(String key)
    {
      MenuItem cascade = new MenuItem(bar,SWT.CASCADE);
      String text = I18N.tr(xml.getString(key,"name",null));
      cascade.setText(text);
      org.eclipse.swt.widgets.Menu submenu = new org.eclipse.swt.widgets.Menu(Application.shell, SWT.DROP_DOWN);
      cascade.setMenu(submenu);
      Enumeration e = xml.getSections(key).elements();
      int index = 0;
      while (e.hasMoreElements())
      {
        String ckey = (String) e.nextElement();
        new MenuElement(submenu, ckey,index++);
      }

    }
  }

  class MenuElement {
    
    MenuElement(org.eclipse.swt.widgets.Menu parent,String ckey,int eindex)
    {
      MenuItem item = new MenuItem (parent, eindex);
      item.addListener (SWT.Selection, new Listener()
      {
        public void handleEvent(org.eclipse.swt.widgets.Event event)
        {
          // TODO: Event
        }
      });
      String text = I18N.tr(xml.getString(ckey,"name",null));
      item.setText(text);
      // closeitem.setAccelerator(SWT.ALT + SWT.F4);
    }
  }

}

/*********************************************************************
 * $Log: Menu.java,v $
 * Revision 1.2  2003/10/23 23:31:17  willuhn
 * @N added Menu via xml
 *
 * Revision 1.1  2003/10/23 22:36:35  willuhn
 * @N added Menu
 *
 **********************************************************************/
