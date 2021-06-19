/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.plugin;

import java.io.File;
import java.util.List;

/**
 * Definiert eine Quelle, in der sich Plugins befinden koennen.
 */
public interface PluginSource extends Comparable<Object>
{
  /**
   * Die verschiedenen Arten von Plugin-Quellen.
   */
  public enum Type
  {
    // Die hier angegebene Reihenfolge entspricht aufgrund der Ordinal-Zahl
    // der Enums auch deren Lade-Reihenfolge und Prioritaet.
    
    /**
     * Plugins im Ordner "plugins" innerhalb des Programm-Ordners von Jameica. 
     */
    SYSTEM,
    
    /**
     * Explizit in der Config-Datei angegebene Plugin-Ordner.
     */
    CONFIG,

    /**
     * Plugins im Ordner "plugins" innerhalb des Benutzer-Verzeichnisses ".jameica".
     */
    USER,

    ;
    
    /**
     * Die Default-Plugin-Quelle.
     */
    public static final Type DEFAULT = USER;
  }
  
  /**
   * Liefert die Liste der Plugin-Ordner aus dieser Quelle.
   * @return die Liste der Plugin-Ordner aus dieser Quelle.
   */
  public List<File> find();
  
  /**
   * Liefert den Typ der Plugin-Quelle.
   * @return der Typ der Plugin-Quelle.
   */
  public Type getType();
  
  /**
   * Prueft, ob die Plugin-Quelle beschreibbar ist.
   * @return true, wenn die Plugin-Quelle grundsaetzlich beschreibbar ist.
   */
  public boolean canWrite();
  
  /**
   * Liefert das Basis-Verzeichnis der Plugin-Quelle.
   * @return das Basis-Verzeichnis der Plugin-Quelle.
   */
  public File getDir();
  
  /**
   * Liefert einen lesbaren Namen der Pluginquelle.
   * @return lesbarer Name der Pluginquelle.
   */
  public String getName();
}



/**********************************************************************
 * $Log: PluginSource.java,v $
 * Revision 1.1  2011/06/01 12:35:58  willuhn
 * @N Die Verzeichnisse, in denen sich Plugins befinden koennen, sind jetzt separate Klassen vom Typ PluginSource. Damit kann das kuenftig um weitere Plugin-Quellen erweitert werden und man muss nicht mehr die Pfade vergleichen, um herauszufinden, in welcher Art von Plugin-Quelle ein Plugin installiert ist
 *
 **********************************************************************/