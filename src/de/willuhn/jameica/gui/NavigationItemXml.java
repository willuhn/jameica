/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/NavigationItemXml.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/08/11 23:37:21 $
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
import java.util.ArrayList;
import java.util.Enumeration;

import net.n3.nanoxml.IXMLElement;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.util.I18N;

/**
 */
public class NavigationItemXml implements NavigationItem
{

	private NavigationItem parent;
	private IXMLElement path;
	private I18N i18n;

  /**
   * ct.
   * @param parent das Eltern-Element.
   * @param sPath Pfad in der XML-Datei.
   * @param i18n optionaler Uebersetzer, um die Navi-Eintraege in die ausgewaehlte Sprache uebersetzen zu koennen.
   */
  public NavigationItemXml(NavigationItem parent, IXMLElement sPath, I18N i18n)
  {
  	this.parent = parent;
  	this.path = sPath;
  	this.i18n = i18n;
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
   * @see de.willuhn.jameica.gui.NavigationItem#getName()
   */
  public String getName()
  {
    String s = this.path.getAttribute("name",null);
    if (s == null || s.length() == 0 || i18n == null)
    	return "unknown";
    return i18n.tr(s);
  }

  /**
   * @see de.willuhn.jameica.gui.NavigationItem#getListener()
   */
  public Listener getListener()
  {
  	return new Listener()
    {
      public void handleEvent(Event event)
      {
				String action = path.getAttribute("action",null);
				GUI.startView(action,null);
				GUI.getStatusBar().setStatusText(getName());
      }
    };
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getChilds()
   */
  public GenericIterator getChilds() throws RemoteException
  {
		// iterate over childs
		ArrayList childs = new ArrayList();
		Enumeration e  = this.path.enumerateChildren();
		while (e.hasMoreElements())
		{
			IXMLElement childPath = (IXMLElement) e.nextElement();
			childs.add(new NavigationItemXml(this,childPath,i18n));
		}
		return PseudoIterator.fromArray((NavigationItem[])childs.toArray(new NavigationItemXml[childs.size()]));
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#hasChild(de.willuhn.datasource.GenericObjectNode)
   */
  public boolean hasChild(GenericObjectNode object) throws RemoteException
  {
    throw new UnsupportedOperationException("not implemented");
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getParent()
   */
  public GenericObjectNode getParent() throws RemoteException
  {
    return this.parent;
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getPossibleParents()
   */
  public GenericIterator getPossibleParents() throws RemoteException
  {
		throw new UnsupportedOperationException("not implemented");
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getPath()
   */
  public GenericIterator getPath() throws RemoteException
  {
		throw new UnsupportedOperationException("not implemented");
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String name) throws RemoteException
  {
  	if ("name".equals(name))
  		return getName();
		return null;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getID()
   */
  public String getID() throws RemoteException
  {
    return this.path.getFullName() + ":" + this.path.getLineNr();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
  	return "name";
  }

  /**
   * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
   */
  public boolean equals(GenericObject other) throws RemoteException
  {
    if (other == null)
    	return false;
    return getID().equals(other.getID());
  }
}


/**********************************************************************
 * $Log: NavigationItemXml.java,v $
 * Revision 1.1  2004/08/11 23:37:21  willuhn
 * @N Navigation ist jetzt modular erweiterbar
 *
 **********************************************************************/