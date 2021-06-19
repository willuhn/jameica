/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.util;

import java.util.Vector;

import net.n3.nanoxml.IXMLElement;

/**
 * Vereinfacht das Parsen komplexer XML-Strukturen mit NanoXML.
 * Hierbei kann eine Art XPath-Syntax verwendet werden.
 */
public class XPathEmu
{
  private IXMLElement node = null;

  /**
   * ct.
   * @param rootNode
   */
  public XPathEmu(IXMLElement rootNode)
  {
    this.node = rootNode;
  }
  
  /**
   * Liefert den Content/Wert des Attributes des angegebenen XML-Elementes.
   * Hier kann ein Pseudo-XPath angegeben werden.
   *
   * @param path der Pseudo-XPath.
   * <pre>{@code
   *   <Kunden>
   *     <Kunde>
   *       <Name>Foo</Name>
   *       <Ort plz="0815"/>
   *     </Kunde>
   *   </Kunden>
   * }</pre>
   * Beispiel 1: path="Kunden/Kunde/Name" ergibt "Foo".
   * Beispiel 2: path="Kunden/Kunde/Ort/@plz" ergibt "0815".
   * @return Content des XML-Elementes oder null, niemals jedoch einen Leerstring.
   */
  public String getContent(String path)
  {
    if (this.node == null || path == null)
      return null;

    IXMLElement element = null;
    
    ////////////////////////////////////////////////////////////////
    // Attribut
    String attribute = null;
    int iAttr = path.indexOf("@");
    if (iAttr != -1)
    {
      if (path.startsWith("@"))
        element = this.node;

      attribute = path.substring(iAttr+1);
      path = path.substring(0,iAttr);
    }
    ////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////
    // Element ermitteln
    if (element == null)
      element = getElement(path);

    if (element == null)
      return null;
    ////////////////////////////////////////////////////////////////
    
    
    ////////////////////////////////////////////////////////////////
    // Wert ermitteln
    String value = attribute == null ? element.getContent() : element.getAttribute(attribute,null);
    if (value == null || value.length() == 0)
      return null;
    return value;
    ////////////////////////////////////////////////////////////////
  }
  
  /**
   * Liefert das erste gefundene XML-Element hinter dem angegebenen Pfad.
   * @param path Pfad.
   * * @return das XML-Element oder null.
   */
  public IXMLElement getElement(String path)
  {
    IXMLElement[] elements = getElements(path);
    if (elements.length == 0)
      return null;
    return elements[0];
  }
  
  /**
   * Liefert eine Liste aller gefundenen XML-Elemente hinter dem angegebenen Pfad.
   * @param path Pfad.
   * @return Liste der XML-Elemente oder ein leeres Array. Niemals null.
   */
  public IXMLElement[] getElements(String path)
  {
    if (this.node == null || path == null)
      return new IXMLElement[0];

    // ggf. fuehrenden Slash abschneiden
    if (path.startsWith("/"))
      path = path.substring(1);

    ////////////////////////////////////////////////////////////////
    // Iterieren ueber die Elemente
    IXMLElement[] elements = new IXMLElement[]{this.node};
    String[] names = path.split("/");
    for (int i=0;i<names.length;++i)
    {
      Vector<?> v = elements[0].getChildrenNamed(names[i]);
      if (v.isEmpty())
        return new IXMLElement[0];
      elements = v.toArray(new IXMLElement[v.size()]);
    }
    return elements;
    ////////////////////////////////////////////////////////////////
  }
}


/*********************************************************************
 * $Log: XPathEmu.java,v $
 * Revision 1.2  2008/12/16 15:18:49  willuhn
 * @N Attribute auch fuer das Root-Element selbst abfragbar
 *
 * Revision 1.1  2007/05/07 23:00:44  willuhn
 * @N Helfer-Klasse zum bequemeren Parsen von XML-Strukturen mit nanoXML
 *
 * Revision 1.1  2007/04/03 12:07:21  willuhn
 * @N XPath-Emulation
 *
 **********************************************************************/