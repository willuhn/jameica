/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details.
 *
 **********************************************************************/

package de.willuhn.jameica.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Liest Wallets im alten Format des {@link java.beans.XMLEncoder}, ohne den
 * {@link java.beans.XMLDecoder} zu verwenden.
 * Unterstuetzt werden genau die Konstrukte, die der XMLEncoder fuer die in
 * {@link WalletTypes} erlaubten Typen erzeugt: Bean-Properties, "add" und "put" auf
 * Collections und Maps, Array-Elemente, Enums, Datumswerte und Referenzen per id/idref.
 * Konstruktor-Argumente, statische Felder und sonstige Methodenaufrufe werden abgelehnt.
 */
final class WalletLegacyReader
{
  private static final int MAX_ARRAY_LENGTH = 1000000;

  private final WalletTypes types;
  private final Map<String,Object> ids = new HashMap<String,Object>();

  private WalletLegacyReader(WalletTypes types)
  {
    this.types = types;
  }

  /**
   * Liest ein Wallet im alten Format.
   * @param xml das entschluesselte XML.
   * @param types die Typ-Regeln.
   * @return die Daten des Wallets.
   * @throws Exception wenn das Wallet nicht erlaubte Daten enthaelt.
   */
  static Hashtable<String,Serializable> read(byte[] xml, WalletTypes types) throws Exception
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities",false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities",false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD,"");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA,"");
    factory.setExpandEntityReferences(false);
    factory.setXIncludeAware(false);
    DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setEntityResolver((publicId,systemId) -> { throw new SAXException("external entities are disabled"); });
    Document document = builder.parse(new ByteArrayInputStream(xml));

    Element root = document.getDocumentElement();
    requireName(root,"java");
    requireAttributes(root,"version","class");
    if (!"java.beans.XMLDecoder".equals(root.getAttribute("class")))
      throw WalletFormat.invalid("invalid root element");

    Object o = new WalletLegacyReader(types).value(onlyChild(root));
    if (o == null || o.getClass() != Hashtable.class)
      throw WalletFormat.invalid("wallet is not a hashtable");

    Hashtable<String,Serializable> result = new Hashtable<String,Serializable>();
    for (Map.Entry<?,?> e:((Hashtable<?,?>) o).entrySet())
      WalletFormat.put(result,e.getKey(),e.getValue());
    return result;
  }

  /**
   * Liest einen einzelnen Wert.
   * @param e das XML-Element.
   * @return der Wert.
   * @throws IOException
   */
  private Object value(Element e) throws IOException
  {
    try
    {
      switch (e.getTagName())
      {
        case "null":
          requireAttributes(e);
          requireNoChildren(e);
          return null;
        case "string":  return string(e);
        case "char":    return Character.valueOf(character(e));
        case "boolean": return Boolean.valueOf(bool(text(e)));
        case "byte":    return Byte.valueOf(text(e));
        case "short":   return Short.valueOf(text(e));
        case "int":     return Integer.valueOf(text(e));
        case "long":    return Long.valueOf(text(e));
        case "float":   return Float.valueOf(text(e));
        case "double":  return Double.valueOf(text(e));
        case "array":   return this.array(e);
        case "object":  return this.object(e);
        default:
          throw WalletFormat.invalid("unsupported element " + e.getTagName());
      }
    }
    catch (NumberFormatException nfe)
    {
      throw WalletFormat.invalid("invalid " + e.getTagName() + " value");
    }
  }

  /**
   * Liest ein Objekt.
   * @param e das XML-Element.
   * @return das Objekt.
   * @throws IOException
   */
  private Object object(Element e) throws IOException
  {
    if (e.hasAttribute("idref"))
    {
      requireAttributes(e,"idref");
      requireNoChildren(e);
      String ref = e.getAttribute("idref");
      if (!this.ids.containsKey(ref))
        throw WalletFormat.invalid("unknown reference " + ref);
      return this.ids.get(ref);
    }

    requireAttributes(e,"class","id","method");
    String className = required(e,"class");
    List<Element> children = children(e);

    // Enums schreibt der XMLEncoder als Aufruf von "Enum.valueOf(Klasse,Name)"
    if (e.hasAttribute("method"))
    {
      if (!"valueOf".equals(e.getAttribute("method")) || !Enum.class.getName().equals(className) || children.size() != 2)
        throw WalletFormat.invalid("unsupported method call");
      requireName(children.get(0),"class");
      Class<?> type = this.types.resolve(text(children.get(0)));
      if (!this.types.isEnum(type))
        throw WalletFormat.invalid("class not allowed: " + type.getName());
      String name = string(children.get(1));
      for (Object o:type.getEnumConstants())
      {
        if (((Enum<?>) o).name().equals(name))
          return this.register(e,o);
      }
      throw WalletFormat.invalid("unknown enum constant " + name);
    }

    Class<?> type = this.types.resolve(className);
    int first = 0;
    Object result = null;
    if (type == Date.class)
    {
      // Datum wird mit den Millisekunden als Konstruktor-Argument geschrieben
      if (children.isEmpty())
        throw WalletFormat.invalid("missing date value");
      requireName(children.get(0),"long");
      result = new Date(Long.parseLong(text(children.get(0))));
      first = 1;
    }
    else
    {
      result = this.types.newInstance(type);
    }
    this.register(e,result);

    for (int i=first;i<children.size();++i)
      this.statement(result,children.get(i));
    return result;
  }

  /**
   * Fuehrt eine "void"-Anweisung des XMLEncoders auf dem Objekt aus.
   * @param target das Objekt.
   * @param e das XML-Element.
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  private void statement(Object target, Element e) throws IOException
  {
    requireName(e,"void");
    List<Element> args = children(e);

    if (e.hasAttribute("method"))
    {
      requireAttributes(e,"method");
      String method = e.getAttribute("method");
      if ("add".equals(method) && args.size() == 1 && WalletTypes.isCollection(target.getClass()))
      {
        ((Collection<Object>) target).add(this.value(args.get(0)));
        return;
      }
      if ("put".equals(method) && args.size() == 2 && WalletTypes.isMap(target.getClass()))
      {
        ((Map<Object,Object>) target).put(this.value(args.get(0)),this.value(args.get(1)));
        return;
      }
      throw WalletFormat.invalid("unsupported method " + method);
    }

    requireAttributes(e,"property","id");
    String name = required(e,"property");

    // Setter
    if (!e.hasAttribute("id") && args.size() == 1 && !"void".equals(args.get(0).getTagName()))
    {
      this.types.set(target,name,this.value(args.get(0)));
      return;
    }

    // Getter, ggf. mit weiteren Anweisungen auf dem Rueckgabewert
    Object value = this.types.get(target,name);
    this.register(e,value);
    if (args.isEmpty())
      return;
    if (value == null || (!WalletTypes.isCollection(value.getClass()) && !WalletTypes.isMap(value.getClass()) && !this.types.isBean(value.getClass())))
      throw WalletFormat.invalid("unsupported property value " + name);
    for (Element a:args)
      this.statement(value,a);
  }

  /**
   * Liest ein Array.
   * @param e das XML-Element.
   * @return das Array.
   * @throws IOException
   */
  private Object array(Element e) throws IOException
  {
    requireAttributes(e,"class","length","id");
    Class<?> component = this.types.resolveComponent(required(e,"class"));
    if (!this.types.isComponent(component))
      throw WalletFormat.invalid("class not allowed: " + component.getName());

    int length;
    try
    {
      length = Integer.parseInt(required(e,"length"));
    }
    catch (NumberFormatException nfe)
    {
      throw WalletFormat.invalid("invalid array length");
    }
    if (length < 0 || length > MAX_ARRAY_LENGTH)
      throw WalletFormat.invalid("invalid array length");

    Object result = this.register(e,Array.newInstance(component,length));
    BitSet assigned = new BitSet(length);
    for (Element item:children(e))
    {
      requireName(item,"void");
      requireAttributes(item,"index");
      int index;
      try
      {
        index = Integer.parseInt(required(item,"index"));
      }
      catch (NumberFormatException nfe)
      {
        throw WalletFormat.invalid("invalid array index");
      }
      if (index < 0 || index >= length || assigned.get(index))
        throw WalletFormat.invalid("invalid array index");
      assigned.set(index);
      try
      {
        Array.set(result,index,this.value(onlyChild(item)));
      }
      catch (IllegalArgumentException iae)
      {
        throw WalletFormat.invalid("invalid array item");
      }
    }
    return result;
  }

  /**
   * Merkt sich das Objekt, falls das Element eine id besitzt.
   * @param e das XML-Element.
   * @param o das Objekt.
   * @return das Objekt.
   * @throws IOException
   */
  private Object register(Element e, Object o) throws IOException
  {
    if (!e.hasAttribute("id"))
      return o;
    String id = e.getAttribute("id");
    if (id.length() == 0 || this.ids.containsKey(id))
      throw WalletFormat.invalid("duplicate id " + id);
    this.ids.put(id,o);
    return o;
  }

  private static String string(Element e) throws IOException
  {
    requireName(e,"string");
    requireAttributes(e);
    StringBuilder sb = new StringBuilder();
    NodeList nodes = e.getChildNodes();
    for (int i=0;i<nodes.getLength();++i)
    {
      Node n = nodes.item(i);
      if (n.getNodeType() == Node.TEXT_NODE)
        sb.append(n.getNodeValue());
      else if (n instanceof Element && "char".equals(((Element) n).getTagName()))
        sb.append(character((Element) n));
      else
        throw WalletFormat.invalid("unexpected string content");
    }
    return sb.toString();
  }

  private static char character(Element e) throws IOException
  {
    requireName(e,"char");
    if (!e.hasAttribute("code"))
    {
      String s = text(e);
      if (s.length() != 1)
        throw WalletFormat.invalid("invalid char value");
      return s.charAt(0);
    }

    // Steuerzeichen schreibt der XMLEncoder als <char code="#hex"/>
    requireAttributes(e,"code");
    requireNoChildren(e);
    String code = e.getAttribute("code");
    if (!code.startsWith("#") || e.getTextContent().length() != 0)
      throw WalletFormat.invalid("invalid char code");
    try
    {
      int c = Integer.parseInt(code.substring(1),16);
      if (c < Character.MIN_VALUE || c > Character.MAX_VALUE)
        throw WalletFormat.invalid("invalid char code");
      return (char) c;
    }
    catch (NumberFormatException nfe)
    {
      throw WalletFormat.invalid("invalid char code");
    }
  }

  private static boolean bool(String s) throws IOException
  {
    if (!"true".equals(s) && !"false".equals(s))
      throw WalletFormat.invalid("invalid boolean value");
    return Boolean.parseBoolean(s);
  }

  private static String text(Element e) throws IOException
  {
    requireAttributes(e);
    requireNoChildren(e);
    return e.getTextContent();
  }

  private static Element onlyChild(Element parent) throws IOException
  {
    List<Element> list = children(parent);
    if (list.size() != 1)
      throw WalletFormat.invalid("expected one child in " + parent.getTagName());
    return list.get(0);
  }

  private static void requireNoChildren(Element parent) throws IOException
  {
    if (!children(parent).isEmpty())
      throw WalletFormat.invalid("unexpected child in " + parent.getTagName());
  }

  private static List<Element> children(Element parent) throws IOException
  {
    List<Element> result = new ArrayList<Element>();
    NodeList nodes = parent.getChildNodes();
    for (int i=0;i<nodes.getLength();++i)
    {
      Node n = nodes.item(i);
      if (n instanceof Element)
        result.add((Element) n);
      else if (n.getNodeType() != Node.TEXT_NODE)
        throw WalletFormat.invalid("unexpected XML content");
    }
    return result;
  }

  private static void requireName(Element e, String name) throws IOException
  {
    if (!name.equals(e.getTagName()))
      throw WalletFormat.invalid("expected " + name + ", got " + e.getTagName());
  }

  private static String required(Element e, String name) throws IOException
  {
    if (!e.hasAttribute(name))
      throw WalletFormat.invalid("missing attribute " + name);
    return e.getAttribute(name);
  }

  private static void requireAttributes(Element e, String... allowed) throws IOException
  {
    Set<String> names = new HashSet<String>(List.of(allowed));
    NamedNodeMap attributes = e.getAttributes();
    for (int i=0;i<attributes.getLength();++i)
    {
      String name = attributes.item(i).getNodeName();
      if (!names.contains(name))
        throw WalletFormat.invalid("unsupported attribute " + name);
    }
  }
}
