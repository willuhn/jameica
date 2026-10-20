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

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.reminder.ReminderInterval;
import de.willuhn.jameica.reminder.ReminderInterval.TimeUnit;

/**
 * Testet das Lesen und Schreiben der Wallet-Dateien.
 */
public class WalletFormatTest
{
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  /**
   * Enum, wie es ein Plugin im Wallet speichern koennte.
   */
  public enum Kind
  {
    /** A */
    A,
    /** B */
    B
  }

  /**
   * Bean, wie sie ein Plugin im Wallet speichern koennte.
   */
  public static class PluginBean implements Serializable
  {
    private static final long serialVersionUID = 1L;
    private String name;
    private int count;
    private char[] secret;
    private Date when;
    private Kind kind;
    private List<Object> tags = new ArrayList<Object>();
    private Map<String,Integer> map;
    private PluginBean child;
    private PluginBean parent;
    private int[] numbers;
    private String[] names;

    /** @return name */
    public String getName() { return name; }
    /** @param v name */
    public void setName(String v) { name = v; }
    /** @return count */
    public int getCount() { return count; }
    /** @param v count */
    public void setCount(int v) { count = v; }
    /** @return secret */
    public char[] getSecret() { return secret; }
    /** @param v secret */
    public void setSecret(char[] v) { secret = v; }
    /** @return when */
    public Date getWhen() { return when; }
    /** @param v when */
    public void setWhen(Date v) { when = v; }
    /** @return kind */
    public Kind getKind() { return kind; }
    /** @param v kind */
    public void setKind(Kind v) { kind = v; }
    /** @return tags */
    public List<Object> getTags() { return tags; }
    /** @param v tags */
    public void setTags(List<Object> v) { tags = v; }
    /** @return map */
    public Map<String,Integer> getMap() { return map; }
    /** @param v map */
    public void setMap(Map<String,Integer> v) { map = v; }
    /** @return child */
    public PluginBean getChild() { return child; }
    /** @param v child */
    public void setChild(PluginBean v) { child = v; }
    /** @return parent */
    public PluginBean getParent() { return parent; }
    /** @param v parent */
    public void setParent(PluginBean v) { parent = v; }
    /** @return numbers */
    public int[] getNumbers() { return numbers; }
    /** @param v numbers */
    public void setNumbers(int[] v) { numbers = v; }
    /** @return names */
    public String[] getNames() { return names; }
    /** @param v names */
    public void setNames(String[] v) { names = v; }
  }

  /**
   * Bestehende Wallets mit Jameica-Datentypen bleiben lesbar.
   * @throws Exception
   */
  @Test
  public void readLegacyCoreWallet() throws Exception
  {
    Hashtable<String,Serializable> wallet = coreWallet();
    assertCoreWallet(WalletFormat.read(legacy(wallet),types()));
  }

  /**
   * Bestehende Wallets mit Beans und Enums aus Plugins bleiben lesbar.
   * @throws Exception
   */
  @Test
  public void readLegacyPluginWallet() throws Exception
  {
    Hashtable<String,Serializable> wallet = new Hashtable<String,Serializable>();
    wallet.put("bean",pluginBean());
    assertPluginBean(WalletFormat.read(legacy(wallet),types(PluginBean.class,Kind.class)).get("bean"));
  }

  /**
   * Beans, die nicht aus einem Plugin stammen, werden nicht instanziiert.
   * @throws Exception
   */
  @Test
  public void rejectLegacyBeanOutsidePlugin() throws Exception
  {
    Hashtable<String,Serializable> wallet = new Hashtable<String,Serializable>();
    wallet.put("bean",pluginBean());
    assertRejected(legacy(wallet),types(Kind.class),"class not allowed");
  }

