/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/BeanService.java,v $
 * $Revision: 1.9 $
 * $Date: 2012/03/28 22:28:07 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import de.willuhn.annotation.Inject;
import de.willuhn.annotation.Injector;
import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.plugin.Plugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.JameicaException;
import de.willuhn.logging.Logger;
import de.willuhn.util.Session;

/**
 * Ein Service, ueber den Beans instanziiert werden.
 * Er unterstuetzt die Annoations {@link Lifecycle}, {@link Resource}, {@link PostConstruct} und {@link PreDestroy}.
 * 
 * Ist die Bean mit der Annotation {@link Lifecycle} versehen, gilt:
 * 
 *   - {@link Type#CONTEXT}: Die Bean wird nur einmal instanziiert und existiert
 *                           ueber die gesamte Lebensdauer des BeanService - also
 *                           ueber die gesamte Laufzeit der Jameica-Instanz
 *   - {@link Type#REQUEST}: Die Bean besitzt keinen Lifecycle - mit jedem Aufruf
 *                           wird eine neue Instanz erzeugt.
 *   - {@link Type#SESSION}: Die Bean besitzt eine Lebensdauer von 30 Minuten.
 *   
 * Ist eine Member-Variable oder Methode mit der Annotation {@link Resource} versehen,
 * wird sie ueber den BeanService bei der Instanziierung der Bean aufgeloest.
 * 
 * Enthaelt die Bean die Annotation {@link PostConstruct}, wird die zugehoerige
 * Methode bei der Instanziierung aufgerufen.
 * 
 * Die Annotation {@link PreDestroy} wird nur bei Beans mit CONTEXT-Lifecycle
 * beruecksichtigt. Der Aufruf der mit dieser Annotation versehenen Funktion erfolgt
 * beim Beenden von Jameica.
 */
