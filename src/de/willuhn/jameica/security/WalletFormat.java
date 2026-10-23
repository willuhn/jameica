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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.SortedSet;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Dateiformat der Wallets.
 * Geschrieben wird per JAXB ein reines Datenformat, in dem jeder Wert mit seinem Typ
 * abgelegt ist. Gelesen werden sowohl dieses Format als auch das alte Format des
 * {@link java.beans.XMLEncoder}. Fuer beide gelten dieselben Typ-Regeln aus {@link WalletTypes}.
 * Ein {@link java.beans.XMLDecoder} wird nicht mehr verwendet, da dieser beliebige
 * Konstruktoren und Methoden ausfuehren kann.
 */
final class WalletFormat
{
  static final int MAX_XML_BYTES = 16 * 1024 * 1024;
  private static final int MAX_ELEMENTS = 1000000;
  private static final int MAX_DEPTH = 256;
  private static final int VERSION = 1;
  private static final String ELEMENT_ATTRIBUTE_LIMIT = "http://www.oracle.com/xml/jaxp/properties/elementAttributeLimit";

  private static JAXBContext context = null;

  private WalletFormat()
  {
  }

  /**
   * Liest ein entschluesseltes Wallet im neuen oder alten Format.
   * @param xml das entschluesselte XML.
   * @param types die Typ-Regeln.
   * @return die Daten des Wallets.
   * @throws Exception wenn die Datei nicht gelesen werden kann oder nicht erlaubte Daten enthaelt.
   */
  static Hashtable<String,Serializable> read(byte[] xml, WalletTypes types) throws Exception
  {
    if (xml.length > MAX_XML_BYTES)
      throw invalid("wallet exceeds size limit");

    String root = preflight(xml);
    if ("java".equals(root))
      return WalletLegacyReader.read(xml,types);
    if (!"wallet".equals(root))
      throw invalid("unknown wallet format");

    SAXParserFactory factory = createParserFactory();
    factory.setNamespaceAware(true);
    XMLReader reader = factory.newSAXParser().getXMLReader();
    Unmarshaller u = getContext().createUnmarshaller();
    u.setEventHandler(e -> false);
    WalletDocument doc = (WalletDocument) u.unmarshal(new SAXSource(reader,new InputSource(new ByteArrayInputStream(xml))));
    if (doc.version != VERSION)
      throw invalid("unsupported wallet version " + doc.version);

    Decoder decoder = new Decoder(types);
    Hashtable<String,Serializable> result = new Hashtable<String,Serializable>();
    for (WalletEntry e:doc.entries)
    {
      Object key = decoder.decode(e.key,0);
      Object value = decoder.decode(e.value,0);
      put(result,key,value);
    }
    return result;
  }

