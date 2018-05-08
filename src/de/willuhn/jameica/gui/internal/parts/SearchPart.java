/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 * GPLv2
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.View;
import de.willuhn.jameica.gui.input.QueryInput;
import de.willuhn.jameica.gui.internal.action.SearchOptions;
import de.willuhn.jameica.gui.parts.Panel;
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.search.SearchResult;
import de.willuhn.jameica.services.SearchService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Customizing;
import de.willuhn.logging.Logger;


/**
 * GUI-Modul fuer das Suchformular.
 */
public class SearchPart implements Part
{
  private QueryInput search = null;
  private boolean started   = false;
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    this.search = new QueryInput() {
      
      @Override
      public void doSearch(String query)
      {
        startSearch();
      }
    };
    this.search.paint(parent);
    
    GUI.getDisplay().addFilter(SWT.KeyUp,new Listener() {
      public void handleEvent(Event event)
      {
        if ((event.stateMask == SWT.CTRL) && (event.keyCode == '^'))
          search.focus();
      }
    });
  }
  
  /**
   * Fuehrt die Suche aus.
   */
  private void startSearch()
  {
    if (this.search == null)
      return;
    
    final String text = (String) this.search.getValue();
    if (text == null || text.length() < 3)
      return; // weniger als 3 Zeichen eingegeben

    try
    {
      final View view = GUI.getView();

      // Wird schon angezeigt.
      if (this.started)
      {
        view.snapOut();
        this.started = false;
      }

      SearchService service = (SearchService) Application.getBootLoader().getBootable(SearchService.class);
      List<SearchResult> result = service.search(text);

      SearchResultPart part = new SearchResultPart(result);
      Panel panel = new Panel(Application.getI18n().tr("Suchergebnis"), part, false);
      panel.addMinimizeListener(new Listener()
      {
        public void handleEvent(Event event)
        {
          view.snapOut();
          started = false;
        }
      });
      
      if (!Customizing.SETTINGS.getBoolean("application.search.hideoptions",false))
        panel.addButton(new PanelButton("document-properties.png",new SearchOptions(),Application.getI18n().tr("Such-Optionen konfigurieren")));

      panel.paint(view.getSnapin());
      view.snapIn();
      this.started = true;
    }
    catch (Exception ex)
    {
      Logger.error("error while opening search result",ex);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Anzeigen des Suchergebnisses: {0}", ex.getMessage()), StatusBarMessage.TYPE_ERROR));
    }
  }
}
