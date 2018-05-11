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

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.services.ScriptingService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Registriert ein Script, welches via Messaging uebergeben wurde.
 */
public class AddScriptMessageConsumer implements MessageConsumer
{

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{QueryMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    QueryMessage msg = (QueryMessage) message;
    Object data = msg.getData();
    if (data == null)
    {
      Logger.warn("no message data given, ignoring message");
      return;
    }
    
    String s = StringUtils.trimToNull(data.toString());
    if (s == null)
    {
      Logger.warn("no message data given, ignoring message");
      return;
    }
    
    File file = new File(s);
    if (!file.exists() || !file.canRead())
    {
      Logger.warn(s + " is no valid file or does not exist");
      return;
    }
    
    ScriptingService service = Application.getBootLoader().getBootable(ScriptingService.class);
    
    if (service.contains(file))
    {
      // loggen wir mit debug-Level, damit das auch problemlos mehrfach aufgerufen werden kann.
      Logger.debug(file + " allready registered");
      return;
    }
    service.addScript(file);
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    // geschieht uebers Manifest.
    return false;
  }

}
