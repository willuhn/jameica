/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details.
 *
 **********************************************************************/

package de.willuhn.jameica.transport;

import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests redirect origin comparison.
 */
public class HttpTransportSecurityTest
{
  /** Default ports and case-insensitive hosts describe the same origin. */
  @Test
  public void acceptSameOrigin() throws Exception
  {
    Assert.assertTrue(HttpTransport.isSameOrigin(
        new URL("https://Updates.Example/start"),new URL("https://updates.example:443/final")));
  }

  /** Redirects may not change protocol, host or effective port. */
  @Test
  public void rejectOriginChanges() throws Exception
  {
    URL source = new URL("https://updates.example/start");
    Assert.assertFalse(HttpTransport.isSameOrigin(source,new URL("http://updates.example/final")));
    Assert.assertFalse(HttpTransport.isSameOrigin(source,new URL("https://attacker.example/final")));
    Assert.assertFalse(HttpTransport.isSameOrigin(source,new URL("https://updates.example:8443/final")));
  }
}
