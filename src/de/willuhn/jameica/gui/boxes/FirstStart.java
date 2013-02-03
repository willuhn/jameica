/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/boxes/FirstStart.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/06/09 10:07:59 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.boxes;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.action.FileClose;
import de.willuhn.jameica.gui.internal.action.PluginInstall;
import de.willuhn.jameica.gui.internal.views.Settings;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.PluginCacheMessageConsumer;
import de.willuhn.jameica.messaging.PluginMessage;
import de.willuhn.jameica.messaging.PluginMessage.Event;
import de.willuhn.jameica.plugin.PluginLoader;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Platform;
import de.willuhn.util.I18N;

/**
 * Eine Box, die angezeigt wird, wenn noch keine Plugins installiert sind.
 */
public class FirstStart extends AbstractBox
{
  private MessageConsumer mc = new MyMessageConsumer();

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Jameica: " + Application.getI18n().tr("Erster Start");
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isActive()
   */
  public boolean isActive()
  {
    return isEnabled();
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isEnabled()
   */
  public boolean isEnabled()
  {
    // Nur anzeigen, wenn wirklich noch keine Plugins vorhanden sind.
    PluginLoader loader = Application.getPluginLoader();
    return loader.getInstalledPlugins().size() == 0 && loader.getInitErrors().size() == 0;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled)
  {
    // Das darf der User nicht.
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 0;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    I18N i18n = Application.getI18n();

    // Wir unterscheiden hier beim Layout nach Windows/OSX und Rest.
    // Unter Windows und OSX sieht es ohne Rahmen und ohne Hintergrund besser aus
    org.eclipse.swt.graphics.Color bg = null;
    int border = SWT.NONE;
    
    int os = Application.getPlatform().getOS();
    if (os != Platform.OS_WINDOWS && os != Platform.OS_WINDOWS_64 && os != Platform.OS_MAC)
    {
      bg = GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE);
      border = SWT.BORDER;
    }
    
    // 2-spaltige Anzeige. Links das Icon, rechts Text und Buttons
    Composite comp = new Composite(parent,border);
    comp.setBackground(bg);
    comp.setBackgroundMode(SWT.INHERIT_FORCE);
    comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    comp.setLayout(new GridLayout(2,false));
    
    // User hat gerade ein Plugin installiert - es ist aber noch nicht aktiviert
    boolean pending = PluginCacheMessageConsumer.getCache().size() > 0;
    
    // Linke Spalte mit dem Icon
    {
      GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
      gd.verticalSpan = 3;
      Label icon = new Label(comp,SWT.NONE);
      icon.setBackground(bg);
      icon.setLayoutData(gd);
      icon.setImage(SWTUtil.getImage("package-x-generic-medium.png"));
    }
    
    // Ueberschrift
    {
      Label title = new Label(comp,SWT.NONE);
      title.setBackground(bg);
      title.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      title.setFont(Font.H2.getSWTFont());
      title.setText(i18n.tr(pending ? "Plugin noch nicht aktiviert" : "Noch keine Plugins installiert"));
    }
    
    // Text
    {
      Label desc = new Label(comp,SWT.NONE);
      desc.setBackground(bg);
      desc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      desc.setText(i18n.tr(pending ? "Bitte starten Sie Jameica jetzt neu." : "Bitte klicken Sie auf \"Neues Plugin installieren...\", um ein neues Plugin hinzuzufügen."));
    }
    
    ButtonArea buttons = new ButtonArea();
    if (pending)
      buttons.addButton(i18n.tr("Jameica beenden"),new FileClose(),null,true,"window-close.png");
    
    buttons.addButton(i18n.tr("Neues Plugin installieren..."),new PluginInstall(),null,true,"emblem-package.png");
    buttons.paint(comp);
    
    Application.getMessagingFactory().registerMessageConsumer(this.mc);
    comp.addDisposeListener(new DisposeListener() {
      
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().unRegisterMessageConsumer(mc);
      }
    });
  }
  
  /**
   * Wird benachrichtigt, wenn das Plugin installiert ist.
   */
  private class MyMessageConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{PluginMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      PluginMessage msg = (PluginMessage) message;
      if (msg.getEvent() == Event.INSTALLED)
        GUI.startView(Settings.class,1); // Wir springen gezielt auf das zweite Tab
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }
    
  }

}


/*********************************************************************
 * $Log: FirstStart.java,v $
 * Revision 1.5  2011/06/09 10:07:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2011-06-09 09:57:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2011-06-09 09:50:39  willuhn
 * @C Rahmen und Hintergrundfarbe nur unter Windows anzeigen
 *
 * Revision 1.2  2011-06-08 13:36:45  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2011-06-08 13:22:22  willuhn
 * @N Neuer First-Start-Assistent, der zum Installieren eines neuen Plugins auffordert
 *
 **********************************************************************/