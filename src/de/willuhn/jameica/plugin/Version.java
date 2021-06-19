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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.willuhn.logging.Logger;

/**
 * Kapselt eine Versionsnummer.
 */
public class Version implements Serializable, Comparable<Version>
{
  private static final long serialVersionUID = -8081873180699134869L;

  /**
   * Dummy-Version: Keine Versionsnummer.
   */
  public static final Version NONE = new Version();

  private Integer major = null;
  private Integer minor = null;
  private Integer patch = null;
  private String suffix = null;

  /**
   * ct.
   */
  public Version()
  {
  }

  /**
   * ct.
   * @param v Versionsnummer.
   */
  public Version(String v)
  {
    v = StringUtils.trimToNull(v);
    if (v == null)
      return;

    int spos = v.indexOf("-");
    if (spos > 0)
    {
      this.suffix = v.substring(spos+1);
      v = v.substring(0,spos);
    }

    List<String> parts = new ArrayList<>(Arrays.asList(v.split("\\.")));
    if (!parts.isEmpty()) this.major = this.parse(parts.remove(0));
    if (!parts.isEmpty()) this.minor = this.parse(parts.remove(0));
    if (!parts.isEmpty()) this.patch = this.parse(parts.remove(0));
  }
  
  /**
   * Parset den Versionsteil.
   * @param part der Versionsteil.
   * @return der Versionsteil oder NULL, wenn er nicht geparst werden konnte.
   */
  private Integer parse(String part)
  {
    try
    {
      return Integer.parseInt(part);
    }
    catch (Exception e)
    {
      Logger.error("unparsable version part: " + part,e);
      return null;
    }
  }

  /**
   * Liefert die Major-Versionsnummer.
   * @return Major-Versionsnummer.
   */
  public int getMajor()
  {
    return this.major != null ? this.major.intValue() : 0;
  }

  /**
   * Speichert die Major-Versionsnummer.
   * @param major Major-Versionsnummer.
   */
  public void setMajor(int major)
  {
    this.major = major;
  }

  /**
   * Liefert die Minor-Versionsnummer.
   * @return Minor-Versionsnummer.
   */
  public int getMinor()
  {
    return this.minor != null ? this.minor.intValue() : 0;
  }

  /**
   * Speichert die Minor-Versionsnummer.
   * @param minor Minor-Versionsnummer.
   */
  public void setMinor(int minor)
  {
    this.minor = minor;
  }

  /**
   * Liefert das Patch-Level.
   * @return Patch-Level.
   */
  public int getPatch()
  {
    return this.patch != null ? this.patch.intValue() : 0;
  }

  /**
   * Speichert das Patch-Level.
   * @param patch das Patch-Level.
   */
  public void setPatch(int patch)
  {
    this.patch = patch;
  }

  /**
   * Liefert einen optionalen Suffix.
   * @return optionaler Suffix.
   */
  public String getSuffix()
  {
    return this.suffix;
  }

