/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/TreePart.java,v $
 * $Revision: 1.15 $
 * $Date: 2007/03/22 22:36:47 $
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.security.Checksum;
import de.willuhn.util.ApplicationException;

/**
 * Erzeugt einen Baum.
 * Dabei werden alle Kind-Objekte rekursiv dargestellt.
 * @author willuhn
 */
public class TreePart extends AbstractTablePart
{

  private TreeFormatter formatter   = null;
  private Action action             = null;
  private GenericIterator list      = null;
  private Tree tree                 = null;

  private String id                 = null;

    
	/**
   * Erzeugt einen neuen Tree basierend auf dem uebergebenen Objekt.
   * @param object Das Objekt, fuer das der Baum erzeugt werden soll. 
   * @param action Action, die bei der Auswahl eines Elements
   * ausgeloest werden soll.
   */
  public TreePart(GenericObjectNode object, Action action)
	{
    this.action = action;
    try
    {
      this.list = PseudoIterator.fromArray(new GenericObject[]{object});
    }
    catch (RemoteException re)
    {
      Logger.error("unable to add item to table",re);
    }
	}

  /**
   * Erzeugt einen neuen Tree basierend auf der uebergebenen Liste
   * von Objekten des Typs GenericObjectNode. Enthaelt der
   * Iterator Objekte, die <b>nicht</b> von GenericObjectNode
   * abgeleitet sind, wird er eine ClassCastException werfen.
   * @param list Liste mit Objekten, fuer die der Baum erzeugt werden soll.
   * @param action Action, die bei der Auswahl eines Elements
   * ausgeloest werden soll.
   */
  public TreePart(GenericIterator list, Action action)
  {
		this.action = action;
    this.list = list;
  }

  /**
   * Definiert einen optionalen Formatierer, mit dem man SWT-maessig ganze Zeilen formatieren kann.
   * @param formatter Formatter.
   */
  public void setFormatter(TreeFormatter formatter)
  {
    this.formatter = formatter;
  }

