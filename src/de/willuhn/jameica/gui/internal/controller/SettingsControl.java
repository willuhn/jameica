/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.ColorInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.internal.parts.CertificateList;
import de.willuhn.jameica.gui.internal.parts.PluginDetailPart.Type;
import de.willuhn.jameica.gui.internal.parts.PluginListPart;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.messaging.PluginCacheMessageConsumer;
import de.willuhn.jameica.messaging.SettingsChangedMessage;
import de.willuhn.jameica.messaging.SettingsRestoredMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.SystrayService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BootstrapSettings;
import de.willuhn.jameica.system.Config;
import de.willuhn.jameica.system.Customizing;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Einstellungen unter <i>Datei-&gt;Einstellungen</i>
 */
public class SettingsControl extends AbstractControl
{
	private final static I18N i18n = Application.getI18n();

  // System
  private Input logLevel;
  private TextInput proxyHost;
  private TextInput httpsProxyHost;
  private IntegerInput proxyPort;
  private IntegerInput httpsProxyPort;
  private CheckboxInput systemProxy;
  private CheckboxInput askWorkDir;
  private SelectInput locale;
  
  private TablePart certs;
  private CheckboxInput trustJavaCerts;

  // Plugins
  private Part plugins;

  // Look & Feel
	private Input colorError;
	private Input colorSuccess;
  private Input colorMandatoryBG;
  private CheckboxInput mandatoryLabel;
  private CheckboxInput randomSplash;
  private CheckboxInput systray;
  private CheckboxInput minimizeToSystray;
	
  /**
   * ct.
   * @param view
   */
  public SettingsControl(AbstractView view)
  {
    super(view);
  }

  /**
   * Liefert ein Auswahl-Feld fuer den Log-Level.
   * @return Auswahl-Feld fuer das Log-Level.
   */
  public Input getLogLevel()
  {
    if (this.logLevel != null)
      return this.logLevel;

    String[] levels = new String[]
    {
      Level.TRACE.getName(),
      Level.DEBUG.getName(),
      Level.INFO.getName(),
      Level.WARN.getName(),
      Level.ERROR.getName()
    };
    logLevel = new SelectInput(levels,Logger.getLevel().getName());
    return logLevel;
  }

  /**
   * Liefert ein Eingabefeld fuer die Definition des Proxy-Hosts.
   * @return Eingabefeld fuer Proxy.
   */
  public Input getProxyHost()
  {
    if (this.proxyHost != null)
      return this.proxyHost;
    
    this.proxyHost = new TextInput(Application.getConfig().getProxyHost());
    this.proxyHost.setName(i18n.tr("HTTP-Proxy"));
    this.proxyHost.setHint("<" + i18n.tr("Hostname") + ">");
    return this.proxyHost;
  }

  /**
   * Liefert ein Eingabefeld fuer die TCP-Portnummer des Proxys.
   * @return Eingabefeld fuer die Proxy-Portnummer.
   */
  public IntegerInput getProxyPort()
  {
    if (this.proxyPort != null)
      return this.proxyPort;
    
    this.proxyPort = new IntegerInput(Application.getConfig().getProxyPort());
    this.proxyPort.setMaxLength(5);
    this.proxyPort.setName("");
    this.proxyPort.setHint("<" + i18n.tr("TCP-Port") + ">");
    this.proxyPort.setComment(Application.getI18n().tr("freilassen, wenn nicht gew�nscht"));
    return this.proxyPort;
  }
  
  /**
   * Liefert ein Eingabefeld fuer die Definition des HTTPS-Proxy-Hosts.
   * @return Eingabefeld fuer den HTTPS-Proxy.
   */
  public TextInput getHttpsProxyHost()
  {
    if (this.httpsProxyHost != null)
      return this.httpsProxyHost;
    
    this.httpsProxyHost = new TextInput(Application.getConfig().getHttpsProxyHost());
    this.httpsProxyHost.setName(i18n.tr("HTTPS-Proxy"));
    this.httpsProxyHost.setHint("<" + i18n.tr("Hostname") + ">");
    return this.httpsProxyHost;
  }

  /**
   * Liefert ein Eingabefeld fuer die TCP-Portnummer des HTTPS-Proxys.
   * @return Eingabefeld fuer die Proxy-Portnummer des HTTPS-Proxy.
   */
  public IntegerInput getHttpsProxyPort()
  {
    if (this.httpsProxyPort != null)
      return this.httpsProxyPort;
    
    this.httpsProxyPort = new IntegerInput(Application.getConfig().getHttpsProxyPort());
    this.httpsProxyPort.setMaxLength(5);
    this.httpsProxyPort.setName("");
    this.httpsProxyPort.setHint("<" + i18n.tr("TCP-Port") + ">");
    this.httpsProxyPort.setComment(Application.getI18n().tr("freilassen, wenn nicht gew�nscht"));
    return this.httpsProxyPort;
  }

