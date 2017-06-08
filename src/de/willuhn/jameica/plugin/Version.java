/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.plugin;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
  public final static Version NONE = new Version();

  private static final Pattern SINGLE  = Pattern.compile("^(\\d+)$");
  private static final Pattern OLD     = Pattern.compile("^(\\d+)\\.(\\d+)$");
  private static final Pattern PATTERN = Pattern.compile("^v?(\\d+)\\.(\\d+)\\.(\\d+)-?(.*)$");

  boolean oldVersion = false;

  private int major     = 0;
  private int minor     = 0;
  private int patch     = 0;
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
    if (v != null && v.length() > 0)
    {
      final Matcher mSin = SINGLE.matcher(v);
      final Matcher mOld = OLD.matcher(v);
      final Matcher mNew = PATTERN.matcher(v);
      if (mSin.matches())
      {
        this.oldVersion = true;
        this.major = Integer.parseInt(mSin.group(1));
      }
      if (mOld.matches())
      {
        this.oldVersion = true;
        this.major = Integer.parseInt(mOld.group(1));
        this.minor = Integer.parseInt(mOld.group(2));
      }
      else if (mNew.matches())
      {
        this.major = Integer.parseInt(mNew.group(1));
        this.minor = Integer.parseInt(mNew.group(2));
        this.patch = Integer.parseInt(mNew.group(3));
        this.suffix = mNew.group(4);
      }
      else
      {
        Logger.warn("unparsable version number: " + v);
      }
    }
  }

  /**
   * Liefert die Major-Versionsnummer.
   * @return Major-Versionsnummer.
   */
  public int getMajor()
  {
    return this.major;
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
    return this.minor;
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
    return this.patch;
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
   * Liefert true, wenn es sich um eine 3-stellige Versionsnummer mit Patch-Level handelt.
   * @return true, wenn es sich um eine 3-stellige Versionsnummer mit Patch-Level handelt.
   */
  public boolean hasPatchLevel()
  {
    return !this.oldVersion;
  }

  /**
   * @see java.lang.Object#hashCode()
   * Generiert von Eclipse.
   */
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.major;
    result = prime * result + this.minor;
    result = prime * result + this.patch;
    result = prime * result
        + ((this.suffix == null) ? 0 : this.suffix.hashCode());
    return result;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   * Generiert von Eclipse.
   */
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Version other = (Version) obj;
    if (this.major != other.major)
      return false;
    if (this.minor != other.minor)
      return false;
    if (this.patch != other.patch)
      return false;
    if (this.suffix == null)
    {
      if (other.suffix != null)
        return false;
    }
    else if (!this.suffix.equals(other.suffix))
      return false;
    return true;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Version v)
  {
    int r = this.major - v.major;
    if (r == 0)
    {
      r = this.minor - v.minor;
      if (r == 0)
        r = this.patch - v.patch;
    }
    return r;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(getMajor());
    sb.append(".");
    sb.append(getMinor());
    if (!this.oldVersion)
    {
      sb.append(".");
      sb.append(getPatch());
      if (getSuffix() != null && getSuffix().length() > 0)
      {
        sb.append("-");
        sb.append(getSuffix());
      }
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
      Version required = new Version(dependency.replaceAll("[+-]",""));

      int compare = this.compareTo(required);
      
      // Versionsnummer ist mit einem Minus angegeben.
      // also darf hoechstens die angegebene Versionsnummer vorhanden sein
      if (dependency.endsWith("-"))
        return compare <= 0;
      
      // Versionsnummer mit Plus, also muss die vorhandene Version
      // gleicher ODER groesser sein
      if (dependency.endsWith("+"))
        return compare >= 0;

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
