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

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.parts.SysTray;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.SystemMessage;
import de.willuhn.jameica.system.Application;

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
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
  }

  /**
   * Liefert true, wenn die Anwendung in das System-Tray minimiert werden soll.
   * @return true, wenn die Anwendung in das System-Tray minimiert werden soll.
   */
  public boolean getEnabled()
  {
    return settings.getBoolean("enabled",false);
  }
  
  /**
   * Legt fest, ob die Anwendung in das System-Tray minimiert werden soll.
   * @param enabled wenn die Anwendung in das System-Tray minimiert werden soll.
   */
  public void setEnabled(boolean enabled)
  {
    settings.setAttribute("enabled",enabled);
  }
  
  /**
   * Registriert den Listener für das Minimieren der GUI.
   */
  private void init()
  {
    // Wir registrieren den Listener auch dann, wenn die Option in den Einstellungen deaktiviert ist.
    // Dann können wir die Funktion auch ohne Neustart anbieten, sobald der Benutzer es aktiviert.
    GUI.getShell().addShellListener(new ShellAdapter() {
      /**
       * @see org.eclipse.swt.events.ShellAdapter#shellIconified(org.eclipse.swt.events.ShellEvent)
       */
      @Override
      public void shellIconified(ShellEvent e)
      {
        if (!getEnabled())
          return;

        systray.start();
      }
      
      /**
       * @see org.eclipse.swt.events.ShellAdapter#shellDeiconified(org.eclipse.swt.events.ShellEvent)
       */
      @Override
      public void shellDeiconified(ShellEvent e)
      {
        systray.stop();
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