  /**
   * Aufrufe beliebiger Konstruktoren und Methoden im alten Format werden abgelehnt,
   * bevor sie ausgefuehrt werden.
   * @throws Exception
   */
  @Test
  public void rejectExecutableLegacyWallet() throws Exception
  {
    Path proof = folder.getRoot().toPath().resolve("proof.txt");
    String path = proof.toString().replace("&","&amp;").replace("<","&lt;");
    String[] payloads = {
      "<object class=\"java.io.PrintWriter\"><string>" + path + "</string><void method=\"close\"/></object>",
      "<object class=\"java.lang.ProcessBuilder\"><array class=\"java.lang.String\" length=\"1\"><void index=\"0\"><string>true</string></void></array><void method=\"start\"/></object>",
      "<object class=\"java.lang.Runtime\" method=\"getRuntime\"/>",
      "<object class=\"java.util.ArrayList\"><void method=\"clear\"/></object>",
      "<object class=\"java.lang.Boolean\" field=\"TRUE\"/>",
      "<object class=\"" + PluginBean.class.getName() + "\"><string>x</string></object>",
      "<object class=\"" + PluginBean.class.getName() + "\"><void method=\"toString\"/></object>",
      "<object class=\"" + PluginBean.class.getName() + "\"><void property=\"tags\"><void method=\"add\"><object class=\"java.io.PrintWriter\"><string>" + path + "</string></object></void></void></object>",
    };
    for (String payload:payloads)
    {
      String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><java version=\"21\" class=\"java.beans.XMLDecoder\">"
          + "<object class=\"java.util.Hashtable\"><void method=\"put\"><string>x</string>" + payload + "</void></object></java>";
      try
      {
        WalletFormat.read(xml.getBytes(StandardCharsets.UTF_8),types(PluginBean.class));
        Assert.fail("accepted " + payload);
      }
      catch (IOException e)
      {
        // erwartet
      }
    }
    Assert.assertFalse(Files.exists(proof));
  }

  /**
   * DTDs und externe Entities werden in beiden Formaten abgelehnt.
   * @throws Exception
   */
  @Test
  public void rejectDoctype() throws Exception
  {
    Path secret = folder.newFile("secret.txt").toPath();
    Files.write(secret,"secret".getBytes(StandardCharsets.UTF_8));
    String dtd = "<!DOCTYPE x [<!ENTITY e SYSTEM \"" + secret.toUri() + "\">]>";
    for (String xml:Arrays.asList(
        "<?xml version=\"1.0\"?>" + dtd + "<java version=\"21\" class=\"java.beans.XMLDecoder\"><object class=\"java.util.Hashtable\"><void method=\"put\"><string>x</string><string>&e;</string></void></object></java>",
        "<?xml version=\"1.0\"?>" + dtd + "<wallet version=\"1\"><entry><key type=\"string\"><text>x</text></key><value type=\"string\"><text>&e;</text></value></entry></wallet>"))
    {
      try
      {
        WalletFormat.read(xml.getBytes(StandardCharsets.UTF_8),types());
        Assert.fail("accepted doctype");
      }
      catch (Exception e)
      {
        Assert.assertFalse(String.valueOf(e.getMessage()).contains("secret"));
      }
    }
  }

  /**
   * Das neue Format enthaelt alle Werte unveraendert, auch Referenzen und Zyklen.
   * @throws Exception
   */
  @Test
  public void roundTrip() throws Exception
  {
    Hashtable<String,Serializable> wallet = coreWallet();
    wallet.put("bean",pluginBean());
    wallet.put("text","\u0000a\rb\tc\n<&>😀\uD800 ");
    wallet.put("empty","");
    wallet.put("nan",Double.valueOf(Double.NaN));
    wallet.put("char",Character.valueOf('\u0001'));
    wallet.put("tree",new TreeMap<String,Object>(Map.of("a",Kind.A,"b",new Object[]{"x",null,Integer.valueOf(1)})));
    wallet.put("set",new LinkedHashSet<Object>(Arrays.asList("x",Long.valueOf(2),null)));

    WalletTypes types = types(PluginBean.class,Kind.class);
    byte[] xml = WalletFormat.write(wallet,types);
    Assert.assertTrue(new String(xml,StandardCharsets.UTF_8).contains("<wallet version=\"1\">"));

    Hashtable<String,Serializable> result = WalletFormat.read(xml,types);
    assertCoreWallet(result);
    assertPluginBean(result.get("bean"));
    Assert.assertEquals(wallet.get("text"),result.get("text"));
    Assert.assertEquals("",result.get("empty"));
    Assert.assertTrue(((Double) result.get("nan")).isNaN());
    Assert.assertEquals(Character.valueOf('\u0001'),result.get("char"));
    TreeMap<?,?> tree = (TreeMap<?,?>) result.get("tree");
    Assert.assertEquals(Kind.A,tree.get("a"));
    Assert.assertArrayEquals(new Object[]{"x",null,Integer.valueOf(1)},(Object[]) tree.get("b"));
    Assert.assertEquals(Arrays.asList("x",Long.valueOf(2),null),new ArrayList<Object>((Set<?>) result.get("set")));
  }

