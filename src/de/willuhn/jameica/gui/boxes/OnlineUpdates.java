/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.boxes;

import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.messaging.SystemRepositoryTrustMessageConsumer;
import de.willuhn.jameica.services.UpdateService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Box zum Einrichten der Online-Updates.
 */
public class OnlineUpdates extends AbstractBox
{
  private final static I18N i18n = Application.getI18n();
  private final static Settings settings = new Settings(OnlineUpdates.class);
  private SystemRepositoryTrustMessageConsumer mc = new SystemRepositoryTrustMessageConsumer();

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Jameica: " + Application.getI18n().tr("Online-Updates");
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
    // Zeigen wir nur an, wenn die Online-Updates noch deaktiviert sind und der Dialog noch nie angezeigt wurde.
    UpdateService service = Application.getBootLoader().getBootable(UpdateService.class);
    return settings.getString("displayed",null) == null && !service.getUpdateCheck();
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
    InfoPanel panel = new InfoPanel();
    panel.setIcon("dialog-question-large.png");
    panel.setTitle(i18n.tr("Online-Updates für Plugins aktivieren?"));
    panel.setText(i18n.tr("Soll Jameica regelmäßig nach Online-Updates zu den installierten Plugins suchen?\n" +
                          "Bei der Aktivierung werden ggf. Informationen zum SSL-Zertifikat des Server angezeigt."));
    panel.setComment(i18n.tr("Sie können diese Einstellungen jederzeit in \"Datei»Einstellungen»Updates\" ändern."));
    
    panel.addButton(new Button(i18n.tr("Online-Updates aktivieren"),new UpdateState(),Boolean.TRUE,false,"ok.png"));
    panel.addButton(new Button(i18n.tr("Online-Updates nicht verwenden"),new UpdateState(),Boolean.FALSE,false,"process-stop.png"));
    panel.addButton(new Button(i18n.tr("Einstellungen..."),new de.willuhn.jameica.gui.internal.action.Settings(),i18n.tr("Updates"),false,"document-properties.png"));
    panel.paint(parent);
  }
  
  private class UpdateState implements Action
  {
    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    public void handleAction(Object context) throws ApplicationException
    {
      boolean b = ((Boolean) context).booleanValue();
      
      // Wir oeffnen einmal das System-Repository, um die Anzeige des Zertifikates zu triggern, falls
      // es vom User noch nicht importiert wurde.
      if (b)
        mc.performTrust();
      
      UpdateService service = Application.getBootLoader().getBootable(UpdateService.class);
      service.setUpdateCheck(b);
      settings.setAttribute("displayed",DateUtil.DEFAULT_FORMAT.format(new Date()));
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(b ? i18n.tr("Online-Updates aktiviert") : i18n.tr("Online-Updates deaktiviert"),b ? StatusBarMessage.TYPE_SUCCESS : StatusBarMessage.TYPE_INFO));
    }
  }
  
}
