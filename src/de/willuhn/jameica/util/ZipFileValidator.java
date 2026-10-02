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
import java.io.IOException;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Validates portable ZIP entry names against an extraction root.
 */
public final class ZipFileValidator
{
  private ZipFileValidator()
  {
  }

  /**
   * Validates every entry against the extraction root.
   * @param zip archive to validate.
   * @param targetDirectory extraction root.
   * @throws IOException if an entry is unsafe.
   */
  public static void validate(ZipFile zip, File targetDirectory) throws IOException
  {
    Enumeration<? extends ZipEntry> entries = zip.entries();
    while (entries.hasMoreElements())
      resolve(entries.nextElement(),targetDirectory);
  }

  /**
   * Validates a plugin archive and enforces its expected top-level directory.
   * @param zip archive to validate.
   * @param targetDirectory plugin extraction root.
   * @param pluginName expected top-level directory.
   * @throws IOException if an entry is unsafe or outside the plugin directory.
   */
  public static void validatePlugin(ZipFile zip, File targetDirectory, String pluginName) throws IOException
  {
    if (pluginName == null || pluginName.length() == 0 || pluginName.indexOf('/') >= 0
        || pluginName.indexOf('\\') >= 0 || pluginName.equals(".") || pluginName.equals(".."))
      throw new IOException("invalid plugin directory: " + pluginName);

    String prefix = pluginName + "/";
    Enumeration<? extends ZipEntry> entries = zip.entries();
    while (entries.hasMoreElements())
    {
      ZipEntry entry = entries.nextElement();
      String name = entry.getName().replace('\\','/');
      resolve(entry,targetDirectory);
      if (!name.startsWith(prefix))
        throw new IOException("invalid plugin ZIP entry: " + entry.getName());
    }
  }

  /**
   * Resolves an entry to a canonical destination below the extraction root.
   * @param entry ZIP entry to resolve.
   * @param targetDirectory extraction root.
   * @return canonical extraction destination.
   * @throws IOException if the name is unsafe or leaves the extraction root.
   */
  public static Path resolve(ZipEntry entry, File targetDirectory) throws IOException
  {
    if (entry == null || targetDirectory == null)
      throw new IOException("ZIP entry and extraction root are required");

    String name = entry.getName();
    String portableName = name.replace('\\','/');
    if (portableName.startsWith("/"))
      throw new IOException("invalid ZIP entry: " + name);

    String[] components = portableName.split("/",-1);
    for (String component:components)
    {
      if (component.equals(".") || component.equals("..") || component.indexOf(':') >= 0
          || component.endsWith(".") || component.endsWith(" "))
        throw new IOException("invalid ZIP entry: " + name);
    }

    Path target = targetDirectory.getCanonicalFile().toPath();
    Path file = new File(targetDirectory,portableName).getCanonicalFile().toPath();
    if (file.equals(target) || !file.startsWith(target))
      throw new IOException("invalid ZIP entry: " + name);
    return file;
  }
}