  /**
   * Ein Wallet im alten Format kann im neuen Format gespeichert und wieder gelesen werden.
   * @throws Exception
   */
  @Test
  public void migrateLegacyWallet() throws Exception
  {
    Hashtable<String,Serializable> wallet = coreWallet();
    wallet.put("bean",pluginBean());
    WalletTypes types = types(PluginBean.class,Kind.class);

    Hashtable<String,Serializable> migrated = WalletFormat.read(WalletFormat.write(WalletFormat.read(legacy(wallet),types),types),types);
    assertCoreWallet(migrated);
    assertPluginBean(migrated.get("bean"));
  }

  /**
   * Werte, die nicht wieder gelesen werden koennten, werden gar nicht erst gespeichert.
   * @throws Exception
   */
  @Test
  public void rejectUnsupportedValue() throws Exception
  {
    List<Serializable> values = Arrays.asList(
        new File("x"),
        pluginBean(),
        new TreeMap<String,String>(Comparator.reverseOrder()),
        new ArrayList<Object>(Arrays.asList(new StringBuilder("x"))));
    for (Serializable value:values)
    {
      Hashtable<String,Serializable> wallet = new Hashtable<String,Serializable>();
      wallet.put("x",value);
      try
      {
        WalletFormat.write(wallet,types(Kind.class));
        Assert.fail("accepted " + value.getClass().getName());
      }
      catch (IOException e)
      {
        // erwartet
      }
    }
  }

  /**
   * Auch das neue Format instanziiert nur erlaubte Klassen.
   * @throws Exception
   */
  @Test
  public void rejectClassInNewFormat() throws Exception
  {
    for (String value:Arrays.asList(
        "<value type=\"bean\" class=\"java.io.PrintWriter\"/>",
        "<value type=\"collection\" class=\"java.util.Properties\"/>",
        "<value type=\"map\" class=\"java.util.ArrayList\"/>",
        "<value type=\"enum\" class=\"java.util.concurrent.TimeUnit\"><text>DAYS</text></value>",
        "<value type=\"array\" class=\"java.io.File\"/>",
        "<value type=\"ref\" ref=\"0\"/>"))
    {
      String xml = "<wallet version=\"1\"><entry><key type=\"string\"><text>x</text></key>" + value + "</entry></wallet>";
      assertRejected(xml.getBytes(StandardCharsets.UTF_8),types(PluginBean.class),null);
    }
  }

  /**
   * Zu tief verschachtelte Wallets werden vor dem Aufbau der Objekte abgelehnt.
   * @throws Exception
   */
  @Test
  public void rejectDeepNesting() throws Exception
  {
    StringBuilder sb = new StringBuilder("<wallet version=\"1\"><entry><key type=\"string\"><text>x</text></key><value type=\"collection\" class=\"java.util.ArrayList\">");
    for (int i=0;i<300;++i)
      sb.append("<item type=\"collection\" class=\"java.util.ArrayList\">");
    for (int i=0;i<300;++i)
      sb.append("</item>");
    sb.append("</value></entry></wallet>");
    assertRejected(sb.toString().getBytes(StandardCharsets.UTF_8),types(),"nested too deeply");
  }

