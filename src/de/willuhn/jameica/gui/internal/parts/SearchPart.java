/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/SearchPart.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/08/31 23:07:10 $
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

import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.system.Application;


/**
 * GUI-Modul fuer das Suchformular.
 */
public class SearchPart implements Part
{
  private Text search = null;
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    if (this.search != null && !this.search.isDisposed())
      this.search.dispose();
      
    final String searchText = Application.getI18n().tr("Suche...");

    this.search = GUI.getStyleFactory().createText(parent);
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

        //SearchService service = (SearchService) Application.getBootLoader().getBootable(SearchService.class);
        // List result = service.search(text);
        // bewirkt, dass Folge-Events nicht mehr ausgeloest werden.
        // Das kann z.Bsp. sein, wenn gerade ein Dialog mit
        // Default-Button angezeigt wird.
        e.doit = false;
      }
    
    });
  }

}


/**********************************************************************
 * $Log: SearchPart.java,v $
 * Revision 1.1  2008/08/31 23:07:10  willuhn
 * @N Erster GUI-Code fuer die Suche
 *
 **********************************************************************/
