/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import de.willuhn.jameica.messaging.MessageBus;
import de.willuhn.logging.Logger;

/**
 * In der ExtensionRegistry werden alle Erweiterungsmodule registriert.
 * Sie ist ausserdem zustaendig, erweiterbare Module an die Erweiterungen
 * zu uebergeben.
 * Text bitte zweimal lesen ;) 
 */
public class ExtensionRegistry
{

  private static Map<String,List<Extension>> extensions = new HashMap<String,List<Extension>>();
  private static Map<Extension,String> sources = new IdentityHashMap<Extension,String>();
  
  /**
   * Erweitert das Extendable insofern Extensions registriert sind.
   * @param extendable
   */
  public static void extend(Extendable extendable)
  {
    if (extendable == null)
      return;
    
    String id = extendable.getExtendableID();
    if (id == null)
      return;
    
    int count = 0;
    
    List<Extension> v = extensions.get(id);
    if (v != null)
    {
      for (Extension e:v)
      {
        try
        {
          e.extend(extendable);
          count++;
        }
        catch (Throwable t)
        {
          Logger.error("error while extending " + id,t);
        }
      }
    }
    MessageBus.sendSync(id,count);
  }

  /**
   * Registriert das Erweiterungsmodul unter den genannten IDs.
   * @param extension
   * @param extendableIDs
   */
  public static void register(Extension extension, String[] extendableIDs)
  {
    register(extension,extendableIDs,null);
  }

  /**
   * Registriert das Erweiterungsmodul unter den genannten IDs.
   * @param extension
   * @param extendableIDs
   * @param source Name des Plugins, aus dem die Extension stammt.
   */
  public static void register(Extension extension, String[] extendableIDs, String source)
  {
    if (extension != null && source != null)
      sources.put(extension,source);

    for (int i=0;i<extendableIDs.length;++i)
    {
      List<Extension> v = extensions.get(extendableIDs[i]);
      if (v == null)
        v = new ArrayList<Extension>();
      v.add(extension);
      
      extensions.put(extendableIDs[i],v);
    }
  }

  /**
   * Registriert das Erweiterungsmodul unter der genannten ID.
   * @param extension
   * @param extendableID
   */
  public static void register(Extension extension, String extendableID)
  {
    register(extension, new String[]{extendableID});
  }

  /**
   * Liefert den Namen des Plugins, aus dem die Extension stammt.
   * @param extension Extension.
   * @return Plugin-Name oder NULL.
   */
  public static String getSource(Extension extension)
  {
    return extension == null ? null : sources.get(extension);
  }

  /**
   * Liefert die Erweiterungsmodule zur genannten Extendable-ID.
   * @param extendableID die Extendable-ID.
   * @return die Liste der gefundenen Extensions.
   */
  public static List<Extension> getExtensions(String extendableID)
  {
    // Ja, wir geben keine Kopie der Liste raus sondern direkt
    // das Original. Damit kann der Aufrufer eine Extension auch
    // wieder deregistrieren. Irgendwann sollte vielleicht nochmal
    // geprueft werden, ob das sinnvoll ist.
    return extensions.get(extendableID);
  }

}
