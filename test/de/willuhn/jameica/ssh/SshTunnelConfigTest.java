/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.ssh;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests der SSH-Tunnel-Konfiguration.
 */
public class SshTunnelConfigTest
{
  /**
   * Die Default-Werte muessen einen passenden Ausgangszustand liefern.
   */
  @Test
  public void defaults()
  {
    SshTunnelConfig c = new SshTunnelConfig();

    Assert.assertFalse(c.isEnabled());
    Assert.assertNull(c.getSshHost());
    Assert.assertEquals(22,c.getSshPort());
    Assert.assertNull(c.getSshUser());
    Assert.assertEquals(SshTunnelConfig.AuthType.PASSWORD,c.getAuthType());
    Assert.assertNull(c.getPassword());
    Assert.assertNull(c.getPrivateKey());
    Assert.assertNull(c.getPassphrase());
    Assert.assertNull(c.getKnownHosts());
    Assert.assertTrue(c.isStrictHostKeyChecking());
    Assert.assertEquals(0,c.getLocalPort());
    Assert.assertEquals("127.0.0.1",c.getRemoteHost());
    Assert.assertEquals(3306,c.getRemotePort());
    Assert.assertEquals(15000,c.getTimeout());
  }

  /**
   * Alle Werte muessen gesetzt und wieder ausgelesen werden koennen.
   */
  @Test
  public void settersAndGetters()
  {
    SshTunnelConfig c = new SshTunnelConfig();
    c.setEnabled(true);
    c.setSshHost("ssh.example.org");
    c.setSshPort(2222);
    c.setSshUser("tunnel");
    c.setAuthType(SshTunnelConfig.AuthType.PUBLICKEY);
    c.setPassword("geheim");
    c.setPrivateKey("/home/tunnel/.ssh/id_ed25519");
    c.setPassphrase("phrase");
    c.setKnownHosts("/home/tunnel/.ssh/known_hosts");
    c.setStrictHostKeyChecking(false);
    c.setLocalPort(13306);
    c.setRemoteHost("db.internal");
    c.setRemotePort(5432);
    c.setTimeout(3000);

    Assert.assertTrue(c.isEnabled());
    Assert.assertEquals("ssh.example.org",c.getSshHost());
    Assert.assertEquals(2222,c.getSshPort());
    Assert.assertEquals("tunnel",c.getSshUser());
    Assert.assertEquals(SshTunnelConfig.AuthType.PUBLICKEY,c.getAuthType());
    Assert.assertEquals("geheim",c.getPassword());
    Assert.assertEquals("/home/tunnel/.ssh/id_ed25519",c.getPrivateKey());
    Assert.assertEquals("phrase",c.getPassphrase());
    Assert.assertEquals("/home/tunnel/.ssh/known_hosts",c.getKnownHosts());
    Assert.assertFalse(c.isStrictHostKeyChecking());
    Assert.assertEquals(13306,c.getLocalPort());
    Assert.assertEquals("db.internal",c.getRemoteHost());
    Assert.assertEquals(5432,c.getRemotePort());
    Assert.assertEquals(3000,c.getTimeout());
  }

  /**
   * Ein {@code null}-Wert darf das Authentifizierungs-Verfahren nicht auf null setzen.
   */
  @Test
  public void setAuthTypeNullFallsBackToPassword()
  {
    SshTunnelConfig c = new SshTunnelConfig();
    c.setAuthType(SshTunnelConfig.AuthType.PUBLICKEY);
    c.setAuthType(null);
    Assert.assertEquals(SshTunnelConfig.AuthType.PASSWORD,c.getAuthType());
  }

  /**
   * Das Parsen der Authentifizierungs-Verfahren muss tolerant und
   * case-insensitiv sein. Unbekannte Werte fallen auf Passwort zurueck.
   */
  @Test
  public void parseAuthType()
  {
    Assert.assertEquals(SshTunnelConfig.AuthType.PASSWORD,SshTunnelConfig.AuthType.parse(null));
    Assert.assertEquals(SshTunnelConfig.AuthType.PASSWORD,SshTunnelConfig.AuthType.parse(""));
    Assert.assertEquals(SshTunnelConfig.AuthType.PASSWORD,SshTunnelConfig.AuthType.parse("password"));
    Assert.assertEquals(SshTunnelConfig.AuthType.PASSWORD,SshTunnelConfig.AuthType.parse("PASSWORD"));
    Assert.assertEquals(SshTunnelConfig.AuthType.PASSWORD,SshTunnelConfig.AuthType.parse("Password"));

    Assert.assertEquals(SshTunnelConfig.AuthType.PUBLICKEY,SshTunnelConfig.AuthType.parse("publickey"));
    Assert.assertEquals(SshTunnelConfig.AuthType.PUBLICKEY,SshTunnelConfig.AuthType.parse("PUBLICKEY"));
    Assert.assertEquals(SshTunnelConfig.AuthType.PUBLICKEY,SshTunnelConfig.AuthType.parse("PublicKey"));
    Assert.assertEquals(SshTunnelConfig.AuthType.PUBLICKEY,SshTunnelConfig.AuthType.parse("  publickey  "));

    Assert.assertEquals(SshTunnelConfig.AuthType.PASSWORD,SshTunnelConfig.AuthType.parse("unbekannt"));
  }
}
