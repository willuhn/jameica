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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.function.Predicate;

import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.reminder.ReminderInterval;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;

/**
 * Legt fest, welche Klassen beim Lesen eines Wallets instanziiert werden duerfen.
 * Erlaubt sind nur einfache Werte, die gaengigen Collections, die Datentypen von Jameica
 * sowie JavaBeans und Enums aus dem eigenen Code installierter Plugins. Beans werden
 * ausschliesslich ueber ihren oeffentlichen Standard-Konstruktor und ihre Bean-Properties
 * befuellt, also genau so, wie sie der {@link java.beans.XMLEncoder} frueher geschrieben hat.
 */
final class WalletTypes
{
  private final static Map<String,Class<?>> PRIMITIVES = Map.of(
      "boolean",boolean.class,"byte",byte.class,"char",char.class,"short",short.class,
      "int",int.class,"long",long.class,"float",float.class,"double",double.class);

  private final static List<Class<?>> VALUES = List.of(
      String.class,Boolean.class,Byte.class,Short.class,Integer.class,Long.class,
      Float.class,Double.class,Character.class,Date.class);

  private final static List<Class<?>> COLLECTIONS = List.of(
      ArrayList.class,LinkedList.class,Vector.class,HashSet.class,LinkedHashSet.class,TreeSet.class);

  private final static List<Class<?>> MAPS = List.of(
      HashMap.class,LinkedHashMap.class,Hashtable.class,TreeMap.class);

  private final static List<Class<?>> CORE = List.of(
      Login.class,Reminder.class,ReminderInterval.class,ReminderInterval.TimeUnit.class);

  private final static Map<String,Class<?>> KNOWN = new HashMap<>();

  static
  {
    for (List<Class<?>> list:List.of(VALUES,COLLECTIONS,MAPS,CORE))
    {
      for (Class<?> c:list)
        KNOWN.put(c.getName(),c);
    }
  }

  private final List<ClassLoader> loaders = new ArrayList<>();
  private final Predicate<Class<?>> pluginClasses;
  private final Map<Class<?>,Map<String,PropertyDescriptor>> properties = new HashMap<>();
  private final Map<Class<?>,Object> defaults = new HashMap<>();

  /**
   * ct.
   * @param loaders die Classloader, ueber die Plugin-Klassen gesucht werden.
   * @param pluginClasses prueft, ob eine Klasse aus dem Code eines Plugins stammt.
   */
  WalletTypes(List<ClassLoader> loaders, Predicate<Class<?>> pluginClasses)
  {
    for (ClassLoader l:loaders)
    {
      if (l != null && !this.loaders.contains(l))
        this.loaders.add(l);
    }
    this.pluginClasses = pluginClasses;
  }

  /**
   * Liefert die Typ-Regeln fuer das Wallet der angegebenen Klasse.
   * Gesucht wird wie frueher im XMLDecoder ueber den Context-Classloader und
   * zusaetzlich ueber den Classloader der Wallet-Klasse.
   * @param owner die Klasse, fuer die das Wallet gilt.
   * @return die Typ-Regeln.
   */
  static WalletTypes forWallet(Class owner)
  {
    return new WalletTypes(Arrays.asList(Thread.currentThread().getContextClassLoader(),owner.getClassLoader()),WalletTypes::isPluginClass);
  }

  /**
   * Prueft, ob die Klasse aus dem eigenen Code eines installierten Plugins stammt.
   * Das ist das Plugin-Verzeichnis selbst, dessen Compile-Ordner oder ein Jar direkt
   * im Plugin-Verzeichnis. Mitgelieferte Bibliotheken in Unterordnern zaehlen nicht dazu.
   * @param type die Klasse.
   * @return true, wenn sie aus einem Plugin stammt.
   */
  static boolean isPluginClass(Class<?> type)
  {
    List<File> dirs = new ArrayList<>();
    for (Manifest mf:Application.getPluginLoader().getInstalledManifests())
    {
      if (mf.getPluginDir() != null)
        dirs.add(new File(mf.getPluginDir()));
    }
    return isPluginClass(type,dirs);
  }

  /**
   * Prueft, ob die Klasse aus dem eigenen Code eines der Plugin-Verzeichnisse stammt.
   * @param type die Klasse.
   * @param pluginDirs die Plugin-Verzeichnisse.
   * @return true, wenn sie aus einem Plugin stammt.
   */
  static boolean isPluginClass(Class<?> type, List<File> pluginDirs)
  {
    try
    {
      CodeSource source = type.getProtectionDomain().getCodeSource();
      if (source == null || source.getLocation() == null)
        return false;

      File location = new File(source.getLocation().toURI()).getCanonicalFile();
      for (File pluginDir:pluginDirs)
      {
        File dir = pluginDir.getCanonicalFile();
        if (location.equals(dir) || location.equals(new File(dir,"bin")) || location.equals(new File(dir,"target/classes")))
          return true;
        if (location.isFile() && location.getName().endsWith(".jar") && dir.equals(location.getParentFile()))
          return true;
      }
    }
    catch (Exception e)
    {
      Logger.write(Level.DEBUG,"unable to determine origin of " + type.getName(),e);
    }
    return false;
  }

