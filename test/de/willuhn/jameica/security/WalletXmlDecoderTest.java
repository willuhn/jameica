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
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.reminder.ReminderInterval;
import de.willuhn.jameica.reminder.ReminderInterval.TimeUnit;

/** Tests the data-only wallet XML decoder. */
public class WalletXmlDecoderTest
{
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  /** Core wallet values written by XMLEncoder remain readable. */
  @Test
  public void decodeCoreWallet() throws Exception
  {
    Date shared = new Date(123456789L);
    Reminder reminder = new Reminder();
    reminder.setQueue("test.queue");
    reminder.setDate(shared);
    reminder.setEnd(shared);
    reminder.setReminderInterval(new ReminderInterval(TimeUnit.WEEKS,2));
    reminder.setData("text","hello");
    reminder.setData("date",shared);
    reminder.setData("boolean",Boolean.TRUE);
    reminder.setData("byte",Byte.valueOf((byte)1));
    reminder.setData("short",Short.valueOf((short)2));
    reminder.setData("int",Integer.valueOf(3));
    reminder.setData("long",Long.valueOf(4));
    reminder.setData("float",Float.valueOf(1.5f));
    reminder.setData("double",Double.valueOf(2.5d));
    reminder.setData("character",Character.valueOf('Z'));
    reminder.setData("null",null);

    Hashtable<String,Serializable> expected = new Hashtable<String,Serializable>();
    expected.put("string","value");
    expected.put("encoded-string","a" + Character.toString((char)0) + "b");
    expected.put("boolean",Boolean.TRUE);
    expected.put("byte",Byte.valueOf((byte)-7));
    expected.put("short",Short.valueOf((short)8));
    expected.put("int",Integer.valueOf(9));
    expected.put("long",Long.valueOf(10));
    expected.put("float",Float.valueOf(3.5f));
    expected.put("double",Double.valueOf(4.5d));
    expected.put("character",Character.valueOf('Q'));
    expected.put("bytes",new byte[]{1,-2,3});
    expected.put("characters",new char[]{'s','e','c'});
    expected.put("encoded-characters",new char[]{(char)0,(char)0xd83d,(char)0xde00});
    expected.put("login",new Login("user",new char[]{'p','w'}));
    expected.put("reminder",reminder);

    Hashtable<String,Serializable> actual = WalletXmlDecoder.decode(encode(expected));
    Assert.assertEquals(expected.get("string"),actual.get("string"));
    Assert.assertEquals(expected.get("encoded-string"),actual.get("encoded-string"));
    Assert.assertEquals(expected.get("boolean"),actual.get("boolean"));
    Assert.assertEquals(expected.get("byte"),actual.get("byte"));
    Assert.assertEquals(expected.get("short"),actual.get("short"));
    Assert.assertEquals(expected.get("int"),actual.get("int"));
    Assert.assertEquals(expected.get("long"),actual.get("long"));
    Assert.assertEquals(expected.get("float"),actual.get("float"));
    Assert.assertEquals(expected.get("double"),actual.get("double"));
    Assert.assertEquals(expected.get("character"),actual.get("character"));
    Assert.assertArrayEquals((byte[])expected.get("bytes"),(byte[])actual.get("bytes"));
    Assert.assertArrayEquals((char[])expected.get("characters"),(char[])actual.get("characters"));
    Assert.assertArrayEquals((char[])expected.get("encoded-characters"),(char[])actual.get("encoded-characters"));

    Login login = (Login)actual.get("login");
    Assert.assertEquals("user",login.getUsername());
    Assert.assertArrayEquals(new char[]{'p','w'},login.getPassword());

    Reminder decoded = (Reminder)actual.get("reminder");
    Assert.assertEquals(reminder.getQueue(),decoded.getQueue());
    Assert.assertSame(decoded.getDate(),decoded.getEnd());
    Assert.assertSame(decoded.getDate(),decoded.getData("date"));
    Assert.assertEquals(reminder.getReminderInterval(),decoded.getReminderInterval());
    Assert.assertEquals(reminder.getData(),decoded.getData());
  }

  /** Executable JavaBeans instructions are rejected without side effects. */
  @Test
  public void rejectExecutableWallet() throws Exception
  {
    Path proof = folder.getRoot().toPath().resolve("proof.txt");
    String path = proof.toString().replace("&","&amp;").replace("<","&lt;");
    String xml = "<java class=\"java.beans.XMLDecoder\">"
        + "<object class=\"java.util.Hashtable\">"
        + "<void method=\"put\"><string>key</string>"
        + "<object class=\"java.io.PrintWriter\"><string>" + path + "</string>"
        + "<void method=\"println\"><string>executed</string></void>"
        + "<void method=\"close\"/></object></void></object></java>";

    assertRejected(xml);
    Assert.assertFalse(Files.exists(proof));
  }

