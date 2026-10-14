/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.io.File;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.security.Wallet;
import de.willuhn.jameica.ssh.SshTunnel;
import de.willuhn.jameica.ssh.SshTunnelConfig;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;

/**
 * Service, der einen konfigurierbaren SSH-Tunnel mit lokalem Port-Forwarding
 * bereitstellt.
 * 
 * <p>Damit kann eine entfernte Datenbank (typischerweise MySQL/MariaDB) im
 * Einzelbenutzermodus ueber einen verschluesselten SSH-Tunnel angesprochen
 * werden. Die Verbindungsdaten werden in den Jameica-Einstellungen abgelegt,
 * die Geheimnisse (Passwort und Passphrase) ausschliesslich verschluesselt
 * im {@link Wallet} - also mit dem Master-Passwort geschuetzt.
 * 
 * <p>Die eigentliche Datenbank-Verbindung wird weiterhin vom jeweiligen
 * Plugin hergestellt. Dieses muss dazu auf den lokalen Port des Tunnels
 * (siehe {@link #getLocalPort()}) verbinden.
 */
public class SshTunnelService implements Bootable
{
  private static final Settings settings = new Settings(SshTunnelService.class);

  // Aliase der verschluesselt gespeicherten Geheimnisse im Wallet.
  private static final String SECRET_PASSWORD   = "password";
  private static final String SECRET_PASSPHRASE = "passphrase";

  private Wallet wallet    = null;
  private SshTunnel tunnel = null;

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    // SSLService ist noetig, damit das Wallet (RSAEngine) mit dem
    // Master-Passwort entschluesselt werden kann.
    return new Class[]{LogService.class,SSLService.class,MessagingService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    if (!isEnabled())
    {
      Logger.info("ssh tunnel is disabled");
      return;
    }

    try
    {
      this.start();
    }
    catch (Exception e)
    {
      Logger.error("unable to start ssh tunnel",e);
    }
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
    this.stop();
  }

  /**
   * Liefert true, wenn der Tunnel beim Start automatisch aufgebaut werden soll.
   * @return true, wenn der Tunnel aktiviert ist.
   */
  public boolean isEnabled()
  {
    return settings.getBoolean("enabled",false);
  }

  /**
   * Baut den Tunnel auf, sofern er aktiviert ist.
   * @throws Exception wenn der Tunnel nicht aufgebaut werden konnte.
   */
  public synchronized void start() throws Exception
  {
    if (!isEnabled())
    {
      Logger.info("ssh tunnel is disabled, not starting");
      return;
    }

    this.stop();

    SshTunnel t = new SshTunnel(this.getConfig());
    int port = t.start();
    this.tunnel = t;

    Logger.info("ssh tunnel started, local port " + port);
  }

  /**
   * Baut den Tunnel ab.
   */
  public synchronized void stop()
  {
    if (this.tunnel != null)
    {
      this.tunnel.stop();
      this.tunnel = null;
    }
  }

  /**
   * Baut den Tunnel neu auf.
   * @throws Exception wenn der Tunnel nicht aufgebaut werden konnte.
   */
  public synchronized void restart() throws Exception
  {
    this.stop();

    if (isEnabled())
      this.start();
  }

  /**
   * Prueft, ob der Tunnel aufgebaut ist.
   * @return true, wenn der Tunnel laeuft.
   */
  public synchronized boolean isRunning()
  {
    return this.tunnel != null && this.tunnel.isRunning();
  }

  /**
   * Liefert den lokalen TCP-Port des Tunnels.
   * @return der lokale TCP-Port oder {@code -1}, wenn der Tunnel nicht laeuft.
   */
  public synchronized int getLocalPort()
  {
    return this.tunnel == null ? -1 : this.tunnel.getLocalPort();
  }

