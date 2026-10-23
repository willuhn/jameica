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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.willuhn.io.ZipExtractor;

/**
 * Tests shared archive path validation.
 */
public class ZipFileValidatorTest
{
  /** Test directory. */
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  /** A normal plugin remains extractable through the validated open archive. */
  @Test
  public void acceptAndExtractValidPlugin() throws Exception
  {
    File target = folder.newFolder("plugins");
    File archive = createZip("safe/",null,"safe/plugin.xml","<plugin/>","safe/lib/plugin.jar","data");
    try (ZipFile zip = new ZipFile(archive))
    {
      ZipFileValidator.validatePlugin(zip,target,"safe");
      new ZipExtractor(zip).extract(target);
    }
    Assert.assertEquals("<plugin/>",Files.readString(new File(target,"safe/plugin.xml").toPath()));
  }

  /** Traversal and platform-specific absolute names are rejected. */
  @Test
  public void rejectUnsafePluginEntries() throws Exception
  {
    assertInvalid("safe/../../outside.txt");
    assertInvalid("safe\\..\\..\\outside.txt");
    assertInvalid("/absolute.txt");
    assertInvalid("C:\\absolute.txt");
    assertInvalid("\\\\server\\share\\absolute.txt");
    assertInvalid("safe/sub/../file.txt");
  }

  /** Every member must belong to the exact declared plugin directory. */
  @Test
  public void rejectForeignTopLevelDirectory() throws Exception
  {
    assertInvalid("other/file.txt");
    assertInvalid("safe-other/file.txt");
    assertInvalid("top-level.txt");
  }

  /** Existing symbolic links may not redirect extraction outside the root. */
  @Test
  public void rejectSymlinkTraversal() throws Exception
  {
    File target = folder.newFolder("symlink-plugins");
    File outside = folder.newFolder("outside");
    try
    {
      Files.createSymbolicLink(new File(target,"safe").toPath(),outside.toPath());
    }
    catch (IOException | UnsupportedOperationException e)
    {
      Assume.assumeNoException(e);
    }
    File archive = createZip("safe/file.txt","data");
    try (ZipFile zip = new ZipFile(archive))
    {
      try
      {
        ZipFileValidator.validatePlugin(zip,target,"safe");
        Assert.fail("symlink traversal accepted");
      }
      catch (IOException expected)
      {
        Assert.assertFalse(Files.exists(new File(outside,"file.txt").toPath()));
      }
    }
  }

  private void assertInvalid(String name) throws Exception
  {
    File target = folder.newFolder("target-" + Math.abs(name.hashCode()));
    File archive = createZip("safe/",null,"safe/plugin.xml","<plugin/>",name,"data");
    try (ZipFile zip = new ZipFile(archive))
    {
      try
      {
        ZipFileValidator.validatePlugin(zip,target,"safe");
        Assert.fail("unsafe entry accepted: " + name);
      }
      catch (IOException expected)
      {
        // expected
      }
    }
  }

  private File createZip(String... values) throws Exception
  {
    File archive = folder.newFile("plugin-" + System.nanoTime() + ".zip");
    try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(archive)))
    {
      for (int i=0;i<values.length;i+=2)
      {
        ZipEntry entry = new ZipEntry(values[i]);
        out.putNextEntry(entry);
        if (values[i + 1] != null)
          out.write(values[i + 1].getBytes(StandardCharsets.UTF_8));
        out.closeEntry();
      }
    }
    return archive;
  }
}
