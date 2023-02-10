/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.action.AttachmentManage;
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.AttachmentService;
import de.willuhn.jameica.system.Application;

/**
 * Vorkonfigurierter Panel-Button fuer Attachments.
 */
public class PanelButtonAttachment extends PanelButton
{
  /**
   * ct.
   */
  public PanelButtonAttachment()
  {
    super("mail-attachment.png",new AttachmentManage(),Application.getI18n().tr("Datei-Anhänge"));
    
    // Anzahl der Attachments ermitteln
    final AttachmentService service = Application.getBootLoader().getBootable(AttachmentService.class);
    
    try
    {
      final int size = service.find().size();
      if (size == 0)
        this.setTooltip(Application.getI18n().tr("Keine Dateianhänge"));
      else if (size == 1)
        this.setTooltip(Application.getI18n().tr("1 Dateianhang"));
      else
        this.setTooltip(Application.getI18n().tr("{0} Dateianhänge",Integer.toString(size)));
      
      if (size > 0)
        this.setText(Integer.toString(size));
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Wir liefern nur dann true, wenn die View Attachments erlaubt.
   * @see de.willuhn.jameica.gui.parts.PanelButton#isEnabled()
   */
  public boolean isEnabled()
  {
    return GUI.getCurrentView().canAttach();
  }
  
}
