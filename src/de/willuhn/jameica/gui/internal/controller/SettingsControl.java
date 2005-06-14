/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/controller/SettingsControl.java,v $
 * $Revision: 1.10 $
 * $Date: 2005/06/14 23:15:30 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.controller;

import java.net.BindException;
import java.net.ServerSocket;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.ColorInput;
import de.willuhn.jameica.gui.input.FileInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.internal.parts.CertificateList;
import de.willuhn.jameica.gui.internal.parts.PluginList;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.style.StyleFactory;
import de.willuhn.jameica.gui.util.Color;
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
  private FileInput logFile;
  private CheckboxInput rmiSSL;
  private Input rmiPort;
  private TablePart certs;

  // Plugins
  private TablePart plugins;

  // Look & Feel
	private Input colorWidgetBG;
	private Input colorWidgetFG;
	private Input colorBackground;
	private Input colorComment;
	private Input colorError;
	private Input colorSuccess;
	private Input colorLink;
	private Input colorLinkActive;
	private Input styleFactory;
	
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
   * Liefert ein Eingabe-Feld fuer die Log-Datei.
   * @return Eingabe-Feld fuer das Log-File.
   */
  public Input getLogFile()
  {
    if (this.logFile != null)
      return this.logFile;

    this.logFile = new FileInput(Application.getConfig().getLogFile());
    this.logFile.disableClientControl();
    return this.logFile;
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

		Locale[] available = Locale.getAvailableLocales();
		ArrayList al = new ArrayList();
		for (int i=0;i<available.length;++i)
		{
			try
			{
				// Wir ueberspringen nicht vorhandene Sprachen
				ResourceBundle.getBundle("lang/messages",available[i]);
				al.add(new LocaleObject(available[i]));
			}
			catch (Exception e)
			{
			}
		}
		
		LocaleObject[] lo = (LocaleObject[]) al.toArray(new LocaleObject[al.size()]);
		locale = new SelectInput(PseudoIterator.fromArray(lo),new LocaleObject(Application.getConfig().getLocale()));
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
		colorWidgetBG = new ColorInput(Color.WIDGET_BG.getSWTColor());
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
  	colorWidgetFG = new ColorInput(Color.WIDGET_FG.getSWTColor());
  	return colorWidgetFG;
  }

  /**
	 * Auswahlfeld.
   * @return Auswahl-Feld.
	 */
	public Input getColorComment()
	{
		if (colorComment != null)
			return colorComment;
		colorComment = new ColorInput(Color.COMMENT.getSWTColor());
		return colorComment;
	}

	/**
	 * Auswahlfeld.
   * @return Auswahl-Feld.
	 */
	public Input getColorBackground()
	{
		if (colorBackground != null)
			return colorBackground;
		colorBackground = new ColorInput(Color.BACKGROUND.getSWTColor());
		return colorBackground;
	}

	/**
	 * Auswahlfeld.
   * @return Auswahl-Feld.
	 */
	public Input getColorError()
	{
		if (colorError != null)
			return colorError;
		colorError = new ColorInput(Color.ERROR.getSWTColor());
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
		colorSuccess = new ColorInput(Color.SUCCESS.getSWTColor());
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
		colorLink = new ColorInput(Color.LINK.getSWTColor());
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
		colorLinkActive = new ColorInput(Color.LINK_ACTIVE.getSWTColor());
		return colorLinkActive;
	}

  /**
   * Speichert die Einstellungen.
   */
  public void handleStore()
  {

  	try
    {

      // System
      String level = (String)getLogLevel().getValue();
      Application.getConfig().setLoglevel(level);
      Logger.setLevel(Level.findByName(level));

      Application.getConfig().setLogFile((String)getLogFile().getValue());
      
      Integer in = (Integer) getRmiPort().getValue();
      if (in == null)
        throw new ApplicationException(i18n.tr("Bitte geben Sie eine TCP-Portnummer ein"));
        
      int i = in.intValue();
      if (i < 1024 || i > 65535)
        throw new ApplicationException(i18n.tr("TCP-Portnummer ausserhalb des gültigen Bereichs von {0} bis {1}", new String[]{""+1024,""+65535}));

      if (i != Application.getConfig().getRmiPort())
      {
        ServerSocket s = null;
        try
        {
          // Wir machen einen Test auf dem Port wenn es nicht der aktuelle ist
          Logger.info("testing TCP port " + i);
          s = new ServerSocket(i);
        }
        catch (BindException e)
        {
          throw new ApplicationException(i18n.tr("Die angegebene TCP-Portnummer {0} ist bereits belegt",""+i));
        }
        finally
        {
          if (s != null)
          {
            try
            {
              s.close();
            }
            catch (Exception e)
            {
              // ignore
            }
          }
        }
      }

      Application.getConfig().setRmiPort(i);

      Boolean b = (Boolean) getRmiSSL().getValue();
      Application.getConfig().setRmiSSL(b.booleanValue());

      // Look & Feel
    	Color.WIDGET_BG.setSWTColor((org.eclipse.swt.graphics.Color)getColorWidgetBG().getValue());
			Color.COMMENT.setSWTColor((org.eclipse.swt.graphics.Color)getColorComment().getValue());
			Color.BACKGROUND.setSWTColor((org.eclipse.swt.graphics.Color)getColorBackground().getValue());
			Color.WIDGET_FG.setSWTColor((org.eclipse.swt.graphics.Color)getColorWidgetFG().getValue());
			Color.ERROR.setSWTColor((org.eclipse.swt.graphics.Color)getColorError().getValue());
			Color.SUCCESS.setSWTColor((org.eclipse.swt.graphics.Color)getColorSuccess().getValue());
			Color.LINK.setSWTColor((org.eclipse.swt.graphics.Color)getColorLink().getValue());
			Color.LINK_ACTIVE.setSWTColor((org.eclipse.swt.graphics.Color)getColorLinkActive().getValue());
			StyleFactoryObject fo = (StyleFactoryObject) getStyleFactory().getValue();
			GUI.setStyleFactory(fo.factory);
			LocaleObject lo = (LocaleObject) getLocale().getValue();
			Application.getConfig().setLocale(lo.locale);

			GUI.getStatusBar().setSuccessText(i18n.tr("Einstellungen gespeichert."));
			
			SimpleDialog d = new SimpleDialog(SimpleDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Neustart erforderlich"));
			d.setText(i18n.tr("Bitte starten Sie Jameica neu, damit Ihre Änderungen wirksam werden."));
			d.open();
    }
    catch (ApplicationException ae)
    {
      GUI.getStatusBar().setErrorText(ae.getMessage());
    }
    catch (Exception e)
    {
    	Logger.error("error while writing config",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der Einstellungen."));
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
      Application.getConfig().setLogFile(null);
      Application.getConfig().setLoglevel(Level.INFO.getName());
      Application.getConfig().setRmiSSL(true);

			GUI.getStatusBar().setSuccessText(i18n.tr("Einstellungen zurückgesetzt."));
			new de.willuhn.jameica.gui.internal.action.Settings().handleAction(null);
  	}
  	catch (Exception e)
  	{
  		Logger.error("error while restoring settings",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Zurücksetzen"));
  	}
  	
  }
  
  /**
   * Hilfs-Klasse, die StyleFactories in GenericObjects wrappt, um sie einfacher
   * in den GUI-Bibliotheken von Jameica anzeigen zu koennen.
   */
  private class StyleFactoryObject implements GenericObject
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

	/**
	 * Hilfsklasse zum Behandeln der Locales.
   */
  private static class LocaleObject implements GenericObject
	{
		private Locale locale;

		/**
		 * ct.
     * @param l
     */
    private LocaleObject(Locale l)
		{
			this.locale = l;
		}

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      return locale.getDisplayName();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return locale.getLanguage() + "_" + locale.getCountry();
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
    public boolean equals(GenericObject arg0) throws RemoteException
    {
    	if (arg0 == null)
    		return false;
      return arg0.getID().equals(this.getID());
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