  /**
   * Speichert einen optionalen Suffix.
   * @param suffix optionaler Suffix.
   */
  public void setSuffix(String suffix)
  {
    this.suffix = suffix;
  }
  
  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((major == null) ? 0 : major.hashCode());
    result = prime * result + ((minor == null) ? 0 : minor.hashCode());
    result = prime * result + ((patch == null) ? 0 : patch.hashCode());
    result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
    return result;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof Version))
      return false;
    Version other = (Version) obj;
    if (major == null)
    {
      if (other.major != null)
        return false;
    } else if (!major.equals(other.major))
      return false;
    if (minor == null)
    {
      if (other.minor != null)
        return false;
    } else if (!minor.equals(other.minor))
      return false;
    if (patch == null)
    {
      if (other.patch != null)
        return false;
    } else if (!patch.equals(other.patch))
      return false;
    if (suffix == null)
    {
      if (other.suffix != null)
        return false;
    } else if (!suffix.equals(other.suffix))
      return false;
    return true;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Version v)
  {
    int r = this.getMajor() - v.getMajor();
    if (r == 0)
    {
      r = this.getMinor() - v.getMinor();
      if (r == 0)
        r = this.getPatch() - v.getPatch();
    }
    return r;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(getMajor());
    sb.append(".");
    sb.append(getMinor());
    sb.append(".");
    sb.append(getPatch());
    if (getSuffix() != null && getSuffix().length() > 0)
    {
      sb.append("-");
      sb.append(getSuffix());
    }
    return sb.toString();
  }
  
  /**
   * Prueft, ob die Version die angegebene Versionsanforderung erfuellt.
   * @param dependency die Anforderung.
   * Sie kann beispielsweise als "1.1+" formuliert sein, um festzulegen, dass
   * mindestens 1.1 aber auch eine hoehere Version erlaubt ist. In dem Fall
   * liefert die Funktion true, wenn die Version in "this" 1.1 oder hoeher ist.
   * Es ist auch moeglich, eine Hoechstversion mit "-" anzugeben.
   * @return true, wenn diese Version die angegebene Anforderung erfuellt.
   */
  public boolean compliesTo(String dependency)
  {
    // Keine bestimmte Versionsnummer gefordert.
    if (dependency == null || dependency.length() == 0)
      return true;
    
    try
    {
      String cleaned = dependency.replaceAll("[\\(\\)\\-\\+]","");
      // Beim Parsen der Version muessen wir das "+"/"-" am Ende entfernen
      Version required = new Version(cleaned);

      
      // Versionsnummer ist mit einem Plus in Klammern angegeben.
      // Dann darf nur der letzte Teil der Versionsnummer, der sich vor dem "(+)"
      // groesser sein.
      if (dependency.endsWith("(+)"))
      {
        // Format 1+
        // Major-Version muss die Anforderung erfuellen
        if (required.minor == null && required.patch == null)
          return this.getMajor() >= required.getMajor();
        
        // Format 1.1+
        // Major-Version muss identisch sein
        // Minor-Version muss die Anforderung erfuellen
        if (required.patch == null)
          return this.getMajor() == required.getMajor() &&
                 this.getMinor() >= required.getMinor();

        // Format 1.1.1+
        // Major- und Minor-Version muessen identisch sein
        // Patch-Version muss die Anforderung erfuellen
        return this.getMajor() == required.getMajor() &&
               this.getMinor() == required.getMinor() &&
               this.getPatch() >= required.getPatch();
      }
      
      // Versionsnummer ist mit einem Minus in Klammern angegeben.
      // Dann darf nur der letzte Teil der Versionsnummer, der sich vor dem "(-)"
      // groesser sein.
      if (dependency.endsWith("(-)"))
      {
        // Format 1-
        // Major-Version muss die Anforderung erfuellen
        if (required.minor == null && required.patch == null)
          return this.getMajor() <= required.getMajor();
        
        // Format 1.1-
        // Major-Version muss identisch sein
        // Minor-Version muss die Anforderung erfuellen
        if (required.patch == null)
          return this.getMajor() == required.getMajor() &&
                 this.getMinor() <= required.getMinor();

        // Format 1.1.1-
        // Major- und Minor-Version muessen identisch sein
        // Patch-Version muss die Anforderung erfuellen
        return this.getMajor() == required.getMajor() &&
               this.getMinor() == required.getMinor() &&
               this.getPatch() <= required.getPatch();
      }

      int compare = this.compareTo(required);

      // Versionsnummer ist mit einem Plus ohne Klammern angegeben
      // Dann ist jede beliebige gleiche oder groessere Versionsnummer erlaubt.
      if (dependency.endsWith("+"))
        return compare >= 0;

        // Versionsnummer ist mit einem Minus ohne Klammern angegeben
        // Dann ist jede beliebige gleiche oder kleinere Versionsnummer erlaubt.
      if (dependency.endsWith("-"))
        return compare <= 0;
      
      // Kein Vorzeichen, dann muss die Versionsnummer exakt passen
      return compare == 0;
    }
    catch (Exception e)
    {
      Logger.error("invalid version number: " + dependency + " - " + e.getMessage(),e);
    }
    return false;
  }
}
