/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.ext;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.FileInput;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.PasswordInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.internal.views.Settings;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.MessagingFactory;
import de.willuhn.jameica.messaging.SettingsChangedMessage;
import de.willuhn.jameica.messaging.SettingsRestoredMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.SshTunnelService;
import de.willuhn.jameica.ssh.SshTunnelConfig;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Erweitert die System-Einstellungen um die Konfiguration des SSH-Tunnels.
 */
public class SshTunnelSettingsView implements Extension
{
  private final static I18N i18n = Application.getI18n();

  private SshTunnelService service      = null;

  private CheckboxInput enabled         = null;
  private TextInput sshHost             = null;
  private IntegerInput sshPort          = null;
  private TextInput sshUser             = null;
  private CheckboxInput authPublicKey   = null;
  private PasswordInput password        = null;
  private FileInput privateKey          = null;
  private PasswordInput passphrase      = null;
  private TextInput knownHosts          = null;
  private CheckboxInput strictHostKey   = null;
  private IntegerInput localPort        = null;
  private TextInput remoteHost          = null;
  private IntegerInput remotePort       = null;

  private MessageConsumer mcChanged     = new SettingsChangedConsumer();
  private MessageConsumer mcRestored    = new SettingsRestoredConsumer();

  /**
   * Liefert den SSH-Tunnel-Service.
   * @return der SSH-Tunnel-Service.
   */
  private SshTunnelService getService()
  {
    if (this.service == null)
      this.service = Application.getBootLoader().getBootable(SshTunnelService.class);
    return this.service;
  }

