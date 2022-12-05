/**********************************************************************
 *
 * Copyright (c) 2022 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.attachment;

import java.util.List;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * Service zum Laden und Speichern von Attachments.
 */
@Lifecycle(Type.CONTEXT)
public class AttachmentService
{
  /**
   * Liefert die Attachments für die aktuelle View mit dem aktuellen Objekt.
   * @return die Liste der Attachments.
   * @throws ApplicationException
   */
  public List<Attachment> find() throws ApplicationException
  {
    final AbstractView view = GUI.getCurrentView();
    final String viewClass  = view.getClass().getName();
    final Object ctx        = view.getCurrentObject();
    
    return null;
  }
}


