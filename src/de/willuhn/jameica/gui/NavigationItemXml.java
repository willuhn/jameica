/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/NavigationItemXml.java,v $
 * $Revision: 1.4 $
 * $Date: 2006/06/27 23:14:11 $
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
import de.willuhn.util.I18N;

/**
 * XML-Implementierung eines Navigations-Elements.
 */
public class NavigationItemXml extends AbstractItemXml implements NavigationItem
{

  /**
   * ct.
   * @param parent das Eltern-Element.
   * @param path Pfad in der XML-Datei.
   * @param i18n optionaler Uebersetzer, um die Navi-Eintraege in die ausgewaehlte Sprache uebersetzen zu koennen.
   */
  public NavigationItemXml(NavigationItem parent, IXMLElement path, I18N i18n)
  {
    super(parent,path,i18n);

		Enumeration e  = this.path.enumerateChildren();
		while (e.hasMoreElements())
		{
			IXMLElement childPath = (IXMLElement) e.nextElement();
			childs.add(new NavigationItemXml(this,childPath,i18n));
		}
  }

  /**
   * @see de.willuhn.jameica.gui.NavigationItem#getIconOpen()
   */
  public Image getIconOpen()
  {
  	String s = this.path.getAttribute("icon-open",null);
  	if (s == null || s.length() == 0)
  	{
  		// Nix fuer oeffnen definiert. Dann nehmen wir "icon-close"
  		// Und wenn das auch nicht existiert, dann gar nichts ;)
  		s = this.path.getAttribute("icon-close","empty.gif");
  	}
		return SWTUtil.getImage(s);
  }

  /**
   * @see de.willuhn.jameica.gui.NavigationItem#getIconClose()
   */
  public Image getIconClose()
  {
		return SWTUtil.getImage(this.path.getAttribute("icon-close","empty.gif"));
  }

  /**
   * @see de.willuhn.jameica.gui.NavigationItem#isExpanded()
   */
  public boolean isExpanded() throws RemoteException
  {
    String expanded = path.getAttribute("expanded",null);
    return expanded == null || expanded.equalsIgnoreCase("true");
  }
  
  /**
   * Ueberschrieben, um dabei auch links die Navigation anzupassen.
   * @see de.willuhn.jameica.gui.Item#setEnabled(boolean, boolean)
   */
  public void setEnabled(boolean enabled, boolean recursive)
      throws RemoteException
  {
    super.setEnabled(enabled, recursive);
    GUI.getNavigation().update(this);
    
    if (recursive)
    {
      for (int i=0;i<this.childs.size();++i)
      {
        NavigationItem child = (NavigationItem) this.childs.get(i);
        child.setEnabled(enabled,recursive);
      }
    }
  }
}


/**********************************************************************
 * $Log: NavigationItemXml.java,v $
 * Revision 1.4  2006/06/27 23:14:11  willuhn
 * @N neue Attribute "expanded" und "enabled" fuer Element "item" in plugin.xml
 *
 * Revision 1.3  2004/10/08 16:41:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/08/15 17:55:17  willuhn
 * @C sync handling
 *
 * Revision 1.1  2004/08/11 23:37:21  willuhn
 * @N Navigation ist jetzt modular erweiterbar
 *
 **********************************************************************/