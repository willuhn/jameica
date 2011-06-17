/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/ManifestMessageConsumer.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/06/17 15:55:18 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import java.lang.reflect.Constructor;
import java.util.List;

import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.plugin.ConsumerDescriptor;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.MultipleClassLoader;

/**
 * Uebernimmt das Registrieren der im Plugin-Manifest definierten
 * Message-Consumer.
 */
public class ManifestMessageConsumer implements MessageConsumer
{
  private static boolean done = false;
  
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
    if (done)
      return;
    
    // Machen wir nur, wenn die "System-gestartet"-Meldung kommt.
    SystemMessage msg = (SystemMessage) message;
    if (msg.getStatusCode() != SystemMessage.SYSTEM_STARTED)
      return;
    
    Logger.info("searching for message consumers from manifests");
    MessagingFactory factory = Application.getMessagingFactory();
    
    List<Manifest> list = Application.getPluginLoader().getInstalledManifests();
    for (Manifest mf:list)
    {
      ConsumerDescriptor[] consumers = mf.getMessageConsumers();
      if (consumers == null || consumers.length == 0)
        continue;
      for (ConsumerDescriptor d:consumers)
      {
        String classname = d.getClassname();
        String queue     = d.getQueue();
        
        if (classname == null || classname.length() == 0)
        {
          Logger.warn("skipping consumer declaration in manifest from " + mf.getName() + ", contains no class name");
          continue;
        }
        if (queue == null || queue.length() == 0)
        {
          Logger.warn("skipping messageconsumer " + classname + " in manifest from " + mf.getName() + ", contains no queue name");
          continue;
        }
        
        try
        {
          // Wir laden die Klasse ueber den Classloader des Plugins
          AbstractPlugin plugin = Application.getPluginLoader().getPlugin(mf.getPluginClass());
          MultipleClassLoader loader = plugin.getResources().getClassLoader();

          Class c = loader.load(classname);
          Constructor ct = c.getConstructor((Class[])null);
          ct.setAccessible(true);
          MessageConsumer mc = (MessageConsumer)(ct.newInstance((Object[])null));
          
          // Wir registrieren hier nur Consumer, die NICHT das autoRegister-Flag gesetzt
          // haben. Denn die werden ja bereits vom AutoRegisterMessageConsumer erfasst.
          if (mc.autoRegister())
            continue;
          
          Logger.info("  " + classname);
          factory.getMessagingQueue(queue).registerMessageConsumer(mc);
        }
        catch (NoSuchMethodException e)
        {
          Logger.warn("message consumer has no default constructor, skipping");
        }
        catch (Throwable t)
        {
          Logger.error("unable to register message consumer " + classname,t);
        }
        
      }
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return false;
  }

}



/**********************************************************************
 * $Log: ManifestMessageConsumer.java,v $
 * Revision 1.1  2011/06/17 15:55:18  willuhn
 * @N Registrieren von Message-Consumern im Manifest
 *
 * Revision 1.1  2011-06-07 11:08:55  willuhn
 * @C Nach automatisch zu registrierenden Message-Consumern erst suchen, nachdem die SystemMessage.SYSTEM_STARTED geschickt wurde. Vorher geschah das bereits beim Senden der ersten Nachricht - was u.U. viel zu frueh ist (z.Bsp. im DeployService)
 *
 **********************************************************************/