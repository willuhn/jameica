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

import java.util.Locale;

/**
 * Konfiguration fuer einen SSH-Tunnel mit lokalem Port-Forwarding.
 */
public class SshTunnelConfig
{
  /**
   * Authentifizierungs-Verfahren.
   */
  public enum AuthType
  {
    /**
     * Authentifizierung via Passwort.
     */
    PASSWORD,

    /**
     * Authentifizierung via privatem Schluessel.
     */
    PUBLICKEY;

    /**
     * Parst den Namen des Verfahrens.
     * @param s Name des Verfahrens.
     * @return das Verfahren. Default ist {@link #PASSWORD}.
     */
    public static AuthType parse(String s)
    {
      if (s == null)
        return PASSWORD;

      try
      {
        return valueOf(s.trim().toUpperCase(Locale.ENGLISH));
      }
      catch (Exception e)
      {
        return PASSWORD;
      }
    }
  }

  private boolean enabled               = false;

  private String sshHost                = null;
  private int sshPort                   = 22;
  private String sshUser                = null;
  private AuthType authType             = AuthType.PASSWORD;

  // Geheimnisse. Werden ausschliesslich verschluesselt im Wallet abgelegt.
  private String password               = null;
  private String privateKey             = null;
  private String passphrase             = null;

  private String knownHosts             = null;
  private boolean strictHostKeyChecking = true;

  // Port-Weiterleitung. localPort 0 bedeutet: freien Port automatisch waehlen.
  private int localPort                 = 0;
  private String remoteHost             = "127.0.0.1";
  private int remotePort                = 3306;

  private int timeout                   = 15000;

  /**
   * @return true, wenn der Tunnel beim Start automatisch aufgebaut werden soll.
   */
  public boolean isEnabled()
  {
    return this.enabled;
  }

  /**
   * @param enabled true, wenn der Tunnel beim Start automatisch aufgebaut werden soll.
   */
  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
  }

  /**
   * @return Hostname/IP des SSH-Servers.
   */
  public String getSshHost()
  {
    return this.sshHost;
  }

  /**
   * @param sshHost Hostname/IP des SSH-Servers.
   */
  public void setSshHost(String sshHost)
  {
    this.sshHost = sshHost;
  }

  /**
   * @return TCP-Port des SSH-Servers.
   */
  public int getSshPort()
  {
    return this.sshPort;
  }

  /**
   * @param sshPort TCP-Port des SSH-Servers.
   */
  public void setSshPort(int sshPort)
  {
    this.sshPort = sshPort;
  }

  /**
   * @return Benutzername am SSH-Server.
   */
  public String getSshUser()
  {
    return this.sshUser;
  }

  /**
   * @param sshUser Benutzername am SSH-Server.
   */
  public void setSshUser(String sshUser)
  {
    this.sshUser = sshUser;
  }

  /**
   * @return das Authentifizierungs-Verfahren.
   */
  public AuthType getAuthType()
  {
    return this.authType;
  }

  /**
   * @param authType das Authentifizierungs-Verfahren.
   */
  public void setAuthType(AuthType authType)
  {
    this.authType = authType == null ? AuthType.PASSWORD : authType;
  }

  /**
   * @return das SSH-Passwort oder {@code null}.
   */
  public String getPassword()
  {
    return this.password;
  }

  /**
   * @param password das SSH-Passwort.
   */
  public void setPassword(String password)
  {
    this.password = password;
  }

  /**
   * @return Pfad zum privaten Schluessel oder {@code null}.
   */
  public String getPrivateKey()
  {
    return this.privateKey;
  }

  /**
   * @param privateKey Pfad zum privaten Schluessel.
   */
  public void setPrivateKey(String privateKey)
  {
    this.privateKey = privateKey;
  }

  /**
   * @return die Passphrase des privaten Schluessels oder {@code null}.
   */
  public String getPassphrase()
  {
    return this.passphrase;
  }

  /**
   * @param passphrase die Passphrase des privaten Schluessels.
   */
  public void setPassphrase(String passphrase)
  {
    this.passphrase = passphrase;
  }

  /**
   * @return Pfad zur known_hosts-Datei oder {@code null}.
   */
  public String getKnownHosts()
  {
    return this.knownHosts;
  }

  /**
   * @param knownHosts Pfad zur known_hosts-Datei.
   */
  public void setKnownHosts(String knownHosts)
  {
    this.knownHosts = knownHosts;
  }

  /**
   * @return true, wenn der Hostkey strikt gegen known_hosts geprueft werden soll.
   */
  public boolean isStrictHostKeyChecking()
  {
    return this.strictHostKeyChecking;
  }

  /**
   * @param strictHostKeyChecking true, wenn der Hostkey strikt geprueft werden soll.
   */
  public void setStrictHostKeyChecking(boolean strictHostKeyChecking)
  {
    this.strictHostKeyChecking = strictHostKeyChecking;
  }

  /**
   * @return der lokale TCP-Port. 0 bedeutet: automatisch waehlen.
   */
  public int getLocalPort()
  {
    return this.localPort;
  }

  /**
   * @param localPort der lokale TCP-Port. 0 bedeutet: automatisch waehlen.
   */
  public void setLocalPort(int localPort)
  {
    this.localPort = localPort;
  }

  /**
   * @return Ziel-Host der Weiterleitung (Sicht des SSH-Servers).
   */
  public String getRemoteHost()
  {
    return this.remoteHost;
  }

  /**
   * @param remoteHost Ziel-Host der Weiterleitung (Sicht des SSH-Servers).
   */
  public void setRemoteHost(String remoteHost)
  {
    this.remoteHost = remoteHost;
  }

  /**
   * @return Ziel-Port der Weiterleitung (Sicht des SSH-Servers).
   */
  public int getRemotePort()
  {
    return this.remotePort;
  }

  /**
   * @param remotePort Ziel-Port der Weiterleitung (Sicht des SSH-Servers).
   */
  public void setRemotePort(int remotePort)
  {
    this.remotePort = remotePort;
  }

  /**
   * @return Verbindungs-Timeout in Millisekunden.
   */
  public int getTimeout()
  {
    return this.timeout;
  }

  /**
   * @param timeout Verbindungs-Timeout in Millisekunden.
   */
  public void setTimeout(int timeout)
  {
    this.timeout = timeout;
  }
}
