/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/SearchDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2003/12/08 16:19:06 $
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.GUI;
import de.willuhn.jameica.I18N;
import de.willuhn.jameica.rmi.DBObject;
import de.willuhn.jameica.views.parts.Controller;
import de.willuhn.jameica.views.parts.Table;

/**
 * Basisklasse fuer Such-Dialoge.
 * @author willuhn
 */
public abstract class SearchDialog extends Dialog
{

  private DBObject object;

  public SearchDialog(Shell parent, int style) {
    super(parent, style);
  }

  public SearchDialog(Shell parent) {
    this(parent, 0);
  }

  public void setObject(DBObject object)
  {
    this.object = object;
  }

  /**
   * Wird aufgerufen, wenn der Dialog geoffnet wird und liefert einen
   * String mit dem ausgewaehlten Objekt zurueck.
   * @return
   */
  public Object open()
  {
    try {
      Shell parent = getParent();
      Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
      shell.setText(getText());
      shell.setSize(400,200);
      shell.setLayout(new GridLayout(1,false));

      final Table table = new Table(this.object.getList(),new SearchController(this.object));
      table.addColumn("Feld",object.getPrimaryField());
      table.paint(shell);

      shell.open();
      Display display = parent.getDisplay();
      while (!shell.isDisposed()) {
        if (!display.readAndDispatch()) display.sleep();
      }
      return object;
    }
    catch (RemoteException e)
    {
      Application.getLog().error("unable to open search dialog");
      GUI.setActionText(I18N.tr("Fehler beim Öffnen des Such-Dialogs."));
    }
    return null;
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
 * Revision 1.2  2003/12/08 16:19:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/12/08 15:41:09  willuhn
 * @N searchInput
 *
 **********************************************************************/