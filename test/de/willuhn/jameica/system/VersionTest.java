/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
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


