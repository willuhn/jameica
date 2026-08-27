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

import java.beans.ExceptionListener;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.reminder.ReminderInterval;
import de.willuhn.jameica.reminder.ReminderInterval.TimeUnit;

/**
 * Data-only compatibility reader for wallet XML written by
 * {@link java.beans.XMLEncoder}.
 */
final class WalletXmlDecoder
{
  private static final int MAX_XML_BYTES = 16 * 1024 * 1024;
  private static final int MAX_ELEMENTS = 300000;
  private static final int MAX_XML_DEPTH = 64;
  private static final String ELEMENT_ATTRIBUTE_LIMIT = "http://www.oracle.com/xml/jaxp/properties/elementAttributeLimit";
  private static final int MAX_GRAPH_DEPTH = 64;
  private static final int MAX_GRAPH_VALUES = 100000;
  private static final int MAX_WALLET_ENTRIES = 10000;
  private static final int MAX_REMINDER_ENTRIES = 2000;
  private static final int MAX_ARRAY_LENGTH = 65536;
  private static final int MAX_TEXT_LENGTH = 128 * 1024;

  private static final String XML_DECODER = "java.beans.XMLDecoder";
  private static final String HASHTABLE = Hashtable.class.getName();
  private static final String LOGIN = Login.class.getName();
  private static final String REMINDER = Reminder.class.getName();
  private static final String INTERVAL = ReminderInterval.class.getName();
  private static final String DATE = Date.class.getName();
  private static final String ENUM = Enum.class.getName();
  private static final String TIME_UNIT = TimeUnit.class.getName();

  private final Map<String,Object> ids = new HashMap<String,Object>();
  private int values;

  private WalletXmlDecoder()
  {
  }

