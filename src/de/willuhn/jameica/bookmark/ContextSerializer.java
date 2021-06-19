/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.bookmark;

import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.datasource.Service;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.Plugin;
import de.willuhn.jameica.plugin.ServiceDescriptor;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.MultipleClassLoader;

/**
 * Diese Klasse serialisiert/deserialisiert die Context-Information des Bookmarks.
 */
@Lifecycle(Type.CONTEXT)
public class ContextSerializer
{
  /**
   * Serialisiert die Context-Information.
   * @param context das zu serialisierende Objekt.
   * @return die serialisierte Form des Contextes.
   */
  public Context serialize(Object context)
  {
    if (context == null) {
      return null;
    }

    Context result = new Context();
    
    // Checken, ob wir ein Plugin haben und dieses das Objekt serialisieren kann
    Plugin plugin = Application.getPluginLoader().findByClass(context.getClass());
    if (plugin != null)
    {
      result.setPlugin(plugin.getManifest().getName());
      
      // Wir haben ein Plugin - dann fragen wir das mal, ob es das Objekt serialisieren moechte.
      QueryMessage msg = new QueryMessage(context);
      Application.getMessagingFactory().getMessagingQueue("jameica.bookmark." + result.getPlugin() + ".serialize").sendSyncMessage(msg);
      Object response = msg.getData();
      
      if ((response instanceof String) && context != response)
      {
        result.setSerialized((String) response);
        
        // Das Plugin hat es selbst serialisiert. Dann vergreifen wir uns nicht dran.
        return result;
      }
    }
    
    // Ne, dann versuchen wir es selbst
    result.setClassName(context.getClass().getName());

    // wenn es ein DBObject ist, holen wir uns noch die ID
    if (context instanceof DBObject)
    {
      try
      {
        result.setId(((DBObject)context).getID());
      }
      catch (RemoteException e)
      {
        Logger.error("unable to determine object ID",e);
      }
    }
    
    return result;
  }
  
  /**
   * Deserialisiert die Context-Information.
   * @param context die serialisierte Context-Information.
   * @return die deserialisierte Context-Information.
   */
  public Object unserialize(Context context)
  {
    if (context == null)
      return null;
    
    try
    {
      BeanService bs = Application.getBootLoader().getBootable(BeanService.class);

      String plugin     = StringUtils.trimToNull(context.getPlugin());
      String className  = StringUtils.trimToNull(context.getClassName());
      String id         = StringUtils.trimToNull(context.getId());
      String serialized = context.getSerialized();

      // Wenn wir kein Plugin haben, dann einfach versuchen, die Klasse zu laden
      if (plugin == null)
      {
        if (className == null)
          return null;
        
        // Mal schauen, ob es ein Klassen-Name ist, den wir instanziieren koennen
        Class<?> c = Application.getClassLoader().load(className);
        return bs.get(c);
      }

      // Haben wir ein Plugin?
      Manifest mf = Application.getPluginLoader().getManifestByName(plugin);
      
      // Das Plugin gibts nicht mehr, da koennen wir nichts deserialisieren
      if (mf == null)
        return null;
      
      // Das Plugin hat selbst serialisiert, dann soll es auch deserialisieren
      if (!StringUtils.isEmpty(serialized))
      {
        // Wir haben ein Plugin. Mal schauen, ob es das deserialisieren kann
        QueryMessage msg = new QueryMessage(serialized);
        Application.getMessagingFactory().getMessagingQueue("jameica.bookmark." + plugin + ".unserialize").sendSyncMessage(msg);
        
        Object response = msg.getData();
        if (response != null && response != serialized)
          return response; // OK, das Plugin hat deserialisiert
        
        // serialized war != null, dann ist es die Verantwortung des Plugins
        return null;
      }
      
      // Weder "serialized" noch "className" - da koennen wir nichts machen
      if (className == null)
        return null;
      
      // Wir laden die Klasse mal. Vielleicht ist es ein DBObject
      
      MultipleClassLoader classLoader = mf.getClassLoader();
      Class<?> c = classLoader.load(className);
      
      if (!DBObject.class.isAssignableFrom(c))
        return bs.get(c); // OK, dann halt einfach instanziieren
      
      
      // Dann mal schauen, ob das Plugin einen DBService hat
      Class<?> pluginClass = classLoader.load(mf.getPluginClass());
      ServiceDescriptor[] serviceNames = mf.getServices();
      for (ServiceDescriptor s:serviceNames)
      {
        Service service = Application.getServiceFactory().lookup(pluginClass,s.getName());
        if (service instanceof DBService)
        {
          // Sieht doch gut aus, mal versuchen, ob wir es laden koennen
          return ((DBService)service).createObject((Class<? extends DBObject>) c,id);
        }
      }
    }
    catch (Exception e)
    {
      Logger.write(Level.DEBUG,"unable to unserialize context",e);
    }
    
    return null;
  }

}


