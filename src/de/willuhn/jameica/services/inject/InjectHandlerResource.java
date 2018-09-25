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
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.annotation.Resource;

import de.willuhn.annotation.Inject;
import de.willuhn.annotation.Injector;
import de.willuhn.boot.Bootable;
import de.willuhn.jameica.plugin.Plugin;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;



/**
 * InjectHandler fuer die "Resource" Annotation.
 */
public class InjectHandlerResource implements InjectHandler
{
  /**
   * @see de.willuhn.jameica.services.inject.InjectHandler#inject(java.lang.Object)
   */
  public void inject(final Object o) throws Exception
  {
    final String name = o.getClass().getSimpleName();

    // Resource-Annotations anwenden
    Inject.inject(o,new Injector()
    {
      /**
       * @see de.willuhn.annotation.Injector#inject(java.lang.Object, java.lang.reflect.AccessibleObject, java.lang.annotation.Annotation)
       */
      public void inject(Object bean, AccessibleObject field, Annotation annotation) throws Exception
      {
        Resource r = (Resource) annotation;
        
        String rname = r.name();
        Class c      = r.type();
        Object dep   = null;
        
        if (c == Object.class) // Das ist der Default-Wert der Annotation
          c = null;

        //////////////////////////////////////////////////////////////////////
        // Auto-Discovery
        
        // Type discovery
        if (c == null)
        {
          if (field instanceof Field)
          {
            c = ((Field)field).getType();
          }
          else if (field instanceof Method)
          {
            Class[] params = ((Method)field).getParameterTypes();
            if (params != null && params.length == 1) // lassen wir nur zu, wenn es nur einen Parameter gibt
              c = params[0];
          }
        }
        
        // Name discovery
        // Machen wir derzeit noch nicht, weil wir sonst immer beides haetten,
        // Name rname UND c. Jameica wuerde dann immer nach einem Service suchen,
        // wenn nichts angegeben ist. Normalerweise ist das aber nicht gewuenscht
        // sondern stattdessen einfach eine Instanz der angegebenen Bean.
        // Daher gibt es derzeit noch kein Auto-Discovery basierend auf dem Namen
        // des Property sondern nur dann ein Lookup nach dem Service, wenn er per
        // Name explizit angegeben ist.
//        if (rname == null || rname.length() == 0)
//        {
//          if (field instanceof Field)
//            rname = ((Field)field).getName();
//          else if (field instanceof Method)
//            rname = BeanUtil.toProperty(((Method)field).getName());
//        }
        //
        //////////////////////////////////////////////////////////////////////
        
        // Anhand des Namens suchen.
        if (rname != null && rname.length() > 0)
        {
          Logger.trace("  inject service " + rname + " into " + name);
          
          // Plugin ermitteln und Service von dort laden
          // Wenn der Typ der Resource angegeben ist, suchen wir nach dessen Plugin, sonst nach dem Plugin der Bean
          Plugin plugin = Application.getPluginLoader().findByClass(c != null ? c : bean.getClass());
          if (plugin != null)
            dep = Application.getServiceFactory().lookup(plugin.getClass(),rname);
          else
            Logger.trace("  no plugin found for service " + rname);
        }

        // Anhand des Typs suchen - aber nur, wenn wir die Abhaengigkeit nicht schon haben
        if (dep == null && c != null)
        {
          if (isBootable(c))
          {
            Logger.trace("  inject bootable " + c.getSimpleName() + " into " + name);
            dep = Application.getBootLoader().getBootable(c); // direkt als Bootable laden
          }
          else
          {
            Logger.trace("  inject bean " + c.getSimpleName() + " into " + name);
            BeanService service = Application.getBootLoader().getBootable(BeanService.class);
            dep = service.get(c); // aufloesen per Beanservice
          }
        }
        
        if (dep == null) // nichts gefunden
        {
          Logger.trace("  resource [name: " + rname + ", type: " + c + "] not found");
          return;
        }
        
        field.setAccessible(true);
        
        if (field instanceof Method)
        {
          ((Method)field).invoke(bean,dep);
        }
        else if (field instanceof Field)
        {
          ((Field)field).set(bean,dep);
        }
      }
    },Resource.class);
  }
  
  /**
   * Prueft, ob die Klasse ein Bootable ist.
   * Die Funktion sucht nur direkt in der Klasse. Nicht in den Elternklassen.
   * @param type der Typ.
   * @return true, wenn es ein Bootable ist.
   */
  private static boolean isBootable(Class type)
  {
    Class[] interfaces = type.getInterfaces();
    if (interfaces == null || interfaces.length == 0)
      return false;
    
    for (Class c:interfaces)
    {
      if (c.equals(Bootable.class))
        return true;
    }
    
    return false;
  }
}


