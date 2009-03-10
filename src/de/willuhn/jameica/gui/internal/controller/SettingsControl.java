/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/controller/SettingsControl.java,v $
 * $Revision: 1.26 $
 * $Date: 2009/03/10 14:06:26 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.controller;

import java.rmi.RemoteException;
import java.util.Locale;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.ColorInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.internal.parts.CertificateList;
import de.willuhn.jameica.gui.internal.parts.PluginList;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.style.StyleFactory;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.messaging.SettingsChangedMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Config;
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
  private CheckboxInput rmiSSL;
  private Input rmiPort;
  private Input proxyHost;
  private Input proxyPort;
  private Input httpsProxyHost;
  private Input httpsProxyPort;
  private CheckboxInput systemProxy;
  
  private TablePart certs;

  // Plugins
  private TablePart plugins;

  // Look & Feel
	private Input colorWidgetBG;
	private Input colorWidgetFG;
	private Input colorComment;
	private Input colorError;
	private Input colorSuccess;
	private Input colorLink;
	private Input colorLinkActive;
  private Input colorMandatoryBG;
	private Input styleFactory;
  private CheckboxInput mandatoryLabel;
	
	private SelectInput locale;
	
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
   * Liefert ein Eingabe-Feld fuer den zu verwendenden RMI-Port.
   * @return Eingabe-Feld fuer den RMI-Port.
   */
  public Input getRmiPort()
  {
    if (this.rmiPort != null)
      return this.rmiPort;

    rmiPort = new IntegerInput(Application.getConfig().getRmiPort());
    return rmiPort;
  }

  /**
   * Liefert eine Checkbox zur Aktivierung von SSL bei der RMI-Uebertragung.
   * @return Checkbox zur Aktivierung von SSL bei der Datenuebertragung.
   */
  public CheckboxInput getRmiSSL()
  {
    if (this.rmiSSL != null)
      return this.rmiSSL;
    rmiSSL = new CheckboxInput(Application.getConfig().getRmiSSL());
    return this.rmiSSL;
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
     this.plugins = new PluginList();
    return this.plugins;
  }
  
  /**
	 * Liefert ein Auswahl-Feld fuer die Style-Factory.
   * @return Auswahl-Feld.
   */
  public Input getStyleFactory()
	{
		if (styleFactory != null)
			return styleFactory;
		try {
			Class[] styles = Application.getClassLoader().getClassFinder().findImplementors(StyleFactory.class);


			GenericObject[] s = new GenericObject[styles.length];
			for (int i=0;i<styles.length;++i)
			{
				s[i] = new StyleFactoryObject((StyleFactory) styles[i].newInstance());
			}
			styleFactory = new SelectInput(PseudoIterator.fromArray(s),new StyleFactoryObject(GUI.getStyleFactory()));
		}
		catch (Exception e)
		{
			Logger.error("unable to load available stylefactories",e);
			styleFactory = new LabelInput(i18n.tr("Fehler beim Laden der Styles"));
		}
		return styleFactory;
	}

  /**
	 * Auswahlfeld fuer die Sprache.
   * @return Sprach-Auswahl.
   * @throws RemoteException
   */
  public SelectInput getLocale() throws RemoteException
	{
		if (locale != null)
			return locale;

		locale = new SelectInput(Locale.getAvailableLocales(),Application.getConfig().getLocale());
    locale.setAttribute("displayName");
		return locale;
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
  public Input getColorWidgetFG()
  {
  	if (colorWidgetFG != null)
  		return colorWidgetFG;
  	colorWidgetFG = new ColorInput(Color.WIDGET_FG.getSWTColor(),true);
  	return colorWidgetFG;
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
	public Input getColorComment()
	{
		if (colorComment != null)
			return colorComment;
		colorComment = new ColorInput(Color.COMMENT.getSWTColor(),true);
		return colorComment;
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
	 * Auswahlfeld.
   * @return Auswahl-Feld.
	 */
	public Input getColorLink()
	{
		if (colorLink != null)
			return colorLink;
		colorLink = new ColorInput(Color.LINK.getSWTColor(),true);
		return colorLink;
	}

	/**
	 * Auswahlfeld.
   * @return Auswahl-Feld.
	 */
	public Input getColorLinkActive()
	{
		if (colorLinkActive != null)
			return colorLinkActive;
		colorLinkActive = new ColorInput(Color.LINK_ACTIVE.getSWTColor(),true);
		return colorLinkActive;
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
   * Speichert die Einstellungen.
   */
  public void handleStore()
  {

  	try
    {
      boolean restartNeeded = false;

      // System
      Application.getConfig().setLoglevel((String)getLogLevel().getValue());

      restartNeeded |= getRmiPort().hasChanged();
      Integer in = (Integer) getRmiPort().getValue();
      if (in != null)
        Application.getConfig().setRmiPort(in.intValue());

      restartNeeded |= getRmiSSL().hasChanged();
      Application.getConfig().setRmiSSL(((Boolean) getRmiSSL().getValue()).booleanValue());

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
    	Color.WIDGET_BG.setSWTColor((org.eclipse.swt.graphics.Color)getColorWidgetBG().getValue());
			Color.COMMENT.setSWTColor((org.eclipse.swt.graphics.Color)getColorComment().getValue());
			Color.WIDGET_FG.setSWTColor((org.eclipse.swt.graphics.Color)getColorWidgetFG().getValue());
			Color.ERROR.setSWTColor((org.eclipse.swt.graphics.Color)getColorError().getValue());
			Color.SUCCESS.setSWTColor((org.eclipse.swt.graphics.Color)getColorSuccess().getValue());
			Color.LINK.setSWTColor((org.eclipse.swt.graphics.Color)getColorLink().getValue());
			Color.LINK_ACTIVE.setSWTColor((org.eclipse.swt.graphics.Color)getColorLinkActive().getValue());
      Color.MANDATORY_BG.setSWTColor((org.eclipse.swt.graphics.Color)getColorMandatoryBG().getValue());

      restartNeeded |= getStyleFactory().hasChanged();
      StyleFactoryObject fo = (StyleFactoryObject) getStyleFactory().getValue();
			GUI.setStyleFactory(fo.factory);

      restartNeeded |= getLocale().hasChanged();
      Locale lo = (Locale) getLocale().getValue();
			Application.getConfig().setLocale(lo);

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
			Color.BACKGROUND.reset();
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
  	catch (Exception e)
  	{
  		Logger.error("error while restoring settings",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Zurücksetzen der Einstellungen."),StatusBarMessage.TYPE_ERROR));
  	}
  	
  }
  
  /**
   * Hilfs-Klasse, die StyleFactories in GenericObjects wrappt, um sie einfacher
   * in den GUI-Bibliotheken von Jameica anzeigen zu koennen.
   */
  private static class StyleFactoryObject implements GenericObject
  {

		private StyleFactory factory;

		/**
		 * ct.
     * @param f
     */
    private StyleFactoryObject(StyleFactory f)
		{
			this.factory = f;
		}

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) throws RemoteException
    {
    	if ("name".equalsIgnoreCase(name))
    		return factory.getName();
    	return factory;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return factory.getClass().getName();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "name";
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject other) throws RemoteException
    {
    	if (other == null)
	      return false;
			return getID().equals(other.getID());
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[] {"name"};
    }
  }
}


/**********************************************************************
 * $Log: SettingsControl.java,v $
 * Revision 1.26  2009/03/10 14:06:26  willuhn
 * @N Proxy-Server fuer HTTPS konfigurierbar
 *
 * Revision 1.25  2008/02/29 01:12:30  willuhn
 * @N Erster Code fuer neues Backup-System
 * @N DirectoryInput
 * @B Fixes an FileInput, TextInput
 *
 * Revision 1.24  2007/12/18 17:50:12  willuhn
 * @R Background-Color nicht mehr aenderbar
 * @C Layout der Startseite
 *
 * Revision 1.23  2007/10/22 23:23:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.22  2007/09/08 14:20:11  willuhn
 * @C Pflichtfelder nicht mehr via GUI deaktivierbar
 *
 * Revision 1.21  2007/09/06 22:21:55  willuhn
 * @N Hervorhebung von Pflichtfeldern konfigurierbar
 * @N Neustart-Hinweis nur bei Aenderungen, die dies wirklich erfordern
 *
 * Revision 1.20  2007/08/20 12:27:08  willuhn
 * @C Pfad zur Log-Datei nicht mehr aenderbar. verursachte nur sinnlose absolute Pfadangaben in der Config
 *
 * Revision 1.19  2007/04/02 23:01:43  willuhn
 * @N SelectInput auf BeanUtil umgestellt
 *
 * Revision 1.18  2006/12/28 15:35:52  willuhn
 * @N Farbige Pflichtfelder
 *
 * Revision 1.17  2006/10/31 23:57:26  willuhn
 * @N MessagingFactory.sendSyncMessage()
 * @N Senden einer SettingsChangedMessage beim Aendern von System-Einstellungen
 *
 * Revision 1.16  2006/08/28 23:41:48  willuhn
 * @N ColorInput verbessert
 *
 * Revision 1.15  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
 * Revision 1.14  2005/08/25 21:18:24  web0
 * @C changes accoring to findbugs eclipse plugin
 *
 * Revision 1.13  2005/06/16 13:47:56  web0
 * *** empty log message ***
 *
 * Revision 1.12  2005/06/16 13:02:55  web0
 * *** empty log message ***
 *
 * Revision 1.11  2005/06/15 17:51:31  web0
 * @N Code zum Konfigurieren der Service-Bindings
 *
 * Revision 1.10  2005/06/14 23:15:30  web0
 * @N added settings for plugins/services
 *
 * Revision 1.9  2005/06/10 22:13:09  web0
 * @N new TabGroup
 * @N extended Settings
 *
 * Revision 1.8  2005/01/13 19:31:37  willuhn
 * @C SSLFactory geaendert
 * @N Settings auf property-Format umgestellt
 *
 * Revision 1.7  2004/12/13 22:48:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/11/05 20:00:44  willuhn
 * @D javadoc fixes
 *
 * Revision 1.4  2004/10/23 18:13:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/10/20 12:08:16  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.2  2004/10/14 23:15:05  willuhn
 * @N maded locale configurable via GUI
 * @B fixed locale handling
 * @B DecimalInput now honors locale
 *
 * Revision 1.1  2004/10/08 13:38:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.27  2004/08/18 23:14:19  willuhn
 * @D Javadoc
 *
 * Revision 1.26  2004/07/23 15:51:20  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.25  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.24  2004/07/09 00:12:47  willuhn
 * @C Redesign
 *
 * Revision 1.23  2004/06/30 20:58:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.22  2004/06/18 19:47:17  willuhn
 * *** empty log message ***
 *
 * Revision 1.21  2004/06/03 00:24:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.20  2004/06/02 21:15:15  willuhn
 * @B win32 fixes in flat style
 * @C made ButtonInput more abstract
 *
 * Revision 1.19  2004/05/27 23:38:25  willuhn
 * @B deadlock in swt event queue while startGUITimeout
 *
 * Revision 1.18  2004/05/23 18:15:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/05/23 16:34:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/04/19 22:05:27  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/04/12 19:16:00  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.14  2004/04/01 00:23:24  willuhn
 * @N FontInput
 * @N ColorInput
 * @C improved ClassLoader
 * @N Tabs in Settings
 *
 * Revision 1.13  2004/03/30 22:08:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/03/24 00:46:03  willuhn
 * @C refactoring
 *
 * Revision 1.11  2004/03/11 08:56:56  willuhn
 * @C some refactoring
 *
 * Revision 1.10  2004/03/06 18:24:24  willuhn
 * @D javadoc
 *
 * Revision 1.9  2004/03/03 22:27:11  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.8  2004/02/24 22:46:53  willuhn
 * @N GUI refactoring
 *
 * Revision 1.7  2004/02/11 00:10:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/01/28 20:51:25  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.5  2004/01/23 00:29:04  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/01/08 20:50:33  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.3  2004/01/06 20:11:22  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/01/06 01:27:30  willuhn
 * @N table order
 *
 * Revision 1.1  2004/01/04 19:51:01  willuhn
 * *** empty log message ***
 *
 **********************************************************************/