  /** External resources and document type declarations are rejected. */
  @Test
  public void rejectDoctype() throws Exception
  {
    Path secret = folder.newFile("secret.txt").toPath();
    Files.writeString(secret,"secret");
    String xml = "<!DOCTYPE java [<!ENTITY xxe SYSTEM \"" + secret.toUri() + "\">]>"
        + "<java class=\"java.beans.XMLDecoder\"><object class=\"java.util.Hashtable\">"
        + "<void method=\"put\"><string>key</string><string>&xxe;</string></void>"
        + "</object></java>";
    assertRejected(xml);
  }

  /** Malformed documents never reach the compatibility reader. */
  @Test
  public void rejectMalformedXml() throws Exception
  {
    assertRejected("<java class=\"java.beans.XMLDecoder\"><object class=\"java.util.Hashtable\"></java>");
  }

  /** Duplicate keys, unknown methods, and custom plugin objects fail closed. */
  @Test
  public void rejectUnsupportedData() throws Exception
  {
    String duplicate = "<java class=\"java.beans.XMLDecoder\">"
        + "<object class=\"java.util.Hashtable\">"
        + "<void method=\"put\"><string>key</string><string>one</string></void>"
        + "<void method=\"put\"><string>key</string><string>two</string></void>"
        + "</object></java>";
    String method = "<java class=\"java.beans.XMLDecoder\">"
        + "<object class=\"java.util.Hashtable\"><void method=\"clear\"/></object></java>";
    String cycle = "<java class=\"java.beans.XMLDecoder\">"
        + "<object class=\"java.util.Hashtable\"><void method=\"put\"><string>key</string>"
        + "<object class=\"de.willuhn.jameica.reminder.Reminder\" id=\"Reminder0\">"
        + "<void property=\"data\" id=\"Map0\"><void method=\"put\"><string>self</string>"
        + "<object idref=\"Reminder0\"/></void></void>"
        + "<void property=\"data\"><object idref=\"Map0\"/></void>"
        + "</object></void></object></java>";
    assertRejected(duplicate);
    assertRejected(method);
    assertRejected(cycle);
    try
    {
      WalletXmlDecoder.validate(new ArrayList<String>());
      Assert.fail("custom collection accepted");
    }
    catch (IOException expected)
    {
      // expected
    }
  }

  /** Decrypted wallet content is bounded before DOM parsing. */
  @Test
  public void rejectOversizedWallet() throws Exception
  {
    WalletXmlDecoder.Buffer buffer = new WalletXmlDecoder.Buffer();
    byte[] chunk = new byte[1024 * 1024];
    for (int i=0;i<16;++i)
      buffer.write(chunk);
    try
    {
      buffer.write(0);
      Assert.fail("oversized wallet accepted");
    }
    catch (IOException expected)
    {
      // expected
    }
  }

  /** Valid XMLEncoder forms for null map keys and shared arrays remain readable. */
  @Test
  public void decodeCompatibilityReferences() throws Exception
  {
    String xml = "<java class=\"java.beans.XMLDecoder\"><object class=\"java.util.Hashtable\">"
        + "<void method=\"put\"><string>a</string><array class=\"char\" length=\"1\" id=\"chars\">"
        + "<void index=\"0\"><char>p</char></void></array></void>"
        + "<void method=\"put\"><string>login</string><object class=\"de.willuhn.jameica.security.Login\">"
        + "<void property=\"password\"><object idref=\"chars\"/></void></object></void>"
        + "<void method=\"put\"><string>reminder</string><object class=\"de.willuhn.jameica.reminder.Reminder\">"
        + "<void property=\"data\" id=\"data\"><void method=\"put\"><null/><string>x</string></void></void>"
        + "<void property=\"data\"><object idref=\"data\"/></void></object></void>"
        + "</object></java>";

    Hashtable<String,Serializable> decoded = WalletXmlDecoder.decode(xml.getBytes(StandardCharsets.UTF_8));
    Assert.assertArrayEquals((char[])decoded.get("a"),((Login)decoded.get("login")).getPassword());
    Assert.assertEquals("x",((Reminder)decoded.get("reminder")).getData(null));
  }

