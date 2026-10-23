/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details.
 *
 **********************************************************************/

package de.willuhn.jameica.util;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.sun.net.httpserver.HttpServer;

import de.willuhn.jameica.plugin.Manifest;
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.StdXMLReader;

/**
 * Tests that NanoXML cannot resolve document type resources.
 */
public class SafeXMLParserTest
{
  /** Test directory. */
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  /** Ordinary documents and predefined entities remain supported. */
  @Test
  public void parseOrdinaryDocument() throws Exception
  {
    IXMLElement root = parse("<plugin name=\"demo\"><description>A &amp; B</description></plugin>");
    Assert.assertEquals("plugin",root.getName());
    Assert.assertEquals("demo",root.getAttribute("name",null));
    Assert.assertEquals("A & B",root.getFirstChildNamed("description").getContent());
  }

  /** Production manifest and component-info readers retain normal XML support. */
  @Test
  public void parseProductionDocuments() throws Exception
  {
    byte[] manifest = "<plugin name=\"demo\" version=\"1.0\"/>".getBytes(StandardCharsets.UTF_8);
    Assert.assertEquals("demo",new Manifest(new ByteArrayInputStream(manifest)).getName());

    String infoXml = "<info><name>demo</name><description>Demo</description>"
        + "<url>https://example.invalid</url><license>test</license></info>";
    InfoReader info = new InfoReader(new ByteArrayInputStream(infoXml.getBytes(StandardCharsets.UTF_8)));
    Assert.assertEquals("demo",info.getName());
  }

  /** The production manifest boundary rejects document types as well. */
  @Test
  public void rejectDocumentTypeInManifest() throws Exception
  {
    String xml = "<!DOCTYPE plugin [<!ENTITY xxe 'expanded'>]><plugin name=\"&xxe;\"/>";
    try
    {
      new Manifest(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
      Assert.fail("unsafe manifest accepted");
    }
    catch (Exception expected)
    {
      Assert.assertTrue(expected.getMessage().contains("DOCTYPE"));
    }
  }

  /** Local external entities are rejected without exposing their contents. */
  @Test
  public void rejectExternalFileEntity() throws Exception
  {
    Path secret = folder.newFile("secret.txt").toPath();
    Files.writeString(secret,"NANOXML_EXTERNAL_ENTITY_EXPANDED");
    assertRejected("<!DOCTYPE plugin [<!ENTITY xxe SYSTEM \"" + secret.toUri() + "\">]>"
        + "<plugin>&xxe;</plugin>");
  }

  /** External DTDs and parameter entities cause no HTTP requests. */
  @Test
  public void rejectExternalHttpResources() throws Exception
  {
    AtomicInteger requests = new AtomicInteger();
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
    server.createContext("/external.dtd",exchange -> {
      requests.incrementAndGet();
      byte[] response = "<!ENTITY xxe 'loaded'>".getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200,response.length);
      exchange.getResponseBody().write(response);
      exchange.close();
    });
    server.start();
    try
    {
      String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/external.dtd";
      assertRejected("<!DOCTYPE plugin SYSTEM \"" + url + "\"><plugin/>");
      assertRejected("<!DOCTYPE plugin [<!ENTITY % remote SYSTEM \"" + url + "\">%remote;]><plugin/>");
      Assert.assertEquals(0,requests.get());
    }
    finally
    {
      server.stop(0);
    }
  }

  /** Entity expansion is rejected at the document type declaration. */
  @Test
  public void rejectEntityExpansionWithinBoundedTime() throws Exception
  {
    long start = System.nanoTime();
    assertRejected("<!DOCTYPE plugin ["
        + "<!ENTITY a '1234567890'>"
        + "<!ENTITY b '&a;&a;&a;&a;&a;&a;&a;&a;&a;&a;'>"
        + "<!ENTITY c '&b;&b;&b;&b;&b;&b;&b;&b;&b;&b;'>"
        + "]><plugin>&c;</plugin>");
    Assert.assertTrue("entity payload took too long",System.nanoTime() - start < 2_000_000_000L);
  }

  private static IXMLElement parse(String xml) throws Exception
  {
    SafeXMLParser parser = new SafeXMLParser();
    parser.setReader(new StdXMLReader(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
    return (IXMLElement) parser.parse();
  }

  private static void assertRejected(String xml) throws Exception
  {
    try
    {
      parse(xml);
      Assert.fail("unsafe XML accepted");
    }
    catch (Exception expected)
    {
      Assert.assertTrue(expected.getMessage().contains("DOCTYPE"));
    }
  }
}
