/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/TablePart.java,v $
 * $Revision: 1.25 $
 * $Date: 2004/11/12 18:23:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.parts;

import java.rmi.RemoteException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Erzeugt eine Standard-Tabelle.
 * @author willuhn
 */
public class TablePart implements Part
{
	// Ordentliche Sortierung fehlt!
  private GenericIterator list					= null;
	private Action action									= null;
  private ArrayList fields 							= new ArrayList();
  private HashMap formatter 						= new HashMap();
  private I18N i18n 										= null;
  private TableFormatter tableFormatter = null;
	private ContextMenu menu 							= null;

	private boolean showSummary						= true;

	private Composite parent							= null;
	private Composite comp 								= null;
	private Label summary									= null;
	 
  private org.eclipse.swt.widgets.Table table = null;

	private int size = 0;

  /**
   * Erzeugt eine neue Standard-Tabelle auf dem uebergebenen Composite.
   * @param list Liste mit Objekten, die angezeigt werden soll.
   * @param action die beim Doppelklick auf ein Element ausgefuehrt wird.
   */
  public TablePart(GenericIterator list, Action action)
  {
    this.list = list;
    this.action = action;
		i18n = Application.getI18n();
  }
  
	/**
	 * Definiert einen optionalen Formatierer, mit dem man SWT-maessig ganze Zeilen formatieren kann.
   * @param formatter Formatter.
   */
  public void setFormatter(TableFormatter formatter)
	{
		this.tableFormatter = formatter;
	}

