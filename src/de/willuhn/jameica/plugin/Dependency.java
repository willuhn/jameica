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

import java.io.Serializable;
import java.util.List;

import de.willuhn.jameica.system.Application;


/**
 * Implementiert eine einzelne Abhaengigkeit eines Plugins zu einem anderen.
 */
public class Dependency implements Serializable
{
  private static final long serialVersionUID = 8344071704015804375L;
  private String name      = null;
  private String version   = null;
  private boolean required = true;

  /**
   * ct.
   * @param name Name des Plugins.
   * @param version Versionsnummer.
   *        Kann mit einem "+" oder "-" vor der Zahl angegeben werden, wenn mindestens
   *        oder hoechstens die angegebene Version vorliegen muss.
   *        Der Parameter kann {@code null} sein, wenn die Versionsnummer egal ist.
   */
  public Dependency(String name, String version)
  {
    this(name,version,true);
  }

  /**
   * ct.
   * @param name Name des Plugins.
   * @param version Versionsnummer.
   *        Kann mit einem "+" oder "-" vor der Zahl angegeben werden, wenn mindestens
   *        oder hoechstens die angegebene Version vorliegen muss.
   *        Der Parameter kann {@code null} sein, wenn die Versionsnummer egal ist.
   * @param required {@code true}, wenn die Abhaengigkeit erfuellt sein MUSS (default).
   */
  public Dependency(String name, String version, boolean required)
  {
    if (name == null)
      throw new NullPointerException("no plugin name given in dependency");
    
    this.name     = name;
    this.version  = version;
    this.required = required;
  }

  @Override
  public String toString()
  {
    return this.name + ": " + (this.version == null ? "<any>" : this.version);
  }
  
  /**
   * Prueft, ob die Abhaengigkeit erfuellt ist.
   * @return {@code true}, wenn sie erfuellt ist, sonst {@code false}.
   */
  public boolean check()
  {
    // Version von Jameica selbst.
    if (this.name.equalsIgnoreCase("jameica"))
      return Application.getManifest().getVersion().compliesTo(this.version);
    
    // Anforderung existiert nicht mehr
    if (Application.getPluginLoader().isObsolete(this.name))
      return true;

    // true liefern, wenn es keine Pflichtaebhaengigkeit ist
    if (!this.isRequired())
      return true;
    
    List<Manifest> all = Application.getPluginLoader().getManifests();
    for (int i=0;i<all.size();++i)
    {
      Manifest mf = all.get(i);
      String n = mf.getName();
      if (n == null || !this.name.equals(n))
        continue;
      
      // Plugin gefunden - schauen, ob die Versionsnummer passt und ob es geladen werden konnte
      return mf.isLoaded() && mf.getVersion().compliesTo(this.version);
    }
    
    // Benoetigte Abhaengigkeit nicht installiert
    return false;
  }
  
  /**
   * Liefert den Namen des Plugins.
   * @return Name des Plugins.
   */
  public String getName()
  {
    return this.name;
  }
  
  /**
   * Liefert die benoetigte Versionsnummer
   * @return Versionsnummer oder {@code null}, wenn es egal ist.
   */
  public String getVersion()
  {
    return this.version;
  }
  
  /**
   * Pr�ft, ob es sich um eine obligatorische Abhaengigkeit handelt.
   * <p>Default: {@code true}
   * @return {@code true}, wenn es sich um eine obligatorische Abhaengigkeit handelt.
   */
  public boolean isRequired()
  {
    if (Application.getPluginLoader().isObsolete(this.name))
      return false;
    return this.required;
  }

  // autogenerated
  @Override
  public int hashCode()
  {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + ((name == null) ? 0 : name.hashCode());
    result = PRIME * result + ((version == null) ? 0 : version.hashCode());
    return result;
  }

  // autogenerated
  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    final Dependency other = (Dependency) obj;
    if (name == null)
    {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (version == null)
    {
      if (other.version != null)
        return false;
    } else if (!version.equals(other.version))
      return false;
    return true;
  }
}


/**********************************************************************
 * $Log: Dependency.java,v $
 * Revision 1.7  2011/06/19 11:15:46  willuhn
 * @B BUGZILLA 1073
 *
 * Revision 1.6  2008-12-30 15:21:42  willuhn
 * @N Umstellung auf neue Versionierung
 *
 * Revision 1.5  2008/12/11 22:42:13  willuhn
 * @C doubles mit Double.compare vergleichen
 *
 * Revision 1.4  2008/12/09 16:43:32  willuhn
 * @N Getter fuer "required"
 *
 * Revision 1.3  2008/11/30 22:57:08  willuhn
 * @N Neues optionales Attribute "required", um optionale Abhaengigkeiten abbilden zu koennen
 *
 * Revision 1.2  2008/11/11 01:09:20  willuhn
 * @N getVersion
 *
 * Revision 1.1  2008/08/27 14:41:17  willuhn
 * @N Angabe der Versionsnummer von abhaengigen Plugins oder der Jameica RT
 *
 **********************************************************************/
