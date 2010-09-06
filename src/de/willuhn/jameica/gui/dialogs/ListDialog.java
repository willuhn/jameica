/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/ListDialog.java,v $
 * $Revision: 1.12 $
 * $Date: 2010/09/06 23:49:39 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

/**
 * Dialog, der eine Tabelle mit Daten aus einer Liste anzeigt.
 * Aus dieser Tabelle kann einer ausgewaehlt werden. Dieses Objekt
 * wird dann von <code>open()</code> zurueckgegeben.
 * @author willuhn
 */
public class ListDialog extends AbstractDialog
{
  private Object object            = null;
  private GenericIterator iterator = null;
  private List list                = null;
  private List<Column> columns     = new ArrayList<Column>();

  /**
   * ct.
   * @param list anzuzeigende Liste.
   * @param position Position.
   * @see AbstractDialog#POSITION_CENTER
   * @see AbstractDialog#POSITION_MOUSE
   */
  public ListDialog(GenericIterator list, int position)
  {
    this(position);
    this.iterator = list;
  }
  
  /**
   * ct.
   * @param list anzuzeigende Liste.
   * @param position Position.
   * @see AbstractDialog#POSITION_CENTER
   * @see AbstractDialog#POSITION_MOUSE
   */
  public ListDialog(List list, int position)
  {
    this(position);
    this.list = list;
  }
  
  /**
   * ct.
   * @param position Position.
   * @see AbstractDialog#POSITION_CENTER
   * @see AbstractDialog#POSITION_MOUSE
   */
  private ListDialog(int position)
  {
    super(position);
    setSize(SWT.DEFAULT,250);
  }


  /**
   * Fuegt der Tabelle eine weitere Spalte hinzu.
   * @param title Ueberschrift der Spalte.
   * @param field Feld fuer den anzuzeigenden Wert.
   */
  public void addColumn(String title, String field)
  {
    addColumn(title,field,null);
  }

  /**
   * Fuegt der Tabelle eine weitere Spalte hinzu.
   * @param title Ueberschrift der Spalte.
   * @param field Feld fuer den anzuzeigenden Wert.
   * @param f Formatierer.
   */
  public void addColumn(String title, String field, Formatter f)
  {
    if (title == null || field == null)
      return;
    
    addColumn(new Column(field,title,f));
  }
  
  /**
   * Fuegt eine Spalte hinzu.
   * @param col
   */
  public void addColumn(Column col)
  {
    if (col == null)
      return;
    this.columns.add(col);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    final TablePart table = (iterator != null) ? new TablePart(iterator,new MyAction()) : new TablePart(list,new MyAction());
    
    for (Column c:this.columns)
      table.addColumn(c);

    table.setSummary(false);
    table.setMulti(false);
    table.setRememberColWidths(true);
    table.setRememberOrder(true);
    table.setRememberState(false);
    table.paint(parent);
    
    ButtonArea b = new ButtonArea();
    b.addButton(i18n.tr("Übernehmen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        object = table.getSelection();
        close();
      }
    });
    b.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        object = null;
        throw new OperationCanceledException();
      }
    });
    b.paint(parent);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return object;
  }
  
  /**
   * Hilfsklasse fuer die Aktion beim Doppelklick auf einen Datensatz.
   */
  private class MyAction implements Action
  {
    public void handleAction(Object context) throws ApplicationException
    {
      object = context;
      // Wir schliessen den Dialog bei Auswahl eines Objektes.
      close();
    }
  }
}

/*********************************************************************
 * $Log: ListDialog.java,v $
 * Revision 1.12  2010/09/06 23:49:39  willuhn
 * *** empty log message ***
 *
 **********************************************************************/