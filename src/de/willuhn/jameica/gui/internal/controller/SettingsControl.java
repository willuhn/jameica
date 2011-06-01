/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/controller/SettingsControl.java,v $
 * $Revision: 1.36 $
 * $Date: 2011/06/01 17:35:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.controller;

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
import de.willuhn.jameica.gui.internal.parts.PluginPart;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.messaging.SettingsChangedMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Config;
import de.willuhn.jameica.system.Customizing;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * 
 */
public class SettingsControl extends AbstractControl
{

	private I18N i18n;

  // System
  private Input logLevel;
  private Input proxyHost;
  private Input proxyPort;
  private Input httpsProxyHost;
  private Input httpsProxyPort;
  private CheckboxInput systemProxy;
  
  private TablePart certs;

  // Plugins
  private Part plugins;

  // Look & Feel
	private Input colorWidgetBG;
	private Input colorError;
	private Input colorSuccess;
  private Input colorMandatoryBG;
  private CheckboxInput mandatoryLabel;
  private CheckboxInput randomSplash;
	
  /**
   * ct.
   * @param view
   */
  public SettingsControl(AbstractView view)
  {
    super(view);
    i18n = Application.getI18n();
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
    return this.proxyHost;
  }

  /**
   * Liefert ein Eingabefeld fuer die TCP-Portnummer des Proxys.
   * @return Eingabefeld fuer die Proxy-Portnummer.
   */
  public Input getProxyPort()
  {
    if (this.proxyPort != null)
      return this.proxyPort;
    this.proxyPort = new IntegerInput(Application.getConfig().getProxyPort());
    this.proxyPort.setComment(Application.getI18n().tr("freilassen, wenn nicht gewünscht"));
    return this.proxyPort;
  }
  
  /**
   * Liefert ein Eingabefeld fuer die Definition des HTTPS-Proxy-Hosts.
   * @return Eingabefeld fuer den HTTPS-Proxy.
   */
  public Input getHttpsProxyHost()
  {
    if (this.httpsProxyHost != null)
      return this.httpsProxyHost;
    this.httpsProxyHost = new TextInput(Application.getConfig().getHttpsProxyHost());
    return this.httpsProxyHost;
  }

  /**
   * Liefert ein Eingabefeld fuer die TCP-Portnummer des HTTPS-Proxys.
   * @return Eingabefeld fuer die Proxy-Portnummer des HTTPS-Proxy.
   */
  public Input getHttpsProxyPort()
  {
    if (this.httpsProxyPort != null)
      return this.httpsProxyPort;
    this.httpsProxyPort = new IntegerInput(Application.getConfig().getHttpsProxyPort());
    this.httpsProxyPort.setComment(Application.getI18n().tr("freilassen, wenn nicht gewünscht"));
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
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Übernehmen der Proxy-Einstellungen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
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
   * Liefert eine Tabelle mit den installierten Zertifikaten.
   * @return Tabelle mit einer Liste der installierten Zertifikate.
   */
  public Part getCertificates()
  {
    if (this.certs != null)
      return this.certs;
    this.certs = new CertificateList();
    return this.certs;
  }

  /**
   * Liefert eine Tabelle mit den installierten Plugins.
   * @return Liste der Plugins.
   */
  public Part getPlugins()
  {
    if (this.plugins != null)
      return this.plugins;
     this.plugins = new PluginPart();
    return this.plugins;
  }
  
	/**
	 * Auswahlfeld.
   * @return Auswahl-Feld.
   */
  public Input getColorWidgetBG()
	{
		if (colorWidgetBG != null)
			return colorWidgetBG;
		colorWidgetBG = new ColorInput(Color.WIDGET_BG.getSWTColor(),false);
		return colorWidgetBG;
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
      boolean restartNeeded = false;

      // System
      Application.getConfig().setLoglevel((String)getLogLevel().getValue());

      restartNeeded |= getProxyPort().hasChanged();
      Integer proxyPort = (Integer) getProxyPort().getValue();
      if (proxyPort == null)
        Application.getConfig().setProxyPort(-1);
      else
        Application.getConfig().setProxyPort(proxyPort.intValue());

      restartNeeded |= getProxyHost().hasChanged();
      Application.getConfig().setProxyHost((String)getProxyHost().getValue());

      restartNeeded |= getHttpsProxyPort().hasChanged();
      proxyPort = (Integer) getHttpsProxyPort().getValue();
      if (proxyPort == null)
        Application.getConfig().setHttpsProxyPort(-1);
      else
        Application.getConfig().setHttpsProxyPort(proxyPort.intValue());

      restartNeeded |= getHttpsProxyHost().hasChanged();
      Application.getConfig().setHttpsProxyHost((String)getHttpsProxyHost().getValue());
      
      restartNeeded |= getUseSystemProxy().hasChanged();
      Application.getConfig().setUseSystemProxy(((Boolean)getUseSystemProxy().getValue()).booleanValue());

      // Look & Feel
      Application.getConfig().setMandatoryLabel(((Boolean)getLabelMandatory().getValue()).booleanValue());
      Customizing.SETTINGS.setAttribute("application.splashscreen.random",((Boolean)getRandomSplash().getValue()).booleanValue());
    	Color.WIDGET_BG.setSWTColor((org.eclipse.swt.graphics.Color)getColorWidgetBG().getValue());
			Color.ERROR.setSWTColor((org.eclipse.swt.graphics.Color)getColorError().getValue());
			Color.SUCCESS.setSWTColor((org.eclipse.swt.graphics.Color)getColorSuccess().getValue());
      Color.MANDATORY_BG.setSWTColor((org.eclipse.swt.graphics.Color)getColorMandatoryBG().getValue());

      Application.getMessagingFactory().sendSyncMessage(new SettingsChangedMessage());
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Einstellungen gespeichert."),StatusBarMessage.TYPE_SUCCESS));

      if (restartNeeded)
        Application.getCallback().notifyUser(i18n.tr("Bitte starten Sie Jameica neu, damit alle Änderungen wirksam werden."));
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
  		prompt.setText(i18n.tr("Alle Einstellungen werden auf die Standard-Werte zurückgesetzt"));
  		if (!((Boolean) prompt.open()).booleanValue())
  			return;

			Color.WIDGET_BG.reset();
			Color.WIDGET_FG.reset();
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

      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Einstellungen zurückgesetzt."),StatusBarMessage.TYPE_SUCCESS));
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
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Zurücksetzen der Einstellungen."),StatusBarMessage.TYPE_ERROR));
  	}
  	
  }
}


/**********************************************************************
 * $Log: SettingsControl.java,v $
 * Revision 1.36  2011/06/01 17:35:58  willuhn
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