  /**
	 * Fuegt ein KontextMenu zur Tabelle hinzu.
   * @param menu das anzuzeigende Menu.
   */
  public void setContextMenu(ContextMenu menu)
	{
		this.menu = menu;
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
   * Schaltet die Anzeige einer Summenzeile am Ende der Tabelle an (Default).
   */
  public void enableSummary()
	{ 
		this.showSummary = true;
	}

	/**
	 * Schaltet die Anzeige einer Summenzeile am Ende der Tabelle aus.
	 */
	public void disableSummary()
	{
		this.showSummary = false;
	}

  /**
   * Legt fest, bis zu welchem Element gescrollt werden soll.
   * @param i Index des Elementes, welches nach dem Scrollen als erstes angezeigt werden soll.
   */
  public void setTopIndex(int i)
  {
    if (table == null)
      return;
    table.setTopIndex(i);
  }

	/**
	 * Entfernt das genannte Element aus der Tabelle.
	 * Wurde die Tabelle mit einer Liste von Objekten erzeugt, die von <code>DBObject</code>
	 * abgeleitet sind, muss das Loeschen nicht manuell vorgenommen werden. Die Tabelle
	 * fuegt in diesem Fall automatisch jedem Objekt einen Listener hinzu, der
	 * beim Loeschen des Objektes benachrichtigt wird. Die Tabelle entfernt
	 * das Element dann selbstaendig.
	 * Hinweis: Der im Konstruktor verwendete GenericIterator zum initialen Befuellen
	 * der Tabelle wird hierbei nicht angefasst. 
   * @param item zu entfernendes Element.
   * @throws RemoteException
   */
  public void removeItem(GenericObject item) throws RemoteException
	{
		if (table == null || item == null || table.isDisposed())
			return;
		TableItem[] items = table.getItems();
		GenericObject o = null;
		for (int i=0;i<items.length;++i)
		{
			
			try
			{
				o = (GenericObject) items[i].getData();
				if (item.equals(o))
				{
					table.remove(i);
					size--;
					refreshSummary();
					return;
				}
				
			}
			catch (Throwable t)
			{
				Logger.error("error while removing item",t);
			}
		}
	}

	/**
	 * Fuegt der Tabelle am Ende ein Element hinzu.
	 * Hinweis: Der im Konstruktor verwendete GenericIterator zum initialen Befuellen
	 * der Tabelle wird hierbei nicht angefasst. 
   * @param object hinzuzufuegendes Element.
   * @throws RemoteException
   */
  public void addItem(GenericObject object) throws RemoteException
	{
		addItem(object,size());
	}

	/**
	 * Fuegt der Tabelle ein Element hinzu.
   * @param object hinzuzufuegendes Element.
   * @param index Position, an der es eingefuegt werden soll.
   * @throws RemoteException
   */
  public void addItem(final GenericObject object, int index) throws RemoteException
	{
		final TableItem item = new TableItem(table, SWT.NONE,index);

		// hihi, wenn es sich um ein DBObject handelt, haengen wir einen
		// Listener dran, der uns ueber das Loeschen des Objektes
		// benachrichtigt. Dann koennen wir es automatisch aus der
		// Tabelle werfen.
		if (object instanceof DBObject)
		{
			((DBObject)object).addDeleteListener(new de.willuhn.datasource.rmi.Listener()
      {
        public void handleEvent(de.willuhn.datasource.rmi.Event e) throws RemoteException
        {
        	removeItem(e.getObject());
        }
      });
		}
		item.setData(object);

		for (int i=0;i<this.fields.size();++i)
		{
			String[] fieldData = (String[]) fields.get(i);
			String field = fieldData[1];
			Object value = object.getAttribute(field);

			if (value == null)
				item.setText(i,"");
			else
				item.setText(i,value.toString());

			if (value instanceof GenericObject)
			{
				// Wert ist ein Fremdschluessel. Also zeigen wir dessn Wert an
				GenericObject go = (GenericObject) value;
				Object attribute = go.getAttribute(go.getPrimaryAttribute());
				item.setText(i,attribute == null ? "" : attribute.toString());
			}

			// Formatter vorhanden?
			Formatter f = (Formatter) formatter.get(field);
			if (f != null)
				item.setText(i,f.format(value));
		}

		// Ganz zum Schluss schicken wir noch einen ggf. vorhandenen
		// TableFormatter drueber
		if (tableFormatter != null)
			tableFormatter.format(item);

		// Tabellengroesse anpassen
		size++;
	}

	/**
	 * Liefert die Anzahl der Elemente in dieser Tabelle.
   * @return Anzahl der Elemente.
   */
  public int size()
	{
		return size;
	}

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
		this.parent = parent;

		if (comp != null && !comp.isDisposed())
		{
			comp.dispose();
		}

		comp = new Composite(parent,SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
		comp.setLayoutData(gridData);
		comp.setBackground(Color.BACKGROUND.getSWTColor());

		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		comp.setLayout(layout);

    int flags = SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

    table = GUI.getStyleFactory().createTable(comp, flags);
    table.setLayoutData(new GridData(GridData.FILL_BOTH));
    table.setLinesVisible(true);
    table.setHeaderVisible(true);
    

		// Beim Schreiben der Titles schauen wir uns auch mal das erste Objekt an. 
		// Vielleicht sind ja welche dabei, die man rechtsbuendig ausrichten kann.
		list.begin();
		GenericObject test = list.hasNext() ? list.next() : null;

    for (int i=0;i<this.fields.size();++i)
    {
      final TableColumn col = new TableColumn(table, SWT.NONE);
      String[] fieldData = (String[]) fields.get(i);
      String title = fieldData[0];
      String field = fieldData[1];
			col.setText(title == null ? "" : title);

			// Sortierung
			final int p = i;
			col.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					orderBy(p);
				}
			});

