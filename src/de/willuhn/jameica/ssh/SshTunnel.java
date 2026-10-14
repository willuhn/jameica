/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.ssh;

import java.io.File;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Kapselt eine SSH-Verbindung inklusive lokalem Port-Forwarding via JSch.
 * 
 * <p>Die Klasse ist fuer den Einzelbenutzermodus gedacht: es wird genau
 * eine SSH-Session aufgebaut und ein lokaler TCP-Port auf einen Host/Port
 * in Sicht des SSH-Servers weitergeleitet. Damit laesst sich zum Beispiel
 * eine entfernte MySQL-Datenbank ueber einen verschluesselten SSH-Tunnel
 * ansprechen, ohne den Datenbank-Port direkt im Internet zu oeffnen.
 */
public class SshTunnel
{
  private final SshTunnelConfig config;

  private Session session = null;
  private int boundPort   = -1;

  /**
   * ct.
   * @param config die Konfiguration des Tunnels.
   */
  public SshTunnel(SshTunnelConfig config)
  {
    this.config = config;
  }

  /**
   * Liefert die Konfiguration des Tunnels.
   * @return die Konfiguration.
   */
  public SshTunnelConfig getConfig()
  {
    return this.config;
  }

  /**
   * Prueft, ob der Tunnel aktuell aufgebaut ist.
   * @return true, wenn die SSH-Session verbunden ist.
   */
  public synchronized boolean isRunning()
  {
    return this.session != null && this.session.isConnected();
  }

  /**
   * Liefert den tatsaechlich belegten lokalen TCP-Port.
   * @return der lokale TCP-Port oder {@code -1}, wenn der Tunnel nicht laeuft.
   */
  public synchronized int getLocalPort()
  {
    return this.isRunning() ? this.boundPort : -1;
  }

  /**
   * Baut den SSH-Tunnel auf und richtet das lokale Port-Forwarding ein.
   * Ist der Tunnel bereits aufgebaut, passiert nichts.
   * @return der tatsaechlich belegte lokale TCP-Port.
   * @throws Exception wenn der Tunnel nicht aufgebaut werden konnte.
   */
  public synchronized int start() throws Exception
  {
    if (this.isRunning())
      return this.boundPort;

    final SshTunnelConfig c = this.config;

    JSch jsch = new JSch();

    // known_hosts verwenden, damit der Hostkey geprueft werden kann.
    String knownHosts = c.getKnownHosts();
    if (knownHosts != null && knownHosts.length() > 0)
    {
      File f = new File(knownHosts);
      if (f.exists() && !f.isFile())
      {
        Logger.warn("known_hosts is not a regular file: " + knownHosts + ", hostkey checking may fail");
      }
      else
      {
        // JSch erlaubt auch eine noch nicht existierende Datei. Sie wird dann
        // bei Bedarf (nach Zustimmung des Benutzers) angelegt.
        try
        {
          jsch.setKnownHosts(knownHosts);
        }
        catch (Exception e)
        {
          Logger.warn("unable to read known_hosts file " + knownHosts + ": " + e.getMessage());
        }
      }
    }

    if (c.getAuthType() == SshTunnelConfig.AuthType.PUBLICKEY)
    {
      if (c.getPrivateKey() == null || c.getPrivateKey().length() == 0)
        throw new Exception(Application.getI18n().tr("Es wurde kein privater Schluessel angegeben"));

      if (c.getPassphrase() != null && c.getPassphrase().length() > 0)
        jsch.addIdentity(c.getPrivateKey(),c.getPassphrase());
      else
        jsch.addIdentity(c.getPrivateKey());
    }

    Session s = jsch.getSession(c.getSshUser(),c.getSshHost(),c.getSshPort());
    s.setUserInfo(new TunnelUserInfo(c));
    if (c.isStrictHostKeyChecking())
    {
      // Bei strikter Pruefung fragen wir den Benutzer bei unbekannten und bei
      // geaenderten Hostkeys und tragen den Schluessel bei Zustimmung ein.
      s.setHostKeyRepository(new PromptingHostKeyRepository(jsch,jsch.getHostKeyRepository()));
      s.setConfig("StrictHostKeyChecking","yes");
    }
    else
    {
      s.setConfig("StrictHostKeyChecking","no");
    }
    s.setConfig("PreferredAuthentications",
        c.getAuthType() == SshTunnelConfig.AuthType.PUBLICKEY ? "publickey" : "password,keyboard-interactive");

    // Keep-Alive, damit die Verbindung bei Idle nicht abgebaut wird.
    s.setServerAliveInterval(30000);
    s.setServerAliveCountMax(3);

    if (c.getAuthType() == SshTunnelConfig.AuthType.PASSWORD)
    {
      if (c.getPassword() == null || c.getPassword().length() == 0)
        throw new Exception(Application.getI18n().tr("Es wurde kein SSH-Passwort angegeben"));
      s.setPassword(c.getPassword());
    }

    Logger.info("connecting ssh tunnel to " + c.getSshUser() + "@" + c.getSshHost() + ":" + c.getSshPort());

    try
    {
      s.connect(c.getTimeout() > 0 ? c.getTimeout() : 15000);
    }
    catch (Exception e)
    {
      // Session bei Fehlschlag sauber schliessen.
      try { s.disconnect(); } catch (Exception ignored) { /* ignore */ }
      throw new Exception(Application.getI18n().tr("Aufbau der SSH-Verbindung fehlgeschlagen: {0}",e.getMessage()),e);
    }

    try
    {
      this.boundPort = s.setPortForwardingL(c.getLocalPort(),c.getRemoteHost(),c.getRemotePort());
      this.session   = s;
    }
    catch (Exception e)
    {
      try { s.disconnect(); } catch (Exception ignored) { /* ignore */ }
      throw new Exception(Application.getI18n().tr("Einrichten der Port-Weiterleitung fehlgeschlagen: {0}",e.getMessage()),e);
    }

    Logger.info("ssh tunnel established: 127.0.0.1:" + this.boundPort + " -> " + c.getRemoteHost() + ":" + c.getRemotePort());
    return this.boundPort;
  }