public class BeanService implements Bootable
{
  private Map<Class,Object> contextScope = new HashMap<Class,Object>();
  private Stack contextOrder             = new Stack();
  private Session sessionScope           = new Session();

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class<Bootable>[] depends()
  {
    return new Class[]{LogService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    // Nicht noetig - wir machen alles on-demand.
  }
  
  /**
   * Liefert eine Instanz der angegebenen Bean.
   * @param <T> Typ der Bean.
   * @param type Typ der Bean.
   * @return die Instanz der Bean.
   * Wenn die Bean mit der {@link Lifecycle} Annotation versehen ist, wird
   * diese beruecksichtigt.
   */
  public <T> T get(Class<T> type)
  {
    if (type == null)
      return null;
    
    String name = type.getSimpleName();
    Logger.debug("searching for bean " + name);
    
    T bean = null;
    
    // 1. Checken, ob wir sie im Context-Scope haben
    bean = (T) contextScope.get(type);
    if (bean != null)
    {
      Logger.debug("  found in context scope");
      return bean;
    }
    
    // 2. Checken, ob wir sie im Session-Scope haben
    bean = (T) sessionScope.get(type);
    if (bean != null)
    {
      Logger.debug("  found in session scope");
      return bean;
    }

    try
    {
      // 3. Bean erzeugen
      Logger.debug("  creating new");
      bean = type.newInstance();

      // Lifecycle ermitteln
      Lifecycle lc = (Lifecycle) type.getAnnotation(Lifecycle.class);
      Lifecycle.Type lct = lc != null ? lc.value() : null;

      if (lct == null)
      {
        Logger.debug("  no lifecycle -> request scope");
      }
      else if (lct == Type.REQUEST)
      {
        Logger.debug("  request scope");
      }
      else if (lct == Type.CONTEXT)
      {
        Logger.debug("  context scope");
        contextScope.put(type,bean);
        contextOrder.add(bean);
      }
      else if (lct == Type.SESSION)
      {
        Logger.debug("  session scope");
        sessionScope.put(type,bean);
      }
      else
      {
        Logger.debug("  unknown scope");
      }
      
      // Abhaengigkeiten aufloesen
      // Das duerfen wir erst machen, NACHDEM wir sie registriert haben
      // Andernfalls koennte man durch zirkulaere Abhaengigkeit eine Endlosschleife ausloesen
      this.resolve(bean);
      
      // Fertig
      return bean;
    }
    catch (JameicaException je)
    {
      throw je;
    }
    catch (Throwable t)
    {
      Logger.error("unable to create instance of " + type,t);
      throw new JameicaException(Application.getI18n().tr("{0} kann nicht erstellt werden: {1}",type.getSimpleName(),t.getMessage()));
    }
  }
  
  /**
   * Loest die Annotations auf.
   * @param bean die Bean.
   * @throws Exception
   */
  private void resolve(Object bean) throws Exception
  {
    this.inject(bean);
    
    final String name = bean.getClass().getSimpleName();

    // PostConstruct anwenden
    Inject.inject(bean,new Injector()
    {
      /**
       * @see de.willuhn.annotation.Injector#inject(java.lang.Object, java.lang.reflect.AccessibleObject, java.lang.annotation.Annotation)
       */
      public void inject(Object bean, AccessibleObject field, Annotation annotation) throws Exception
      {
        Method m = (Method) field;
        Logger.debug("  " + name + "." + m.getName());
        m.setAccessible(true);
        m.invoke(bean,(Object[]) null);
      }
    },PostConstruct.class);
  }
  
  /**
   * Injiziert die Abhaengigkeiten in die Bean.
   * @param bean die Bean.
   * @throws JameicaException wenn beim Injezieren der Dependencies etwas schief ging.
   */
  public void inject(Object bean) throws JameicaException
  {
    final String name = bean.getClass().getSimpleName();
    
    try
    {
      // Resource-Annotations anwenden
      Inject.inject(bean,new Injector()
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
//          if (rname == null || rname.length() == 0)
//          {
//            if (field instanceof Field)
//              rname = ((Field)field).getName();
//            else if (field instanceof Method)
//              rname = BeanUtil.toProperty(((Method)field).getName());
//          }
          //
          //////////////////////////////////////////////////////////////////////
          
          // Anhand des Namens suchen.
          if (rname != null && rname.length() > 0)
          {
            Logger.debug("  inject service " + rname + " into " + name);
            
            // Plugin ermitteln und Service von dort laden
            // Wenn der Typ der Resource angegeben ist, suchen wir nach dessen Plugin, sonst nach dem Plugin der Bean
            Plugin plugin = Application.getPluginLoader().findByClass(c != null ? c : bean.getClass());
            if (plugin != null)
              dep = Application.getServiceFactory().lookup(plugin.getClass(),rname);
            else
              Logger.debug("  no plugin found for service " + rname);
          }

          // Anhand des Typs suchen - aber nur, wenn wir die Abhaengigkeit nicht schon haben
          if (dep == null && c != null)
          {
            Logger.debug("  inject bean " + c.getSimpleName() + " into " + name);
            dep = get(c); // aufloesen
          }
          
          if (dep == null) // nichts gefunden
          {
            Logger.debug("  resource [name: " + rname + ", type: " + c + "] not found");
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
    catch (JameicaException je)
    {
      throw je;
    }
    catch (Exception e)
    {
      Logger.error("unable to inject dependencies into " + name,e);
      throw new JameicaException(Application.getI18n().tr("Abhängigkeiten können nicht in {0} injiziert werden: {1}",name,e.getMessage()));
    }
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
    try
    {
      Logger.debug("invoking predestroy for context beans");
      // PostDestroy bei den Context-Beans aufrufen
      // Erfolgt in umgekehrter Lade-Reihenfolge
      while (!this.contextOrder.isEmpty())
      {
        Object bean = this.contextOrder.pop();
        try
        {
          Inject.inject(bean,new Injector()
          {
            /**
             * @see de.willuhn.annotation.Injector#inject(java.lang.Object, java.lang.reflect.AccessibleObject, java.lang.annotation.Annotation)
             */
            public void inject(Object bean, AccessibleObject field, Annotation annotation) throws Exception
            {
              Method m = (Method) field;
              Logger.debug("  " + bean.getClass().getSimpleName() + "." + m.getName());
              m.setAccessible(true);
              m.invoke(bean,(Object[]) null);
            }
          },PreDestroy.class);
        }
        catch (Exception e)
        {
          Logger.error("unable to predestroy " + bean.getClass().getSimpleName(),e);
        }
      }
    }
    finally
    {
      contextOrder.clear();
      contextScope.clear();
      sessionScope.clear();
    }
  }

}



/**********************************************************************
 * $Log: BeanService.java,v $
 * Revision 1.9  2012/03/28 22:28:07  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.8  2011-09-08 11:11:55  willuhn
 * @N inject(Object) ist jetzt public und kann daher nun auch dann verwendet werden, wenn die Bean-Instanz schon vom Aufrufer erstellt wurde
 *
 * Revision 1.7  2011-08-29 16:45:59  willuhn
 * @B via "type" angegebene Abhaengkeit wurde nicht aufgeloest
 *
 * Revision 1.6  2011-07-12 15:21:30  willuhn
 * @N JameicaException
 *
 * Revision 1.5  2011-06-29 16:56:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2011-06-29 11:50:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2011-06-29 11:49:33  willuhn
 * @N In der Resource-Annotation kann jetzt auch das Attribut "name" angegeben werden - dann wird der gleichnamige Service gefunden
 *
 * Revision 1.2  2011-06-28 12:28:07  willuhn
 * @N Neuer BeanService als Dependency-Injection-Tool - yeah cool ;)
 *
 * Revision 1.1  2011-06-28 09:57:39  willuhn
 * @N Lifecycle-Annotation aus jameica.webadmin in util verschoben
 *
 **********************************************************************/