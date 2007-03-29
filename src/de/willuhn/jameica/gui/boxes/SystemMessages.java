/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/boxes/SystemMessages.java,v $
 * $Revision: 1.4 $
 * $Date: 2007/03/29 15:29:48 $
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
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isActive()
   */
  public boolean isActive()
  {
    String[] messages = Application.getWelcomeMessages();
    return super.isActive() && messages != null && messages.length > 0;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isEnabled()
   */
  public boolean isEnabled()
  {
    String[] messages = Application.getWelcomeMessages();
    return messages != null && messages.length > 0;
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
    String[] messages = Application.getWelcomeMessages();
    if (messages == null || messages.length == 0)
      return;
    StringBuffer sb = new StringBuffer();
    sb.append("<form>");
    sb.append("<p><span color=\"header\" font=\"header\">" + Application.getI18n().tr("System-Meldungen") + "</span></p>");
    for (int i=0;i<messages.length;++i)
    {
      if (messages[i] == null || messages[i].length() == 0)
        continue;
      
      sb.append("<li>");
      
      // Wenn in dem Text die Begriffe "Error", "Fehler" oder "Exception" auftreten, markieren wir es gleich rot
      if (messages[i].toLowerCase().matches(".*(error|fehler|exception).*"))
        sb.append("<span color=\"error\">" + messages[i] + "</span>");
      else
        sb.append(messages[i]);
      sb.append("</li>");
    }
    sb.append("<br/><br/><p>Klicken Sie ggf. unten rechts auf die Jameica-Statusleiste, um die letzten Meldungen des System-Logs anzuzeigen.</p>");
    sb.append("</form>");
    FormTextPart part = new FormTextPart(sb.toString());
    part.paint(parent);
  }

}


/*********************************************************************
 * $Log: SystemMessages.java,v $
 * Revision 1.4  2007/03/29 15:29:48  willuhn
 * @N Uebersichtlichere Darstellung der Systemstart-Meldungen
 *
 * Revision 1.3  2006/07/03 23:10:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2006/06/30 13:51:34  willuhn
 * @N Pluginloader Redesign in HEAD uebernommen
 *
 * Revision 1.1  2006/06/29 23:10:01  willuhn
 * @N Box-System aus Hibiscus in Jameica-Source verschoben
 *
 **********************************************************************/