  /**
   * Liefert die Checkbox zum Aktivieren der System-Proxy-Einstellungen.
   * @return Checkbox.
   */
  public CheckboxInput getUseSystemProxy()
  {
    if (this.systemProxy == null)
    {
      this.systemProxy = new CheckboxInput(Application.getConfig().getUseSystemProxy());
      final Listener l = new Listener()
      {
        public void handleEvent(Event event)
        {
          try
          {
            boolean value = ((Boolean)systemProxy.getValue()).booleanValue();
            getProxyHost().setEnabled(!value);
            getProxyPort().setEnabled(!value);
            getHttpsProxyHost().setEnabled(!value);
            getHttpsProxyPort().setEnabled(!value);
          }
          catch (Exception e)
          {
            Logger.error("unable to apply settings",e);
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim �bernehmen der Proxy-Einstellungen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
          }
        }
      };
      this.systemProxy.addListener(l);
      
      // einmal initial ausloesen
      l.handleEvent(null);
    }
    return this.systemProxy;
  }
  
  /**
   * Liefert eine Checkbox, mit der eingestellt werden kann, ob beim Start nach dem
   * Benutzer-Ordner gefragt werden soll. Die Option wird nur eingeblendet, wenn
   * der User die Option "kuenftig nicht mehr fragen" im WorkdirChooser aktiviert hat,
   * da er sonst keine komfortable Moeglichkeit mehr hat, die Auswahl rueckgaengig
   * zu machen.
   * @return die Checkbox.
   */
  public CheckboxInput getAskWorkdir()
  {
    if (this.askWorkDir == null)
    {
      this.askWorkDir = new CheckboxInput(BootstrapSettings.getAskWorkdir());
      this.askWorkDir.setName(i18n.tr("Zu verwendenden Benutzer-Ordner bei Start ausw�hlen"));
    }
    return this.askWorkDir;
  }
  
  /**
   * Liefert eine Auswahlbox fuer die Sprache.
   * @return Auswahlbox fuer die Sprache.
   */
  public SelectInput getLocale()
  {
    if (this.locale != null)
      return this.locale;
    
    final List<Locale> list = new ArrayList<Locale>();
    list.add(Locale.GERMANY);
    list.add(Locale.ENGLISH);
    
    final Locale current = Application.getConfig().getLocale();
    final Locale selected = Objects.equals(current.getLanguage(),Locale.ENGLISH.getLanguage()) ? Locale.ENGLISH : Locale.GERMAN;
    this.locale = new SelectInput(list,selected);
    this.locale.setAttribute("displayName");
    
    return this.locale;
  }
  
  /**
   * Liefert eine Tabelle mit den installierten Zertifikaten.
   * @return Tabelle mit einer Liste der installierten Zertifikate.
   * @throws Exception
   */
  public Part getCertificates() throws Exception
  {
    if (this.certs != null)
      return this.certs;
    this.certs = new CertificateList();
    return this.certs;
  }
  
  /**
   * Liefert eine Checkbox, mit der festgelegt werden kann, ob den CA-Zertifikaten von Java vertraut werden soll.
   * @return Checkbox.
   */
  public CheckboxInput getTrustJavaCerts()
  {
    if (this.trustJavaCerts != null)
      return this.trustJavaCerts;
    this.trustJavaCerts = new CheckboxInput(Application.getConfig().getTrustJavaCerts());
    this.trustJavaCerts.setName(i18n.tr("Den Aussteller-Zertifikaten von Java vertrauen"));
    return this.trustJavaCerts;
  }

  /**
   * Liefert die Plugin-Verwaltung.
   * @return Liste der Plugins.
   */
  public Part getPlugins()
  {
    if (this.plugins != null)
      return this.plugins;
    Type t = PluginCacheMessageConsumer.getCache().size() > 0 ? Type.INSTALLED : Type.AVAILABLE;
    this.plugins = new PluginListPart(t);
    return this.plugins;
  }

  /**
   * Auswahlfeld.
   * @return Auswahl-Feld.
   */
  public Input getColorMandatoryBG()
  {
    if (colorMandatoryBG != null)
      return colorMandatoryBG;
    colorMandatoryBG = new ColorInput(Color.MANDATORY_BG.getSWTColor(),false);
    return colorMandatoryBG;
  }

	/**
	 * Auswahlfeld.
   * @return Auswahl-Feld.
	 */
	public Input getColorError()
	{
		if (colorError != null)
			return colorError;
		colorError = new ColorInput(Color.ERROR.getSWTColor(),true);
		return colorError;
	}

