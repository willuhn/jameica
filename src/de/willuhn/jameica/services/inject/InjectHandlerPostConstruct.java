/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.services.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

import javax.annotation.PostConstruct;

import de.willuhn.annotation.Inject;
import de.willuhn.annotation.Injector;
import de.willuhn.logging.Logger;


/**
 * Durchsucht die Funktionen in der Klasse nach der "PostConstruct" Annotation
 * und fuehrt diese Methoden nach der Erzeugun der Instanz aus.
 */
public class InjectHandlerPostConstruct implements InjectHandler
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
      public void inject(Object bean, AccessibleObject field, Annotation annotation) throws Exception
      {
        Method m = (Method) field;
        Logger.trace("  executing post-construct " + name + "." + m.getName());
        m.setAccessible(true);
        m.invoke(bean,(Object[]) null);
      }
    },PostConstruct.class);
  }
}


