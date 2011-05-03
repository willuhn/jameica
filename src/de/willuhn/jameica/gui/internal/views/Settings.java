/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/views/Settings.java,v $
 * $Revision: 1.33 $
 * $Date: 2011/05/03 10:13:11 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.views;

import java.io.File;
import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabFolder;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.internal.action.CertificateImport;
import de.willuhn.jameica.gui.internal.controller.SettingsControl;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog fuer die Programm-Einstellungen.
 */
public class Settings extends AbstractView implements Extendable
{

  /**
   * Wir merken uns das letzte aktive Tab
   */
  private static Integer lastActiveTab = null;

  /**
   * Der Tabfolder.
   */
  private TabFolder folder = null;
  
  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {

		final I18N i18n = Application.getI18n();

		GUI.getView().setTitle(i18n.tr("Einstellungen"));
		final SettingsControl control = new SettingsControl(this);

    /////////////////////////////////////////////////////////////////
    // System-Einstellungen
    TabGroup system = new TabGroup(getTabFolder(),i18n.tr("System"));
    
    system.addHeadline(i18n.tr("System-Optionen"));
    system.addLabelPair(i18n.tr("Log-Level"), control.getLogLevel());

    system.addHeadline(i18n.tr("Proxy-Optionen"));
    system.addCheckbox(control.getUseSystemProxy(),i18n.tr("System-Einstellungen verwenden"));
    ColumnLayout cl = new ColumnLayout(system.getComposite(),2);

    SimpleContainer left = new SimpleContainer(cl.getComposite());
    left.addLabelPair(i18n.tr("HTTP Proxy"), control.getProxyHost());
    left.addLabelPair(i18n.tr("HTTPS Proxy"), control.getHttpsProxyHost());
    SimpleContainer right = new SimpleContainer(cl.getComposite());
    right.addLabelPair(i18n.tr("Port"), control.getProxyPort());
    right.addLabelPair(i18n.tr("Port"), control.getHttpsProxyPort());

    system.addHeadline(i18n.tr("Installierte SSL-Zertifikate"));
    system.addPart(control.getCertificates());
    
    ButtonArea certButtons = new ButtonArea();
    certButtons.addButton(i18n.tr("Zertifikat importieren"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new CertificateImport().handleAction(context);
        GUI.startView(GUI.getCurrentView().getClass(),GUI.getCurrentView().getCurrentObject());
      }
    },null,false,"document-open.png");
    system.addButtonArea(certButtons);
    
    //
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Plugin-Einstellungen
    TabGroup plugins = new TabGroup(getTabFolder(),i18n.tr("Plugins"));

    plugins.addPart(control.getPlugins());

    //
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
		// Farb-Einstellungen

    TabGroup lnfGroup = new TabGroup(getTabFolder(),i18n.tr("Look and Feel"));
		try
		{
      lnfGroup.addLabelPair(i18n.tr("Vorausgewählte Sprache"), control.getLocale());
		}
		catch (RemoteException e)
		{
			Logger.error("error while reading locale settings",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Ermitteln der Sprach-Einstellungen."),StatusBarMessage.TYPE_ERROR));
		}
    lnfGroup.addLabelPair(i18n.tr("Style"), control.getStyleFactory());

    File f = new File("lib/splash.jar");
    if (f.exists() && f.isFile() && f.canRead())
      lnfGroup.addCheckbox(control.getRandomSplash(),i18n.tr("Zufallsbild in Splashscreen anzeigen"));

    lnfGroup.addSeparator();
    lnfGroup.addLabelPair(i18n.tr("Hintergrundfarbe von Pflichtfeldern"),control.getColorMandatoryBG());
    lnfGroup.addCheckbox(control.getLabelMandatory(),i18n.tr("Auch den Text vor diesen Pflichtfeldern (Label) hervorheben"));
    lnfGroup.addSeparator();
    lnfGroup.addLabelPair(i18n.tr("Hintergrundfarbe von Eingabefeldern"),control.getColorWidgetBG());
    lnfGroup.addLabelPair(i18n.tr("Textfarbe von Fehler- und Warnmeldungen"),control.getColorError());
    lnfGroup.addLabelPair(i18n.tr("Textfarbe von Erfolgsmeldungen"),control.getColorSuccess());
		//
		/////////////////////////////////////////////////////////////////

		// Mal checken, ob wir uns das zuletzt aktive Tab gemerkt haben.
    if (lastActiveTab != null)
      getTabFolder().setSelection(lastActiveTab.intValue());

    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Zurücksetzen"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleRestore();
      }
    },null,false,"edit-undo.png");
    buttons.addButton(i18n.tr("Speichern"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleStore();
      }
    },null,false,"document-save.png");
    buttons.paint(getParent());
  }
  
  /**
   * Liefert den Tab-Folder, in dem die einzelnen Module der Einstellungen
   * untergebracht sind.
   * @return der Tab-Folder.
   */
  public TabFolder getTabFolder()
  {
    if (this.folder != null)
      return this.folder;
    
    this.folder = new TabFolder(getParent(), SWT.NONE);
    this.folder.setLayoutData(new GridData(GridData.FILL_BOTH));
    return this.folder;
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
    // Wir merken uns das aktive Tab
    lastActiveTab = new Integer(getTabFolder().getSelectionIndex());
  }

  /**
   * @see de.willuhn.jameica.gui.extension.Extendable#getExtendableID()
   */
  public String getExtendableID()
  {
    return this.getClass().getName();
  }

}


