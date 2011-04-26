/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/views/SearchResultView.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/04/26 11:51:24 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.views;

import java.util.List;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.parts.SearchResultPart;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;


/**
 * Zeigt die Ergebnisse der Suche als Baum an.
 */
public class SearchResultView extends AbstractView
{
  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    I18N i18n = Application.getI18n();
    GUI.getView().setTitle(i18n.tr("Suchergebnis"));
    
    SearchResultPart part = new SearchResultPart((List)getCurrentObject());
    part.paint(getParent());
  }

}


/**********************************************************************
 * $Log: SearchResultView.java,v $
 * Revision 1.3  2011/04/26 11:51:24  willuhn
 * @R Back-Button entfernt
 *
 * Revision 1.2  2008/09/03 00:21:07  willuhn
 * @C SearchResultPart in "internal"-Package verschoben (wo auch schon das SearchPart ist)
 *
 * Revision 1.1  2008/09/03 00:11:43  willuhn
 * @N Erste Version eine funktionsfaehigen Suche - zur Zeit in Navigation.java deaktiviert
 *
 **********************************************************************/
