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

import java.util.LinkedList;
import java.util.List;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;

/**
 * Empfaengt Boot-Messages und haelt sie fuer den spaeteren Abruf bereit.
 */
@Lifecycle(Type.CONTEXT)
public class BootMessageConsumer implements MessageConsumer
{
  private List<BootMessage> messages = new LinkedList<>();

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{BootMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    this.messages.add((BootMessage)message);
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    // passiert ueber das Manifest.
    return false;
  }
  
  /**
   * Liefert die eingesammelten Messages.
   * @return die eingesammelten Messages.
   */
  public List<BootMessage> getMessages()
  {
    // wir liefern direkt die Messages aus, damit sie entfernt werden koennen,
    // wenn sie erledigt sind.
    return this.messages;
  }

}


