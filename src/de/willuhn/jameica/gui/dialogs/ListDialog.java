/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/ListDialog.java,v $
 * $Revision: 1.8 $
 * $Date: 2004/07/23 15:51:20 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.dialogs;

import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.views.AbstractView;

/**
 * Dialog, der eine Tabelle mit Daten aus einer Liste anzeigt.
 * Aus dieser Tabelle kann einer ausgewaehlt werden. Dieses Objekt
 * wird dann von <code>open()</code> zurueckgegeben.
 * @author willuhn
 */
public class ListDialog extends AbstractDialog
{

  private Object object = null;
  private GenericIterator list = null;
  private Hashtable fields = new Hashtable();
  private Hashtable formatter = new Hashtable();

  /**
   * ct.
   * @param list anzuzeigende Liste.
   * @param position Position.
   * @see AbstractDialog#POSITION_CENTER
   * @see AbstractDialog#POSITION_MOUSE
   */
  public ListDialog(GenericIterator list, int position)
  {
    super(position);
    setSize(SWT.DEFAULT,250);
    this.list = list;

  }

  /**
   * Fuegt der Tabelle eine weitere Spalte hinzu.
   * @param title Ueberschrift der Spalte.
   * @param field Feld fuer den anzuzeigenden Wert.
   */
  public void addColumn(String title, String field)
  {
    if (title == null || field == null)
      return;
    this.fields.put(title,field);
  }

  /**
   * Fuegt der Tabelle eine weitere Spalte hinzu.
   * @param title Ueberschrift der Spalte.
   * @param field Feld fuer den anzuzeigenden Wert.
   * @param f Formatierer.
   */
  public void addColumn(String title, String field, Formatter f)
  {
    addColumn(title,field);

    if (title == null || f == null)
      return;
    this.formatter.put(title,f);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception {

    TablePart table = new TablePart(list,new ListController(null));

    Enumeration keys = this.fields.keys();
    while (keys.hasMoreElements())
    {
      String title = (String) keys.nextElement();
      String field = (String) this.fields.get(title);
      Formatter f  = (Formatter) this.formatter.get(title);
      table.addColumn(title,field,f);
    }
    table.paint(parent);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception {
    return object;
  }


  /**
   * Controller, der an der Liste haengt und bei Selection Aktionen durchfuehrt.
   */
  private class ListController extends AbstractControl
  {


    /**
     * ct.
     * @param view die View, fuer die der Controller zustaendig ist.
     */
    public ListController(AbstractView view)
    {
      super(view);
    }

    /**
     * @see de.willuhn.jameica.gui.controller.AbstractControl#handleDelete()
     */
    public void handleDelete() {}
    /**
     * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCancel()
     */
    public void handleCancel() {}
    /**
     * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
     */
    public void handleStore() {}
    /**
     * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
     */
    public void handleCreate() {}
    /**
     * @see de.willuhn.jameica.gui.controller.AbstractControl#handleOpen(java.lang.Object)
     */
    public void handleOpen(Object o)
    {
      object = o;
      // Wir schliessen den Dialog bei Auswahl eines Objektes.
      close();
    }

  }

}

/*********************************************************************
 * $Log: ListDialog.java,v $
 * Revision 1.8  2004/07/23 15:51:20  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.7  2004/06/18 19:47:17  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/04/24 19:05:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/04/21 22:28:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/04/12 19:15:59  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.3  2004/03/06 18:24:23  willuhn
 * @D javadoc
 *
 * Revision 1.2  2004/02/26 18:47:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/24 22:46:53  willuhn
 * @N GUI refactoring
 *
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