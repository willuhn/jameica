/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/SearchPart.java,v $
 * $Revision: 1.3 $
 * $Date: 2008/09/03 11:14:20 $
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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.internal.dialogs.SearchOptionsDialog;
import de.willuhn.jameica.gui.internal.views.SearchResultView;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.SearchService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;


/**
 * GUI-Modul fuer das Suchformular.
 */
public class SearchPart implements Part
{
  private Composite comp = null;
  private Text search = null;
  
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
      
    final String searchText = Application.getI18n().tr("Suche...");

    this.search = GUI.getStyleFactory().createText(this.comp);
    this.search.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    this.search.setText(searchText);
    this.search.setForeground(Color.COMMENT.getSWTColor());
    this.search.addFocusListener(new FocusAdapter()
    {
      public void focusLost(FocusEvent e)
      {
        if (search == null || search.isDisposed())
          return;
        String currentText = search.getText();
        if (currentText == null || currentText.length() == 0)
        {
          search.setText(searchText);
          search.setForeground(Color.COMMENT.getSWTColor());
        }
      }
    
      /**
       * @see org.eclipse.swt.events.FocusAdapter#focusGained(org.eclipse.swt.events.FocusEvent)
       */
      public void focusGained(FocusEvent e)
      {
        if (search == null || search.isDisposed())
          return;
        String currentText = search.getText();
        if (currentText != null && currentText.equals(searchText))
        {
          search.setText("");
          search.setForeground(Color.WIDGET_FG.getSWTColor());
        }
      }
    });
    this.search.addTraverseListener(new TraverseListener()
    {
    
      public void keyTraversed(TraverseEvent e)
      {
        if (search == null || search.isDisposed())
          return;
        
        String text = search.getText();
        if (text == null || text.length() < 3)
          return; // weniger als 3 Zeichen eingegeben

        SearchService service = (SearchService) Application.getBootLoader().getBootable(SearchService.class);
        List result = service.search(text);
        GUI.startView(SearchResultView.class,result);

        // bewirkt, dass Folge-Events nicht mehr ausgeloest werden.
        // Das kann z.Bsp. sein, wenn gerade ein Dialog mit
        // Default-Button angezeigt wird.
        e.doit = false;
      }
    });
    
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
        catch (Exception ex)
        {
          Logger.error("error while opening options dialog",ex);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Öffnen der Optionen"), StatusBarMessage.TYPE_ERROR));
        }
      }
    
    });
  }

}


/**********************************************************************
 * $Log: SearchPart.java,v $
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
