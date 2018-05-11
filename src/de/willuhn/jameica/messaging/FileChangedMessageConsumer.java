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
import java.util.Observable;
import java.util.Observer;

import de.willuhn.io.FileWatch;
import de.willuhn.jameica.system.Application;

/**
 * Ueberwacht alle Programm-Dateien auf Aenderungen und informiert ggf das System.
 */
public class FileChangedMessageConsumer implements MessageConsumer, Observer
{

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return true;
  }

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
    // Wir initialisieren uns, wenn wir vom System die Meldung kriegen,
    // dass Jameica vollstaendig gestartet ist. Erst dann wissen wir
    // genau, dass alle Programmteile geladen wurden.
    if (message == null)
      return;
    
    SystemMessage m = (SystemMessage) message;
    if (m.getStatusCode() != SystemMessage.SYSTEM_STARTED)
      return;

    File[] files = Application.getClassLoader().getFiles();
    for (int i=0;i<files.length;++i)
    {
      if (!files[i].exists())
        continue;
      FileWatch.addFile(files[i],this);
    }

  }

  /**
   * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
   */
  public void update(Observable o, Object arg)
  {
    if (arg == null || !(arg instanceof File))
      return;
    Application.getMessagingFactory().sendMessage(new FileChangedMessage((File)arg));
  }

}


/*********************************************************************
 * $Log: FileChangedMessageConsumer.java,v $
 * Revision 1.3  2007/03/20 16:07:22  willuhn
 * @N ignore deleted files
 *
 * Revision 1.2  2007/03/13 11:15:57  willuhn
 * @C removed log message
 *
 * Revision 1.1  2007/03/09 18:03:08  willuhn
 * @N classloader updates
 * @N FileWatch in de.willuhn.util
 * @N Ueberwachung der Programm-Dateien auf Aenderungen
 *
 **********************************************************************/