  /**
   * @see de.willuhn.jameica.gui.extension.Extension#extend(de.willuhn.jameica.gui.extension.Extendable)
   */
  public void extend(Extendable extendable)
  {
    if (!(extendable instanceof Settings))
      return;

    final MessagingFactory mf = Application.getMessagingFactory();
    mf.registerMessageConsumer(this.mcChanged);
    mf.registerMessageConsumer(this.mcRestored);

    try
    {
      SshTunnelConfig c = this.getService().getConfig();

      Settings view = (Settings) extendable;
      TabGroup tab = new TabGroup(view.getTabFolder(),i18n.tr("SSH-Tunnel"),true);

      // Da wir keine echte View sind, disposen wir unsere Consumer ueber die Tabgroup.
      tab.getComposite().addDisposeListener(new DisposeListener() {
        public void widgetDisposed(DisposeEvent e)
        {
          enabled = null;
          mf.unRegisterMessageConsumer(mcChanged);
          mf.unRegisterMessageConsumer(mcRestored);
        }
      });

      tab.addText(i18n.tr("Ueber einen SSH-Tunnel mit Port-Weiterleitung kann eine entfernte " +
          "Datenbank (z.Bsp. MySQL/MariaDB) verschluesselt angesprochen werden. Der Datenbank-" +
          "Zugang selbst wird im jeweiligen Plugin auf den lokalen Port des Tunnels konfiguriert. " +
          "Passwort und Passphrase werden verschluesselt mit dem Master-Passwort gespeichert."),true);

      tab.addHeadline(i18n.tr("SSH-Verbindung"));
      this.enabled = new CheckboxInput(c.isEnabled());
      tab.addCheckbox(this.enabled,i18n.tr("SSH-Tunnel beim Start automatisch aufbauen"));

      this.sshHost = new TextInput(c.getSshHost());
      this.sshHost.setHint("<" + i18n.tr("Hostname") + ">");
      tab.addLabelPair(i18n.tr("SSH-Server"),this.sshHost);

      this.sshPort = new IntegerInput(c.getSshPort());
      this.sshPort.setMaxLength(5);
      tab.addLabelPair(i18n.tr("SSH-Port"),this.sshPort);

      this.sshUser = new TextInput(c.getSshUser());
      tab.addLabelPair(i18n.tr("Benutzername"),this.sshUser);

      this.authPublicKey = new CheckboxInput(c.getAuthType() == SshTunnelConfig.AuthType.PUBLICKEY);

      // Ein-/Ausblenden der Felder je nach Authentifizierungs-Verfahren.
      // Der Listener muss bereits vor dem Malen registriert werden, weil
      // AbstractInput die Listener erst beim paint() an die Widgets haengt.
      final Listener l = new Listener() {
        public void handleEvent(Event event)
        {
          boolean key = ((Boolean)authPublicKey.getValue()).booleanValue();
          password.setEnabled(!key);
          privateKey.setEnabled(key);
          passphrase.setEnabled(key);
        }
      };
      this.authPublicKey.addListener(l);
      tab.addCheckbox(this.authPublicKey,i18n.tr("Authentifizierung per privatem Schluessel (Public Key)"));

      this.password = new PasswordInput(c.getPassword());
      tab.addLabelPair(i18n.tr("SSH-Passwort"),this.password);

      this.privateKey = new FileInput(c.getPrivateKey());
      tab.addLabelPair(i18n.tr("Schluessel-Datei"),this.privateKey);

      this.passphrase = new PasswordInput(c.getPassphrase());
      tab.addLabelPair(i18n.tr("Passphrase"),this.passphrase);

      // Initialen Zustand der Felder setzen.
      l.handleEvent(null);

      this.knownHosts = new TextInput(c.getKnownHosts());
      tab.addLabelPair(i18n.tr("known_hosts-Datei"),this.knownHosts);

      this.strictHostKey = new CheckboxInput(c.isStrictHostKeyChecking());
      tab.addCheckbox(this.strictHostKey,i18n.tr("Hostkey strikt gegen known_hosts pruefen"));

      tab.addHeadline(i18n.tr("Port-Weiterleitung"));
      tab.addText(i18n.tr("Der lokale Port wird auf den Ziel-Host und Ziel-Port in Sicht des " +
          "SSH-Servers weitergeleitet. Ein lokaler Port von 0 waehlt automatisch einen freien Port."),true);

      this.localPort = new IntegerInput(c.getLocalPort());
      this.localPort.setMaxLength(5);
      this.localPort.setComment(i18n.tr("0 = automatisch"));
      tab.addLabelPair(i18n.tr("Lokaler Port"),this.localPort);

      this.remoteHost = new TextInput(c.getRemoteHost());
      tab.addLabelPair(i18n.tr("Ziel-Host"),this.remoteHost);

      this.remotePort = new IntegerInput(c.getRemotePort());
      this.remotePort.setMaxLength(5);
      tab.addLabelPair(i18n.tr("Ziel-Port"),this.remotePort);

      ButtonArea buttons = new ButtonArea();
      buttons.addButton(i18n.tr("Verbindung testen"),new Action() {
        public void handleAction(Object context) throws ApplicationException
        {
          testConnection();
        }
      },null,false,"network-workgroup.png");
      tab.addButtonArea(buttons);
    }
    catch (Exception e)
    {
      Logger.error("unable to extend settings with ssh tunnel options",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(
          i18n.tr("Fehler beim Anzeigen der SSH-Tunnel-Einstellungen"),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Sammelt die aktuellen Eingaben und baut daraus eine Konfiguration.
   * @return die Konfiguration.
   */
  private SshTunnelConfig collect()
  {
    SshTunnelConfig c = new SshTunnelConfig();
    c.setEnabled(((Boolean)this.enabled.getValue()).booleanValue());
    c.setSshHost(trim((String)this.sshHost.getValue()));
    c.setSshPort(intValue(this.sshPort,22));
    c.setSshUser(trim((String)this.sshUser.getValue()));
    c.setAuthType(((Boolean)this.authPublicKey.getValue()).booleanValue() ? SshTunnelConfig.AuthType.PUBLICKEY : SshTunnelConfig.AuthType.PASSWORD);
    c.setPassword(trim((String)this.password.getValue()));
    c.setPrivateKey(trim((String)this.privateKey.getValue()));
    c.setPassphrase(trim((String)this.passphrase.getValue()));
    c.setKnownHosts(trim((String)this.knownHosts.getValue()));
    c.setStrictHostKeyChecking(((Boolean)this.strictHostKey.getValue()).booleanValue());
    c.setLocalPort(intValue(this.localPort,0));
    c.setRemoteHost(trim((String)this.remoteHost.getValue()));
    c.setRemotePort(intValue(this.remotePort,3306));
    return c;
  }

  /**
   * Speichert die Einstellungen und baut den Tunnel neu auf.
   * @throws ApplicationException wenn der Tunnel nicht aufgebaut werden konnte.
   */
  private void handleStore() throws ApplicationException
  {
    if (this.enabled == null)
      return; // SSH-Tab wurde in dieser Sitzung nie gezeichnet

    try
    {
      this.getService().save(this.collect());

      if (this.getService().isRunning())
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(
            i18n.tr("SSH-Tunnel aktiv auf lokalem Port {0}",""+this.getService().getLocalPort()),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception e)
    {
      Logger.error("unable to start ssh tunnel",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(
          i18n.tr("SSH-Tunnel konnte nicht aufgebaut werden: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Setzt die SSH-Tunnel-Einstellungen zurueck.
   */
  private void handleRestore()
  {
    try
    {
      SshTunnelConfig c = new SshTunnelConfig();
      c.setEnabled(false);
      c.setSshPort(22);
      c.setRemoteHost("127.0.0.1");
      c.setRemotePort(3306);
      c.setStrictHostKeyChecking(true);
      this.getService().save(c);
    }
    catch (Exception e)
    {
      Logger.error("unable to reset ssh tunnel settings",e);
    }
  }

  /**
   * Testet die aktuell eingegebene Konfiguration.
   */
  private void testConnection()
  {
    if (this.enabled == null)
      return;

    String text = null;
    try
    {
      int port = this.getService().test(this.collect());
      text = i18n.tr("SSH-Tunnel erfolgreich aufgebaut.") + "\n" + i18n.tr("Lokaler Port: {0}",""+port);
    }
    catch (Exception e)
    {
      Logger.error("ssh tunnel test failed",e);
      text = i18n.tr("SSH-Tunnel-Test fehlgeschlagen:") + "\n" + e.getMessage();
    }

    try
    {
      Application.getCallback().notifyUser(text);
    }
    catch (Exception e)
    {
      Logger.error("unable to show test result",e);
    }
  }

  /**
   * Trimmt den Wert.
   * @param s der Wert.
   * @return der getrimmte Wert oder {@code null}.
   */
  private String trim(String s)
  {
    if (s == null)
      return null;
    s = s.trim();
    return s.length() == 0 ? null : s;
  }

  /**
   * Liefert den Integer-Wert des Eingabefeldes.
   * @param input das Eingabefeld.
   * @param defaultValue Default-Wert.
   * @return der Wert.
   */
  private int intValue(IntegerInput input, int defaultValue)
  {
    Object o = input.getValue();
    return o instanceof Integer ? ((Integer)o).intValue() : defaultValue;
  }

  /**
   * Wird benachrichtigt, wenn die Jameica-Einstellungen gespeichert wurden.
   */
  private class SettingsChangedConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{SettingsChangedMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      handleStore();
    }
  }

  /**
   * Wird benachrichtigt, wenn die Einstellungen zurueckgesetzt wurden.
   */
  private class SettingsRestoredConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{SettingsRestoredMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      handleRestore();
    }
  }
}