  /**
   * Liefert eine fuer die Tabelle eindeutige ID.
   * @return eindeutige ID.
   * @throws Exception
   */
  private String getID() throws Exception
  {
    if (this.id != null)
      return id;

    this.list.begin();
    StringBuffer sb = new StringBuffer();
    if (this.list.hasNext())
      sb.append(this.list.next().getClass().getName());

    for (int i=0;i<this.columns.size();++i)
    {
      Column col = (Column) this.columns.get(i);
      sb.append(col.columnId);
    }

    String s = sb.toString();
    if (s == null || s.length() == 0)
      s = "unknown";
    this.id = Checksum.md5(s.getBytes());
    return this.id;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    if (this.list == null)
      throw new RemoteException("Keine darstellbaren Objekte übergeben.");

    /////////////////////////////////////////////////////////////////
    // Tree erzeugen
    this.tree = new org.eclipse.swt.widgets.Tree(parent, SWT.BORDER);
    final GridData gridData = new GridData(GridData.FILL_BOTH);
    this.tree.setLayoutData(gridData);
    
    // Listener fuer "Folder auf machen"
    this.tree.addListener(SWT.Expand, new Listener() {
      public void handleEvent(Event event) {
        handleFolderOpen(event);
      }
    });
    // Listener fuer "Folder auf machen"
    this.tree.addListener(SWT.Collapse, new Listener() {
      public void handleEvent(Event event) {
        handleFolderClose(event);
      }
    });

    // Listener fuer die Doppelklick und Menu.
    this.tree.addMouseListener(new MouseAdapter()
    {
      public void mouseDoubleClick(MouseEvent e)
      {
        handleSelect(e);
      }
      public void mouseDown(MouseEvent e)
      {
        handleMenu(e);
      }
    });

    // Spalten hinzufuegen
    if (this.columns.size() > 0)
    {
      this.tree.setHeaderVisible(true);
      this.tree.setLinesVisible(true);

      GenericObject test = list.hasNext() ? list.next() : null;

      for (int i=0;i<this.columns.size();++i)
      {
        Column col = (Column) this.columns.get(i);
        final TreeColumn tc = new TreeColumn(this.tree,SWT.LEFT);
        tc.setText(col.name == null ? "" : col.name);

        // Testobjekt laden fuer Ausrichtung von Spalten
        if (test != null)
        {
          Object value = test.getAttribute(col.columnId);
          if (value instanceof Number)  tc.setAlignment(SWT.RIGHT);
        }

        // Wenn wir uns die Spalten merken wollen, duerfen
        // wir den DisposeListener nicht an die Tabelle haengen
        // sondern an die TreeColumns. Denn wenn das Dispose-
        // Event fuer die Tabelle kommt, hat sie ihre TableColumns
        // bereits disposed. Mit dem Effekt, dass ein table.getColumn(i)
        // eine NPE werfen wuerde.
        if (rememberColWidth)
        {
          final int index = i;
          tc.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
              try
              {
                if (tc == null || tc.isDisposed())
                  return;
                settings.setAttribute("width." + getID() + "." + index,tc.getWidth());
              }
              catch (Exception ex)
              {
                Logger.error("unable to store width for column " + index,ex);
              }
            }
          });
        }
      
      }

      // Liste zuruecksetzen
      if (test != null)
        list.begin();
    }
    
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Nutzdaten einfuegen
    while (list.hasNext())
      new Item(null,(GenericObject) list.next());
    /////////////////////////////////////////////////////////////////
    
    /////////////////////////////////////////////////////////////////
    // Alles aufklappen
    TreeItem[] items = this.tree.getItems();
    for (int i=0;i<items.length;++i)
      expand(items[i]);
    /////////////////////////////////////////////////////////////////
    
    // Jetzt tun wir noch die Spaltenbreiten neu berechnen.
    int cols = this.tree.getColumnCount();
    for (int i=0;i<cols;++i)
    {
      TreeColumn col = this.tree.getColumn(i);
      if (rememberColWidth)
      {
        int size = 0;
        try
        {
          size = settings.getInt("width." + getID() + "." + i,0);
        }
        catch (Exception e)
        {
          Logger.error("unable to restore column width",e);
        }
        if (size <= 0)
          col.pack();
        else
          col.setWidth(size);
      }
      else
      {
        col.pack();
      }
    }
    
    // Und jetzt noch das ContextMenu malen
    if (menu != null)
      menu.paint(this.tree);
  }
  
  /**
   * Klappt das Element und alle Kinder dessen auf.
   * @param item
   */
  private void expand(TreeItem item)
  {
    if (item == null || item.isDisposed())
      return;

    TreeItem[] children = item.getItems();
    for (int i=0;i<children.length; ++i)
      expand(children[i]);

    // Zum Schluss klappen wir uns selbst auf
    item.setExpanded(true);
  }
  
	/**
   * Behandelt das Event "Ordner auf".
   * @param event das ausgeloeste Event.
   */
  private void handleFolderOpen(Event event)
	{
		Widget widget = event.item;
		if (!(widget instanceof TreeItem))
			return;
		TreeItem item = (TreeItem) widget;
		item.setImage(SWTUtil.getImage("folderopen.gif"));
	}

	/**
	 * Behandelt das Event "Ordner zu".
	 * @param event das ausgeloeste Event.
	 */
	private void handleFolderClose(Event event)
	{
		Widget widget = event.item;
		if (!(widget instanceof TreeItem))
			return;
		TreeItem item = (TreeItem) widget;
  	item.setImage(SWTUtil.getImage("folder.gif"));
	}

  /**
   * Oeffnet das Menu. 
   * @param event das ausgeloeste Event.
   */
  private void handleMenu(MouseEvent event)
  {
    if (menu == null) return;

    Widget widget = tree.getItem(new Point(event.x,event.y));
    if (!(widget instanceof TreeItem))
      return;
    TreeItem item = (TreeItem) widget;

    menu.setCurrentObject(item.getData());
  }

  /**
	 * Behandelt das Event "action". 
	 * @param event das ausgeloeste Event.
	 */
	private void handleSelect(MouseEvent event)
	{
		Widget widget = tree.getItem(new Point(event.x,event.y));
		if (!(widget instanceof TreeItem))
			return;
		TreeItem item = (TreeItem) widget;

    Object o = item.getData();
    if (o == null)
      return;
    try
    {
			this.action.handleAction(o);
    }
    catch (ApplicationException e)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
	}


	/**
   * Bildet ein einzelnes Element des Baumes ab.
   * Es laedt rekursiv alle Kind-Elemente.
   */
  class Item {

		private final TreeItem item;

		/**
		 * ct. Laed ein neues Element des Baumes.
     * @param parent das Eltern-Element.
     * @param data das Fachobjekt.
     * @throws RemoteException
     */
    Item(TreeItem parent, GenericObject data) throws RemoteException
		{
      if (parent == null)
        this.item = new TreeItem(tree,SWT.NONE); // Root-Element
      else
        this.item = new TreeItem(parent,SWT.NONE); // Kind-Element

			item.setData(data);

      if (columns.size() == 0)
      {
        item.setText(getValue(data,null));
      }
      else
      {
        for (int i=0;i<columns.size();++i)
        {
          item.setText(i,getValue(data,(Column) columns.get(i)));
        }
      }
      
      // Ganz zum Schluss schicken wir noch einen ggf. vorhandenen
      // TableFormatter drueber
      if (formatter != null)
        formatter.format(item);

      // Kinder laden
      if (data instanceof GenericObjectNode)
      {
        final GenericObjectNode node = (GenericObjectNode) data;
        final GenericIterator children = node.getChildren();
        
        // Nur als Ordner darstellen, wenn es Kinder hat
        item.setImage(SWTUtil.getImage(children != null && children.size() > 0 ? "folder.gif" : "page.gif"));

        // load the childs
        if (children != null)
        {
          while(children.hasNext())
          {
            try
            {
              new Item(this.item,children.next());
            }
            catch (Exception e)
            {
              e.printStackTrace();
              break;
            }
          }
        }
      }
      else
      {
        item.setImage(SWTUtil.getImage("page.gif"));
      }
		}

    /**
     * Liefert den Wert eines Attributes des Objektes.
     * @param object das Objekt.
     * @param die Spalte.
     * @return Wert des Attributs.
     * @throws RemoteException
     */
    private String getValue(GenericObject object, Column col) throws RemoteException
    {
      if (object == null)
        return "";

      String attribute = null;

      if (col == null || col.columnId == null)
        attribute = object.getPrimaryAttribute();
      else
        attribute = col.columnId;

      Object value = object.getAttribute(attribute);
      if (value instanceof GenericObject)
      {
        GenericObject foreign = (GenericObject) value;
        value = foreign.getAttribute(foreign.getPrimaryAttribute());
      }

      if (value == null)
        return "";
      
      if (col == null || col.formatter == null)
        return value.toString();

      return col.formatter.format(value);
    }
  }

  
}


