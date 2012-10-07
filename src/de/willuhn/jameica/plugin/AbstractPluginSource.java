/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/AbstractPluginSource.java,v $
 * $Revision: 1.3 $
 * $Date: 2012/03/20 23:28:01 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.plugin;



/**
 * Abstrakte Basis-Implementierung der Plugin-Quellen.
 */
public abstract class AbstractPluginSource implements PluginSource
{
  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   * Wir sortieren anhand der Ordinal-Zahl des Type-Enums.
   */
  public int compareTo(Object o)
  {
    if (!(o instanceof PluginSource))
      return -1;
    
    Type myType = this.getType();
    if (myType == null)
      return 1; // Wenn wir keinen Typ haben - dann der zuerst
    
    Type otherType = ((PluginSource)o).getType();
    if (otherType == null)
      return -1; // Wenn der keinen Typ hat - dann wir zuerst

    // Sortierung basierend auf der Ordinal-Zahl des Enums
    return myType.compareTo(otherType);
  }
}



/**********************************************************************
 * $Log: AbstractPluginSource.java,v $
 * Revision 1.3  2012/03/20 23:28:01  willuhn
 * @N BUGZILLA 1209
 *
 * Revision 1.2  2011-08-30 16:02:23  willuhn
 * @N Alle restlichen Stellen, in denen Instanzen via Class#newInstance erzeugt wurden, gegen BeanService ersetzt. Damit kann jetzt quasi ueberall Dependency-Injection verwendet werden, wo Jameica selbst die Instanzen erzeugt
 *
 * Revision 1.1  2011-06-01 12:35:57  willuhn
 * @N Die Verzeichnisse, in denen sich Plugins befinden koennen, sind jetzt separate Klassen vom Typ PluginSource. Damit kann das kuenftig um weitere Plugin-Quellen erweitert werden und man muss nicht mehr die Pfade vergleichen, um herauszufinden, in welcher Art von Plugin-Quelle ein Plugin installiert ist
 *
 **********************************************************************/