  /**
   * Liefert die Klasse fuer einen Klassennamen aus der Wallet-Datei.
   * @param name der Klassenname.
   * @return die Klasse.
   * @throws IOException wenn die Klasse unbekannt oder nicht erlaubt ist.
   */
  Class<?> resolve(String name) throws IOException
  {
    Class<?> type = KNOWN.get(name);
    if (type != null)
      return type;

    for (ClassLoader loader:this.loaders)
    {
      try
      {
        type = Class.forName(name,false,loader);
        break;
      }
      catch (ClassNotFoundException | LinkageError e)
      {
        // Im naechsten Classloader suchen
      }
    }
    if (type == null)
      throw WalletFormat.invalid("unknown class " + name);
    if (!this.isPluginBean(type) && !this.isPluginEnum(type))
      throw WalletFormat.invalid("class not allowed: " + name);
    return type;
  }

  /**
   * Liefert den Komponenten-Typ eines Arrays.
   * @param name Name des Typs.
   * @return der Typ.
   * @throws IOException wenn der Typ nicht erlaubt ist.
   */
  Class<?> resolveComponent(String name) throws IOException
  {
    Class<?> type = PRIMITIVES.get(name);
    if (type != null)
      return type;
    if (Object.class.getName().equals(name))
      return Object.class;
    return this.resolve(name);
  }

  /**
   * Prueft, ob es sich um einen der erlaubten einfachen Werte handelt.
   * @param type der Typ.
   * @return true, wenn es ein einfacher Wert ist.
   */
  static boolean isValue(Class<?> type)
  {
    return VALUES.contains(type);
  }

  /**
   * Prueft, ob es sich um eine der erlaubten Collections handelt.
   * @param type der Typ.
   * @return true, wenn es eine erlaubte Collection ist.
   */
  static boolean isCollection(Class<?> type)
  {
    return COLLECTIONS.contains(type);
  }

  /**
   * Prueft, ob es sich um eine der erlaubten Maps handelt.
   * @param type der Typ.
   * @return true, wenn es eine erlaubte Map ist.
   */
  static boolean isMap(Class<?> type)
  {
    return MAPS.contains(type);
  }

  /**
   * Prueft, ob es sich um eine erlaubte Bean handelt.
   * @param type der Typ.
   * @return true, wenn es eine erlaubte Bean ist.
   */
  boolean isBean(Class<?> type)
  {
    return type == Login.class || type == Reminder.class || type == ReminderInterval.class || this.isPluginBean(type);
  }

  /**
   * Prueft, ob es sich um ein erlaubtes Enum handelt.
   * @param type der Typ.
   * @return true, wenn es ein erlaubtes Enum ist.
   */
  boolean isEnum(Class<?> type)
  {
    return type == ReminderInterval.TimeUnit.class || this.isPluginEnum(type);
  }

  /**
   * Prueft, ob es sich um einen Typ handelt, der in einem Array stehen darf.
   * @param type der Typ.
   * @return true, wenn er erlaubt ist.
   */
  boolean isComponent(Class<?> type)
  {
    return type.isPrimitive() || type == Object.class || isValue(type) || this.isBean(type) || this.isEnum(type);
  }