  /**
   * Als Plugin-Code gelten nur das Plugin-Verzeichnis, dessen Compile-Ordner und
   * Jars direkt im Plugin-Verzeichnis, nicht aber mitgelieferte Bibliotheken.
   * @throws Exception
   */
  @Test
  public void detectPluginClasses() throws Exception
  {
    File classes = new File(WalletFormatTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    Assert.assertTrue(WalletTypes.isPluginClass(WalletFormatTest.class,List.of(classes)));
    Assert.assertFalse(WalletTypes.isPluginClass(WalletFormatTest.class,List.of(classes.getParentFile())));

    File jar = new File(Test.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    Assert.assertTrue(jar.getName().endsWith(".jar"));
    Assert.assertTrue(WalletTypes.isPluginClass(Test.class,List.of(jar.getParentFile())));
    Assert.assertFalse(WalletTypes.isPluginClass(Test.class,List.of(jar.getParentFile().getParentFile())));

    Assert.assertFalse(WalletTypes.isPluginClass(String.class,List.of(classes,jar.getParentFile())));
  }

  private static WalletTypes types(Class<?>... pluginClasses)
  {
    Set<Class<?>> allowed = Set.of(pluginClasses);
    return new WalletTypes(Arrays.asList(WalletFormatTest.class.getClassLoader()),allowed::contains);
  }

  private static void assertRejected(byte[] xml, WalletTypes types, String message) throws Exception
  {
    try
    {
      WalletFormat.read(xml,types);
      Assert.fail("accepted wallet");
    }
    catch (Exception e)
    {
      if (message != null)
        Assert.assertTrue(e.getMessage(),String.valueOf(e.getMessage()).contains(message));
    }
  }

  private static byte[] legacy(Hashtable<String,Serializable> wallet) throws Exception
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    List<Exception> errors = new ArrayList<Exception>();
    XMLEncoder encoder = new XMLEncoder(bos);
    encoder.setExceptionListener(errors::add);
    encoder.writeObject(wallet);
    encoder.close();
    Assert.assertEquals(List.of(),errors);
    return bos.toByteArray();
  }

  private static Hashtable<String,Serializable> coreWallet()
  {
    Date date = new Date(123456789L);
    Reminder reminder = new Reminder();
    reminder.setQueue("test.queue");
    reminder.setDate(date);
    reminder.setEnd(date);
    reminder.setReminderInterval(new ReminderInterval(TimeUnit.WEEKS,2));
    reminder.setData("text","hello");
    reminder.setData("date",date);
    reminder.setData("long",Long.valueOf(4));

    Hashtable<String,Serializable> wallet = new Hashtable<String,Serializable>();
    wallet.put("reminder",reminder);
    wallet.put("login",new Login("user","secret".toCharArray()));
    wallet.put("string","value");
    wallet.put("bytes",new byte[]{1,-2,3});
    wallet.put("int",Integer.valueOf(42));
    wallet.put("boolean",Boolean.TRUE);
    wallet.put("date",date);
    return wallet;
  }

  private static void assertCoreWallet(Hashtable<String,Serializable> wallet)
  {
    Reminder reminder = (Reminder) wallet.get("reminder");
    Assert.assertEquals("test.queue",reminder.getQueue());
    Assert.assertEquals(new Date(123456789L),reminder.getDate());
    Assert.assertEquals(new Date(123456789L),reminder.getEnd());
    Assert.assertEquals(new ReminderInterval(TimeUnit.WEEKS,2),reminder.getReminderInterval());
    Assert.assertEquals(Map.of("text","hello","date",new Date(123456789L),"long",Long.valueOf(4)),new HashMap<String,Serializable>(reminder.getData()));

    Login login = (Login) wallet.get("login");
    Assert.assertEquals("user",login.getUsername());
    Assert.assertArrayEquals("secret".toCharArray(),login.getPassword());

    Assert.assertEquals("value",wallet.get("string"));
    Assert.assertArrayEquals(new byte[]{1,-2,3},(byte[]) wallet.get("bytes"));
    Assert.assertEquals(Integer.valueOf(42),wallet.get("int"));
    Assert.assertEquals(Boolean.TRUE,wallet.get("boolean"));
    Assert.assertEquals(new Date(123456789L),wallet.get("date"));
  }

  private static PluginBean pluginBean()
  {
    PluginBean bean = new PluginBean();
    bean.setName("x\u0000y<&");
    bean.setCount(3);
    bean.setSecret("pw".toCharArray());
    bean.setWhen(new Date(1000));
    bean.setKind(Kind.B);
    bean.getTags().add("t1");
    bean.getTags().add(Kind.A);
    bean.setMap(new HashMap<String,Integer>(Map.of("k",1)));
    bean.setNumbers(new int[]{1,2});
    bean.setNames(new String[]{"a",null});

    PluginBean child = new PluginBean();
    child.setParent(bean);
    bean.setChild(child);
    return bean;
  }

  private static void assertPluginBean(Object o)
  {
    PluginBean bean = (PluginBean) o;
    Assert.assertEquals("x\u0000y<&",bean.getName());
    Assert.assertEquals(3,bean.getCount());
    Assert.assertArrayEquals("pw".toCharArray(),bean.getSecret());
    Assert.assertEquals(new Date(1000),bean.getWhen());
    Assert.assertEquals(Kind.B,bean.getKind());
    Assert.assertEquals(Arrays.asList("t1",Kind.A),bean.getTags());
    Assert.assertEquals(Map.of("k",1),bean.getMap());
    Assert.assertArrayEquals(new int[]{1,2},bean.getNumbers());
    Assert.assertArrayEquals(new String[]{"a",null},bean.getNames());
    Assert.assertSame(bean,bean.getChild().getParent());
  }
}
