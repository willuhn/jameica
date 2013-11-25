/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
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

  /**
   * @see de.willuhn.jameica.gui.AbstractView#canBookmark()
   */
  public boolean canBookmark()
  {
    return false;
  }

}
