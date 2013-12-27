/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/views/Settings.java,v $
 * $Revision: 1.41 $
 * $Date: 2012/02/23 22:03:36 $
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.internal.action.CertificateImport;
import de.willuhn.jameica.gui.internal.action.SystemCertificates;
import de.willuhn.jameica.gui.internal.controller.SettingsControl;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BootstrapSettings;
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
    
    system.addHeadline(i18n.tr("System-Einstellungen"));
    system.addLabelPair(i18n.tr("Log-Level"), control.getLogLevel());
    
    // Nur anzeigen, wenn der User die Option deaktiviert hat
    if (!BootstrapSettings.getAskWorkdir())
      system.addInput(control.getAskWorkdir());

    system.addHeadline(i18n.tr("Proxy-Einstellungen"));
    system.addCheckbox(control.getUseSystemProxy(),i18n.tr("System-Einstellungen verwenden"));
    ColumnLayout cl = new ColumnLayout(system.getComposite(),2);

    SimpleContainer left = new SimpleContainer(cl.getComposite());
    left.addInput(control.getProxyHost());
    left.addInput(control.getHttpsProxyHost());
    SimpleContainer right = new SimpleContainer(cl.getComposite());
    right.addInput(control.getProxyPort());
    right.addInput(control.getHttpsProxyPort());

    system.addHeadline(i18n.tr("Installierte SSL-Zertifikate"));
    system.addPart(control.getCertificates());
    system.addInput(control.getTrustJavaCerts());
    
    ButtonArea certButtons = new ButtonArea();
    certButtons.addButton(i18n.tr("Aussteller-Zertifikate von Java anzeigen"),new SystemCertificates(),null,false,"stock_keyring.png");
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
    TabGroup plugins = new TabGroup(getTabFolder(),i18n.tr("Plugins"),false,1);
    control.getPlugins().paint(plugins.getComposite());

    //
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
		// Farb-Einstellungen

    TabGroup lnfGroup = new TabGroup(getTabFolder(),i18n.tr("Look and Feel"));

    lnfGroup.addLabelPair(i18n.tr("Hintergrundfarbe von Pflichtfeldern"),control.getColorMandatoryBG());
    lnfGroup.addCheckbox(control.getLabelMandatory(),i18n.tr("Auch den Text vor diesen Pflichtfeldern (Label) hervorheben"));
    lnfGroup.addSeparator();
    lnfGroup.addLabelPair(i18n.tr("Textfarbe von Fehler- und Warnmeldungen"),control.getColorError());
    lnfGroup.addLabelPair(i18n.tr("Textfarbe von Erfolgsmeldungen"),control.getColorSuccess());

    lnfGroup.addSeparator();

    File f = new File("lib/splash.jar");
    if (f.exists() && f.isFile() && f.canRead())
      lnfGroup.addCheckbox(control.getRandomSplash(),i18n.tr("Zufallsbild in Splashscreen anzeigen"));

    //
		/////////////////////////////////////////////////////////////////

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
    
    // geht leider nicht anders, weil das Tab "Updates" erst gezeichnet wird,
    // nach dem bind() bereits verlassen wurde (ist ja eine Extension)
    getParent().addControlListener(new ControlAdapter() {
      public void controlResized(ControlEvent e)
      {
        focus();
      }
    });
    focus();
  }
  
  /**
   * Fokussiert das aktive Tab.
   */
  private void focus()
  {
    TabFolder folder = this.getTabFolder();
    if (folder == null || folder.isDisposed())
      return;
    
    /////////////////////////////////////////////////////////////////
    // Mal checken, ob ein konkretes Tab angegeben ist.
    Integer activeTab = lastActiveTab;
    Object context = this.getCurrentObject();

    if (context != null)
    {
      // ist explizit eines angegeben?
      if (context instanceof Integer)
      {
        activeTab = (Integer) context;
      }
      else if (context instanceof String)
      {
        Object o = folder.getData((String) context);
        if (o instanceof TabItem)
        {
          folder.setSelection((TabItem)o);
          return; // Auswahl getroffen
        }
      }
    }
    
    if (activeTab != null)
      folder.setSelection(activeTab);
    
    /////////////////////////////////////////////////////////////////
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
