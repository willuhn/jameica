/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/Menu.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/10/23 22:36:35 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;

/**
 * @author willuhn
 */
public class Menu
{

  private org.eclipse.swt.widgets.Menu menu;

  public Menu()
  {
    menu = new org.eclipse.swt.widgets.Menu(Application.shell,SWT.BAR);
    Application.shell.setMenuBar(menu);

    int index = 0;
    /// FILE /////////////////////////////
    MenuItem file = new MenuItem(menu,SWT.CASCADE);
    file.setText("File");

    org.eclipse.swt.widgets.Menu filemenu = new org.eclipse.swt.widgets.Menu(Application.shell, SWT.DROP_DOWN);
    file.setMenu(filemenu);

    MenuItem closeitem = new MenuItem (filemenu, index++);
    closeitem.addListener (SWT.Selection, new Listener()
    {
      public void handleEvent(org.eclipse.swt.widgets.Event event)
      {
        Application.shutDown();
      }
    });
    closeitem.setText ("Close\tAlt+F4");
    closeitem.setAccelerator(SWT.ALT + SWT.F4);
    /// FILE /////////////////////////////


    MenuItem edit = new MenuItem(menu,SWT.CASCADE);
    edit.setText("Edit");

    org.eclipse.swt.widgets.Menu editmenu = new org.eclipse.swt.widgets.Menu(Application.shell, SWT.DROP_DOWN);
    edit.setMenu(editmenu);

    MenuItem copyitem = new MenuItem (editmenu, index++);
    copyitem.addListener (SWT.Selection, new Listener()
    {
      public void handleEvent(org.eclipse.swt.widgets.Event event)
      {
      }
    });
    copyitem.setText ("Copy\tCtrl+C");
    copyitem.setAccelerator(SWT.CTRL + 'C');

  }

}

/*********************************************************************
 * $Log: Menu.java,v $
 * Revision 1.1  2003/10/23 22:36:35  willuhn
 * @N added Menu
 *
 **********************************************************************/
