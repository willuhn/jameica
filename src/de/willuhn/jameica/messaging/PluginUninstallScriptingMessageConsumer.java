/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import java.io.File;
import java.util.List;

import de.willuhn.io.FileFinder;
import de.willuhn.jameica.messaging.PluginMessage.Event;
import de.willuhn.jameica.services.ScriptingService;
import de.willuhn.jameica.system.Application;

/**
 * Wird benachrichtigt, wenn ein Plugin deinstalliert wird.
 * Wenn das Plugin ein Script registriert hatte, entfernen wir das hierbei.
 */
public class PluginUninstallScriptingMessageConsumer implements MessageConsumer
{

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{PluginMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    PluginMessage msg = (PluginMessage) message;
    Event e = msg.getEvent();
    if (e == null || e != Event.UNINSTALLED)
      return;
    
    // Basis-Verzeichnis des Plugins ermitteln und nachschauen, ob in dem
    // Ordner eine Script-Datei liegt, die registriert ist.
    File dir = new File(msg.getManifest().getPluginDir());
    if (!dir.exists() || !dir.isDirectory())
      return;
    
    FileFinder finder = new FileFinder(dir);
    finder.extension(".js");
    File[] matches     = finder.findRecursive();
    
    ScriptingService service = Application.getBootLoader().getBootable(ScriptingService.class);
    List<File> scripts = service.getScripts();
    
    for (File f:matches)
    {
      if (scripts.contains(f))
      {
        Application.getMessagingFactory().getMessagingQueue("jameica.scripting.remove").sendMessage(new QueryMessage(f));
      }
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
