/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/extension/ExtensionRegistry.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/05/30 12:01:33 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.extension;

import java.util.Hashtable;
import java.util.Vector;

import de.willuhn.logging.Logger;

/**
 * 
 */
public class ExtensionRegistry
{

  private static Hashtable extensions = new Hashtable();
  
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
      catch (Exception e)
      {
        Logger.error("error while extending " + id);
      }
    }
  }

  public static void register(Extension extension, String[] extendableIDs)
  {
    if (extension == null || extendableIDs == null || extendableIDs.length == 0)
      return;
      
    for (int i=0;i<extendableIDs.length;++i)
    {
      Vector v = (Vector) extensions.get(extendableIDs[i]);
      if (v == null)
        v = new Vector();
      v.add(extension);
      
      extensions.put(extendableIDs[i],v);
    }
  }

  public static void register(Extension extension, String extendableID)
  {
    register(extension, new String[]{extendableID});
  }

}


/*********************************************************************
 * $Log: ExtensionRegistry.java,v $
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