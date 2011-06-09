/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/SearchDialog.java,v $
 * $Revision: 1.6 $
 * $Date: 2011/06/09 11:09:25 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SearchInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.search.Result;
import de.willuhn.jameica.search.SearchResult;
import de.willuhn.jameica.services.SearchService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog, der eine Suche mit Drop-Down-Ergebnissen anzeigt.
 */
public class SearchDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getI18n();
  
  private final static int WINDOW_WIDTH = 400;
  
  private Result result = null;
  
  /**
   * ct.
   */
  public SearchDialog()
  {
    super(POSITION_CENTER);
    this.setTitle(i18n.tr("Suche..."));
    this.setSize(WINDOW_WIDTH,SWT.DEFAULT);
    this.setSideImage(SWTUtil.getImage("system-search-large.png"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container container = new SimpleContainer(parent);

    LabelInput label = new LabelInput(i18n.tr("Bitte geben Sie einen Suchbegriff ein."));
    label.setName("");
    label.setColor(Color.COMMENT);
    container.addInput(label);
    
    final SearchInput input = new SearchInput()
    {
      /**
       * @see de.willuhn.jameica.gui.input.SearchInput#format(java.lang.Object)
       */
      protected String format(Object bean)
      {
        String space = (bean instanceof Result) ? "   " : "";
        return space + super.format(bean);
      }

      /**
       * @see de.willuhn.jameica.gui.input.SearchInput#startSearch(java.lang.String)
       */
      public List startSearch(String text)
      {
        SearchService service = (SearchService) Application.getBootLoader().getBootable(SearchService.class);
        List<SearchResult> providers = service.search(text);
        List list = new ArrayList();
        for (SearchResult sr:providers)
        {
          try
          {
            List<Result> results = sr.getResult();
            if (results == null || results.size() == 0)
              continue;
            list.add(sr.getSearchProvider());
            list.addAll(results);
          }
          catch (Exception e)
          {
            Logger.error("unable to search in " + sr.getSearchProvider().getName() + ", ignoring",e);
          }
        }
        return list;
      }
    };
    input.setName("");
    input.setAttribute("name");
    input.setDelay(1000);
    input.setStartAt(3);
    input.setSearchString("");
    input.setMinWidth(500);
    input.focus();
    input.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        Object value = input.getValue();
        if (value instanceof Result)
        {
          result = (Result) value;
          close();
        }
        else
          input.setText(""); // Es wurde ein Provider ausgewaehlt, Suchbegriff resetten
      }
    });
    container.addInput(input);
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Abbrechen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    },null,false,"process-stop.png");
    container.addButtonArea(buttons);
    
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return this.result;
  }

}



/**********************************************************************
 * $Log: SearchDialog.java,v $
 * Revision 1.6  2011/06/09 11:09:25  willuhn
 * @C Wartezeit etwas verlaengert
 *
 * Revision 1.5  2011-06-09 10:31:00  willuhn
 * @C Wartezeit etwas verlaengert
 *
 * Revision 1.4  2011-05-13 11:11:27  willuhn
 * @N Neuer Such-Dialog, der mit CTRL+^ geoeffnet werden kann. Damit kann man jetzt schnell mal was suchen, ohne die Maus benutzen zu muessen
 *
 **********************************************************************/