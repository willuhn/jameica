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

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.UserInfo;

import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Hostkey-Repository, welches den Benutzer interaktiv fragt, wenn der Hostkey
 * eines Servers unbekannt ist oder sich geaendert hat.
 *
 * <p>Bei unbekannten Hostkeys und bei geaenderten Hostkeys wird ein Dialog
 * angezeigt. Stimmt der Benutzer zu, wird der Schluessel in die known_hosts-
 * Datei eingetragen beziehungsweise der alte Eintrag ersetzt. Bei einem
 * geaenderten Hostkey wird bewusst eine deutliche Warnung angezeigt, weil dies
 * auf einen Man-in-the-Middle-Angriff hindeuten kann. Ein Abbruch der Frage
 * gilt als Ablehnung.
 *
 * <p>Das Repository wird nur verwendet, wenn die strikte Pruefung des Hostkeys
 * aktiviert ist. Alle Aufrufe werden an ein Delegat weitergereicht, welches das
 * Lesen und Schreiben der known_hosts-Datei uebernimmt.
 */
class PromptingHostKeyRepository implements HostKeyRepository
{
  /**
   * Ein UserInfo, das das Anlegen einer noch nicht existierenden known_hosts-
   * Datei automatisch erlaubt. Es wird nur dazu verwendet, die Datei nach einer
   * bereits erteilten Zustimmung des Benutzers anzulegen.
   */
  private final static UserInfo FILE_CREATION_APPROVER = new UserInfo()
  {
    public String getPassphrase() { return null; }
    public String getPassword() { return null; }
    public boolean promptPassword(String message) { return false; }
    public boolean promptPassphrase(String message) { return false; }
    public void showMessage(String message) { Logger.info(message); }

    public boolean promptYesNo(String message)
    {
      // Der Benutzer hat dem Schluessel bereits zugestimmt. Das Anlegen der
      // known_hosts-Datei ist nur die Folge davon - daher automatisch erlauben.
      Logger.info(message);
      return true;
    }
  };

  private final JSch jsch;
  private final HostKeyRepository delegate;

  /**
   * ct.
   * @param jsch die JSch-Instanz (wird zur Berechnung der Fingerprints benoetigt).
   * @param delegate das eigentliche, dateibasierte Repository.
   */
  PromptingHostKeyRepository(JSch jsch, HostKeyRepository delegate)
  {
    this.jsch     = jsch;
    this.delegate = delegate;
  }

  /**
   * @see com.jcraft.jsch.HostKeyRepository#check(java.lang.String, byte[])
   */
  public int check(String host, byte[] key)
  {
    int result = this.delegate.check(host,key);

    if (result == OK)
      return OK;

    // Ohne hinterlegte Datei koennen wir uns die Entscheidung nicht merken.
    // Dann fragen wir lieber gar nicht erst, damit der Benutzer nicht bei
    // jedem Start erneut gefragt wird.
    if (this.delegate.getKnownHostsRepositoryID() == null)
    {
      Logger.warn("no known_hosts file configured, hostkey checking cannot remember trusted keys");
      return result;
    }

    try
    {
      HostKey newKey = new HostKey(host,key);

      if (result == NOT_INCLUDED)
      {
        if (!askUnknown(host,newKey))
          return NOT_INCLUDED;

        Logger.info("trusting new ssh hostkey for " + host + ": " + newKey.getFingerPrint(this.jsch));
        this.delegate.add(newKey,FILE_CREATION_APPROVER);
        return OK;
      }

      // result == CHANGED
      if (!askChanged(host,newKey))
        return CHANGED;

      Logger.warn("replacing changed ssh hostkey for " + host + ": " + newKey.getFingerPrint(this.jsch));
      this.delegate.remove(host,null);
      this.delegate.add(newKey,FILE_CREATION_APPROVER);
      return OK;
    }
    catch (Exception e)
    {
      Logger.error("unable to verify ssh hostkey for " + host,e);
      return result;
    }
  }

  /**
   * Fragt den Benutzer, ob er einem unbekannten Hostkey vertraut.
   * @param host der Hostname.
   * @param key der angebotene Hostkey.
   * @return true, wenn der Benutzer vertraut.
   */
  boolean askUnknown(String host, HostKey key)
  {
    I18N i18n = Application.getI18n();
    String message = i18n.tr("Der SSH-Server {0} ist unbekannt.",host) + "\n\n" +
      i18n.tr("Schluesseltyp: {0}",key.getType()) + "\n" +
      i18n.tr("Fingerabdruck: {0}",key.getFingerPrint(this.jsch)) + "\n\n" +
      i18n.tr("Wollen Sie diesem Server vertrauen und den Schluessel dauerhaft speichern?");
    return askUser(message);
  }