	/**
	 * Auswahlfeld.
   * @return Auswahl-Feld.
	 */
	public Input getColorSuccess()
	{
		if (colorSuccess != null)
			return colorSuccess;
		colorSuccess = new ColorInput(Color.SUCCESS.getSWTColor(),true);
		return colorSuccess;
	}
	
	/**
	 * Liefert eine Checkbox, mit der eingestellt werden kann, ob die Anwendung in das Systray minimiert werden soll.
	 * @return eine Checkbox, mit der eingestellt werden kann, ob die Anwendung in das Systray minimiert werden soll.
	 */
	public Input getMinimizeToSystray()
	{
	  if (this.minimizeToSystray != null)
	    return this.minimizeToSystray;
	  
	  final SystrayService service = Application.getBootLoader().getBootable(SystrayService.class);
	  this.minimizeToSystray = new CheckboxInput(service.isMinimizeToSystray());
	  this.minimizeToSystray.setName(i18n.tr("Fenster beim Minimieren/Schlie�en in System-Tray verschieben"));
	  return this.minimizeToSystray;
	}
  
  /**
   * Liefert eine Checkbox, mit der eingestellt werden kann, ob das Systray-Symbol angezeigt werden soll.
   * @return eine Checkbox, mit der eingestellt werden kann, ob das Systray-Symbol angezeigt werden soll.
   */
  public Input getSystray()
  {
    if (this.systray != null)
      return this.systray;
    
    final SystrayService service = Application.getBootLoader().getBootable(SystrayService.class);
    this.systray = new CheckboxInput(service.isEnabled());
    this.systray.setName(i18n.tr("Symbol im System-Tray anzeigen"));
    
    final Listener l = new Listener() {
      
      @Override
      public void handleEvent(Event event)
      {
        final boolean enabled = ((Boolean)systray.getValue()).booleanValue();
        getMinimizeToSystray().setEnabled(enabled);
      }
    };
    this.systray.addListener(l);
    l.handleEvent(null);
    return this.systray;
  }
	
  /**
   * Liefert eine Checkbox, mit der konfiguriert werden kann, ob auch die Labels vor Pflichtfeldern rot gefaerbt werden.
   * @return Checkbox.
   */
  public CheckboxInput getLabelMandatory()
  {
    if (this.mandatoryLabel != null)
      return this.mandatoryLabel;
    this.mandatoryLabel = new CheckboxInput(Application.getConfig().getMandatoryLabel());
    return this.mandatoryLabel;
  }

  /**
   * Liefert eine Checkbox, mit der konfiguriert werden kann, ob ein zufaelliger Splashscreen angezeigt werden soll.
   * @return Checkbox.
   */
  public CheckboxInput getRandomSplash()
  {
    if (this.randomSplash != null)
      return this.randomSplash;
    this.randomSplash = new CheckboxInput(Customizing.SETTINGS.getBoolean("application.splashscreen.random",false));
    return this.randomSplash;
  }

