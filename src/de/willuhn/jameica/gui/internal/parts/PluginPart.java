/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/Attic/PluginPart.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/06/01 17:35:58 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.internal.action.FileClose;
import de.willuhn.jameica.gui.internal.action.PluginInstall;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.PluginMessage;
import de.willuhn.jameica.messaging.PluginMessage.Event;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Komponente, die die Plugins in einer huebsch formatierten Liste mit
 * Buttons zum Managen anzeigt.
 */
public class PluginPart implements Part
{
  private static boolean haveNewInstalled = false;
  
  private MessageConsumer mc = new MyMessageConsumer();
  private Map<String,PluginInfoPart> plugins = new HashMap<String,PluginInfoPart>();
  private Composite parent = null;

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite comp) throws RemoteException
  {
    I18N i18n = Application.getI18n();

    org.eclipse.swt.graphics.Color bg = comp.getBackground();

    ScrolledComposite scrolled = new ScrolledComposite(comp,SWT.V_SCROLL);
    scrolled.setLayoutData(new GridData(GridData.FILL_BOTH));
    scrolled.setLayout(new GridLayout());
    scrolled.setExpandHorizontal(true);
    if (bg != null) scrolled.setBackground(bg);
    
    this.parent = new Composite(scrolled,SWT.NONE);
    this.parent.setLayoutData(new GridData(GridData.FILL_BOTH));
    this.parent.setLayout(new GridLayout());
    if (bg != null) this.parent.setBackground(bg);

    scrolled.setContent(this.parent);
    
    List<Manifest> mfs = Application.getPluginLoader().getInstalledManifests();
    for (Manifest m:mfs)
    {
      PluginInfoPart part = new PluginInfoPart(m);
      plugins.put(m.getName(),part);
      part.paint(this.parent);
    }
    
    // Groesse berechnen
    this.parent.setSize(this.parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));

    
    Container container = new SimpleContainer(comp);
    
    if (haveNewInstalled)
      container.addText(i18n.tr("Bitte starten Sie Jameica neu, damit die installierten Plugins aktiviert werden"),true,Color.ERROR);
    else
      container.addText(i18n.tr("Hinweis: Nur Plugins im Benutzer-Ordner können aktualisiert oder deinstalliert werden."),true,Color.COMMENT);
    
    ButtonArea buttons = new ButtonArea();
    Button install = new Button(i18n.tr("Neues Plugin installieren..."),new PluginInstall(),null,false,"emblem-package.png");
    install.setEnabled(!haveNewInstalled);
    buttons.addButton(install);
    buttons.paint(comp);


    Application.getMessagingFactory().registerMessageConsumer(this.mc);
    this.parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().unRegisterMessageConsumer(mc);
      }
    });
    
  }

  /**
   * Wird benachrichtigt, wenn ein Plugin deinstalliert wurde und loescht es aus
   * der Liste
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
      final PluginMessage m = (PluginMessage) message;
      final Manifest mf = m.getManifest();
      
      if (mf == null)
        return;

      GUI.getDisplay().syncExec(new Runnable() {
        public void run()
        {
          try
          {
            // Das entfernen machen wir erstmal beim Deinstallieren und Installieren
            Iterator<String> i = plugins.keySet().iterator();
            while (i.hasNext())
            {
              String name = i.next();
              if (mf.getName().equals(name))
              {
                PluginInfoPart part = plugins.get(name);
                part.dispose();
              }
            }

            // Neu einfuegen. Aber nur, wenn es installiert wurde
            if (m.getEvent() == Event.INSTALLED)
            {
              PluginInfoPart part = new PluginInfoPart(mf);
              plugins.put(mf.getName(),part);
              part.paint(parent);
              haveNewInstalled = true;
            }

            // Groesse neu berechnen
            parent.setSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));

            // Layout aktualisieren
            parent.layout();
            
            if (Application.getCallback().askUser(Application.getI18n().tr("Jameica jetzt beenden?")))
              new FileClose().handleAction(null);
          }
          catch (Exception e)
          {
            Logger.error("unable to update plugin list",e);
          }
        }
      });
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



/**********************************************************************
 * $Log: PluginPart.java,v $
 * Revision 1.1  2011/06/01 17:35:58  willuhn
 * @N Ergonomischere Verwaltung der Plugins
 *
 **********************************************************************/