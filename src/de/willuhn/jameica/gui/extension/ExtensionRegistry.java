/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/extension/ExtensionRegistry.java,v $
 * $Revision: 1.7 $
 * $Date: 2010/06/03 12:41:43 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.extension;

import java.util.Hashtable;
import java.util.Vector;

import de.willuhn.logging.Logger;

/**
 * In der ExtensionRegistry werden alle Erweiterungsmodule registriert.
 * Sie ist ausserdem zustaendig, erweiterbare Module an die Erweiterungen
 * zu uebergeben.
 * Text bitte zweimal lesen ;) 
 */
public class ExtensionRegistry
{

  private static Hashtable extensions = new Hashtable();
  
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

    Vector v = (Vector) extensions.get(id);
    if (v == null || v.size() == 0)
      return;
    for (int i=0;i<v.size();++i)
    {
      try
      {
        Extension e = (Extension) v.get(i);
        e.extend(extendable);
      }
      catch (Throwable t)
      {
        Logger.error("error while extending " + id,t);
      }
    }
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
      Vector v = (Vector) extensions.get(extendableIDs[i]);
      if (v == null)
        v = new Vector();
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

}


/*********************************************************************
 * $Log: ExtensionRegistry.java,v $
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