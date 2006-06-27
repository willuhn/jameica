/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/AbstractItemXml.java,v $
 * $Revision: 1.11 $
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
import java.util.ArrayList;

import net.n3.nanoxml.IXMLElement;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * @author willuhn
 */
public abstract class AbstractItemXml implements Item
{

  protected Item parent;
  protected IXMLElement path;
  protected I18N i18n = Application.getI18n();
  protected ArrayList childs = new ArrayList();

	private Action action = null;
  private boolean enabled = true;

  /**
   * ct.
   * @param parent das Eltern-Element.
   * @param path Pfad in der XML-Datei.
   * @param i18n optionaler Uebersetzer, um die Menu/Navi-Eintraege in die
   * ausgewaehlte Sprache uebersetzen zu koennen.
   */
  AbstractItemXml(Item parent, IXMLElement path, I18N i18n)
  {
    this.parent = parent;
    this.path = path;
    if (i18n != null)
      this.i18n = i18n;

    String s = path.getAttribute("enabled",null);
    this.enabled = s == null || s.equalsIgnoreCase("true");
  }

  /**
   * @see de.willuhn.jameica.gui.Item#getName()
   */
  public String getName()
  {
  	String name = path.getAttribute("name",null);
    return name == null ? null : i18n.tr(name);
  }

  /**
   * @see de.willuhn.jameica.gui.Item#isEnabled()
   */
  public boolean isEnabled() throws RemoteException
  {
    return this.enabled;
  }

  /**
   * @see de.willuhn.jameica.gui.Item#setEnabled(boolean, boolean)
   */
  public void setEnabled(boolean enabled, boolean recursive) throws RemoteException
  {
    this.enabled = enabled;
  }

  /**
   * @see de.willuhn.jameica.gui.Item#getAction()
   */
  public Action getAction()
  {
		if (action != null)
			return action; // hatten wir schonmal geladen

  	String s = path.getAttribute("action",null);

  	if (s == null)
  		return null;

		try
		{
			Class c = Application.getClassLoader().load(s);
			action = (Action) c.newInstance();
			return action;
		}
		catch (Exception e)
		{
			Logger.error("error while instantiating action " + s,e);
		}
		return null;
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getChildren()
   */
  public GenericIterator getChildren() throws RemoteException
  {
    return PseudoIterator.fromArray((Item[])childs.toArray(new Item[childs.size()]));
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
    return path.getAttribute(name,null);
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getID()
   */
  public String getID()
  {
    String id = this.path.getAttribute("id",null);
    return id != null ? id : this.path.getFullName() + ":" + this.path.getLineNr();
  }

  /**
   * @see de.willuhn.jameica.gui.extension.Extendable#getExtendableID()
   */
  public String getExtendableID()
  {
    return getID();
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


  /**
   * @see de.willuhn.datasource.GenericObject#getAttributeNames()
   */
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[] {"name"};
  }

  /**
   * @see de.willuhn.jameica.gui.Item#addChild(de.willuhn.jameica.gui.Item)
   */
  public void addChild(Item i) throws RemoteException
  {
    if (i == null)
      return;
    childs.add(i);
  }
}


/*********************************************************************
 * $Log: AbstractItemXml.java,v $
 * Revision 1.11  2006/06/27 23:14:11  willuhn
 * @N neue Attribute "expanded" und "enabled" fuer Element "item" in plugin.xml
 *
 * Revision 1.10  2006/04/20 08:44:03  web0
 * @C s/Childs/Children/
 *
 * Revision 1.9  2005/05/30 12:01:33  web0
 * @R removed gui packages from rmic.xml
 *
 * Revision 1.8  2005/05/02 11:26:03  web0
 * *** empty log message ***
 *
 * Revision 1.7  2005/03/09 01:06:36  web0
 * @D javadoc fixes
 *
 * Revision 1.6  2004/12/13 22:48:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/11/12 18:23:58  willuhn
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