/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/ManifestMessageConsumer.java,v $
 * $Revision: 1.8 $
 * $Date: 2012/04/05 23:25:46 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.plugin.ConsumerDescriptor;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.MessageDescriptor;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.MultipleClassLoader;

/**
 * Uebernimmt das Registrieren der im Plugin-Manifest definierten
 * Message-Consumer sowie das Versenden von Messages, die im Manifest definiert wurden.
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
    
    this.registerConsumers();
    this.sendMessages();
  }
  
  /**
   * Registriert die Message-Consumer.
   */
  private void registerConsumers()
  {
    Logger.debug("searching for message consumers from manifests");
    MessagingFactory factory = Application.getMessagingFactory();
    BeanService beanService = Application.getBootLoader().getBootable(BeanService.class);
    
    List<Manifest> list = new LinkedList<Manifest>();
    list.addAll(Application.getPluginLoader().getInstalledManifests()); // die Plugins
    list.add(Application.getManifest()); // Jameica selbst
    
    int count = 0;
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
        
        try
        {
          Class c = null;
          
          if (mf.isSystemManifest())
          {
            // ueber den System-Classloader laden
            c = Application.getClassLoader().load(classname);
          }
          else
          {
            // Wir laden die Klasse ueber den Classloader des Plugins
            MultipleClassLoader loader = mf.getClassLoader();
            c = loader.load(classname);
          }
          MessageConsumer mc = (MessageConsumer) beanService.get(c);
          
          // Wir registrieren hier nur Consumer, die NICHT das autoRegister-Flag gesetzt
          // haben. Denn die werden ja bereits vom AutoRegisterMessageConsumer erfasst.
          if (mc.autoRegister())
            continue;
          
          if (StringUtils.trimToNull(queue) != null)
          {
            Logger.debug("  " + queue + ": " + classname);
            factory.getMessagingQueue(queue).registerMessageConsumer(mc);
          }
          else
          {
            Logger.debug("  <default-queue>: " + classname);
            factory.registerMessageConsumer(mc);
          }
          count++;
        }
        catch (Throwable t)
        {
          Logger.error("unable to register message consumer " + classname,t);
        }
        
      }
    }
    Logger.info("message consumers from manifests: " + count);
  }

  /**
   * Sendet die Messages.
   */
  private void sendMessages()
  {
    Logger.debug("searching for messages from manifests");
    MessagingFactory factory = Application.getMessagingFactory();
    
    List<Manifest> list = new LinkedList<Manifest>();
    list.addAll(Application.getPluginLoader().getInstalledManifests()); // die Plugins
    list.add(Application.getManifest()); // Jameica selbst
    
    int count = 0;
    for (Manifest mf:list)
    {
      MessageDescriptor[] messages = mf.getMessages();
      if (messages == null || messages.length == 0)
        continue;
      for (MessageDescriptor d:messages)
      {
        String queue = d.getQueue();
        String data  = d.getData();
        if (queue == null || queue.length() == 0)
        {
          Logger.warn("skipping message in manifest from " + mf.getName() + ", contains no queue name");
          continue;
        }
        if (data == null || data.length() == 0)
        {
          Logger.warn("skipping message in manifest from " + mf.getName() + ", contains no data");
          continue;
        }
        factory.getMessagingQueue(queue).sendMessage(new QueryMessage(data));
      }
    }
    Logger.info("messages from manifests: " + count);
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
 * Revision 1.8  2012/04/05 23:25:46  willuhn
 * @N Support fuer das Senden von Messages direkt aus dem Manifest heraus (wurde zum Registrieren von Javascripts aus Java-losen Plugins heraus benoetigt)
 *
 * Revision 1.7  2012/03/28 22:28:07  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.6  2011/10/18 09:29:06  willuhn
 * @N Reminder in eigenes Package verschoben
 * @N ReminderStorageProvider, damit der ReminderService auch Reminder aus anderen Datenquellen verwenden kann
 *
 * Revision 1.5  2011-10-05 16:51:16  willuhn
 * @N Auch MessageConsumer beruecksichtigen, die im System-Manifest von Jameica stehen
 *
 * Revision 1.4  2011-08-31 07:46:41  willuhn
 * @B Compile-Fixes
 *
 * Revision 1.3  2011-08-30 15:51:11  willuhn
 * @N Message-Consumer via Bean-Service instanziieren, damit dort jetzt auch Dependency-Injection moeglich ist
 *
 * Revision 1.2  2011-06-17 16:06:17  willuhn
 * @C Logging
 *
 * Revision 1.1  2011-06-17 15:55:18  willuhn
 * @N Registrieren von Message-Consumern im Manifest
 *
 * Revision 1.1  2011-06-07 11:08:55  willuhn
 * @C Nach automatisch zu registrierenden Message-Consumern erst suchen, nachdem die SystemMessage.SYSTEM_STARTED geschickt wurde. Vorher geschah das bereits beim Senden der ersten Nachricht - was u.U. viel zu frueh ist (z.Bsp. im DeployService)
 *
 **********************************************************************/