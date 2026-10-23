/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.gui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.internal.buttons.Cancel;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Dialog, der eine durchsuchbare Tabelle mit Daten aus einer Liste anzeigt.
 */
public class SearchableListDialog extends AbstractDialog
{
  private Object object            = null;
  private String text              = null;
  private List list                = null;
  private List<Column> columns     = new ArrayList<Column>();
  private TableFormatter formatter = null;

  /**
   * ct.
   * @param list anzuzeigende Liste.
   * @param position Position.
   */
  public SearchableListDialog(List list, int position)
  {
    super(position);
    this.list = list;
    setSize(SWT.DEFAULT,420);
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
   * @param col Spalte.
   */
  public void addColumn(Column col)
  {
    if (col == null)
      return;
    this.columns.add(col);
  }

  /**
   * Definiert einen optionalen Formatter fuer die Tabelle.
   * @param formatter Formatter.
   */
  public void setFormatter(TableFormatter formatter)
  {
    this.formatter = formatter;
  }
  
  /**
   * Fügt noch einen optionalen Hinweis-Text hinzu.
   * @param text der anzuzeigende Text.
   */
  public void setText(String text)
  {
    if (text == null)
      return;
    
    this.text = text;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container container = new SimpleContainer(parent,true,1);
    
    if (this.text != null && this.text.length() > 0)
      container.addText(this.text,true);

    final TablePart table = new TablePart(this.list,new MyAction());

    final Text search = new Text(container.getComposite(),SWT.SEARCH | SWT.ICON_SEARCH | SWT.CANCEL);
    search.setMessage(i18n.tr("Suche..."));
    search.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    search.addModifyListener(new ModifyListener()
    {
      public void modifyText(ModifyEvent event)
      {
        try
        {
          applySearch(table,list,search.getText());
        }
        catch (Exception e)
        {
          Logger.error("unable to filter searchable list dialog",e);
        }
      }
    });

    for (Column c:this.columns)
      table.addColumn(c);

    if (this.formatter != null)
      table.setFormatter(this.formatter);

    table.removeFeature(FeatureSummary.class);
    table.setMulti(false);
    table.setRememberColWidths(true);
    table.setRememberOrder(true);
    table.setRememberState(false);
    container.addPart(table);
    
    ButtonArea b = new ButtonArea();
    b.addButton(i18n.tr("\u00dcbernehmen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        object = table.getSelection();
        close();
      }
    },null,false,"ok.png");
    b.addButton(new Cancel());
    
    container.addButtonArea(b);
  }

  /**
   * Wendet den Suchfilter auf die Tabelle an.
   * @param table Tabelle.
   * @param source vollstaendige Quellliste.
   * @param query Suchbegriff.
   * @throws Exception
   */
  private void applySearch(TablePart table, List source, String query) throws Exception
  {
    String filter = query == null ? null : query.trim().toLowerCase();
    table.removeAll();

    for (Object item:source)
    {
      if (filter == null || filter.length() == 0 || matches(item,filter))
        table.addItem(item);
    }
  }

  /**
   * Prueft, ob der Suchbegriff in einer Spalte enthalten ist.
   * @param item Eintrag.
   * @param filter Suchbegriff in Kleinschreibung.
   * @return true, wenn passend.
   */
  private boolean matches(Object item, String filter)
  {
    for (Column column:this.columns)
    {
      try
      {
        Object value = BeanUtil.get(item,column.getColumnId());
        String display = column.getFormattedValue(value,item);
        if (display != null && display.toLowerCase().contains(filter))
          return true;
      }
      catch (Exception e)
      {
        // Fehlerhafte Spaltenwerte ignorieren wir fuer die Suche.
      }
    }
    return false;
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
      close();
    }
  }
}
