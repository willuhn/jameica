/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/SearchPart.java,v $
 * $Revision: 1.9 $
 * $Date: 2011/05/11 10:27:25 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.View;
import de.willuhn.jameica.gui.input.QueryInput;
import de.willuhn.jameica.gui.internal.dialogs.SearchOptionsDialog;
import de.willuhn.jameica.gui.parts.Panel;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.search.SearchResult;
import de.willuhn.jameica.services.SearchService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Customizing;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;


/**
 * GUI-Modul fuer das Suchformular.
 */
public class SearchPart implements Part
{
  private Composite comp    = null;
  private QueryInput search = null;
  private boolean started   = false;
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    this.comp = new Composite(parent,SWT.NONE);
    this.comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    GridLayout layout = new GridLayout(2,false);
    layout.horizontalSpacing = 3;
    layout.verticalSpacing   = 3;
    layout.marginHeight = 3;
    layout.marginWidth  = 3;
    this.comp.setLayout(layout);

    this.search = new QueryInput() {
      
      @Override
      public void doSearch(String query)
      {
        startSearch();
      }
    };
    this.search.paint(this.comp);
    
    if (!Customizing.SETTINGS.getBoolean("application.search.hideoptions",false))
    {
      Link link = new Link(this.comp,SWT.NONE);
      link.setText("<a>" + Application.getI18n().tr("Optionen") + "</a>");
      link.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER));
      link.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e)
        {
          try
          {
            new SearchOptionsDialog(SearchOptionsDialog.POSITION_CENTER).open();
          }
          catch (OperationCanceledException oce)
          {
            Logger.info(oce.getMessage());
            return;
          }
          catch (Exception ex)
          {
            Logger.error("error while opening options dialog",ex);
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Öffnen der Optionen"), StatusBarMessage.TYPE_ERROR));
          }
        }
      
      });
    }
  }
  
  /**
   * Fuehrt die Suche aus.
   */
  private void startSearch()
  {
    if (search == null)
      return;
    
    final String text = (String) search.getValue();
    if (text == null || text.length() < 3)
      return; // weniger als 3 Zeichen eingegeben

    try
    {
      final View view = GUI.getView();

      // Wird schon angezeigt.
      if (started)
      {
        view.snapOut();
        started = false;
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
      panel.paint(view.getSnapin());
      view.snapIn();
      started = true;
    }
    catch (Exception ex)
    {
      Logger.error("error while opening search result",ex);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Anzeigen des Suchergebnisses: {0}", ex.getMessage()), StatusBarMessage.TYPE_ERROR));
    }
  }
}


/**********************************************************************
 * $Log: SearchPart.java,v $
 * Revision 1.9  2011/05/11 10:27:25  willuhn
 * @N OCE fangen
 *
 * Revision 1.8  2010-11-03 11:52:18  willuhn
 * @N Anzeige des "Optionen..."-Links via Customizing ausblendbar
 *
 * Revision 1.7  2010-08-17 16:05:32  willuhn
 * @N Update auf SWT 3.6
 * @N Such-Feld ist jetzt ein SWT.SEARCH mit Icons
 *
 * Revision 1.6  2010/02/24 22:44:16  willuhn
 * @B Suche konnte ausgeloest werden - auch wenn das Feld gar keinen Focus hat oder "Suche..." drin stand
 *
 * Revision 1.5  2009/02/23 23:44:11  willuhn
 * @D
 *
 * Revision 1.4  2008/09/03 23:32:14  willuhn
 * @C Suchergebnis nicht mehr als View sondern als Snapin am unteren Rand anzeigen. Dann kann man durch die Elemente klicken, ohne das Suchergebnis zu verlassen
 *
 * Revision 1.3  2008/09/03 11:14:20  willuhn
 * @N Suchfeld anzeigen
 * @N Such-Optionen
 *
 * Revision 1.2  2008/09/03 00:11:43  willuhn
 * @N Erste Version eine funktionsfaehigen Suche - zur Zeit in Navigation.java deaktiviert
 *
 * Revision 1.1  2008/08/31 23:07:10  willuhn
 * @N Erster GUI-Code fuer die Suche
 *
 **********************************************************************/
