/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/SearchDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/02/22 20:05:21 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.dialogs;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.parts.Formatter;
import de.willuhn.jameica.gui.parts.Table;
import de.willuhn.jameica.gui.util.Style;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.util.I18N;

/**
 * Basisklasse fuer Such-Dialoge.
 * Dieser Dialoge kann erstmal nur jeweils eine Tabelle enthalten, in
 * der ein Datensatz ausgewaehlt werden kann.
 * @author willuhn
 */
public abstract class SearchDialog
{

  private Table table;
  private String title;
  private Shell shell;

  private String id = null;

  /**
   * Setzt die Liste, die fuer den Dialog verwendet werden soll.
   * @param list die anzizeigende Liste.
   */
  protected void setList(DBIterator list)
  {
    if (list == null)
    {
      Application.getLog().error("unable to init a search dialog without given list.");
      return;
    }
    this.table = new Table(list,new SearchController(null));
  }

  /**
   * Setzt den Titel des Suchdialogs.
   * @param title Titel des Such-Dialogs.
   */
  protected void setTitle(String title)
  {
    if (title == null || "".equals(title))
      Application.getLog().debug("given title for search dialog is null, skipping.");
    
    this.title = title;
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
   * Diese Funktion bitte aufrufen, um den Such-Dialog zu oeffnen.
   * @return den Wert des definierten Feldes des ausgewaehlten Objektes. 
   */
  public String open()
  {
    if (table == null)
    {
      Application.getLog().warn("table not initialized");
      return null;
    }

    try {
      Display display = GUI.getDisplay();
      shell = new Shell(GUI.getShell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
      shell.setText(title == null ? I18N.tr("Suche") : title);
      shell.setLocation(display.getCursorLocation());
      shell.setBackground(Style.COLOR_BG);
      shell.setLayout(new GridLayout(1,false));

      table.paint(shell);

      shell.pack();
      shell.setSize(shell.getBounds().width,300); // die Hoehe legen wir auf 300 Pixel fest (unabhaengig vom Inhalt)
      shell.open();
      while (!shell.isDisposed()) {
        if (!display.readAndDispatch()) display.sleep();
      }
      return load(id);
    }
    catch (RemoteException e)
    {
      Application.getLog().error("unable to open search dialog");
      GUI.setActionText(I18N.tr("Fehler beim Öffnen des Such-Dialogs."));
    }
    return "";
  }

  /**
   * Diese Funktion muss von abgeleiteten Suchdialogen implementiert werden.
   * Sie wird von der open() Funktion aufgerufen und uebergibt ihr die ID
   * des ausgewaehlten Objektes. Es ist Sache des implementierenden Dialoges,
   * welchen Wert des Objektes der Suchdialog letztendlich zurueckgeben soll.
   * @param id ID des ausgewaehlten Objektes.
   * @return der tatsaechlich zurueckzugebende Wert.
   */
  protected abstract String load(String id);

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
      shell.dispose();
    }
    
  }

}

/*********************************************************************
 * $Log: SearchDialog.java,v $
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