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
 * Tests des Zustands-Lebenszyklus eines SSH-Tunnels.
 * 
 * <p>Der eigentliche Verbindungsaufbau benoetigt einen SSH-Server und wird
 * hier nicht getestet. Geprueft wird der Zustand vor dem Verbindungsaufbau
 * sowie das idempotente Verhalten von {@link SshTunnel#stop()}.
 */
public class SshTunnelTest
{
  /**
   * Vor dem Aufbau ist der Tunnel nicht aktiv und hat keinen lokalen Port.
   */
  @Test
  public void initialState()
  {
    SshTunnel t = new SshTunnel(new SshTunnelConfig());

    Assert.assertFalse(t.isRunning());
    Assert.assertEquals(-1,t.getLocalPort());
  }

  /**
   * Die uebergebene Konfiguration muss unveraendert zugaenglich sein.
   */
  @Test
  public void keepsConfigReference()
  {
    SshTunnelConfig c = new SshTunnelConfig();
    c.setSshHost("ssh.example.org");

    SshTunnel t = new SshTunnel(c);
    Assert.assertSame(c,t.getConfig());
    Assert.assertEquals("ssh.example.org",t.getConfig().getSshHost());
  }

  /**
   * Ein Stop ohne vorherigen Start darf keinen Fehler werfen und muss
   * den Tunnel weiterhin als inaktiv melden.
   */
  @Test
  public void stopWithoutStartIsSafe()
  {
    SshTunnel t = new SshTunnel(new SshTunnelConfig());
    t.stop();

    Assert.assertFalse(t.isRunning());
    Assert.assertEquals(-1,t.getLocalPort());
  }

  /**
   * Mehrfaches Stoppen muss idempotent sein.
   */
  @Test
  public void stopIsIdempotent()
  {
    SshTunnel t = new SshTunnel(new SshTunnelConfig());
    t.stop();
    t.stop();

    Assert.assertFalse(t.isRunning());
    Assert.assertEquals(-1,t.getLocalPort());
  }
}