  /**
   * Fragt den Benutzer, ob der neue Hostkey anstelle des alten uebernommen
   * werden soll. Die Warnung ist bewusst deutlich gehalten, weil ein
   * geaenderter Hostkey auf einen Man-in-the-Middle-Angriff hindeuten kann.
   * @param host der Hostname.
   * @param key der neue Hostkey.
   * @return true, wenn der Benutzer den neuen Schluessel uebernimmt.
   */
  boolean askChanged(String host, HostKey key)
  {
    I18N i18n = Application.getI18n();
    StringBuffer sb = new StringBuffer();
    sb.append("<form>");
    sb.append("<p><span color=\"error\"><b>");
    sb.append(i18n.tr("WARNUNG: Der Hostkey des SSH-Servers hat sich geaendert!"));
    sb.append("</b></span></p>");
    sb.append("<p>").append(i18n.tr("Server: {0}",host)).append("</p>");

    sb.append("<p>").append(i18n.tr("Bisher gespeichert:")).append("<br/>");
    HostKey[] old = this.delegate.getHostKey(host,null);
    if (old != null && old.length > 0)
    {
      for (int i=0;i<old.length;++i)
      {
        sb.append("- ").append(old[i].getFingerPrint(this.jsch));
        sb.append(" (").append(old[i].getType()).append(")<br/>");
      }
    }
    else
    {
      sb.append("- ").append(i18n.tr("unbekannt")).append("<br/>");
    }
    sb.append("</p>");

    sb.append("<p>").append(i18n.tr("Aktuell angeboten:")).append("<br/>");
    sb.append("- ").append(key.getFingerPrint(this.jsch));
    sb.append(" (").append(key.getType()).append(")");
    sb.append("</p>");

    sb.append("<p><span color=\"error\">");
    sb.append(i18n.tr("Das kann auf einen Angriff (Man-in-the-Middle) hindeuten. " +
      "Vergewissern Sie sich bitte beim Server-Administrator, dass sich der Hostkey " +
      "tatsaechlich geaendert hat."));
    sb.append("</span></p>");

    sb.append("<p>");
    sb.append(i18n.tr("Wollen Sie den neuen Schluessel uebernehmen und den alten Eintrag ersetzen?"));
    sb.append("</p>");
    sb.append("</form>");

    return askUser(sb.toString());
  }

  /**
   * Zeigt dem Benutzer eine Ja/Nein-Frage an. Die Antwort wird bewusst nicht
   * gespeichert, damit insbesondere bei geaenderten Hostkeys immer erneut
   * gefragt wird.
   * @param message die anzuzeigende Nachricht.
   * @return true, wenn der Benutzer mit "Ja" geantwortet hat.
   */
  private boolean askUser(String message)
  {
    try
    {
      return Application.getCallback().askUser(message,null,false);
    }
    catch (Exception e)
    {
      Logger.error("unable to ask user about hostkey",e);
      return false;
    }
  }

  /**
   * @see com.jcraft.jsch.HostKeyRepository#add(com.jcraft.jsch.HostKey, com.jcraft.jsch.UserInfo)
   */
  public void add(HostKey hostkey, UserInfo userinfo)
  {
    this.delegate.add(hostkey,userinfo);
  }

  /**
   * @see com.jcraft.jsch.HostKeyRepository#remove(java.lang.String, java.lang.String)
   */
  public void remove(String host, String type)
  {
    this.delegate.remove(host,type);
  }

  /**
   * @see com.jcraft.jsch.HostKeyRepository#remove(java.lang.String, java.lang.String, byte[])
   */
  public void remove(String host, String type, byte[] key)
  {
    this.delegate.remove(host,type,key);
  }

  /**
   * @see com.jcraft.jsch.HostKeyRepository#getKnownHostsRepositoryID()
   */
  public String getKnownHostsRepositoryID()
  {
    return this.delegate.getKnownHostsRepositoryID();
  }

  /**
   * @see com.jcraft.jsch.HostKeyRepository#getHostKey()
   */
  public HostKey[] getHostKey()
  {
    return this.delegate.getHostKey();
  }

  /**
   * @see com.jcraft.jsch.HostKeyRepository#getHostKey(java.lang.String, java.lang.String)
   */
  public HostKey[] getHostKey(String host, String type)
  {
    return this.delegate.getHostKey(host,type);
  }
}
