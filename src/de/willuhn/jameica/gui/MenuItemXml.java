/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/MenuItemXml.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/10/12 23:49:31 $
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
import de.willuhn.util.I18N;

/**
 * @author willuhn
 */
public class MenuItemXml extends AbstractItemXml implements MenuItem 
{

  /**
   * ct.
   * @param parent das Eltern-Element.
   * @param sPath Pfad in der XML-Datei.
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
   */  public String getShortcut() throws RemoteException
  {
    return (String) getAttribute("shortcut");
  }

}


/*********************************************************************
 * $Log: MenuItemXml.java,v $
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