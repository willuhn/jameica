/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/Table.java,v $
 * $Revision: 1.13 $
 * $Date: 2003/12/29 20:07:19 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.views.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import de.willuhn.jameica.Application;
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
  private HashMap formatter = new HashMap();

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
   * Fuegt der Tabelle eine neue Spalte hinzu und dazu noch einen Formatierer.
   * @param title Name der Spaltenueberschrift.
   * @param field Name des Feldes aus dem dbObject, der angezeigt werden soll.
   * @param f Formatter, der fuer die Anzeige des Wertes verwendet werden soll.
   */
  public void addColumn(String title, String field, Formatter f)
  {
    addColumn(title,field);
    
    if (field != null && f != null)
      formatter.put(field,f);
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
        String type = _o.getFieldType(field);
        if (
          DBObject.FIELDTYPE_DOUBLE.equalsIgnoreCase(type) ||
          DBObject.FIELDTYPE_DECIMAL.equalsIgnoreCase(type)
        )
          col.setAlignment(SWT.RIGHT);
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
      // nicht weiter tragisch, wenn das fehlschlaegt
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
        {
          // Wert ist null. Also zeigen wir einen Leerstring an
          item.setText(i,"");
        }
        else if (value instanceof DBObject)
        {
          // Wert ist ein Fremdschluessel. Also zeigen wir dessn Wert an
          DBObject dbo = (DBObject) value;
          item.setText(i,dbo.getField(dbo.getPrimaryField()).toString());
        }
        else
        {
          // Regulaerer Wert.
          // Wir schauen aber noch, ob wir einen Formatter haben
          Formatter f = (Formatter) formatter.get(field);
          if (f != null)
            item.setText(i,f.format(value));
          else 
            item.setText(i,value.toString());
        }
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
//    Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
//    table.setMenu(menu);
//    MenuItem editItem = new MenuItem(menu, SWT.PUSH);
//    editItem.setText (I18N.tr("Bearbeiten"));
//    editItem.addListener (SWT.Selection, new Listener () {
//      public void handleEvent (Event e) {
//        TableItem item = table.getItem(table.getSelectionIndex());
//        if (item == null) return;
//        String id = (String) item.getData();
//        if (id == null) return;
//        controller.handleLoad(id);
//      }
//    });


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

    // Und jetzt rollen wir noch den Pointer der Tabelle zurueck.
    // Damit kann das Control wiederverwendet werden ;) 
    try {
      this.list.begin();
    }
    catch (RemoteException e)
    {
      if (Application.DEBUG)
        e.printStackTrace();
      Application.getLog().warn("unable to restore list back to first element. this list will not be reusable.");
    }

  }

}

/*********************************************************************
 * $Log: Table.java,v $
 * Revision 1.13  2003/12/29 20:07:19  willuhn
 * @N Formatter
 *
 * Revision 1.12  2003/12/26 21:43:30  willuhn
 * @N customers changable
 *
 * Revision 1.11  2003/12/19 01:43:27  willuhn
 * @N added Tree
 *
 * Revision 1.10  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.9  2003/12/10 00:47:12  willuhn
 * @N SearchDialog done
 * @N ErrorView
 *
 * Revision 1.8  2003/12/08 15:41:09  willuhn
 * @N searchInput
 *
 * Revision 1.7  2003/12/05 17:12:23  willuhn
 * @C SelectInput
 *
 * Revision 1.6  2003/11/27 00:22:18  willuhn
 * @B paar Bugfixes aus Kombination RMI + Reflection
 * @N insertCheck(), deleteCheck(), updateCheck()
 * @R AbstractDBObject#toString() da in RemoteObject ueberschrieben (RMI-Konflikt)
 *
 * Revision 1.5  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
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