  /**
   * Liest die aktuelle Konfiguration inklusive der entschluesselten Geheimnisse.
   * @return die aktuelle Konfiguration.
   */
  public SshTunnelConfig getConfig()
  {
    SshTunnelConfig c = new SshTunnelConfig();
    c.setEnabled(this.isEnabled());
    c.setSshHost(settings.getString("ssh.host",null));
    c.setSshPort(settings.getInt("ssh.port",22));
    c.setSshUser(settings.getString("ssh.user",null));
    c.setAuthType(SshTunnelConfig.AuthType.parse(settings.getString("ssh.auth","password")));
    c.setPrivateKey(settings.getString("ssh.keyfile",null));
    c.setPassword(this.getSecret(SECRET_PASSWORD));
    c.setPassphrase(this.getSecret(SECRET_PASSPHRASE));
    c.setKnownHosts(settings.getString("ssh.knownhosts",this.getDefaultKnownHosts()));
    c.setStrictHostKeyChecking(settings.getBoolean("ssh.stricthostkeycheck",true));
    c.setLocalPort(settings.getInt("local.port",0));
    c.setRemoteHost(settings.getString("remote.host","127.0.0.1"));
    c.setRemotePort(settings.getInt("remote.port",3306));
    c.setTimeout(settings.getInt("timeout",15000));
    return c;
  }

  /**
   * Speichert die Konfiguration und baut den Tunnel neu auf.
   * @param c die zu speichernde Konfiguration.
   * @throws Exception wenn der Tunnel nicht neu aufgebaut werden konnte.
   */
  public synchronized void save(SshTunnelConfig c) throws Exception
  {
    if (c == null)
      return;

    settings.setAttribute("enabled",c.isEnabled());
    settings.setAttribute("ssh.host",c.getSshHost());
    settings.setAttribute("ssh.port",c.getSshPort());
    settings.setAttribute("ssh.user",c.getSshUser());
    settings.setAttribute("ssh.auth",c.getAuthType().name().toLowerCase());
    settings.setAttribute("ssh.keyfile",c.getPrivateKey());
    settings.setAttribute("ssh.knownhosts",c.getKnownHosts());
    settings.setAttribute("ssh.stricthostkeycheck",c.isStrictHostKeyChecking());
    settings.setAttribute("local.port",c.getLocalPort());
    settings.setAttribute("remote.host",c.getRemoteHost());
    settings.setAttribute("remote.port",c.getRemotePort());
    settings.setAttribute("timeout",c.getTimeout());

    this.setSecret(SECRET_PASSWORD,c.getPassword());
    this.setSecret(SECRET_PASSPHRASE,c.getPassphrase());

    this.restart();
  }

  /**
   * Testet eine Konfiguration, ohne sie zu speichern.
   * Es wird ein temporaerer Tunnel aufgebaut und sofort wieder abgebaut.
   * @param c die zu testende Konfiguration.
   * @return der lokale TCP-Port, auf dem der Test erfolgreich war.
   * @throws Exception wenn der Test fehlgeschlagen ist.
   */
  public int test(SshTunnelConfig c) throws Exception
  {
    SshTunnel t = new SshTunnel(c);
    try
    {
      return t.start();
    }
    finally
    {
      t.stop();
    }
  }

  /**
   * Liefert die Einstellungen dieses Service.
   * @return die Einstellungen.
   */
  public static Settings getSettings()
  {
    return settings;
  }

  /**
   * Liefert den Default-Pfad der known_hosts-Datei.
   * @return Default-Pfad der known_hosts-Datei.
   */
  private String getDefaultKnownHosts()
  {
    return Application.getConfig().getConfigDir() + File.separator + "known_hosts";
  }

  /**
   * Liefert ein entschluesseltes Geheimnis aus dem Wallet.
   * @param alias Alias des Geheimnisses.
   * @return das Geheimnis oder {@code null}.
   */
  private String getSecret(String alias)
  {
    try
    {
      Object o = this.getWallet().get(alias);
      return o == null ? null : o.toString();
    }
    catch (Exception e)
    {
      Logger.error("unable to read secret from wallet",e);
      return null;
    }
  }

  /**
   * Speichert ein Geheimnis verschluesselt im Wallet.
   * Ein {@code null}- oder Leerwert loescht den Eintrag.
   * @param alias Alias des Geheimnisses.
   * @param value das zu speichernde Geheimnis.
   */
  private void setSecret(String alias, String value)
  {
    try
    {
      this.getWallet().set(alias,value == null || value.length() == 0 ? null : value);
    }
    catch (Exception e)
    {
      Logger.error("unable to store secret in wallet",e);
    }
  }

  /**
   * Liefert das Wallet mit den verschluesselt gespeicherten Geheimnissen.
   * @return das Wallet.
   * @throws Exception wenn das Wallet nicht geladen werden konnte.
   */
  private synchronized Wallet getWallet() throws Exception
  {
    if (this.wallet == null)
      this.wallet = new Wallet(SshTunnelService.class);
    return this.wallet;
  }
}