  /** Array bounds are compatible with the element budget used by XMLEncoder. */
  @Test
  public void decodeDenseByteArray() throws Exception
  {
    byte[] expected = new byte[30000];
    Arrays.fill(expected,(byte)1);
    Hashtable<String,Serializable> wallet = new Hashtable<String,Serializable>();
    wallet.put("bytes",expected);
    Hashtable<String,Serializable> decoded = WalletXmlDecoder.decode(encode(wallet));
    Assert.assertArrayEquals(expected,(byte[])decoded.get("bytes"));
  }

  /** Deep documents fail during streaming preflight instead of overflowing. */
  @Test
  public void rejectDeepReminderGraph() throws Exception
  {
    StringBuilder xml = new StringBuilder("<java class=\"java.beans.XMLDecoder\"><object class=\"java.util.Hashtable\"><void method=\"put\"><string>k</string>");
    for (int i=0;i<200;++i)
      xml.append("<object class=\"de.willuhn.jameica.reminder.Reminder\"><void property=\"data\" id=\"m").append(i).append("\"><void method=\"put\"><string>x</string>");
    xml.append("<string>end</string>");
    for (int i=199;i>=0;--i)
      xml.append("</void></void><void property=\"data\"><object idref=\"m").append(i).append("\"/></void></object>");
    xml.append("</void></object></java>");
    assertRejected(xml.toString());
  }

  /** Excessively wide documents are rejected before DOM construction. */
  @Test
  public void rejectTooManyElements() throws Exception
  {
    StringBuilder xml = new StringBuilder("<java class=\"java.beans.XMLDecoder\">");
    for (int i=0;i<300001;++i)
      xml.append("<null/>");
    xml.append("</java>");
    assertRejected(xml.toString());
  }

  /** Non-element DOM nodes cannot bypass the streaming structure budget. */
  @Test
  public void rejectIgnoredDomNodesBeforeParsing() throws Exception
  {
    String prefix = "<java class=\"java.beans.XMLDecoder\"><object class=\"java.util.Hashtable\">";
    String suffix = "</object></java>";
    assertRejected(prefix + "<!---->" + suffix);
    assertRejected(prefix + "<![CDATA[]]>" + suffix);
    assertRejected(prefix + "<?wallet ignored?>" + suffix);
  }

  /** One start tag cannot allocate an unbounded attribute collection. */
  @Test
  public void rejectTooManyAttributes() throws Exception
  {
    StringBuilder xml = new StringBuilder("<java class=\"java.beans.XMLDecoder\"");
    for (int i=0;i<17;++i)
      xml.append(" a").append(i).append("=\"x\"");
    xml.append("><object class=\"java.util.Hashtable\"/></java>");
    assertRejected(xml.toString());
  }

  /** Values that cannot fit in the bounded encoded form are rejected early. */
  @Test
  public void rejectUnencodableTextBound() throws Exception
  {
    char[] characters = new char[128 * 1024 + 1];
    try
    {
      WalletXmlDecoder.validate(new String(characters));
      Assert.fail("oversized text accepted");
    }
    catch (IOException expected)
    {
      // expected
    }
  }

  /** Aggregate encoder output is bounded even when each value is valid. */
  @Test
  public void rejectOversizedEncodedWallet() throws Exception
  {
    char[] characters = new char[128 * 1024];
    Hashtable<String,Serializable> wallet = new Hashtable<String,Serializable>();
    Arrays.fill(characters,'x');
    for (int i=0;i<130;++i)
      wallet.put("value-" + i,new String(characters));
    try
    {
      WalletXmlDecoder.encode(wallet);
      Assert.fail("oversized encoded wallet accepted");
    }
    catch (IOException expected)
    {
      // expected
    }
  }

  /** A worst-case escaped string at the advertised limit round-trips. */
  @Test
  public void decodeEscapedStringAtLimit() throws Exception
  {
    String expected = new String(new char[128 * 1024]);
    Hashtable<String,Serializable> wallet = new Hashtable<String,Serializable>();
    wallet.put("text",expected);
    Hashtable<String,Serializable> decoded = WalletXmlDecoder.decode(encode(wallet));
    Assert.assertEquals(expected,decoded.get("text"));
  }

  private static byte[] encode(Hashtable<String,Serializable> wallet) throws Exception
  {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (XMLEncoder encoder = new XMLEncoder(bytes))
    {
      encoder.writeObject(wallet);
    }
    return bytes.toByteArray();
  }

  private static void assertRejected(String xml) throws Exception
  {
    try
    {
      WalletXmlDecoder.decode(xml.getBytes(StandardCharsets.UTF_8));
      Assert.fail("unsafe wallet XML accepted");
    }
    catch (Exception expected)
    {
      // expected
    }
  }
}
