/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/boxes/SystemMessages.java,v $
 * $Revision: 1.8 $
 * $Date: 2010/03/18 11:38:03 $
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

import org.apache.commons.lang.StringEscapeUtils;
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
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#getHeight()
   */
  public int getHeight()
  {
    return 250;
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
      if (messages[i] == null || messages[i].length() == 0)
        continue;
      
      sb.append("<li>");
      
      String text = StringEscapeUtils.escapeXml(messages[i]);
      text = text.replaceAll("\n","<br/>");
      
      // Wenn in dem Text die Begriffe "Error", "Fehler" oder "Exception" auftreten, markieren wir es gleich rot
      if (text.toLowerCase().matches(".*(error|fehler|exception).*"))
        sb.append("<span color=\"error\">" + Application.getI18n().tr("Fehler") + "</span><br/>");
      sb.append(text);
      sb.append("</li>");
    }
    sb.append("<br/><br/><p>" + Application.getI18n().tr("Klicken Sie ggf. auf das Pfeil-Symbol in der Jameica-Statusleiste, um die letzten Meldungen des System-Logs anzuzeigen.") + "</p>");
    sb.append("</form>");
    FormTextPart part = new FormTextPart(sb.toString());
    part.paint(parent);
  }

}


/*********************************************************************
 * $Log: SystemMessages.java,v $
 * Revision 1.8  2010/03/18 11:38:03  willuhn
 * @N Ausfuehrlichere und hilfreichere Fehlermeldung, wenn Hibiscus-Datenbank defekt ist oder nicht geoeffnet werden konnte.
 *
 * Revision 1.7  2008/12/11 00:00:37  willuhn
 * @C Box wird sonst in voller verfuegbarer Hoehe angezeigt.
 *
 * Revision 1.6  2008/01/07 23:31:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2007/12/18 17:10:14  willuhn
 * @N Neues ExpandPart
 * @N Boxen auf der Startseite koennen jetzt zusammengeklappt werden
 *
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