/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/ExtensionDescriptor.java,v $
 * $Revision: 1.3 $
 * $Date: 2012/04/04 20:43:37 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.plugin;

import java.util.StringTokenizer;

import net.n3.nanoxml.IXMLElement;

/**
 * Ein Extension-Deskriptor beschreibt eine Extension, welche
 * bei einem Plugin beliegen kann, um die Funktionalitaet eines
 * anderen Plugins zu erweitern.
 * Diese finden sich in der plugin.xml eines Plugins.
 * Bsp.:
 * <extensions>
 *   <extension class="de.willuhn.jameica.plugin.Extension1" extends="id.extendable.1,id.extendable.2" />
 *   <extension class="de.willuhn.jameica.plugin.Extension2" extends="id.extendable.3" />
 * </extensions>
 */
public class ExtensionDescriptor
{

	private IXMLElement root 	= null;
	private String className 	= null;
	private String[] ids    	= null;
	private String[] plugins  = null;

  /**
   * ct.
   * @param root
   */
  public ExtensionDescriptor(IXMLElement root)
  {
  	this.root = root;
  }

  /**
   * Liefert den Namen der Java-Klasse der Extension.
   * @return Name der Klasse.
   * Diese muss das Interface Extension implementieren.
   */
  public String getClassname()
  {
  	if (className != null)
  		return className;
		className = root.getAttribute("class",null);
		return className;
  }

  /**
   * Liefert eine Liste von Extendable-IDs, welche diese Extension
   * erweitert.
   * @return Liste von Extendables.
   * Die Funktion darf nie <code>null</code> liefern, da sie dann kein
   * Extendable erweitern wuerde. Die Extension waere damit nutzlos.
   */
  public String[] getExtendableIDs()
  {
  	if (ids != null)
  		return ids;

  	ids = parseArray("extends");
		return ids;
  }

  /**
   * Liste von Plugins, die installiert sein muessen, damit die
   * Extension registriert wird.
   * @return Liste von Plugin-Namen.
   */
  public String[] getRequiredPlugins()
  {
    if (plugins != null)
      return plugins;
    
    plugins = parseArray("requires");
    return plugins;
  }
  
  /**
   * Parst den Wert des genannten XML-Elements als Array aus einer
   * kommaseparierten Liste von Werten.
   * @param element Name des XML-Elements.
   * @return Liste der Werte oder NULL.
   */
  private String[] parseArray(String element)
  {
    if (element == null || element.length() == 0)
      return null;

    String s = root.getAttribute(element,null);
    if (s == null || s.length() == 0)
      return null;

    StringTokenizer st = new StringTokenizer(s,",");
    String[] values = new String[st.countTokens()];
    for (int i=0;i<values.length;++i)
    {
      values[i] = st.nextToken();
    }
    return values;
  }

}

/**********************************************************************
 * $Log: ExtensionDescriptor.java,v $
 * Revision 1.3  2012/04/04 20:43:37  willuhn
 * @R Ueberfluessige Interface+XMLImpl entfernt
 * @N MessageDescriptor
 *
 * Revision 1.2  2010/06/03 13:52:55  willuhn
 * @N Neues optionales Attribut "requires", damit Extensions nur dann registriert werden, wenn ein benoetigtes Plugin installiert ist
 *
 * Revision 1.1  2005/05/27 17:31:46  web0
 * @N extension system
 *
 **********************************************************************/