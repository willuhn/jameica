/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details.
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the multicast lookup policy.
 */
public class LookupServiceTest
{
  /**
   * Plaintext TCP services must not be selected through unauthenticated discovery.
   */
  @Test
  public void useOnlyExplicitEndpointForCanonicalLegacyLookup()
  {
    Assert.assertEquals("archive.example:9099",LookupService.resolveTcpLookup(ArchiveServerEndpoint.LOOKUP_NAME,"archive.example:9099"));
    Assert.assertEquals("[2001:db8::1]:9099",LookupService.resolveTcpLookup(ArchiveServerEndpoint.LOOKUP_NAME,"[2001:db8::1]:9099"));
    Assert.assertNull(LookupService.resolveTcpLookup(ArchiveServerEndpoint.LOOKUP_NAME,null));
  }

  /**
   * Case, whitespace, aliases, and unrelated TCP names must not become
   * compatibility lookups.
   */
  @Test
  public void rejectTcpLookupVariants()
  {
    String[] invalid = new String[]{
        ArchiveServerEndpoint.LOOKUP_NAME.toUpperCase(),
        " " + ArchiveServerEndpoint.LOOKUP_NAME,
        ArchiveServerEndpoint.LOOKUP_NAME + " ",
        "TCP :de.willuhn.jameica.messaging.Plugin.connector.tcp",
        "t c p:de.willuhn.jameica.messaging.Plugin.connector.tcp",
        "\u00a0tcp:de.willuhn.jameica.messaging.Plugin.connector.tcp",
        "tcp:de.willuhn.jameica.messaging.Plugin.connector.other",
        "tcp://archive.example:9099"
    };
    for (String name:invalid)
    {
      Assert.assertTrue(ArchiveServerEndpoint.isTcpLookup(name));
      Assert.assertNull(LookupService.resolveTcpLookup(name,"archive.example:9099"));
    }
  }
}
