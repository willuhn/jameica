/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/Table.java,v $
 * $Revision: 1.4 $
 * $Date: 2003/11/24 17:27:50 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.views.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.jameica.GUI;
import de.willuhn.jameica.I18N;
import de.willuhn.jameica.rmi.DBIterator;
import de.willuhn.jameica.rmi.DBObject;

/**
 * Erzeugt eine Standard-Tabelle.
 * @author willuhn
 */
public class Table
{

  private DBIterator list;
  private Controller controller;
  private ArrayList fields = new ArrayList();

  /**
   * Erzeugt eine neue Standard-Tabelle auf dem uebergebenen Composite.
   * @param list Liste mit Objekten, die angezeigt werden soll.
   * @param controller der die ausgewaehlten Daten dieser Liste empfaengt.
   */
  public Table(DBIterator list, Controller controller)
  {
    this.list = list;
    this.controller = controller;
  }
  
  /**
   * Fuegt der Tabelle eine neue Spalte hinzu.
   * @param title Name der Spaltenueberschrift.
   * @param field Name des Feldes aus dem dbObject, der angezeigt werden soll.
   */
  public void addColumn(String title, String field)
  {
    this.fields.add(new String[]{title,field});
  }
  
  /**
   * Malt die Tabelle auf das uebergebene Composite.
   * @param parent das Composite, auf dem die Tabelle plaziert werden soll.
   */
  public void paint(Composite parent) throws RemoteException
  {

    int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
    final org.eclipse.swt.widgets.Table table = new org.eclipse.swt.widgets.Table(parent, style);
    final GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
    gridData.horizontalSpan = 3;
    table.setLayoutData(gridData);
    table.setLinesVisible(true);
    table.setHeaderVisible(true);

    // Das hier ist eigentlich nur noetig um die Ausrichtung der Spalten zu ermitteln
    DBObject _o = null;
    try {
      _o = list.next();
    }
    catch (Exception e) {
      // das kann ruhig schiefgehen
    }

    for (int i=0;i<this.fields.size();++i)
    {
      final TableColumn col = new TableColumn(table, SWT.NONE);
      String[] fieldData = (String[]) fields.get(i);
      String title = fieldData[0];
      String field = fieldData[1];
      if (title == null) title = "";
      try {
        if (DBObject.FIELDTYPE_DOUBLE.equals(_o.getFieldType(field)) ||
            DBObject.FIELDTYPE_INT. equals(_o.getFieldType(field)))
        {
          col.setAlignment(SWT.RIGHT);
        }
      }
      catch (Exception e)
      {
        // nicht weiter tragisch, wenn das fehlschlaegt
      }
      col.setText(title);
    }

    try {
      list.previous(); // zurueckblaettern nicht vergessen
    }
    catch (Exception e) {
      // das kann ruhig schiefgehen
    }


    // Iterieren ueber alle Elemente der Liste
    while (list.hasNext())
    {
      final TableItem item = new TableItem(table, SWT.NONE);
      final DBObject o = list.next();
      for (int i=0;i<this.fields.size();++i)
      {
        String[] fieldData = (String[]) fields.get(i);
        String field = fieldData[1];
        item.setData(o.getID());
        Object value = o.getField(field);
        if (value == null)
          item.setText(i,"");
        else 
          item.setText(i,value.toString());
      }
    }

    // noch der Listener fuer den Doppelklick drauf.
    table.addListener(SWT.MouseDoubleClick,
      new Listener(){
        public void handleEvent(Event e){
          TableItem item = table.getItem( new Point(e.x,e.y));
            if (item == null) return;
            String id = (String) item.getData();
            if (id == null) return;
            controller.handleLoad(id);
        }
      }
    );

    // und jetzt fuegen wir noch die Kontext-Menues hinzu,
    Menu menu = new Menu(GUI.shell, SWT.POP_UP);
    table.setMenu(menu);
    MenuItem editItem = new MenuItem(menu, SWT.PUSH);
    editItem.setText (I18N.tr("Bearbeiten"));
    editItem.addListener (SWT.Selection, new Listener () {
      public void handleEvent (Event e) {
        TableItem item = table.getItem(table.getSelectionIndex());
        if (item == null) return;
        String id = (String) item.getData();
        if (id == null) return;
        controller.handleLoad(id);
      }
    });


    // Jetzt tun wir noch die Spaltenbreiten neu berechnen.
    int cols = table.getColumnCount();
    for (int i=0;i<cols;++i)
    {
      TableColumn col = table.getColumn(i);
      col.pack();
    }
    
    // So, und jetzt malen wir noch ein Label mit der Anzahl der Treffer drunter.
    Label summary = new Label(parent,SWT.NONE);
    summary.setText(list.size() + " " + (list.size() == 1 ? I18N.tr("Datensatz") : I18N.tr("Datensätze")) + ".");

  }

}

/*********************************************************************
 * $Log: Table.java,v $
 * Revision 1.4  2003/11/24 17:27:50  willuhn
 * @N Context menu in table
 *
 * Revision 1.3  2003/11/24 16:25:53  willuhn
 * @N AbstractDBObject is now able to resolve foreign keys
 *
 * Revision 1.2  2003/11/24 11:51:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/11/22 20:43:05  willuhn
 * *** empty log message ***
 *
 **********************************************************************/