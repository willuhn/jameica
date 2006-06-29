/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/boxes/SystemMessages.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/06/29 23:10:01 $
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

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.system.Application;

/**
 * Eine Box, die die System-Meldungen des Starts anzeigt (insofern welche vorliegen).
 */
public class SystemMessages extends AbstractBox
{

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Jameica: " + Application.getI18n().tr("System-Meldungen");
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
    String[] messages = Application.getWelcomeMessages();
    if (messages == null || messages.length == 0)
      return;
    StringBuffer sb = new StringBuffer();
    sb.append("<form>");
    for (int i=0;i<messages.length;++i)
    {
      sb.append("<li>");
      sb.append(messages[i]);
      sb.append("</li>");
    }
    sb.append("</form>");
    FormTextPart part = new FormTextPart(sb.toString());
    part.paint(parent);
  }

}


/*********************************************************************
 * $Log: SystemMessages.java,v $
 * Revision 1.1  2006/06/29 23:10:01  willuhn
 * @N Box-System aus Hibiscus in Jameica-Source verschoben
 *
 **********************************************************************/