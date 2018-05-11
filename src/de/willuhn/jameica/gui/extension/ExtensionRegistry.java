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


/*********************************************************************
 * $Log: ExtensionRegistry.java,v $
 * Revision 1.10  2011/10/05 10:48:55  willuhn
 * @R Messaging wieder entfernt - erzeugt haufenweise Queues, die wir im Moment noch gar nicht nutzen
 *
 * Revision 1.9  2011-09-28 12:41:29  willuhn
 * @N Extensions koennen jetzt auch dynamisch via Messaging verwendet werden
 *
 * Revision 1.8  2010/06/03 17:06:51  willuhn
 * @N getExtension(), damit man an die Instanz von bereits registrierten Extensions rankommt
 *
 * Revision 1.7  2010/06/03 12:41:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2010/06/03 12:41:33  willuhn
 * @N Throwable toleriert auch NoClassDefFoundError
 *
 * Revision 1.5  2005/06/07 21:57:32  web0
 * *** empty log message ***
 *
 * Revision 1.4  2005/06/06 10:10:43  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/05/30 12:01:33  web0
 * @R removed gui packages from rmic.xml
 *
 * Revision 1.2  2005/05/27 17:31:46  web0
 * @N extension system
 *
 * Revision 1.1  2005/05/25 16:11:47  web0
 * @N first code for extension system
 *
 *********************************************************************/