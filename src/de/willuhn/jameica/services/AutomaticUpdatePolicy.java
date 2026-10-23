/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details.
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.net.URL;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.plugin.Manifest;

/**
 * Trust policy for unattended plugin updates.
 */
final class AutomaticUpdatePolicy
{
  private AutomaticUpdatePolicy()
  {
  }

  /**
   * Checks whether an installed plugin explicitly pins the candidate download
   * to the same HTTPS origin.
   * @param installed installed plugin manifest.
   * @param download candidate download URL.
   * @return true if unattended installation is allowed by origin policy.
   */
  static boolean isAllowed(Manifest installed, URL download)
  {
    if (installed == null || download == null || !installed.validateHomepage())
      return false;

    try
    {
      String homepage = StringUtils.trimToNull(installed.getHomepage());
      if (homepage == null)
        return false;
      return isSameHttpsOrigin(new URL(homepage),download);
    }
    catch (Exception e)
    {
      return false;
    }
  }

  /** Checks scheme, host and effective port for a strict HTTPS origin match. */
  static boolean isSameHttpsOrigin(URL first, URL second)
  {
    if (first == null || second == null)
      return false;
    if (!"https".equalsIgnoreCase(first.getProtocol()) || !"https".equalsIgnoreCase(second.getProtocol()))
      return false;
    if (!first.getHost().equalsIgnoreCase(second.getHost()))
      return false;
    return effectivePort(first) == effectivePort(second);
  }

  private static int effectivePort(URL url)
  {
    return url.getPort() >= 0 ? url.getPort() : url.getDefaultPort();
  }
}
