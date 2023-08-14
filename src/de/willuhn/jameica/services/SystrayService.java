/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.parts.SysTray;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.SystemMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Service, der sich um das Systay kümmert.
 */
public class SystrayService implements Bootable
{
  private final static de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(SystrayService.class);
  
  private SysTray systray;

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class<Bootable>[] depends()
  {
    return new Class[]{BeanService.class,MessagingService.class};
  }
  
  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    if (Application.inServerMode())
      return;
    
    Application.getMessagingFactory().registerMessageConsumer(new InitMessageConsumer());
    
    final BeanService bs = loader.getBootable(BeanService.class);
    this.systray = bs.get(SysTray.class);
  }
  
  /**
   * Legt fest, ob das Symbol neue Aktivität anzeigen soll.
   * @param b true, wenn neue Aktivität angezeigt werden soll.
   */
  public void setNewActivity(boolean b)
  {
    if (this.systray != null)
      this.systray.setNewActivity(b);
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
  }

  /**
   * Liefert true, wenn das Systray aktiviert werden soll.
   * @return true, wenn das Systray aktiviert werden soll.
   */
  public boolean isEnabled()
  {
    return settings.getBoolean("enabled",false);
  }


  /**
   * Liefert true, wenn das Systray aktiviert werden soll.
   * @param enabled true, wenn das Systray aktiviert werden soll.
   */
  public void setEnabled(boolean enabled)
  {
    settings.setAttribute("enabled",enabled);
    this.update();
  }

  /**
   * Liefert true, wenn die Anwendung in das System-Tray minimiert werden soll.
   * @return true, wenn die Anwendung in das System-Tray minimiert werden soll.
   */
  public boolean isMinimizeToSystray()
  {
    return settings.getBoolean("minimize",false);
  }
  
  /**
   * Legt fest, ob die Anwendung in das System-Tray minimiert werden soll.
   * @param enabled wenn die Anwendung in das System-Tray minimiert werden soll.
   */
  public void setMinimizeToSystray(boolean enabled)
  {
    settings.setAttribute("minimize",enabled);
  }
  
  /**
   * Aktualisiert den Systray-Status.
   */
  public void update()
  {
    if (this.isEnabled())
      systray.start();
    else
      systray.stop();
  }
  
  /**
   * Registriert den Listener für das Minimieren der GUI.
   */
  private void init()
  {
    this.update();
    GUI.getShell().addShellListener(new ShellAdapter() {
      
      /**
       * @see org.eclipse.swt.events.ShellAdapter#shellIconified(org.eclipse.swt.events.ShellEvent)
       */
      @Override
      public void shellIconified(ShellEvent e)
      {
        final Shell shell = GUI.getShell();
        if (shell == null || shell.isDisposed())
          return;

        if (!shell.isVisible())
          return;
        
        if (!isEnabled() || !isMinimizeToSystray())
          return;
        
        if (shell.getData(SysTray.KEY_SYSTRAY_DATA) != null)
        {
          shell.setData(SysTray.KEY_SYSTRAY_DATA,null);
          return;
        }
        
        Logger.info("minimize to systray");
        shell.setVisible(false);
      }
    });
  }


  /**
   * Fuehrt die Initialisierung des Scripting-Service aus, wenn das System gebootet wurde.
   */
  private class InitMessageConsumer implements MessageConsumer
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
      return new Class[]{SystemMessage.class};
    }
    
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      SystemMessage msg = (SystemMessage) message;
      
      if (msg.getStatusCode() == SystemMessage.SYSTEM_STARTED)
        init();
    }
  }

}
