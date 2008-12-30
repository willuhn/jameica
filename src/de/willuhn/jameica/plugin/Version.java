/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/Version.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/12/30 14:43:20 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
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

  private static final Pattern PATTERN = Pattern.compile("^v?(\\d+)\\.(\\d+)\\.(\\d+)-?(.*)$");

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
    final Matcher m = PATTERN.matcher(v);
    if (!m.matches())
    {
      Logger.warn("unparsable version number: " + v);
    }
    else
    {
      this.major = Integer.parseInt(m.group(1));
      this.minor = Integer.parseInt(m.group(2));
      this.patch = Integer.parseInt(m.group(3));
      this.suffix = m.group(4);
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
   * @param major Minor-Versionsnummer.
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
    } else if (!this.suffix.equals(other.suffix))
      return false;
    return true;
  }

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
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(getMajor()); sb.append(".");
    sb.append(getMinor()); sb.append(".");
    sb.append(getPatch());
    if (getSuffix() != null)
    {
      sb.append("-");
      sb.append(getSuffix());
    }
    return sb.toString();
  }
}


/**********************************************************************
 * $Log: Version.java,v $
 * Revision 1.1  2008/12/30 14:43:20  willuhn
 * @N Versionsobjekt
 *
 **********************************************************************/
