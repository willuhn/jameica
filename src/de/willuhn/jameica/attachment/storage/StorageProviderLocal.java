/**********************************************************************
 *
 * Copyright (c) 2022 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.attachment.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.io.IOUtil;
import de.willuhn.jameica.attachment.Attachment;
import de.willuhn.jameica.attachment.Context;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Storage-Provider, der die Speicherung in lokalen Dateien �bernimmt.
 */
@Lifecycle(Type.CONTEXT)
public class StorageProviderLocal implements StorageProvider
{
  private final static I18N i18n = Application.getI18n();
  private final static Settings settings = new Settings(StorageProviderLocal.class);
  
  /**
   * @see de.willuhn.jameica.attachment.storage.StorageProvider#getId()
   */
  @Override
  public String getId()
  {
    return "jameica.local";
  }
  
  /**
   * @see de.willuhn.jameica.attachment.storage.StorageProvider#isEnabled()
   */
  @Override
  public boolean isEnabled()
  {
    return settings.getBoolean("enabled",true);
  }
  
  /**
   * @see de.willuhn.jameica.attachment.storage.StorageProvider#getName()
   */
  @Override
  public String getName()
  {
    return i18n.tr("Speicherung in lokalen Dateien");
  }
  
  /**
   * @see de.willuhn.jameica.attachment.storage.StorageProvider#getAttachments(de.willuhn.jameica.attachment.Context)
   */
  @Override
  public List<Attachment> getAttachments(Context ctx) throws IOException
  {
    final List<Attachment> result = new LinkedList<>();
    
    if (ctx == null)
      return result;

    final File dir = this.getDir(ctx);
    
    // OK; wir haben Dateien. Dann die Meta-Daten abrufen, um die Attachments zu erzeugen
    for (File f:dir.listFiles())
    {
      if (!f.isFile() || !f.canRead())
        continue;
      
      final Attachment a = new Attachment();
      a.setStorageId(this.getId());
      a.setDate(f.lastModified());
      a.setContext(ctx);
      a.setFilename(f.getName());
      result.add(a);
    }

    return result;
  }

  /**
   * @see de.willuhn.jameica.attachment.storage.StorageProvider#create(de.willuhn.jameica.attachment.Attachment, java.io.InputStream)
   */
  @Override
  public void create(Attachment a, InputStream is) throws IOException
  {
    final File dir = this.getDir(a.getContext());
    final File target = new File(dir,a.getFilename());
    Logger.info("creating new attachment file " + target);

    OutputStream os = null;
    try
    {
      os = new BufferedOutputStream(new FileOutputStream(target));
      final long bytes = IOUtil.copy(is,os);
      Logger.info("wrote " + bytes + " bytes");
    }
    finally
    {
      IOUtil.close(is,os);
    }
  }
  
  /**
   * @see de.willuhn.jameica.attachment.storage.StorageProvider#update(de.willuhn.jameica.attachment.Attachment, java.io.InputStream)
   */
  @Override
  public void update(Attachment a, InputStream is) throws IOException
  {
    // Wir k�nnen einfach erstellen. Dabei wird die Datei �berschrieben
    this.create(a,is);
  }
  
  /**
   * @see de.willuhn.jameica.attachment.storage.StorageProvider#copy(de.willuhn.jameica.attachment.Attachment, java.io.OutputStream)
   */
  @Override
  public void copy(Attachment a, OutputStream os) throws IOException
  {
    final File dir = this.getDir(a.getContext());
    final File src = new File(dir,a.getFilename());
    Logger.info("read attachment file " + src);

    InputStream is = null;
    try
    {
      is = new BufferedInputStream(new FileInputStream(src));
      final long bytes = IOUtil.copy(is,os);
      Logger.info("read " + bytes + " bytes");
    }
    finally
    {
      IOUtil.close(is,os);
    }
  }
  
  /**
   * @see de.willuhn.jameica.attachment.storage.StorageProvider#delete(de.willuhn.jameica.attachment.Attachment)
   */
  @Override
  public void delete(Attachment a) throws IOException
  {
    final File dir = this.getDir(a.getContext());
    final File target = new File(dir,a.getFilename());
    Logger.info("delete attachment file " + target);
    if (!target.delete())
      throw new IOException(i18n.tr("Datei {0} kann nicht gel�scht werden",target.getAbsolutePath()));
  }
  
  /**
   * Liefert das konfigurierte Arbeitsverzeichnis.
   * @param ctx der Kontext.
   * @return das Arbeitsverzeichnis.
   * @throw IOException
   */
  private File getDir(Context ctx) throws IOException
  {
    final File basedir = this.getBaseDir();
    final File dir = new File(basedir.getAbsolutePath() + File.separator +
                              StringUtils.defaultIfBlank(ctx.getPlugin(),"default") + File.separator +
                              StringUtils.defaultIfBlank(ctx.getClassName(),"default"),StringUtils.defaultIfBlank(ctx.getId(),"default"));
    if (!dir.exists() && !dir.mkdirs())
      throw new IOException(i18n.tr("Ordner {0} kann nicht erstellt werden",dir.getAbsolutePath()));
    
    return dir;
  }
  
  /**
   * Liefert das Basis-Verzeichnis f�r die Speicherung.
   * @return das Basis-Verzeichnis f�r die Speicherung.
   */
  public File getBaseDir()
  {
    final File def = this.getDefaultBaseDir();
    if (!this.useCustomBaseDir())
      return def;
    
    // Wenn ein abweichendes Verzeichnis existiert, muss eines angegeben sein. Ansonsten verwenden wir dann doch wieder das Default-Dir.
    final String basedir = settings.getString("basedir",null);
    return basedir != null ? new File(basedir) : def;
  }
  
  /**
   * Liefert das Default-Basis-Verzeichnis.
   * @return das Default-Basis-Verzeichnis.
   */
  public File getDefaultBaseDir()
  {
    return new File(Application.getConfig().getWorkDir() + File.separator + "attachments");
  }
  
  /**
   * Liefert das abweichende Basis-Verzeichnis f�r die Speicherung.
   * @return das abweichende Basis-Verzeichnis f�r die Speicherung.
   */
  public String getCustomBaseDir()
  {
    return settings.getString("basedir",null);
  }

  /**
   * Speichert das Basis-Verzeichnis f�r die Speicherung.
   * @param dir das Basis-Verzeichnis f�r die Speicherung.
   */
  public void setCustomBaseDir(String dir)
  {
    settings.setAttribute("basedir",dir);
  }
  
  /**
   * Liefert true, wenn ein abweichendes Basis-Verzeichnis f�r die Sicherung verwendet werden soll.
   * @return true, wenn ein abweichendes Basis-Verzeichnis f�r die Sicherung verwendet werden soll.
   */
  public boolean useCustomBaseDir()
  {
    return settings.getBoolean("basedir.custom",false);
  }
  
  /**
   * Legt fest, ob ein abweichendes Basis-Verzeichnis f�r die Sicherung verwendet werden soll.
   * @param b true, wenn ein abweichendes Basis-Verzeichnis f�r die Sicherung verwendet werden soll.
   */
  public void useCustomBaseDir(boolean b)
  {
    settings.setAttribute("basedir.custom",b);
  }
}
