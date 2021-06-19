/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
import de.willuhn.jameica.services.inject.InjectHandler;
import de.willuhn.jameica.services.inject.InjectHandlerPostConstruct;
import de.willuhn.jameica.services.inject.InjectHandlerReceive;
import de.willuhn.jameica.services.inject.InjectHandlerResource;
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
  private List<InjectHandler> injectHandlers = new LinkedList<>();
  private Map<Class<?>,Object> contextScope  = new HashMap<>();
  private Stack contextOrder                 = new Stack();
  private Session sessionScope               = new Session();

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
    // Default-Inject-Handler registrieren
    this.injectHandlers.clear();
    this.injectHandlers.add(new InjectHandlerResource());
    this.injectHandlers.add(new InjectHandlerPostConstruct());
    this.injectHandlers.add(new InjectHandlerReceive());
  }

  /**
   * Registriert einen Inject-Handler.
   * @param handler der zu registrierende Handler.
   */
  public void addInjectHandler(InjectHandler handler)
  {
    this.injectHandlers.add(handler);
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
    Logger.trace("searching for bean " + name);
    
    T bean = null;
    
    // 1. Checken, ob wir sie im Context-Scope haben
    bean = (T) contextScope.get(type);
    if (bean != null)
    {
      Logger.trace("  found in context scope");
      return bean;
    }
    
    // 2. Checken, ob wir sie im Session-Scope haben
    bean = (T) sessionScope.get(type);
    if (bean != null)
    {
      Logger.trace("  found in session scope");
      return bean;
    }

    try
    {
      // 3. Bean erzeugen
      Logger.debug("  creating new " + type);
      bean = type.getDeclaredConstructor().newInstance();

      // Lifecycle ermitteln
      Lifecycle lc = type.getAnnotation(Lifecycle.class);
      Lifecycle.Type lct = lc != null ? lc.value() : null;

      if (lct == null)
      {
        Logger.trace("  no lifecycle -> request scope");
      }
      else if (lct == Type.REQUEST)
      {
        Logger.trace("  request scope");
      }
      else if (lct == Type.CONTEXT)
      {
        Logger.trace("  context scope");
        contextScope.put(type,bean);
        contextOrder.add(bean);
      }
      else if (lct == Type.SESSION)
      {
        Logger.trace("  session scope");
        sessionScope.put(type,bean);
      }
      else
      {
        Logger.trace("  unknown scope");
      }
      
      // Abhaengigkeiten aufloesen
      // Das duerfen wir erst machen, NACHDEM wir sie registriert haben
      // Andernfalls koennte man durch zirkulaere Abhaengigkeit eine Endlosschleife ausloesen
      this.inject(bean);
      
      
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
   * Injiziert die Abhaengigkeiten in die Bean.
   * @param bean die Bean.
   * @throws JameicaException wenn beim Injezieren der Dependencies etwas schief ging.
   */
  public void inject(Object bean) throws JameicaException
  {
    final String name = bean.getClass().getSimpleName();
    
    try
    {
      for (InjectHandler handler:this.injectHandlers)
      {
        handler.inject(bean);
      }
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
      Logger.trace("invoking predestroy for context beans");
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
              Logger.trace("  " + bean.getClass().getSimpleName() + "." + m.getName());
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
      injectHandlers.clear();
    }
  }

}