  /**
   * Schreibt ein Wallet im neuen Format.
   * @param wallet die Daten des Wallets.
   * @param types die Typ-Regeln.
   * @return das XML.
   * @throws Exception wenn das Wallet nicht erlaubte Daten enthaelt.
   */
  static byte[] write(Hashtable<String,Serializable> wallet, WalletTypes types) throws Exception
  {
    Encoder encoder = new Encoder(types);
    WalletDocument doc = new WalletDocument();
    doc.version = VERSION;
    for (Map.Entry<String,Serializable> e:wallet.entrySet())
    {
      WalletEntry entry = new WalletEntry();
      entry.key = encoder.encode(e.getKey(),0);
      entry.value = encoder.encode(e.getValue(),0);
      doc.entries.add(entry);
    }

    Buffer buffer = new Buffer();
    Marshaller m = getContext().createMarshaller();
    m.setProperty(Marshaller.JAXB_ENCODING,"UTF-8");
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.FALSE);
    m.marshal(doc,buffer);
    return buffer.toByteArray();
  }

  /**
   * Uebernimmt einen gelesenen Eintrag in das Wallet.
   * @param wallet das Wallet.
   * @param key der Alias.
   * @param value der Wert.
   * @throws IOException wenn der Eintrag ungueltig ist.
   */
  static void put(Hashtable<String,Serializable> wallet, Object key, Object value) throws IOException
  {
    if (!(key instanceof String) || !(value instanceof Serializable))
      throw invalid("invalid wallet entry");
    if (wallet.put((String) key,(Serializable) value) != null)
      throw invalid("duplicate wallet key");
  }

  private static synchronized JAXBContext getContext() throws Exception
  {
    if (context == null)
      context = JAXBContext.newInstance(WalletDocument.class);
    return context;
  }

  /**
   * Erzeugt eine Parser-Factory, die DTDs, externe Entities und XInclude ablehnt.
   * @return die Parser-Factory.
   * @throws Exception
   */
  static SAXParserFactory createParserFactory() throws Exception
  {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setXIncludeAware(false);
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities",false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities",false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
    return factory;
  }

  /**
   * Prueft Groesse und Struktur des XML, bevor es weiterverarbeitet wird.
   * @param xml das XML.
   * @return Name des Root-Elements.
   * @throws Exception wenn das XML nicht erlaubt ist.
   */
  private static String preflight(byte[] xml) throws Exception
  {
    XMLReader reader = createParserFactory().newSAXParser().getXMLReader();
    reader.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD,"");
    reader.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA,"");
    reader.setProperty(ELEMENT_ATTRIBUTE_LIMIT,"16");
    LimitHandler handler = new LimitHandler();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.setEntityResolver(handler);
    reader.setProperty("http://xml.org/sax/properties/lexical-handler",handler);
    reader.parse(new InputSource(new ByteArrayInputStream(xml)));
    return handler.root;
  }

  /**
   * Erzeugt eine Exception fuer ungueltige Wallet-Daten.
   * @param message die Meldung.
   * @return die Exception.
   */
  static IOException invalid(String message)
  {
    return new IOException("invalid wallet data: " + message);
  }

  /**
   * Wandelt die Werte des Wallets in das Dateiformat.
   */
  private static class Encoder
  {
    private final WalletTypes types;
    private final Map<Object,String> ids = new IdentityHashMap<Object,String>();

    private Encoder(WalletTypes types)
    {
      this.types = types;
    }

    private WalletValue encode(Object o, int depth) throws IOException
    {
      if (depth > MAX_DEPTH)
        throw invalid("wallet value is nested too deeply");

      WalletValue v = new WalletValue();
      if (o == null)
      {
        v.type = "null";
        return v;
      }

      Class<?> type = o.getClass();
      if (type == String.class)
      {
        v.type = "string";
        setText(v,(String) o);
        return v;
      }
      if (type == Character.class)
      {
        v.type = "char";
        v.text = Integer.toString((Character) o);
        return v;
      }
      if (type == Date.class)
      {
        v.type = "date";
        v.text = Long.toString(((Date) o).getTime());
        return v;
      }
      if (WalletTypes.isValue(type))
      {
        // Boolean und die Zahlen-Typen
        v.type = WalletTypes.unwrap(type).getName();
        v.text = o.toString();
        return v;
      }
      if (o instanceof Enum)
      {
        Class<?> e = ((Enum<?>) o).getDeclaringClass();
        if (!this.types.isEnum(e))
          throw invalid("unsupported wallet value: " + e.getName());
        v.type = "enum";
        v.className = e.getName();
        v.text = ((Enum<?>) o).name();
        return v;
      }

      // Ab hier Werte, die mehrfach im Wallet referenziert sein koennen
      String ref = this.ids.get(o);
      if (ref != null)
      {
        v.type = "ref";
        v.ref = ref;
        return v;
      }
      v.id = Integer.toString(this.ids.size());
      this.ids.put(o,v.id);

      if (type == byte[].class)
      {
        v.type = "bytes";
        v.text = Base64.getEncoder().encodeToString((byte[]) o);
      }
      else if (type == char[].class)
      {
        v.type = "chars";
        setText(v,new String((char[]) o));
      }
      else if (type.isArray())
      {
        Class<?> component = type.getComponentType();
        if (!this.types.isComponent(component))
          throw invalid("unsupported wallet value: " + type.getName());
        v.type = "array";
        v.className = component.getName();
        v.items = new ArrayList<WalletValue>();
        for (int i=0;i<Array.getLength(o);++i)
          v.items.add(this.encode(Array.get(o,i),depth+1));
      }
      else if (WalletTypes.isCollection(type))
      {
        if (o instanceof SortedSet && ((SortedSet<?>) o).comparator() != null)
          throw invalid("unsupported wallet value: sorted set with comparator");
        v.type = "collection";
        v.className = type.getName();
        v.items = new ArrayList<WalletValue>();
        for (Object item:(Collection<?>) o)
          v.items.add(this.encode(item,depth+1));
      }
      else if (WalletTypes.isMap(type))
      {
        if (o instanceof SortedMap && ((SortedMap<?,?>) o).comparator() != null)
          throw invalid("unsupported wallet value: sorted map with comparator");
        v.type = "map";
        v.className = type.getName();
        v.entries = new ArrayList<WalletEntry>();
        for (Map.Entry<?,?> e:((Map<?,?>) o).entrySet())
        {
          WalletEntry entry = new WalletEntry();
          entry.key = this.encode(e.getKey(),depth+1);
          entry.value = this.encode(e.getValue(),depth+1);
          v.entries.add(entry);
        }
      }
      else if (this.types.isBean(type))
      {
        v.type = "bean";
        v.className = type.getName();
        v.properties = new ArrayList<WalletProperty>();
        Object defaults = this.types.getDefault(type);
        for (String name:this.types.getProperties(type).keySet())
        {
          Object value = this.types.get(o,name);
          // Wie beim XMLEncoder: Unveraenderte Default-Werte speichern wir nicht
          if (Objects.deepEquals(value,this.types.get(defaults,name)))
            continue;
          WalletProperty p = new WalletProperty();
          p.name = name;
          p.value = this.encode(value,depth+1);
          v.properties.add(p);
        }
      }
      else
      {
        throw invalid("unsupported wallet value: " + type.getName());
      }
      return v;
    }

    private static void setText(WalletValue v, String s)
    {
      // Zeichen, die XML nicht unveraendert transportieren kann, speichern wir als Base64
      for (int i=0;i<s.length();++i)
      {
        char c = s.charAt(i);
        if (c == '\t' || c == '\n' || (c >= 0x20 && c < 0xD800) || (c >= 0xE000 && c <= 0xFFFD))
          continue;

        byte[] b = new byte[s.length() * 2];
        for (int k=0;k<s.length();++k)
        {
          b[2*k]   = (byte) (s.charAt(k) >> 8);
          b[2*k+1] = (byte) s.charAt(k);
        }
        v.encoding = "base64";
        v.text = Base64.getEncoder().encodeToString(b);
        return;
      }
      v.text = s;
    }
  }

  /**
   * Erzeugt aus dem Dateiformat wieder die Werte des Wallets.
   */
  private static class Decoder
  {
    private final WalletTypes types;
    private final Map<String,Object> ids = new HashMap<String,Object>();

    private Decoder(WalletTypes types)
    {
      this.types = types;
    }

    private Object decode(WalletValue v, int depth) throws IOException
    {
      if (v == null || v.type == null)
        throw invalid("missing value");
      if (depth > MAX_DEPTH)
        throw invalid("wallet value is nested too deeply");

      try
      {
        switch (v.type)
        {
          case "null":    return null;
          case "string":  return getText(v);
          case "char":    return Character.valueOf((char) Integer.parseInt(text(v)));
          case "boolean": return Boolean.valueOf(parseBoolean(text(v)));
          case "byte":    return Byte.valueOf(text(v));
          case "short":   return Short.valueOf(text(v));
          case "int":     return Integer.valueOf(text(v));
          case "long":    return Long.valueOf(text(v));
          case "float":   return Float.valueOf(text(v));
          case "double":  return Double.valueOf(text(v));
          case "date":    return new Date(Long.parseLong(text(v)));
          case "enum":    return this.decodeEnum(v);
          case "ref":
            if (v.ref == null || !this.ids.containsKey(v.ref))
              throw invalid("unknown reference " + v.ref);
            return this.ids.get(v.ref);
        }
      }
      catch (NumberFormatException e)
      {
        throw invalid("invalid " + v.type + " value");
      }

      switch (v.type)
      {
        case "bytes":
          try
          {
            return this.register(v,Base64.getDecoder().decode(text(v)));
          }
          catch (IllegalArgumentException e)
          {
            throw invalid("invalid bytes value");
          }
        case "chars":
          return this.register(v,getText(v).toCharArray());
        case "array":
          return this.decodeArray(v,depth);
        case "collection":
          return this.decodeCollection(v,depth);
        case "map":
          return this.decodeMap(v,depth);
        case "bean":
          return this.decodeBean(v,depth);
        default:
          throw invalid("unsupported value type " + v.type);
      }
    }

    private Object decodeEnum(WalletValue v) throws IOException
    {
      Class<?> type = this.types.resolve(v.className);
      if (!this.types.isEnum(type))
        throw invalid("class not allowed: " + v.className);
      for (Object o:type.getEnumConstants())
      {
        if (((Enum<?>) o).name().equals(text(v)))
          return o;
      }
      throw invalid("unknown enum constant " + v.text);
    }

    private Object decodeArray(WalletValue v, int depth) throws IOException
    {
      Class<?> component = this.types.resolveComponent(v.className);
      if (!this.types.isComponent(component))
        throw invalid("class not allowed: " + v.className);
      List<WalletValue> items = items(v.items);
      Object result = this.register(v,Array.newInstance(component,items.size()));
      for (int i=0;i<items.size();++i)
      {
        try
        {
          Array.set(result,i,this.decode(items.get(i),depth+1));
        }
        catch (IllegalArgumentException e)
        {
          throw invalid("invalid array item");
        }
      }
      return result;
    }

    @SuppressWarnings("unchecked")
    private Object decodeCollection(WalletValue v, int depth) throws IOException
    {
      Class<?> type = this.types.resolve(v.className);
      if (!WalletTypes.isCollection(type))
        throw invalid("class not allowed: " + v.className);
      Collection<Object> result = (Collection<Object>) this.register(v,this.types.newInstance(type));
      for (WalletValue item:items(v.items))
        result.add(this.decode(item,depth+1));
      return result;
    }

    @SuppressWarnings("unchecked")
    private Object decodeMap(WalletValue v, int depth) throws IOException
    {
      Class<?> type = this.types.resolve(v.className);
      if (!WalletTypes.isMap(type))
        throw invalid("class not allowed: " + v.className);
      Map<Object,Object> result = (Map<Object,Object>) this.register(v,this.types.newInstance(type));
      for (WalletEntry e:items(v.entries))
        result.put(this.decode(e.key,depth+1),this.decode(e.value,depth+1));
      return result;
    }

    private Object decodeBean(WalletValue v, int depth) throws IOException
    {
      Class<?> type = this.types.resolve(v.className);
      if (!this.types.isBean(type))
        throw invalid("class not allowed: " + v.className);
      Object result = this.register(v,this.types.newInstance(type));
      List<String> done = new ArrayList<String>();
      for (WalletProperty p:items(v.properties))
      {
        if (p.name == null || done.contains(p.name))
          throw invalid("invalid property");
        done.add(p.name);
        this.types.set(result,p.name,this.decode(p.value,depth+1));
      }
      return result;
    }

    private Object register(WalletValue v, Object o) throws IOException
    {
      if (v.id == null)
        return o;
      if (this.ids.containsKey(v.id))
        throw invalid("duplicate id " + v.id);
      this.ids.put(v.id,o);
      return o;
    }

    private static <T> List<T> items(List<T> list)
    {
      return list != null ? list : new ArrayList<T>();
    }

    private static boolean parseBoolean(String s) throws IOException
    {
      if (!"true".equals(s) && !"false".equals(s))
        throw invalid("invalid boolean value");
      return Boolean.parseBoolean(s);
    }

    private static String text(WalletValue v) throws IOException
    {
      if (v.text == null || v.encoding != null)
        throw invalid("invalid " + v.type + " value");
      return v.text;
    }

    private static String getText(WalletValue v) throws IOException
    {
      if (v.encoding == null)
        return text(v);
      if (!"base64".equals(v.encoding) || v.text == null)
        throw invalid("invalid text encoding");
      try
      {
        byte[] b = Base64.getDecoder().decode(v.text);
        if (b.length % 2 != 0)
          throw invalid("invalid text encoding");
        char[] c = new char[b.length / 2];
        for (int i=0;i<c.length;++i)
          c[i] = (char) (((b[2*i] & 0xff) << 8) | (b[2*i+1] & 0xff));
        return new String(c);
      }
      catch (IllegalArgumentException e)
      {
        throw invalid("invalid text encoding");
      }
    }
  }

  /**
   * Das Root-Element der Wallet-Datei.
   */
  @XmlRootElement(name="wallet")
  @XmlAccessorType(XmlAccessType.FIELD)
  static class WalletDocument
  {
    @XmlAttribute(required=true)
    private int version;

    @XmlElement(name="entry")
    private List<WalletEntry> entries = new ArrayList<WalletEntry>();
  }

  /**
   * Ein Schluessel-Wert-Paar im Wallet oder in einer Map.
   */
  @XmlAccessorType(XmlAccessType.FIELD)
  static class WalletEntry
  {
    @XmlElement(required=true)
    private WalletValue key;

    @XmlElement(required=true)
    private WalletValue value;
  }

  /**
   * Eine Property einer Bean.
   */
  @XmlAccessorType(XmlAccessType.FIELD)
  static class WalletProperty
  {
    @XmlAttribute(required=true)
    private String name;

    @XmlElement(required=true)
    private WalletValue value;
  }

  /**
   * Ein einzelner Wert samt Typ.
   */
  @XmlAccessorType(XmlAccessType.FIELD)
  static class WalletValue
  {
    @XmlAttribute(required=true)
    private String type;

    @XmlAttribute(name="class")
    private String className;

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String ref;

    @XmlAttribute
    private String encoding;

    @XmlElement
    private String text;

    @XmlElement(name="item")
    private List<WalletValue> items;

    @XmlElement(name="entry")
    private List<WalletEntry> entries;

    @XmlElement(name="property")
    private List<WalletProperty> properties;
  }

  /**
   * Zaehlt die Struktur des XML, bevor ein Objektbaum erzeugt wird.
   */
  private static class LimitHandler extends DefaultHandler implements LexicalHandler
  {
    private String root = null;
    private int elements = 0;
    private int depth = 0;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
      if (this.root == null)
        this.root = qName;
      if (++this.elements > MAX_ELEMENTS)
        throw new SAXException("wallet contains too many elements");
      if (++this.depth > MAX_DEPTH)
        throw new SAXException("wallet is nested too deeply");
    }

    @Override
    public void endElement(String uri, String localName, String qName)
    {
      this.depth--;
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
    public void comment(char[] ch, int start, int length) throws SAXException
    {
      throw new SAXException("comments are disabled");
    }

    @Override
    public void startCDATA() throws SAXException
    {
      throw new SAXException("CDATA is disabled");
    }

    @Override public void endCDATA() {}
    @Override public void startDTD(String name, String publicId, String systemId) {}
    @Override public void endDTD() {}
    @Override public void startEntity(String name) {}
    @Override public void endEntity(String name) {}
  }

  /**
   * In der Groesse begrenzter Puffer fuer das XML eines Wallets.
   */
  static final class Buffer extends OutputStream
  {
    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    @Override
    public void write(int b) throws IOException
    {
      this.ensureCapacity(1);
      this.bytes.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
      this.ensureCapacity(len);
      this.bytes.write(b,off,len);
    }

    byte[] toByteArray()
    {
      return this.bytes.toByteArray();
    }

    private void ensureCapacity(int additional) throws IOException
    {
      if (additional < 0 || this.bytes.size() > MAX_XML_BYTES - additional)
        throw invalid("wallet exceeds size limit");
    }
  }
}
