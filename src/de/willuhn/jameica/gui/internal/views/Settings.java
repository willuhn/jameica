/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.views;

import java.io.File;

import org.eclipse.swt.SWT;
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
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BootstrapSettings;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog fuer die Programm-Einstellungen.
 */
public class Settings extends AbstractView implements Extendable
{
  private MessageConsumer mc = new MyMessageConsumer();

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
    TabGroup system = new TabGroup(getTabFolder(),i18n.tr("System"),true);
    
    system.addHeadline(i18n.tr("System-Einstellungen"));
    system.addLabelPair(i18n.tr("Sprache"), control.getLocale());
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

    File f = new File("lib/splash.jar");
    if (f.exists() && f.isFile() && f.canRead())
      lnfGroup.addCheckbox(control.getRandomSplash(),i18n.tr("Zufallsbild in Splashscreen anzeigen"));

    lnfGroup.addHeadline(i18n.tr("System-Tray Symbol"));
    lnfGroup.addInput(control.getSystray());
    lnfGroup.addInput(control.getMinimizeToSystray());

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
    
    // Damit wir benachrichtigt werden, wenn die Extensions mit dem Rendern fertig sind und wir dann erst das focus() machen
    Application.getMessagingFactory().getMessagingQueue(this.getExtendableID()).registerMessageConsumer(this.mc);
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
    Application.getMessagingFactory().getMessagingQueue(this.getExtendableID()).unRegisterMessageConsumer(this.mc);
    
    // Wir merken uns das aktive Tab
    lastActiveTab = Integer.valueOf(getTabFolder().getSelectionIndex());
  }

  /**
   * @see de.willuhn.jameica.gui.extension.Extendable#getExtendableID()
   */
  public String getExtendableID()
  {
    return this.getClass().getName();
  }
  
  /**
   * @see de.willuhn.jameica.gui.AbstractView#canBookmark()
   */
  public boolean canBookmark()
  {
    return false;
  }
  
  /**
   * @see de.willuhn.jameica.gui.AbstractView#canAttach()
   */
  @Override
  public boolean canAttach()
  {
    return false;
  }

  /**
   * Wird beanchtrichtigt, wenn die Extensions ihre Tabs gezeichnet haben.
   */
  private class MyMessageConsumer implements MessageConsumer
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
      return new Class[]{QueryMessage.class};
    }
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      GUI.getDisplay().syncExec(new Runnable()
      {
        public void run()
        {
          focus();
        }
      });
    }
  }
}