			// Evtl. rechts ausrichten
			if (test != null)
			{
				Object value = test.getAttribute(field);
				if (value instanceof Double)  col.setAlignment(SWT.RIGHT);
				if (value instanceof Integer) col.setAlignment(SWT.RIGHT);
				if (value instanceof Number)  col.setAlignment(SWT.RIGHT);
			}
    }

		list.begin(); // zurueckblaettern nicht vergessen
		while (list.hasNext())
		{
			addItem(list.next());
		}

    // noch der Listener fuer den Doppelklick drauf.
    table.addListener(SWT.MouseDoubleClick,
      new Listener(){
        public void handleEvent(Event e){
        		if (action == null) return;
          TableItem item = table.getItem( new Point(e.x,e.y));
            if (item == null) return;
            Object o = item.getData();
            if (o == null) return;
            try
            {
							action.handleAction(o);
            }
            catch (ApplicationException ae)
            {
            	GUI.getStatusBar().setErrorText(ae.getMessage());
            }
        }
      }
    );

		// jetzt noch dem Menu Bescheid sagen, wenn ein Element markiert wurde
		table.addListener(SWT.MouseDown,new Listener()
    {
      public void handleEvent(Event e)
      {
      	if (menu == null) return;

				TableItem item = table.getItem( new Point(e.x,e.y));
				menu.setCurrentObject(item != null ? item.getData() : null);
      }
    });
    
    // Und jetzt noch das ContextMenu malen
    if (menu != null)
    	menu.paint(table);
		
    // Jetzt tun wir noch die Spaltenbreiten neu berechnen.
    int cols = table.getColumnCount();
    for (int i=0;i<cols;++i)
    {
      TableColumn col = table.getColumn(i);
      col.pack();
    }

		refreshSummary();
		
    // Und jetzt rollen wir noch den Pointer der Tabelle zurueck.
    // Damit kann das Control wiederverwendet werden ;) 
	  this.list.begin();

  }

	/**
   * Aktualisiert die Summenzeile.
   */
  private void refreshSummary()
	{
		if (!showSummary)
			return;
		if (summary == null)
		{
			summary = new Label(comp,SWT.NONE);
			summary.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			summary.setBackground(Color.BACKGROUND.getSWTColor());
		}
		summary.setText(size() + " " + (size() == 1 ? i18n.tr("Datensatz") : i18n.tr("Datensätze")) + ".");
	}

	/**
	 * Sortiert die Tabelle nach der angegebenen Spaltennummer.
   * @param index Spaltennummer.
   */
  private void orderBy(int index)
	{
		final TableItem[] items = table.getItems();
		Collator collator = Collator.getInstance(Application.getConfig().getLocale());

		for (int s = 1; s < items.length; ++s)
		{

			String value1 = table.getItem(s).getText(index);

			for (int j = 0; j < s; ++j)
			{

				String value2 = table.getItem(j).getText(index);

				if (collator.compare(value1, value2) < 0)
				{
					final TableItem item = new TableItem(table, SWT.NONE, j);
					for (int r=0;r<table.getColumnCount();++r)
					{
						item.setText(r,items[s].getText(r));
					}
					item.setData(items[s].getData());
					items[s].dispose();
					if (tableFormatter != null)
						tableFormatter.format(item);
					break;
				}

			}
		}
	}
}

/*********************************************************************
 * $Log: TablePart.java,v $
 * Revision 1.25  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.24  2004/11/10 17:48:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.23  2004/11/05 20:00:43  willuhn
 * @D javadoc fixes
 *
 * Revision 1.22  2004/10/26 23:47:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.21  2004/10/25 17:59:15  willuhn
 * @N aenderbare Tabellen
 *
 * Revision 1.20  2004/10/19 23:33:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2004/10/08 13:38:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/09/14 23:27:57  willuhn
 * @C redesign of service handling
 *
 * Revision 1.17  2004/09/13 23:27:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/08/16 19:15:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/08/11 23:37:21  willuhn
 * @N Navigation ist jetzt modular erweiterbar
 *
 * Revision 1.14  2004/07/23 15:51:20  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.13  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.12  2004/07/20 21:47:44  willuhn
 * @N ContextMenu
 *
 * Revision 1.11  2004/07/20 00:16:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/07/09 00:12:46  willuhn
 * @C Redesign
 *
 * Revision 1.9  2004/06/17 22:07:12  willuhn
 * @C cleanup in tablePart and statusBar
 *
 * Revision 1.8  2004/06/17 00:05:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.6  2004/05/11 23:32:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/05/09 17:40:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/05/04 23:05:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/04/27 00:04:44  willuhn
 * @D javadoc
 *
 * Revision 1.2  2004/04/19 22:53:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.14  2004/04/05 23:29:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/04/01 22:07:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/03/30 22:08:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/03/24 00:46:03  willuhn
 * @C refactoring
 *
 * Revision 1.10  2004/03/18 01:24:47  willuhn
 * @C refactoring
 *
 * Revision 1.9  2004/03/11 08:56:55  willuhn
 * @C some refactoring
 *
 * Revision 1.8  2004/03/06 18:24:23  willuhn
 * @D javadoc
 *
 * Revision 1.7  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.6  2004/02/26 18:47:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/02/24 22:46:53  willuhn
 * @N GUI refactoring
 *
 * Revision 1.4  2004/02/22 20:05:21  willuhn
 * @N new Logo panel
 *
 * Revision 1.3  2004/02/18 17:14:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/18 01:40:29  willuhn
 * @N new white style
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.19  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.17  2004/01/06 20:11:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/01/06 01:27:30  willuhn
 * @N table order
 *
 * Revision 1.15  2004/01/04 19:51:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/01/03 18:08:06  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
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
 * @N FatalErrorView
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