  private boolean isPluginBean(Class<?> type)
  {
    if (type.isInterface() || type.isArray() || type.isPrimitive() || type.isEnum())
      return false;
    int mod = type.getModifiers();
    if (!Modifier.isPublic(mod) || Modifier.isAbstract(mod))
      return false;
    if (!Serializable.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type))
      return false;
    try
    {
      if (!Modifier.isPublic(type.getConstructor().getModifiers()))
        return false;
    }
    catch (NoSuchMethodException e)
    {
      return false;
    }
    return this.pluginClasses.test(type);
  }

  private boolean isPluginEnum(Class<?> type)
  {
    return type.isEnum() && Modifier.isPublic(type.getModifiers()) && this.pluginClasses.test(type);
  }

  /**
   * Erzeugt eine neue Instanz einer erlaubten Bean, Collection oder Map.
   * @param type der Typ.
   * @return die neue Instanz.
   * @throws IOException wenn der Typ nicht erlaubt ist.
   */
  Object newInstance(Class<?> type) throws IOException
  {
    if (!isCollection(type) && !isMap(type) && !this.isBean(type))
      throw WalletFormat.invalid("class not allowed: " + type.getName());
    try
    {
      Constructor<?> c = type.getConstructor();
      return c.newInstance();
    }
    catch (Exception e)
    {
      throw new IOException("unable to create " + type.getName(),e);
    }
  }

  /**
   * Liefert eine Default-Instanz der Bean. Properties mit unveraenderten Werten
   * werden wie beim XMLEncoder nicht gespeichert.
   * @param type der Typ der Bean.
   * @return die Default-Instanz.
   * @throws IOException
   */
  Object getDefault(Class<?> type) throws IOException
  {
    Object o = this.defaults.get(type);
    if (o == null)
    {
      o = this.newInstance(type);
      this.defaults.put(type,o);
    }
    return o;
  }

  /**
   * Liefert die Properties einer erlaubten Bean, die sowohl einen Getter als auch
   * einen Setter besitzen und nicht als transient markiert sind.
   * @param type der Typ der Bean.
   * @return Map mit den Properties. Key ist der Name.
   * @throws IOException
   */
  Map<String,PropertyDescriptor> getProperties(Class<?> type) throws IOException
  {
    if (!this.isBean(type))
      throw WalletFormat.invalid("class not allowed: " + type.getName());

    Map<String,PropertyDescriptor> result = this.properties.get(type);
    if (result != null)
      return result;

    result = new TreeMap<>();
    try
    {
      BeanInfo info = Introspector.getBeanInfo(type,Object.class);
      for (PropertyDescriptor pd:info.getPropertyDescriptors())
      {
        Method read = pd.getReadMethod();
        Method write = pd.getWriteMethod();
        if (read == null || write == null || Boolean.TRUE.equals(pd.getValue("transient")))
          continue;
        if (!Modifier.isPublic(read.getModifiers()) || !Modifier.isPublic(write.getModifiers()))
          continue;
        result.put(pd.getName(),pd);
      }
    }
    catch (Exception e)
    {
      throw new IOException("unable to inspect " + type.getName(),e);
    }
    this.properties.put(type,result);
    return result;
  }

  /**
   * Liefert eine einzelne Property einer erlaubten Bean.
   * @param type der Typ der Bean.
   * @param name Name der Property.
   * @return die Property.
   * @throws IOException wenn die Property nicht existiert.
   */
  PropertyDescriptor getProperty(Class<?> type, String name) throws IOException
  {
    PropertyDescriptor pd = this.getProperties(type).get(name);
    if (pd == null)
      throw WalletFormat.invalid("unknown property " + type.getName() + "." + name);
    return pd;
  }

  /**
   * Liest den Wert einer Bean-Property.
   * @param bean die Bean.
   * @param name Name der Property.
   * @return der Wert.
   * @throws IOException
   */
  Object get(Object bean, String name) throws IOException
  {
    PropertyDescriptor pd = this.getProperty(bean.getClass(),name);
    try
    {
      return pd.getReadMethod().invoke(bean);
    }
    catch (Exception e)
    {
      throw new IOException("unable to read property " + name,e);
    }
  }

  /**
   * Setzt den Wert einer Bean-Property.
   * @param bean die Bean.
   * @param name Name der Property.
   * @param value der Wert.
   * @throws IOException
   */
  void set(Object bean, String name, Object value) throws IOException
  {
    PropertyDescriptor pd = this.getProperty(bean.getClass(),name);
    Class<?> type = pd.getPropertyType();
    if (value == null ? type.isPrimitive() : !wrap(type).isInstance(value))
      throw WalletFormat.invalid("invalid value for property " + name);
    try
    {
      pd.getWriteMethod().invoke(bean,value);
    }
    catch (Exception e)
    {
      throw new IOException("unable to write property " + name,e);
    }
  }

  /**
   * Liefert zu einer Wrapper-Klasse den primitiven Typ.
   * @param type die Wrapper-Klasse.
   * @return der primitive Typ oder der Typ selbst.
   */
  static Class<?> unwrap(Class<?> type)
  {
    for (Class<?> c:PRIMITIVES.values())
    {
      if (wrap(c) == type)
        return c;
    }
    return type;
  }

  /**
   * Liefert zu einem primitiven Typ die Wrapper-Klasse.
   * @param type der Typ.
   * @return die Wrapper-Klasse oder der Typ selbst.
   */
  static Class<?> wrap(Class<?> type)
  {
    if (!type.isPrimitive())
      return type;
    if (type == boolean.class) return Boolean.class;
    if (type == byte.class)    return Byte.class;
    if (type == char.class)    return Character.class;
    if (type == short.class)   return Short.class;
    if (type == int.class)     return Integer.class;
    if (type == long.class)    return Long.class;
    if (type == float.class)   return Float.class;
    return Double.class;
  }
}
