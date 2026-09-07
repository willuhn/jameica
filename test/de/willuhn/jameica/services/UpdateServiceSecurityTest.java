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

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.willuhn.jameica.plugin.Manifest;

/**
 * Tests unattended update origin policy.
 */
public class UpdateServiceSecurityTest
{
  /** Explicitly pinned downloads from the same HTTPS origin are accepted. */
  @Test
  public void acceptPinnedHttpsOrigin() throws Exception
  {
    Manifest installed = manifest("<homepage validate=\"true\">https://Updates.Example/plugin</homepage>");
    Assert.assertTrue(AutomaticUpdatePolicy.isAllowed(installed,
        new URL("https://updates.example:443/releases/plugin.zip")));
  }

  /** Opt-in remains mandatory for unattended installation. */
  @Test
  public void rejectManifestWithoutOriginPin() throws Exception
  {
    Manifest installed = manifest("<homepage>https://updates.example/plugin</homepage>");
    Assert.assertFalse(AutomaticUpdatePolicy.isAllowed(installed,
        new URL("https://updates.example/plugin.zip")));
  }

  /** Scheme, host and effective port are all part of the trusted origin. */
  @Test
  public void rejectOriginChanges() throws Exception
  {
    Manifest installed = manifest("<homepage validate=\"true\">https://updates.example/plugin</homepage>");
    Assert.assertFalse(AutomaticUpdatePolicy.isAllowed(installed,
        new URL("http://updates.example/plugin.zip")));
    Assert.assertFalse(AutomaticUpdatePolicy.isAllowed(installed,
        new URL("https://attacker.example/plugin.zip")));
    Assert.assertFalse(AutomaticUpdatePolicy.isAllowed(installed,
        new URL("https://updates.example:8443/plugin.zip")));
  }

  /** A higher-version name collision cannot outrank a trusted-origin candidate. */
  @Test
  public void rejectCrossRepositoryNameCollision() throws Exception
  {
    Manifest installed = manifest("<homepage validate=\"true\">https://publisher.example/plugin</homepage>");
    URL attacker = new URL("https://attacker.example/plugin-999.zip");
    URL publisher = new URL("https://publisher.example/plugin-2.zip");
    List<URL> candidates = Arrays.asList(attacker,publisher);
    URL selected = null;
    for (URL candidate:candidates)
    {
      if (AutomaticUpdatePolicy.isAllowed(installed,candidate))
      {
        selected = candidate;
        break;
      }
    }
    Assert.assertEquals(publisher,selected);
  }

  private static Manifest manifest(String homepage) throws Exception
  {
    String xml = "<plugin name=\"demo\" version=\"1.0\">" + homepage + "</plugin>";
    return new Manifest(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
  }
}