  /**
   * Speichert die Einstellungen.
   */
  public void handleStore()
  {

  	try
    {
  	  Config config = Application.getConfig();
  	  
      boolean restartNeeded = false;

      // System
      config.setLoglevel((String)getLogLevel().getValue());
      
      BootstrapSettings.setAskWorkdir(((Boolean)getAskWorkdir().getValue()).booleanValue());

      final Locale l = (Locale) this.getLocale().getValue();
      restartNeeded |= !Application.getConfig().getLocale().equals(l);
      config.setLocale(l);
      
      restartNeeded |= getProxyPort().hasChanged();
      Integer proxyPort = (Integer) getProxyPort().getValue();
      config.setProxyPort(proxyPort == null ? -1 : proxyPort.intValue());

      restartNeeded |= getProxyHost().hasChanged();
      config.setProxyHost((String)getProxyHost().getValue());

      restartNeeded |= getHttpsProxyPort().hasChanged();
      proxyPort = (Integer) getHttpsProxyPort().getValue();
      config.setHttpsProxyPort(proxyPort == null ? -1 : proxyPort.intValue());

      restartNeeded |= getHttpsProxyHost().hasChanged();
      config.setHttpsProxyHost((String)getHttpsProxyHost().getValue());
      
      restartNeeded |= getUseSystemProxy().hasChanged();
      config.setUseSystemProxy(((Boolean)getUseSystemProxy().getValue()).booleanValue());
      
      restartNeeded |= getTrustJavaCerts().hasChanged();
      config.setTrustJavaCerts(((Boolean)getTrustJavaCerts().getValue()).booleanValue());
      
      // Look & Feel
      config.setMandatoryLabel(((Boolean)getLabelMandatory().getValue()).booleanValue());
      Customizing.SETTINGS.setAttribute("application.splashscreen.random",((Boolean)getRandomSplash().getValue()).booleanValue());
			Color.ERROR.setSWTColor((org.eclipse.swt.graphics.Color)getColorError().getValue());
			Color.SUCCESS.setSWTColor((org.eclipse.swt.graphics.Color)getColorSuccess().getValue());
      Color.MANDATORY_BG.setSWTColor((org.eclipse.swt.graphics.Color)getColorMandatoryBG().getValue());
      
      final SystrayService systray = Application.getBootLoader().getBootable(SystrayService.class);
      systray.setEnabled(((Boolean)getSystray().getValue()).booleanValue());
      systray.setMinimizeToSystray(((Boolean)getMinimizeToSystray().getValue()).booleanValue());
      
      Application.getMessagingFactory().sendSyncMessage(new SettingsChangedMessage());
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Einstellungen gespeichert."),StatusBarMessage.TYPE_SUCCESS));

      if (restartNeeded)
        Application.getCallback().notifyUser(i18n.tr("Bitte starten Sie Jameica neu, damit alle �nderungen wirksam werden."));
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (Exception e)
    {
    	Logger.error("error while writing config",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Speichern der Einstellungen."),StatusBarMessage.TYPE_ERROR));
    }
  	
  }

  /**
   * Setzt die Einstellungen zurueck.
   */
  public void handleRestore()
  {
  	try {
  		YesNoDialog prompt = new YesNoDialog(YesNoDialog.POSITION_CENTER);
  		prompt.setTitle(i18n.tr("Sicher?"));
  		prompt.setText(i18n.tr("Alle Einstellungen werden auf die Standard-Werte zur�ckgesetzt"));
  		if (!((Boolean) prompt.open()).booleanValue())
  			return;

			Color.COMMENT.reset();
			Color.ERROR.reset();
			Color.SUCCESS.reset();
			Color.LINK.reset();
			Color.LINK_ACTIVE.reset();
      Application.getConfig().setRmiPort(Config.RMI_DEFAULT_PORT);
      Application.getConfig().setLoglevel(Level.INFO.getName());
      Application.getConfig().setRmiSSL(true);
      Application.getConfig().setProxyHost(null);
      Application.getConfig().setProxyPort(-1);

      Application.getMessagingFactory().sendSyncMessage(new SettingsRestoredMessage());
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Einstellungen zur�ckgesetzt."),StatusBarMessage.TYPE_SUCCESS));
			new de.willuhn.jameica.gui.internal.action.Settings().handleAction(null);
  	}
    catch (OperationCanceledException oce)
    {
      Logger.info(oce.getMessage());
      return;
    }
  	catch (Exception e)
  	{
  		Logger.error("error while restoring settings",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Zur�cksetzen der Einstellungen."),StatusBarMessage.TYPE_ERROR));
  	}
  	
  }
}


/**********************************************************************
 * $Log: SettingsControl.java,v $
 * Revision 1.39  2012/02/23 22:03:36  willuhn
 * @N wenn der User im Workdir-Chooser die Option "kuenftig nicht mehr anzeigen" aktiviert hat, kann er die Einstellung jetzt unter Datei->Einstellungen wieder rueckgaengig machen. Es gab sonst keine komfortable Moeglichkeit, den Dialog wieder "hervorzuholen"
 *
 * Revision 1.38  2011-06-27 17:51:43  willuhn
 * @N Man kann sich jetzt die Liste der von Java bereits mitgelieferten Aussteller-Zertifikate unter Datei->Einstellungen anzeigen lassen - um mal einen Ueberblick zu kriegen, wem man so eigentlich alles blind vertraut ;)
 * @N Mit der neuen Option "Aussteller-Zertifikaten von Java vertrauen" kann man die Vertrauensstellung zu diesen Zertifikaten deaktivieren - dann muss der User jedes Zertifikate explizit bestaetigen - auch wenn Java die CA kennt
 *
 * Revision 1.37  2011-06-02 12:15:16  willuhn
 * @B Das Handling beim Update war noch nicht sauber
 *
 * Revision 1.36  2011-06-01 17:35:58  willuhn
 * @N Ergonomischere Verwaltung der Plugins
 *
 * Revision 1.35  2011-05-11 10:27:25  willuhn
 * @N OCE fangen
 *
 * Revision 1.34  2011-05-03 16:45:20  willuhn
 * @R Locale nicht mehr ueber GUI aenderbar - hat eh keiner verwendet
 * @R Style nicht mehr aenderbar - der Flatstyle war eh nicht mehr zeitgemaess und rendere auf aktuellen OS sowieso haesslich
 *
 * Revision 1.33  2011-05-03 10:13:11  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.32  2011-04-26 12:01:42  willuhn
 * @D javadoc Fixes
 **********************************************************************/