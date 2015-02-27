/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/MenuItemXml.java,v $
 * $Revision: 1.7 $
 * $Date: 2010/08/26 21:47:48 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui;

import java.rmi.RemoteException;
import java.util.Enumeration;

import net.n3.nanoxml.IXMLElement;

import org.eclipse.swt.graphics.Image;

import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * @author willuhn
 */
public class MenuItemXml extends AbstractItemXml implements MenuItem 
{
  /**
   * ct.
   * @param parent das Eltern-Element.
   * @param path Pfad in der XML-Datei.
   * @param i18n optionaler Uebersetzer, um die Menu-Eintraege in die ausgewaehlte Sprache uebersetzen zu koennen.
   */
  public MenuItemXml(MenuItem parent, IXMLElement path, I18N i18n)
  {
    super(parent,path,i18n);

    Enumeration e  = this.path.enumerateChildren();
    while (e.hasMoreElements())
    {
      IXMLElement child = (IXMLElement) e.nextElement();
      childs.add(new MenuItemXml(this,child,i18n));
    }
  }

  /**
   * @see de.willuhn.jameica.gui.MenuItem#getShortcut()
   */
  public String getShortcut() throws RemoteException
  {
    return Application.getPlatform().mapSWTShortcut((String) getAttribute("shortcut"));
  }

  /**
   * @see de.willuhn.jameica.gui.MenuItem#getIcon()
   */
  public Image getIcon() throws RemoteException
  {
    String icon = (String) getAttribute("icon");
    if (icon == null || icon.length() == 0)
      return null;
    return SWTUtil.getImage(icon);
  }

  /**
   * Ueberschrieben, um dabei auch das Menu anzupassen.
   * @see de.willuhn.jameica.gui.Item#setEnabled(boolean, boolean)
   */
  public void setEnabled(boolean enabled, boolean recursive)
      throws RemoteException
  {
    super.setEnabled(enabled, recursive);
    GUI.getMenu().update(this);
    
    if (recursive)
    {
      for (int i=0;i<this.childs.size();++i)
      {
        MenuItem child = (MenuItem) this.childs.get(i);
        child.setEnabled(enabled,recursive);
      }
    }
  }

}


/*********************************************************************
 * $Log: MenuItemXml.java,v $
 * Revision 1.7  2010/08/26 21:47:48  willuhn
 * @N Icons auch im Hauptmenu
 *
 * Revision 1.6  2006/06/29 14:56:48  willuhn
 * @N Menu ist nun auch deaktivierbar
 *
 * Revision 1.5  2004/12/13 22:48:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/11/05 20:00:44  willuhn
 * @D javadoc fixes
 *
 * Revision 1.3  2004/10/12 23:49:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/11 22:41:17  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/08 16:41:58  willuhn
 * *** empty log message ***
 *
 **********************************************************************/