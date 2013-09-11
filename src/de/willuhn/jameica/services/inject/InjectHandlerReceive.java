/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.services.inject;

import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;

import de.willuhn.annotation.Inject;
import de.willuhn.annotation.Injector;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.MessagingQueue;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.Receive;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;


/**
 * Durchsucht die Funktionen in der Klasse nach der "Receive" Annotation
 * und registriert passende MessageConsumer.
 */
public class InjectHandlerReceive implements InjectHandler
{
  /**
   * @see de.willuhn.jameica.services.inject.InjectHandler#inject(java.lang.Object)
   */
  public void inject(final Object o) throws Exception
  {
    final String name = o.getClass().getSimpleName();

    Inject.inject(o,new Injector()
    {
      /**
       * @see de.willuhn.annotation.Injector#inject(java.lang.Object, java.lang.reflect.AccessibleObject, java.lang.annotation.Annotation)
       */
      public void inject(final Object bean, AccessibleObject field, Annotation annotation) throws Exception
      {
        final Method m = (Method) field;
        Receive r      = (Receive) annotation;
        String s       = StringUtils.trimToNull(r.queue());
        Logger.debug("  applying message receiver for queue " + (s != null ? s : "[default]") + " to " + name + "." + m.getName());
        
        final MessagingQueue queue = (s != null ? Application.getMessagingFactory().getMessagingQueue(s) : Application.getMessagingFactory());
        
        MessageConsumer consumer = new MessageConsumer()
        {
          private WeakReference ref = new WeakReference(bean);
          
          public void handleMessage(Message message) throws Exception
          {
            Object o = ref.get();
            if (o == null)
            {
              // auto-unregister
              queue.unRegisterMessageConsumer(this);
              return;
            }
            
            QueryMessage msg = (QueryMessage) message;
            if (!m.isAccessible())
              m.setAccessible(true);
            m.invoke(o,msg.getData());
          }
          
          /**
           * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
           */
          public Class[] getExpectedMessageTypes()
          {
            return new Class[]{QueryMessage.class};
          }
          
          /**
           * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
           */
          public boolean autoRegister()
          {
            return false;
          }
        };
        
        queue.registerMessageConsumer(consumer);
      }
    },Receive.class);
  }
}