/**********************************************************************
 * $Log: Settings.java,v $
 * Revision 1.33  2011/05/03 10:13:11  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.32  2011-04-26 11:38:16  willuhn
 * @R Back-Button entfernt
 * @N Icons auf Buttons
 *
 * Revision 1.31  2010-11-04 10:31:13  willuhn
 * @N Checken, ob splash.jar existiert
 *
 * Revision 1.30  2010-11-04 01:11:20  willuhn
 * @N Random Splashscreen ;)
 *
 * Revision 1.29  2010-10-10 21:20:55  willuhn
 * @R RMI-Einstellungen entfernt - braucht kein Schwein und irritiert nur
 *
 * Revision 1.28  2009-10-26 09:26:33  willuhn
 * @R Scroll-View-Parameter entfernt - verursachte Darstellungsfehler
 *
 * Revision 1.27  2009/03/20 16:38:09  willuhn
 * @N BUGZILLA 576
 *
 * Revision 1.26  2009/03/10 14:06:26  willuhn
 * @N Proxy-Server fuer HTTPS konfigurierbar
 *
 * Revision 1.25  2009/01/20 10:51:51  willuhn
 * @N Mehr Icons - fuer Buttons
 *
 * Revision 1.24  2008/08/29 13:15:42  willuhn
 * @C Java 1.4 Compatibility - wieso zur Hoelle sind die Fehler vorher nie aufgefallen? Ich compiliere immer gegen 1.4? Suspekt
 *
 * Revision 1.23  2008/07/22 22:30:08  willuhn
 * @C Zum Speichern des letzten aktiven Tabs braucht man gar keine Session sondern nur einen statischen Integer. Keine Ahnung, warum ich das mal so umstaendlich implementiert hatte ;)
 *
 * Revision 1.22  2007/12/18 17:50:12  willuhn
 * @R Background-Color nicht mehr aenderbar
 * @C Layout der Startseite
 *
 * Revision 1.21  2007/09/08 14:20:11  willuhn
 * @C Pflichtfelder nicht mehr via GUI deaktivierbar
 *
 * Revision 1.20  2007/09/06 22:21:55  willuhn
 * @N Hervorhebung von Pflichtfeldern konfigurierbar
 * @N Neustart-Hinweis nur bei Aenderungen, die dies wirklich erfordern
 *
 * Revision 1.19  2007/08/20 12:27:08  willuhn
 * @C Pfad zur Log-Datei nicht mehr aenderbar. verursachte nur sinnlose absolute Pfadangaben in der Config
 *
 * Revision 1.18  2007/06/21 18:33:25  willuhn
 * @B typo
 *
 * Revision 1.17  2007/04/02 23:01:43  willuhn
 * @N SelectInput auf BeanUtil umgestellt
 **********************************************************************/