  /**
   * Baut den SSH-Tunnel ab.
   */
  public synchronized void stop()
  {
    Session s = this.session;
    this.session   = null;
    this.boundPort = -1;

    if (s == null)
      return;

    try
    {
      Logger.info("closing ssh tunnel");
      s.disconnect();
    }
    catch (Exception e)
    {
      Logger.warn("error while closing ssh tunnel: " + e.getMessage());
    }
  }

  /**
   * Liefert Benutzer-interaktive Informationen (Passwort, Passphrase und
   * die Bestaetigung unbekannter Hostkeys) fuer JSch.
   */
  private static class TunnelUserInfo implements UserInfo
  {
    private final SshTunnelConfig config;

    private TunnelUserInfo(SshTunnelConfig config)
    {
      this.config = config;
    }

    /**
     * @see com.jcraft.jsch.UserInfo#getPassphrase()
     */
    public String getPassphrase()
    {
      return this.config.getPassphrase();
    }

    /**
     * @see com.jcraft.jsch.UserInfo#getPassword()
     */
    public String getPassword()
    {
      return this.config.getPassword();
    }

    /**
     * @see com.jcraft.jsch.UserInfo#promptPassword(java.lang.String)
     */
    public boolean promptPassword(String message)
    {
      return this.config.getPassword() != null && this.config.getPassword().length() > 0;
    }

    /**
     * @see com.jcraft.jsch.UserInfo#promptPassphrase(java.lang.String)
     */
    public boolean promptPassphrase(String message)
    {
      return this.config.getPassphrase() != null && this.config.getPassphrase().length() > 0;
    }

    /**
     * @see com.jcraft.jsch.UserInfo#promptYesNo(java.lang.String)
     */
    public boolean promptYesNo(String message)
    {
      try
      {
        return Application.getCallback().askUser(message);
      }
      catch (Exception e)
      {
        Logger.error("unable to ask user for hostkey confirmation",e);
        return false;
      }
    }

    /**
     * @see com.jcraft.jsch.UserInfo#showMessage(java.lang.String)
     */
    public void showMessage(String message)
    {
      Logger.info(message);
    }
  }
}
