/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import de.willuhn.io.IOUtil;
import de.willuhn.jameica.plugin.Plugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Wird beim Herunterfahren von Jameica benachrichtigt und speichert Strings ab, zu denen
 * waehrend der Sitzung keine Uebersetzungen gefunden wurden.
 */
public class I18nMessageConsumer implements MessageConsumer
{
  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{SystemMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    SystemMessage msg = (SystemMessage) message;
    if (msg.getStatusCode() != SystemMessage.SYSTEM_SHUTDOWN)
      return;
    
    // Checken, ob wir nicht uebersetzte Strings wegspeichern sollen
    String dir = Application.getConfig().getStoreUntranslatedDir();
    if (dir == null)
      return;
    
    // 1. Ordner anlegen
    File parent = new File(dir);
    if (!parent.exists())
      parent.mkdirs();

    // 2. Jameica selbst
    this.store(Application.getI18n(),new File(parent,"system_messages.properties"));
    
    // 3. Und jetzt die Plugins
    List<Plugin> plugins = Application.getPluginLoader().getInstalledPlugins();
    for (Plugin p:plugins)
    {
      String name = p.getManifest().getName();
      this.store(p.getResources().getI18N(),new File(parent,name + "_messages.properties"));
    }
    
  }
  
  /**
   * Speichert die nicht uebersetzten Strings der I18N-Instanz in der angegebenen Datei.
   * @param i18n die i18n Instanz.
   * @param file die Datei.
   */
  private void store(I18N i18n, File file)
  {
    OutputStream os = null;
    
    try
    {
      Logger.info("storing untranslated strings in " + file);
      os = new BufferedOutputStream(new FileOutputStream(file));
      i18n.storeUntranslated(os);
    }
    catch (Exception e)
    {
      Logger.error("unable to store untranslated strings",e);
    }
    finally
    {
       IOUtil.close(os);
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return true;
  }
}

