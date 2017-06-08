/**********************************************************************
 *
 * Copyright (c) 2017 Olaf Willuhn
 * GNU GPLv2
 *
 **********************************************************************/

package de.willuhn.jameica.update;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;

import de.willuhn.jameica.plugin.Dependency;
import de.willuhn.jameica.plugin.Version;

/**
 * Kapselt das Ergebnis der aufgeloesten Abhaengigkeiten fuer ein Plugin.
 */
public class ResolverResult
{
  private PluginData pluginData = null;
  private List<PluginData> resolved = new ArrayList<PluginData>();
  private List<Dependency> missing = new ArrayList<Dependency>();
  
  /**
   * ct.
   * @param pluginData das Plugin, fuer welches die Abhaengigkeiten aufgeloest werden sollen.
   */
  public ResolverResult(PluginData pluginData)
  {
    this.pluginData = pluginData;
  }
  
  /**
   * Liefert das Plugin, auf welches sich das Ergebnis bezieht.
   * @return das Plugin, auf welches sich das Ergebnis bezieht.
   */
  public PluginData getPluginData()
  {
    return this.pluginData;
  }
  
  /**
   * Liefert die Liste der gefundenen Abhaengigkeiten.
   * Die Liste enthaelt auch indirekte Abhaengigkeiten.
   * Hierbei jedoch nur jene, die noch zu installieren sind.
   * Die bereits installierten Abhaengigkeiten sind nicht enthalten.
   * @return die Liste der gefundendenen und installierbaren Abhaengigkeiten.
   */
  public List<PluginData> getResolved()
  {
    return this.resolved;
  }
  
  /**
   * Liefert die Liste der nicht erfuellbaren Abhaengigkeiten.
   * Sobald in dieser Liste eine Abhaengigkeit enthalten ist, kann das Plugin
   * nicht installiert werden.
   * @return die Liste der nicht erfuellbaren erfüllbar Abhaengigkeiten.
   */
  public List<Dependency> getMissing()
  {
    return missing;
  }
  
  /**
   * Merged ein zweites Resolver-Result in dieses.
   * @param merge das zu mergende Resolver-Result.
   */
  public void merge(ResolverResult merge)
  {
    // Mergen der gefundenen Abhaengigkeiten. Wenn wir eine schon haben,
    // dann uebernehmen wir die mit der hoeheren Versionsnummer.
    for (PluginData p:merge.getResolved())
    {
      this.mergeResolved(p);
    }
    
    // Mergen der nicht gefundenen Abhaengigkeiten
    for (Dependency dep:merge.getMissing())
    {
      this.mergeMissing(dep);
    }
  }
  
  /**
   * Aktualisiert die Abhaengigkeit in der Liste oder fuegt sie hinzu, wenn sie noch nicht existiert.
   * @param d die Abhaengigkeit.
   */
  private void mergeResolved(PluginData d)
  {
    for (int i=0;i<this.resolved.size();++i)
    {
      PluginData our = this.resolved.get(i);
      
      if (!ObjectUtils.equals(our.getName(),d.getName()))
        continue; // ist ein anderes Plugin.
      
      // Name passt. Versionen checken
      // Wenn die von "d" aktueller ist, uebernehmen wir sie
      if (d.getAvailableVersion().compareTo(our.getAvailableVersion()) > 0)
        this.resolved.set(i,d);
      
      return;
    }
    
    // Wir haben die Abhaengigkeit noch nicht. Also hinzufuegen.
    this.resolved.add(d);
  }
  
  /**
   * Aktualisiert die Abhaengigkeit in der Liste oder fuegt sie hinzu, wenn sie noch nicht existiert.
   * @param d die Abhaengigkeit.
   */
  private void mergeMissing(Dependency d)
  {
    Version v = new Version(d.getVersion());
    for (int i=0;i<this.missing.size();++i)
    {
      Dependency our = this.missing.get(i);
      
      if (!ObjectUtils.equals(our.getName(),d.getName()))
        continue; // ist ein anderes Plugin.
      
      // Name passt. Versionen checken
      // Wenn die von "d" aktueller ist, uebernehmen wir sie
      if (v.compareTo(new Version(our.getVersion())) > 0)
        this.missing.set(i,d);
      
      return;
    }
    
    // Wir haben die Abhaengigkeit noch nicht. Also hinzufuegen.
    this.missing.add(d);
  }
  
}