  /**
   * Decodes a wallet without executing constructors or methods selected by
   * XML input.
   * @param xml decrypted wallet XML.
   * @return wallet values.
   * @throws Exception if the document contains unsupported data.
   */
  static Hashtable<String,Serializable> decode(byte[] xml) throws Exception
  {
    if (xml == null)
      throw invalid("missing XML");
    if (xml.length > MAX_XML_BYTES)
      throw invalid("wallet XML exceeds size limit");

    preflight(xml);

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities",false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities",false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD,"");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA,"");
    factory.setAttribute(ELEMENT_ATTRIBUTE_LIMIT,"16");
    factory.setExpandEntityReferences(false);
    factory.setXIncludeAware(false);
    DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setEntityResolver((publicId,systemId) -> { throw new SAXException("external entities are disabled"); });
    Document document = builder.parse(new ByteArrayInputStream(xml));
    enforceElementLimit(document);

    Element root = document.getDocumentElement();
    requireName(root,"java");
    requireAttributes(root,"version","class");
    requireAttribute(root,"class",XML_DECODER);

    WalletXmlDecoder decoder = new WalletXmlDecoder();
    return decoder.parseWallet(onlyChild(root));
  }

  /** Encodes and immediately validates a wallet within the same byte bound. */
  static byte[] encode(Hashtable<String,Serializable> wallet) throws Exception
  {
    validateWallet(wallet);
    Buffer buffer = new Buffer();
    final Exception[] failure = new Exception[1];
    XMLEncoder encoder = new XMLEncoder(buffer);
    encoder.setExceptionListener(new ExceptionListener() {
      @Override
      public void exceptionThrown(Exception exception)
      {
        if (failure[0] == null)
          failure[0] = exception;
      }
    });
    try
    {
      encoder.writeObject(wallet);
    }
    finally
    {
      encoder.close();
    }
    if (failure[0] != null)
      throw new IOException("unable to encode wallet",failure[0]);

    byte[] xml = buffer.toByteArray();
    decode(xml);
    return xml;
  }

  /**
   * Rejects values which cannot be read by the data-only decoder.
   * @param value wallet value.
   * @throws IOException if the value is not supported.
   */
  static void validate(Serializable value) throws IOException
  {
    new ValidationContext().validate(value,0);
  }

  private static void validateWallet(Hashtable<String,Serializable> wallet) throws IOException
  {
    if (wallet == null)
      throw invalid("missing wallet");
    validateEntryCount(wallet.size());
    ValidationContext validation = new ValidationContext();
    for (Map.Entry<String,Serializable> entry:wallet.entrySet())
    {
      String key = entry.getKey();
      if (key == null)
        throw invalid("null wallet key");
      validateAlias(key);
      Serializable value = entry.getValue();
      if (value == null)
        throw invalid("null wallet value");
      validation.validate(value,0);
    }
  }

  static void validateAlias(String alias) throws IOException
  {
    if (alias.length() > MAX_TEXT_LENGTH)
      throw invalid("wallet key exceeds size limit");
  }

  static void validateEntryCount(int count) throws IOException
  {
    if (count > MAX_WALLET_ENTRIES)
      throw invalid("too many wallet entries");
  }

  private static final class ValidationContext
  {
    private final IdentityHashMap<Object,Byte> states = new IdentityHashMap<Object,Byte>();
    private int values;

    private void validate(Object value, int depth) throws IOException
    {
      if (depth > MAX_GRAPH_DEPTH)
        throw invalid("wallet object graph is too deep");
      if (++this.values > MAX_GRAPH_VALUES)
        throw invalid("wallet object graph is too large");
      if (value == null || value instanceof Boolean || value instanceof Byte || value instanceof Short || value instanceof Integer ||
          value instanceof Long || value instanceof Float || value instanceof Double || value instanceof Character)
        return;
      if (value.getClass() == Date.class || value.getClass() == ReminderInterval.class)
        return;
      if (value instanceof String)
      {
        if (((String)value).length() > MAX_TEXT_LENGTH)
          throw invalid("text value exceeds limit");
        return;
      }
      if (value instanceof byte[])
      {
        if (((byte[])value).length > MAX_ARRAY_LENGTH)
          throw invalid("byte array exceeds size limit");
        return;
      }
      if (value instanceof char[])
      {
        if (((char[])value).length > MAX_ARRAY_LENGTH)
          throw invalid("character array exceeds size limit");
        return;
      }

      Byte state = this.states.get(value);
      if (state != null)
      {
        if (state.byteValue() == 1)
          throw invalid("cyclic wallet value");
        return;
      }
      this.states.put(value,Byte.valueOf((byte)1));
      if (value.getClass() == Login.class)
      {
        Login login = (Login)value;
        validate(login.getUsername(),depth + 1);
        validate(login.getPassword(),depth + 1);
      }
      else if (value.getClass() == Reminder.class)
      {
        Reminder reminder = (Reminder)value;
        validate(reminder.getQueue(),depth + 1);
        validate(reminder.getDate(),depth + 1);
        validate(reminder.getEnd(),depth + 1);
        validate(reminder.getReminderInterval(),depth + 1);
        Map<String,Serializable> data = reminder.getData();
        if (data.size() > MAX_REMINDER_ENTRIES)
          throw invalid("too many reminder data entries");
        for (Map.Entry<String,Serializable> entry:data.entrySet())
        {
          validate(entry.getKey(),depth + 1);
          validate(entry.getValue(),depth + 1);
        }
      }
      else
      {
        throw invalid("unsupported wallet value: " + value.getClass().getName());
      }
      this.states.put(value,Byte.valueOf((byte)2));
    }
  }

  private Hashtable<String,Serializable> parseWallet(Element object) throws Exception
  {
    requireName(object,"object");
    requireAttributes(object,"class","id");
    requireAttribute(object,"class",HASHTABLE);
    Hashtable<String,Serializable> result = new Hashtable<String,Serializable>();
    ValidationContext validation = new ValidationContext();
    registerOptionalId(object,result);
    List<Element> entries = children(object);
    if (entries.size() > MAX_WALLET_ENTRIES)
      throw invalid("too many wallet entries");
    for (Element entry:entries)
    {
      requireName(entry,"void");
      requireAttributes(entry,"method");
      requireAttribute(entry,"method","put");
      List<Element> values = children(entry);
      if (values.size() != 2)
        throw invalid("invalid wallet entry");
      String key = parseString(values.get(0));
      Serializable value = parseValue(values.get(1));
      if (result.containsKey(key))
        throw invalid("duplicate wallet key");
      if (value == null)
        throw invalid("null wallet value");
      validation.validate(value,0);
      result.put(key,value);
    }
    return result;
  }

  private Serializable parseValue(Element value) throws Exception
  {
    if (++this.values > MAX_GRAPH_VALUES)
      throw invalid("too many wallet values");
    String name = value.getTagName();
    if ("null".equals(name))
    {
      requireAttributes(value);
      requireNoChildren(value);
      return null;
    }
    if ("string".equals(name))
      return parseString(value);
    if ("boolean".equals(name))
      return Boolean.valueOf(parseBoolean(value));
    if ("byte".equals(name))
      return Byte.valueOf(parseByte(value));
    if ("short".equals(name))
      return Short.valueOf(parseShort(value));
    if ("int".equals(name))
      return Integer.valueOf(parseInt(value));
    if ("long".equals(name))
      return Long.valueOf(parseLong(value));
    if ("float".equals(name))
      return Float.valueOf(parseFloat(value));
    if ("double".equals(name))
      return Double.valueOf(parseDouble(value));
    if ("char".equals(name))
      return Character.valueOf(parseChar(value));
    if ("array".equals(name))
      return parseArray(value);
    if (!"object".equals(name))
      throw invalid("unsupported wallet value: " + name);

    if (value.hasAttribute("idref"))
    {
      requireAttributes(value,"idref");
      requireNoChildren(value);
      Object referenced = this.ids.get(requiredAttribute(value,"idref"));
      if (!(referenced instanceof Serializable) || referenced instanceof Map)
        throw invalid("invalid wallet reference");
      return (Serializable)referenced;
    }

    String type = requiredAttribute(value,"class");
    if (DATE.equals(type))
      return parseDate(value);
    if (LOGIN.equals(type))
      return parseLogin(value);
    if (REMINDER.equals(type))
      return parseReminder(value);
    if (INTERVAL.equals(type))
      return parseInterval(value);
    throw invalid("unsupported wallet class: " + type);
  }

  private Login parseLogin(Element object) throws Exception
  {
    requireAttributes(object,"class","id");
    requireAttribute(object,"class",LOGIN);
    Login result = new Login();
    registerOptionalId(object,result);
    Set<String> properties = new HashSet<String>();
    for (Element property:children(object))
    {
      requireProperty(property,properties);
      String name = requiredAttribute(property,"property");
      Element value = onlyChild(property);
      if ("username".equals(name))
        result.setUsername(parseNullableString(value));
      else if ("password".equals(name))
        result.setPassword(parseNullableChars(value));
      else
        throw invalid("unsupported login property: " + name);
    }
    return result;
  }

  private Reminder parseReminder(Element object) throws Exception
  {
    requireAttributes(object,"class","id");
    requireAttribute(object,"class",REMINDER);
    Reminder result = new Reminder();
    registerOptionalId(object,result);
    Map<String,Serializable> data = new HashMap<String,Serializable>();
    Set<String> properties = new HashSet<String>();
    String dataId = null;
    boolean dataReference = false;

    for (Element property:children(object))
    {
      requireName(property,"void");
      String name = requiredAttribute(property,"property");
      if ("data".equals(name))
      {
        if (property.hasAttribute("id"))
        {
          requireAttributes(property,"property","id");
          if (dataId != null)
            throw invalid("duplicate reminder data definition");
          dataId = requiredAttribute(property,"id");
          registerId(dataId,data);
          parseMapEntries(property,data);
        }
        else
        {
          requireAttributes(property,"property");
          if (dataReference)
            throw invalid("duplicate reminder data reference");
          Element reference = onlyChild(property);
          requireName(reference,"object");
          requireAttributes(reference,"idref");
          requireNoChildren(reference);
          if (dataId == null || !dataId.equals(requiredAttribute(reference,"idref")))
            throw invalid("invalid reminder data reference");
          dataReference = true;
        }
        continue;
      }

      requireProperty(property,properties);
      Element value = onlyChild(property);
      if ("queue".equals(name))
        result.setQueue(parseNullableString(value));
      else if ("date".equals(name))
        result.setDate(requireType(parseValue(value),Date.class,"date"));
      else if ("end".equals(name))
        result.setEnd(nullableType(parseValue(value),Date.class,"end"));
      else if ("reminderInterval".equals(name))
        result.setReminderInterval(nullableType(parseValue(value),ReminderInterval.class,"interval"));
      else
        throw invalid("unsupported reminder property: " + name);
    }
    if (dataId != null && !dataReference)
      throw invalid("missing reminder data reference");
    result.setData(data);
    return result;
  }

  private void parseMapEntries(Element definition, Map<String,Serializable> data) throws Exception
  {
    List<Element> entries = children(definition);
    if (entries.size() > MAX_REMINDER_ENTRIES)
      throw invalid("too many reminder data entries");
    for (Element entry:entries)
    {
      requireName(entry,"void");
      requireAttributes(entry,"method");
      requireAttribute(entry,"method","put");
      List<Element> values = children(entry);
      if (values.size() != 2)
        throw invalid("invalid reminder data entry");
      String key = parseNullableString(values.get(0));
      if (data.containsKey(key))
        throw invalid("duplicate reminder data key");
      data.put(key,parseValue(values.get(1)));
    }
  }

  private ReminderInterval parseInterval(Element object) throws Exception
  {
    requireAttributes(object,"class","id");
    requireAttribute(object,"class",INTERVAL);
    ReminderInterval result = new ReminderInterval();
    registerOptionalId(object,result);
    Set<String> properties = new HashSet<String>();
    for (Element property:children(object))
    {
      requireProperty(property,properties);
      String name = requiredAttribute(property,"property");
      Element value = onlyChild(property);
      if ("interval".equals(name))
        result.setInterval(parseInt(value));
      else if ("timeUnit".equals(name))
        result.setTimeUnit(parseTimeUnit(value));
      else
        throw invalid("unsupported reminder interval property: " + name);
    }
    return result;
  }

  private TimeUnit parseTimeUnit(Element object) throws Exception
  {
    requireName(object,"object");
    requireAttributes(object,"class","method");
    requireAttribute(object,"class",ENUM);
    requireAttribute(object,"method","valueOf");
    List<Element> values = children(object);
    if (values.size() != 2)
      throw invalid("invalid time unit");
    requireName(values.get(0),"class");
    if (!TIME_UNIT.equals(parseText(values.get(0))))
      throw invalid("invalid time unit type");
    try
    {
      return TimeUnit.valueOf(parseString(values.get(1)));
    }
    catch (IllegalArgumentException e)
    {
      throw invalid("invalid time unit");
    }
  }

  private Date parseDate(Element object) throws Exception
  {
    requireName(object,"object");
    requireAttributes(object,"class","id");
    requireAttribute(object,"class",DATE);
    Date result = new Date(parseLong(onlyChild(object)));
    registerOptionalId(object,result);
    return result;
  }

  private Serializable parseArray(Element array) throws Exception
  {
    requireAttributes(array,"class","length","id");
    String type = requiredAttribute(array,"class");
    int length = parseLength(requiredAttribute(array,"length"));
    Serializable result;
    if ("char".equals(type))
      result = new char[length];
    else if ("byte".equals(type))
      result = new byte[length];
    else
      throw invalid("unsupported array type: " + type);
    registerOptionalId(array,result);

    boolean[] assigned = new boolean[length];
    for (Element assignment:children(array))
    {
      requireName(assignment,"void");
      requireAttributes(assignment,"index");
      int index = parseIndex(requiredAttribute(assignment,"index"),length);
      if (assigned[index])
        throw invalid("duplicate array index");
      assigned[index] = true;
      Element value = onlyChild(assignment);
      if (result instanceof char[])
        ((char[])result)[index] = parseChar(value);
      else
        ((byte[])result)[index] = parseByte(value);
    }
    return result;
  }

  private void requireProperty(Element property, Set<String> properties) throws IOException
  {
    requireName(property,"void");
    requireAttributes(property,"property");
    String name = requiredAttribute(property,"property");
    if (!properties.add(name))
      throw invalid("duplicate property: " + name);
  }

  private void registerOptionalId(Element element, Object value) throws IOException
  {
    if (element.hasAttribute("id"))
      registerId(requiredAttribute(element,"id"),value);
  }

  private void registerId(String id, Object value) throws IOException
  {
    if (id.length() == 0 || this.ids.put(id,value) != null)
      throw invalid("duplicate or empty id");
  }

  private static <T> T requireType(Serializable value, Class<T> type, String name) throws IOException
  {
    if (!type.isInstance(value))
      throw invalid("invalid " + name);
    return type.cast(value);
  }

  private static <T> T nullableType(Serializable value, Class<T> type, String name) throws IOException
  {
    if (value == null)
      return null;
    return requireType(value,type,name);
  }

  private static String parseNullableString(Element value) throws IOException
  {
    if ("null".equals(value.getTagName()))
    {
      requireAttributes(value);
      requireNoChildren(value);
      return null;
    }
    return parseString(value);
  }

  private char[] parseNullableChars(Element value) throws Exception
  {
    if ("null".equals(value.getTagName()))
    {
      requireAttributes(value);
      requireNoChildren(value);
      return null;
    }
    Serializable result = parseValue(value);
    if (result == null || result.getClass() != char[].class)
      throw invalid("invalid password array");
    return (char[])result;
  }

  private static String parseString(Element value) throws IOException
  {
    requireName(value,"string");
    requireAttributes(value);
    StringBuilder text = new StringBuilder();
    NodeList nodes = value.getChildNodes();
    for (int i=0;i<nodes.getLength();++i)
    {
      Node node = nodes.item(i);
      if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE)
        text.append(node.getNodeValue());
      else if (node.getNodeType() == Node.COMMENT_NODE)
        continue;
      else if (node instanceof Element && "char".equals(((Element)node).getTagName()))
        text.append(parseChar((Element)node));
      else
        throw invalid("unexpected string content");
      if (text.length() > MAX_TEXT_LENGTH)
        throw invalid("text value exceeds limit");
    }
    return text.toString();
  }

  private static boolean parseBoolean(Element value) throws IOException
  {
    requireName(value,"boolean");
    String text = parseText(value);
    if (!"true".equals(text) && !"false".equals(text))
      throw invalid("invalid boolean");
    return Boolean.parseBoolean(text);
  }

  private static byte parseByte(Element value) throws IOException
  {
    requireName(value,"byte");
    try { return Byte.parseByte(parseText(value)); }
    catch (NumberFormatException e) { throw invalid("invalid byte"); }
  }

  private static short parseShort(Element value) throws IOException
  {
    requireName(value,"short");
    try { return Short.parseShort(parseText(value)); }
    catch (NumberFormatException e) { throw invalid("invalid short"); }
  }

  private static int parseInt(Element value) throws IOException
  {
    requireName(value,"int");
    try { return Integer.parseInt(parseText(value)); }
    catch (NumberFormatException e) { throw invalid("invalid integer"); }
  }

  private static long parseLong(Element value) throws IOException
  {
    requireName(value,"long");
    try { return Long.parseLong(parseText(value)); }
    catch (NumberFormatException e) { throw invalid("invalid long"); }
  }

  private static float parseFloat(Element value) throws IOException
  {
    requireName(value,"float");
    try { return Float.parseFloat(parseText(value)); }
    catch (NumberFormatException e) { throw invalid("invalid float"); }
  }

  private static double parseDouble(Element value) throws IOException
  {
    requireName(value,"double");
    try { return Double.parseDouble(parseText(value)); }
    catch (NumberFormatException e) { throw invalid("invalid double"); }
  }

  private static char parseChar(Element value) throws IOException
  {
    requireName(value,"char");
    if (value.hasAttribute("code"))
    {
      requireAttributes(value,"code");
      requireNoChildren(value);
      if (value.getTextContent().length() != 0)
        throw invalid("invalid character code");
      String code = requiredAttribute(value,"code");
      if (!code.startsWith("#"))
        throw invalid("invalid character code");
      try
      {
        int number = Integer.parseInt(code.substring(1),16);
        if (number < Character.MIN_VALUE || number > Character.MAX_VALUE)
          throw invalid("invalid character code");
        return (char)number;
      }
      catch (NumberFormatException e)
      {
        throw invalid("invalid character code");
      }
    }
    String text = parseText(value);
    if (text.length() != 1)
      throw invalid("invalid character");
    return text.charAt(0);
  }

  private static int parseLength(String text) throws IOException
  {
    try
    {
      int value = Integer.parseInt(text);
      if (value < 0 || value > MAX_ARRAY_LENGTH)
        throw invalid("array length exceeds limit");
      return value;
    }
    catch (NumberFormatException e)
    {
      throw invalid("invalid array length");
    }
  }

  private static int parseIndex(String text, int length) throws IOException
  {
    try
    {
      int value = Integer.parseInt(text);
      if (value < 0 || value >= length)
        throw invalid("array index out of bounds");
      return value;
    }
    catch (NumberFormatException e)
    {
      throw invalid("invalid array index");
    }
  }

  private static String parseText(Element value) throws IOException
  {
    requireAttributes(value);
    requireNoChildren(value);
    String text = value.getTextContent();
    if (text.length() > MAX_TEXT_LENGTH)
      throw invalid("text value exceeds limit");
    return text;
  }

  private static Element onlyChild(Element parent) throws IOException
  {
    List<Element> values = children(parent);
    if (values.size() != 1)
      throw invalid("expected one child in " + parent.getTagName());
    return values.get(0);
  }

  private static void requireNoChildren(Element parent) throws IOException
  {
    if (!children(parent).isEmpty())
      throw invalid("unexpected child in " + parent.getTagName());
  }

  private static List<Element> children(Element parent) throws IOException
  {
    List<Element> result = new ArrayList<Element>();
    NodeList nodes = parent.getChildNodes();
    for (int i=0;i<nodes.getLength();++i)
    {
      Node node = nodes.item(i);
      if (node instanceof Element)
        result.add((Element)node);
      else if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE || node.getNodeType() == Node.COMMENT_NODE)
        continue;
      else
        throw invalid("unexpected XML content");
    }
    return result;
  }

  private static void requireName(Element element, String name) throws IOException
  {
    if (!name.equals(element.getTagName()))
      throw invalid("expected " + name + ", got " + element.getTagName());
  }

  private static void requireAttributes(Element element, String... allowed) throws IOException
  {
    Set<String> names = new HashSet<String>();
    for (String name:allowed)
      names.add(name);
    NamedNodeMap attributes = element.getAttributes();
    for (int i=0;i<attributes.getLength();++i)
    {
      String name = attributes.item(i).getNodeName();
      if (!names.contains(name))
        throw invalid("unsupported attribute: " + name);
    }
  }

  private static String requiredAttribute(Element element, String name) throws IOException
  {
    if (!element.hasAttribute(name))
      throw invalid("missing attribute: " + name);
    return element.getAttribute(name);
  }

  private static void requireAttribute(Element element, String name, String value) throws IOException
  {
    if (!value.equals(requiredAttribute(element,name)))
      throw invalid("invalid " + name + " attribute");
  }

  private static void enforceElementLimit(Document document) throws IOException
  {
    int count = 0;
    Deque<Node> nodes = new ArrayDeque<Node>();
    nodes.add(document.getDocumentElement());
    while (!nodes.isEmpty())
    {
      Node current = nodes.removeFirst();
      if (++count > MAX_ELEMENTS)
        throw invalid("too many XML elements");
      NodeList children = current.getChildNodes();
      for (int i=0;i<children.getLength();++i)
      {
        Node child = children.item(i);
        if (child instanceof Element)
          nodes.addLast(child);
      }
    }
  }

  /** Counts structure before a DOM is allocated. */
  private static void preflight(byte[] xml) throws Exception
  {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(false);
    factory.setXIncludeAware(false);
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities",false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities",false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);

    XMLReader reader = factory.newSAXParser().getXMLReader();
    reader.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD,"");
    reader.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA,"");
    reader.setProperty(ELEMENT_ATTRIBUTE_LIMIT,"16");
    LimitHandler handler = new LimitHandler();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.setEntityResolver(handler);
    reader.setProperty("http://xml.org/sax/properties/lexical-handler",handler);
    reader.parse(new InputSource(new ByteArrayInputStream(xml)));
  }

  private static final class LimitHandler extends DefaultHandler implements LexicalHandler
  {
    private int structure;
    private int depth;
    private long text;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
      addStructure(1 + attributes.getLength());
      if (++this.depth > MAX_XML_DEPTH)
        throw new SAXException("wallet XML is too deep");
    }

    @Override
    public void endElement(String uri, String localName, String qName)
    {
      this.depth--;
    }

    @Override
    public void characters(char[] characters, int start, int length) throws SAXException
    {
      this.text += length;
      if (this.text > MAX_XML_BYTES)
        throw new SAXException("wallet XML text exceeds limit");
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException
    {
      throw new SAXException("external entities are disabled");
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException
    {
      throw new SAXException("processing instructions are disabled");
    }

    @Override
    public void comment(char[] characters, int start, int length) throws SAXException
    {
      throw new SAXException("comments are disabled");
    }

    @Override
    public void startCDATA() throws SAXException
    {
      throw new SAXException("CDATA is disabled");
    }

    @Override public void endCDATA() throws SAXException {}
    @Override public void startDTD(String name, String publicId, String systemId) throws SAXException {}
    @Override public void endDTD() throws SAXException {}
    @Override public void startEntity(String name) throws SAXException {}
    @Override public void endEntity(String name) throws SAXException {}

    private void addStructure(int count) throws SAXException
    {
      if (count < 0 || this.structure > MAX_ELEMENTS - count)
        throw new SAXException("too much XML structure");
      this.structure += count;
    }
  }

  private static IOException invalid(String message)
  {
    return new IOException("invalid wallet XML: " + message);
  }

  /** Bounded buffer for decrypted wallet XML. */
  static final class Buffer extends OutputStream
  {
    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    @Override
    public void write(int value) throws IOException
    {
      ensureCapacity(1);
      this.bytes.write(value);
    }

    @Override
    public void write(byte[] value, int offset, int length) throws IOException
    {
      ensureCapacity(length);
      this.bytes.write(value,offset,length);
    }

    byte[] toByteArray()
    {
      return this.bytes.toByteArray();
    }

    private void ensureCapacity(int additional) throws IOException
    {
      if (additional < 0 || this.bytes.size() > MAX_XML_BYTES - additional)
        throw invalid("wallet XML exceeds size limit");
    }
  }
}
