/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/SearchDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/02/23 20:30:34 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.dialogs;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.parts.Formatter;
import de.willuhn.jameica.gui.parts.Table;
import de.willuhn.jameica.gui.views.AbstractView;

/**
 * Basisklasse fuer Such-Dialoge.
 * Dieser Dialoge kann erstmal nur jeweils eine Tabelle enthalten, in
 * der ein Datensatz ausgewaehlt werden kann.
 * @author willuhn
 */
public abstract class SearchDialog extends AbstractDialog
{

  private Table table;
  private String id = null;

	/**
	 * ct.
   * @param list anzuzeigende Liste.
   * @param position Position.
   * @see AbstractDialog#POSITION_CENTER
   * @see AbstractDialog#POSITION_MOUSE
   */
  public SearchDialog(DBIterator list, int position)
	{
		super(position);
		this.table = new Table(list,new SearchController(null));
		
	}

  /**
   * Fuegt der Tabelle des Such-Dialogs eine weitere Spalte hinzu.
   * @param title Ueberschrift der Spalte.
   * @param field Feld fuer den anzuzeigenden Wert.
   */
  protected void addColumn(String title, String field)
  {
    if (table == null)
    {
      Application.getLog().warn("table not initialized, skipping column " + title);
      return;
    }
    this.table.addColumn(title,field);
  }
  
  /**
   * Fuegt der Tabelle des Such-Dialogs eine weitere Spalte hinzu.
   * @param title Ueberschrift der Spalte.
   * @param field Feld fuer den anzuzeigenden Wert.
   * @param f optionaler Formatierer.
   */
  protected void addColumn(String title, String field, Formatter f)
  {
    if (table == null)
    {
      Application.getLog().warn("table not initialized, skipping column " + title);
      return;
    }
    this.table.addColumn(title,field,f);
  }



	/**
	 * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
	 */
	public void paint(Composite parent) throws Exception {
    if (table == null)
    {
      Application.getLog().warn("table not initialized");
      return;
    }

    table.paint(parent);
  }

	/**
	 * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
	 */
	public Object getData() throws Exception {
		return id;
	}

  /**
   * Das ist eigentlich eher ein Dummy-AbstractControl. Wir erstellen hier nur einen,
   * weil er von der Tabelle verlangt wird.
   * @author willuhn
   */
  class SearchController extends AbstractControl
  {


    /**
     * @param view
     */
    public SearchController(AbstractView view)
    {
      super(view);
    }

    /**
     * @see de.willuhn.jameica.views.parts.AbstractControl#handleDelete()
     */
    public void handleDelete() {}

    /**
     * @see de.willuhn.jameica.views.parts.AbstractControl#handleDelete(java.lang.String)
     */
    public void handleDelete(String id) {}

    /**
     * @see de.willuhn.jameica.views.parts.AbstractControl#handleCancel()
     */
    public void handleCancel() {}

    /**
     * @see de.willuhn.jameica.views.parts.AbstractControl#handleStore()
     */
    public void handleStore() {}

    /**
     * @see de.willuhn.jameica.views.parts.AbstractControl#handleCreate()
     */
    public void handleCreate() {}

    /**
     * @see de.willuhn.jameica.views.parts.AbstractControl#handleLoad(java.lang.String)
     */
    public void handleLoad(String s)
    {
      id = s;
    }
    
  }

}

/*********************************************************************
 * $Log: SearchDialog.java,v $
 * Revision 1.2  2004/02/23 20:30:34  willuhn
 * @C refactoring in AbstractDialog
 *
 * Revision 1.1  2004/02/22 20:05:21  willuhn
 * @N new Logo panel
 *
 * Revision 1.12  2004/02/18 17:14:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/01/28 20:51:25  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.10  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/01/08 20:50:33  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.8  2003/12/29 20:07:19  willuhn
 * @N Formatter
 *
 * Revision 1.7  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.6  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.4  2003/12/10 01:12:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/12/10 00:47:12  willuhn
 * @N SearchDialog done
 * @N FatalErrorView
 *
 * Revision 1.2  2003/12/08 16:19:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/12/08 15:41:09  willuhn
 * @N searchInput
 *
 **********************************************************************/