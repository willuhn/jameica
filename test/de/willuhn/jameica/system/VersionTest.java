/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.system;

import org.junit.Assert;
import org.junit.Test;

import de.willuhn.jameica.plugin.Version;

/**
 * Testet das Parsen von Versionsnummern.
 */
public class VersionTest
{
  /**
   * 
   */
  @Test
  public void test001()
  {
    test("1.0.0",1,0,0,"");
  }
  
  /**
   * 
   */
  @Test
  public void test002()
  {
    test("1.2.3",1,2,3,"");
  }

  /**
   * 
   */
  @Test
  public void test003()
  {
    test("1.2",1,2,0,null);
  }

  /**
   * 
   */
  @Test
  public void test004()
  {
    test("1.2.3-test",1,2,3,"test");
  }
  
  /**
   * 
   */
  @Test
  public void test005()
  {
    Version v = new Version("1.0.0-foobar");
    Assert.assertTrue(v.compliesTo("1+"));
    Assert.assertTrue(v.compliesTo("1.0+"));
    Assert.assertTrue(v.compliesTo("1.0.0+"));
    Assert.assertTrue(v.compliesTo("1-"));
    Assert.assertTrue(v.compliesTo("1.0-"));
    Assert.assertTrue(v.compliesTo("1.0.0-"));

    Assert.assertTrue(v.compliesTo("1.0.0"));

    Assert.assertFalse(v.compliesTo("1.0.1+"));
    Assert.assertFalse(v.compliesTo("0.9-"));
  }
  
  /**
   * 
   */
  @Test
  public void test006()
  {
    Version v1 = new Version("1.0.1-foo");
    Version v2 = new Version("1.0.2-bar");
    Assert.assertEquals(-1,v1.compareTo(v2));
  }
  
  /**
   * 
   */
  @Test
  public void test007()
  {
    Version v1 = new Version("1.0.1-foo");
    Version v2 = new Version("1.0.1-bar");
    Assert.assertEquals(0,v1.compareTo(v2));
  }
  
  /**
   * 
   */
  @Test
  public void test008()
  {
    Version v1 = new Version("1.0.2-foo");
    Version v2 = new Version("1.0.1-bar");
    Assert.assertEquals(1,v1.compareTo(v2));
  }

  /**
   * Parst die Versionsnummer und vergleicht das Ergebnis mit der Erwartung.
   * @param text die zu parsende Versionsnummer.
   * @param major die erwartete Major-Version.
   * @param minor die erwartete Minor-Version.
   * @param patch das erwartete Patch-Level.
   * @param suffix der erwartete Suffix.
   */
  private void test(String text, int major, int minor, int patch, String suffix)
  {
    Version version = new Version(text);
    Assert.assertEquals("Major-Version falsch",major,version.getMajor());
    Assert.assertEquals("Minor-Version falsch",minor,version.getMinor());
    Assert.assertEquals("Patch-Level falsch",patch,version.getPatch());
    Assert.assertEquals("Suffix falsch",suffix,version.getSuffix());
  }

}


