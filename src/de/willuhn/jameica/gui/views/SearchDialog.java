/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/SearchDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/12/08 15:41:09 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.views;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.GUI;
import de.willuhn.jameica.I18N;
import de.willuhn.jameica.rmi.DBObject;
import de.willuhn.jameica.views.parts.Controller;
import de.willuhn.jameica.views.parts.LabelGroup;
import de.willuhn.jameica.views.parts.Table;

/**
 * Basisklasse fuer Such-Dialoge.
 * @author willuhn
 */
public abstract class SearchDialog
{

  private DBObject object;

  /**
   * Erzeugt einen neuen Suchdialog fuer das uebergebene Objekt.
   * @param object das Objekt.
   */
  public SearchDialog(DBObject object)
  {
    this.object = object;
  }

  /**
   * Wird aufgerufen, wenn der Dialog geoffnet wird und liefert einen
   * String mit dem ausgewaehlten Objekt zurueck.
   * @return
   */
  public String open()
  {
    try {
      final Shell shell = new Shell();
      final Table table = new Table(this.object.getList(),new SearchController(this.object));
      table.addColumn("Feld",object.getPrimaryField());
      table.paint(shell);

      Label label = new Label(shell,SWT.BORDER);
      label.setText("Foooo");
      shell.setText ("Dialog");
      // shell.setSize (200, 200);
      shell.open();
      while (!shell.isDisposed ()) {
        if (!display.readAndDispatch ()) display.sleep ();
      }
      display.dispose ();    }
    catch (RemoteException e)
    {
      Application.getLog().error("unable to open search dialog");
      GUI.setActionText(I18N.tr("Fehler beim Öffnen des Such-Dialogs."));
    }
    return "foo";
  }

  class SearchController extends Controller
  {

    /**
     * @param object
     */
    public SearchController(DBObject object)
    {
      super(object);
    }

    /**
     * @see de.willuhn.jameica.views.parts.Controller#handleDelete()
     */
    public void handleDelete()
    {
    }

    /**
     * @see de.willuhn.jameica.views.parts.Controller#handleDelete(java.lang.String)
     */
    public void handleDelete(String id)
    {
    }

    /**
     * @see de.willuhn.jameica.views.parts.Controller#handleCancel()
     */
    public void handleCancel()
    {
    }

    /**
     * @see de.willuhn.jameica.views.parts.Controller#handleStore()
     */
    public void handleStore()
    {
    }

    /**
     * @see de.willuhn.jameica.views.parts.Controller#handleCreate()
     */
    public void handleCreate()
    {
    }

    /**
     * @see de.willuhn.jameica.views.parts.Controller#handleLoad(java.lang.String)
     */
    public void handleLoad(String id)
    {
      // TODO Auto-generated method stub
      
    }
    
  }

}

/*********************************************************************
 * $Log: SearchDialog.java,v $
 * Revision 1.1  2003/12/08 15:41:09  willuhn
 * @N searchInput
 *
 **********************************************************************/