/*********************************************************************
 * $Log: TreePart.java,v $
 * Revision 1.15  2007/03/22 22:36:47  willuhn
 * @N Contextmenu in Trees
 * @C Kategorie-Baum in separates TreePart ausgelagert
 *
 * Revision 1.14  2007/03/21 18:42:16  willuhn
 * @N Formatter fuer TreePart
 * @C mehr gemeinsamer Code in AbstractTablePart
 *
 * Revision 1.13  2007/03/08 18:55:49  willuhn
 * @N Tree mit Unterstuetzung fuer Spalten
 *
 * Revision 1.12  2007/01/18 09:49:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2006/04/20 08:44:03  web0
 * @C s/Childs/Children/
 *
 * Revision 1.10  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
 * Revision 1.9  2004/11/05 20:00:44  willuhn
 * @D javadoc fixes
 *
 * Revision 1.8  2004/10/20 12:08:17  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.7  2004/10/08 13:38:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/07/23 15:51:20  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.5  2004/07/09 00:12:46  willuhn
 * @C Redesign
 *
 * Revision 1.4  2004/06/17 00:05:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.2  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.1  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.4  2004/03/06 18:24:23  willuhn
 * @D javadoc
 *
 * Revision 1.3  2004/02/24 22:46:53  willuhn
 * @N GUI refactoring
 *
 * Revision 1.2  2004/02/22 20:05:21  willuhn
 * @N new Logo panel
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.6  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.4  2003/12/30 03:41:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/12/29 20:07:19  willuhn
 * @N Formatter
 *
 * Revision 1.2  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.1  2003/12/19 01:43:27  willuhn
 * @N added Tree
 *
 **********************************************************************/