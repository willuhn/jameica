/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/TablePart.java,v $
 * $Revision: 1.10 $
 * $Date: 2004/07/09 00:12:46 $
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.rmi.GenericIterator;
import de.willuhn.datasource.rmi.GenericObject;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.util.I18N;

/**
 * Erzeugt eine Standard-Tabelle.
 * @author willuhn
 */
public class TablePart implements Part
{
	//TODO: Sortierung!
  private GenericIterator list;

  private AbstractControl controller;
  private ArrayList fields = new ArrayList();
  private HashMap formatter = new HashMap();
  private I18N i18n = null;
  private TableFormatter tableFormatter = null;
  private ArrayList menus = new ArrayList();

	private Composite comp = null;
	 
  private org.eclipse.swt.widgets.Table table;

  /**
   * Erzeugt eine neue Standard-Tabelle auf dem uebergebenen Composite.
   * @param list Liste mit Objekten, die angezeigt werden soll.
   * @param controller der die ausgewaehlten Daten dieser Liste empfaengt.
   */
  public TablePart(GenericIterator list, AbstractControl controller)
  {
    this.list = list;
    this.controller = controller;
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
	 * Fuegt einen neuen Kontext Menu-Eintrag zur Tabelle hinzu.
   * @param title Name des Menu-Eintrages.
   * @param l Listener, der ausgefuehrt werden soll. Er erhaelt in <code>event.data</code> das ausgewaehlte Objekt.
   */
  public void addMenu(String title,Listener l)
	{
		if (title == null || l == null)
			return;
		MenuEntry entry = new MenuEntry(title,l);
		menus.add(entry);
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
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
		if (comp != null && !comp.isDisposed())
			SWTUtil.disposeChilds(comp);
		else
		{
			comp = new Composite(parent,SWT.NONE);
			GridData gridData = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
			comp.setLayoutData(gridData);
		}

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
			final TableItem item = new TableItem(table, SWT.NONE);
			final GenericObject o = list.next();
			item.setData(o);

			for (int i=0;i<this.fields.size();++i)
			{
				String[] fieldData = (String[]) fields.get(i);
				String field = fieldData[1];
				Object value = o.getAttribute(field);
				if (value == null)
				{
					// Wert ist null. Also zeigen wir einen Leerstring an
					item.setText(i,"");
				}
				else if (value instanceof GenericObject)
				{
					// Wert ist ein Fremdschluessel. Also zeigen wir dessn Wert an
					GenericObject go = (GenericObject) value;
					item.setText(i,go.getAttribute(go.getPrimaryAttribute()).toString());
				}
				else
				{
					// Regulaerer Wert.
					// Wir schauen aber noch, ob wir einen Formatter haben
					Formatter f = (Formatter) formatter.get(field);
					if (f != null)
					{
						item.setText(i,f.format(value));
					}
					else 
						item.setText(i,value.toString());
				}
			}

			// Ganz zum Schluss schicken wir noch einen ggf. vorhandenen
			// TableFormatter drueber
			if (tableFormatter != null)
				tableFormatter.format(item);

		}

    // noch der Listener fuer den Doppelklick drauf.
    table.addListener(SWT.MouseDoubleClick,
      new Listener(){
        public void handleEvent(Event e){
        		if (controller == null) return;
          TableItem item = table.getItem( new Point(e.x,e.y));
            if (item == null) return;
            Object o = item.getData();
            if (o == null) return;
            controller.handleOpen(o);
        }
      }
    );

		addMenus();
		
    // Jetzt tun wir noch die Spaltenbreiten neu berechnen.
    int cols = table.getColumnCount();
    for (int i=0;i<cols;++i)
    {
      TableColumn col = table.getColumn(i);
      col.pack();
    }

		Label summary = new Label(comp,SWT.NONE);
		summary.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		summary.setBackground(Color.BACKGROUND.getSWTColor());
		summary.setText(list.size() + " " + (list.size() == 1 ? i18n.tr("Datensatz") : i18n.tr("Datensätze")) + ".");

    // Und jetzt rollen wir noch den Pointer der Tabelle zurueck.
    // Damit kann das Control wiederverwendet werden ;) 
	  this.list.begin();

  }


	/**
	 * Sortiert die Tabelle nach der angegebenen Spaltennummer.
   * @param index Spaltennummer.
   */
  public void orderBy(int index)
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

	/**
   * Fuegt die Menus hinzu.
   */
  private void addMenus()
	{
		if (menus.size() == 0)
			return;

		// und jetzt fuegen wir noch die Kontext-Menues hinzu,
		Menu menu = new Menu(comp.getShell(), SWT.POP_UP);
		table.setMenu(menu);

		for (int i=0;i<menus.size();++i)
		{
			final MenuEntry entry = (MenuEntry) menus.get(i);
			if ("-".equals(entry.name))
			{
				new MenuItem(menu, SWT.SEPARATOR);
				continue;
			}

			final MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setText(entry.name);
			item.addListener(SWT.Selection,new Listener() {
				// Wir packen hier noch einen eigenen Listener drum,
				// damit der Aufrufer das Objekt direkt aus dem Event
				// holen kann und nicht erst den selectionIndex-Mist machen muss
				public void handleEvent(Event event) {
					int i = table.getSelectionIndex();
					if (i == -1)
						return;
					TableItem item = table.getItem(i);
					if (item == null) return;
					Object o = item.getData();
					if (o == null) return;
					Event e = new Event();
					e.data = o;
					entry.listener.handleEvent(e);
				}
			});
		}
	}

	/**
	 * Hilfklasse fuer die Menu-Eintraege.
   */
  private class MenuEntry
	{
		private String name;
		private Listener listener;

		private MenuEntry(String name,Listener l)
		{
			this.name = name;
			this.listener = l;
		}
	}
}

/*********************************************************************
 * $Log: TablePart.java,v $
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