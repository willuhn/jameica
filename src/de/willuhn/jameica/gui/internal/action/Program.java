/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/action/Program.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/03/31 22:35:37 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import org.eclipse.ui.forms.events.HyperlinkEvent;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die lediglich versucht, den uebergebenen Context in einen String
 * zu wandeln und via org.eclipse.swt.program.Program.launch auszufuehren.
 * Man kann also z.Bsp. einen String mit einer URL uebergeben. Die Klasse
 * wird diese dann im Browser oeffnen. 
 */
public class Program implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null)
    {
      Logger.warn("no context given, skipping");
      return;
    }
    try
    {
      String s = null;
      if (context instanceof HyperlinkEvent)
        s = ((HyperlinkEvent) context).getLabel();
       else
        s = context.toString();

      Logger.info("trying to launch associated program for context: " + s);
      org.eclipse.swt.program.Program.launch(s);
    }
    catch (Throwable t)
    {
      Logger.error("error while executing program for context " + context,t);
      I18N i18n = Application.getI18n();
      throw new ApplicationException(i18n.tr("Fehler beim Starten des Programms"),t);
    }
  }

}


/**********************************************************************
 * $Log: Program.java,v $
 * Revision 1.1  2005/03/31 22:35:37  web0
 * @N flexible Actions fuer FormTexte
 *
 **********************************************************************/