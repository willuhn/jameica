/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/Menu.java,v $
 * $Revision: 1.12 $
 * $Date: 2003/12/12 01:28:05 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui;

import java.io.InputStream;
import java.util.Enumeration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;

import de.bb.util.XmlFile;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.I18N;

/**
 * Bildet das Dropdown-Menu ab.
 * @author willuhn
 */
public class Menu
{

  private XmlFile xml;
  private final org.eclipse.swt.widgets.Menu bar;


  /**
   * Erzeugt eine neue Instanz des Dropdown-Menus.
   */
  protected Menu()
  {

    bar = new org.eclipse.swt.widgets.Menu(GUI.getShell(),SWT.BAR);
		GUI.getShell().setMenuBar(bar);

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

  /**
   * Fuegt dem Menu noch weitere Eintraege hinzu, die sich in dem uebergebenen
   * Inputstream befinden. Der Stream muss eine menu.xml enthalten.
   * Wird von GUI nach der Initialisierung der Plugins aufgerufen.
   * @param menu
   */
  protected void appendMenu(InputStream menu)
  {
    if (menu == null)
      return;
    xml.read(menu);
    // add elements
    Enumeration e = xml.getSections("/menu/").elements();
    while (e.hasMoreElements())
    {
      String key = (String) e.nextElement();
      new MenuCascade(key);
    }
  }

  /**
   * Innere Hilfsklasse zur Abbildung des Menu-Baumes.
   * @author willuhn
   */
  class MenuCascade {

    /**
     * ct.
     * @param key Pfad zum aktuellen Menupunkt in der Config-Datei.
     */
    MenuCascade(String key)
    {
      final MenuItem cascade = new MenuItem(bar,SWT.CASCADE);
      String text = I18N.tr(xml.getString(key,"name",null));
      cascade.setText(text);
      final org.eclipse.swt.widgets.Menu submenu = new org.eclipse.swt.widgets.Menu(GUI.getShell(), SWT.DROP_DOWN);
      cascade.setMenu(submenu);
      Enumeration e = xml.getSections(key).elements();
      while (e.hasMoreElements())
      {
        String ckey = (String) e.nextElement();
        new MenuElement(submenu, ckey);
      }

    }
  }

  /**
   * Innere Hilfsklasse zur Abbildung des Menu-Baumes.
   * @author willuhn
   */
  class MenuElement {

    /**
     * ct.
     * @param parent Eltern-Element.
     * @param ckey Pfad zum aktuellen Menupunkt in der Config-Datei.
     */
    MenuElement(org.eclipse.swt.widgets.Menu parent,String ckey)
    {
      final String s = ckey;
      String c    = xml.getString(s,"class",null);
      String text = I18N.tr(xml.getString(s,"name",null));
      if (text != null && text.startsWith("-"))
      {
        new MenuItem(parent,SWT.SEPARATOR);
        return;
      }
      final MenuItem item = new MenuItem(parent,SWT.CASCADE);
      
      item.addListener (SWT.Selection, new MenuListener(c));

      String shortCut = xml.getString(s,"shortcut",null);
      if (shortCut != null)
      try {
        String modifier = shortCut.substring(0,shortCut.indexOf("+"));
        String key = shortCut.substring(shortCut.indexOf("+")+1);
        int m = SWT.ALT;
        if ("CTRL".equalsIgnoreCase(modifier)) m = SWT.CTRL;
        item.setAccelerator(m + key.getBytes()[0]);
        text += "\t" + shortCut;
      }
      catch (Exception e)
      {
        if (Application.DEBUG)
          e.printStackTrace();
      }
      item.setText(text);
    }
  }
  
  class MenuListener implements Listener
  {
    private String c = null;
    MenuListener(String clazz)
    {
      c = clazz;
    }

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(org.eclipse.swt.widgets.Event event)
    {
      try {
        GUI.startView(c,null);
      }
      catch (Exception e)
      {
        if (Application.DEBUG)
          e.printStackTrace();
        Application.getLog().error("unable to execute menu entry. exception: " + e.getClass() + "["+e.getMessage()+"]");
        throw new RuntimeException(e); // wir werfen eine RuntimeException, weil handleEvent nix werfen darf ;)
      }
    }
    
  }

}

/*********************************************************************
 * $Log: Menu.java,v $
 * Revision 1.12  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.10  2003/12/10 00:47:12  willuhn
 * @N SearchDialog done
 * @N ErrorView
 *
 * Revision 1.9  2003/12/05 17:12:23  willuhn
 * @C SelectInput
 *
 * Revision 1.8  2003/11/25 01:23:27  willuhn
 * @N added Menu shortcuts
 *
 * Revision 1.7  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
 * Revision 1.6  2003/11/18 19:13:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2003/11/18 18:56:08  willuhn
 * @N added support for pluginmenus and plugin navigation
 *
 * Revision 1.4  2003/11/13 00:37:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/10/29 00:41:26  willuhn
 * *** empty log message ***
 *
 **********************************************************************/
