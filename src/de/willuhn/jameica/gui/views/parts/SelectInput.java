/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/SelectInput.java,v $
 * $Revision: 1.3 $
 * $Date: 2003/11/22 20:43:05 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.views.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.GUI;
import de.willuhn.jameica.I18N;
import de.willuhn.jameica.rmi.DBIterator;
import de.willuhn.jameica.rmi.DBObject;

/**
 * @author willuhn
 * Ist zustaendig fuer Eingabefelder des Typs "Select" aka "Combo".
 */
public class SelectInput extends Input
{

  private String[] values;
  private String preselected;
  private Combo combo;

  /**
   * Erzeugt ein neues Eingabefeld und schreib den uebergebenen Wert rein.
   * @param values Werteliste, mit denen die Select-Box gefuellt werden soll. 
   * @param preselected Wert des vorausgewaehlten Feldes.
   */
  public SelectInput(String[] values, String preselected)
  {
    this.preselected = preselected;
    this.values      = values;
  }

  /**
   * Erzeugt ein neues Eingabefeld und schreib den uebergebenen Wert rein.
   * @param values Werteliste, mit denen die Select-Box gefuellt werden soll. 
   * @param preselected Wert des vorausgewaehlten Feldes.
   */
  public SelectInput(ArrayList values, String preselected)
  {
    this.preselected = preselected;
    this.values      = (String[]) values.toArray(new String[]{});
  }

  /**
   * Erzeugt ein neues Eingabefeld und schreib den uebergebenen Wert rein.
   * @param object das darzustellende Objekt. Es wird auch gleich verwendet,
   * um darueber eine Liste zu holen, mit der die Selectbox gefuellt wird. 
   * @param preselected Wert des vorausgewaehlten Feldes.
   * @throws RemoteException
   */
  public SelectInput(DBObject object) throws RemoteException
  {
    this.preselected = (String) object.getField(object.getPrimaryField());

    try {
      DBIterator list = object.getList();
      this.values = new String[list.size()];
      DBObject o = null;
      int i = 0;
      String primaryField = object.getPrimaryField();
      while (list.hasNext())
      {
        o = list.next();
        this.values[i++] = (String) o.getField(primaryField);
      }
    }
    catch (RemoteException e)
    {
      GUI.setActionText(I18N.tr("Fehler beim Lesen der Liste."));
      this.values = new String[0];
    }
  }

  /**
   * Erzeugt ein neues Eingabefeld und schreib den uebergebenen Wert rein.
   * @param list eine Liste von Objekten.
   * @param der Name des Feldes aus der Objektliste, welches fuer die Anzeige verwendet werden soll.
   *        Das Feld muss vom Typ String sein.  
   * @param preselected Wert des vorausgewaehlten Feldes.
   */
  public SelectInput(DBIterator list, String field, String preselected)
  {

    this.preselected = preselected;

    try {
      this.values = new String[list.size()];
      DBObject o = null;
      int i = 0;
      while (list.hasNext())
      {
        o = list.next();
        this.values[i++] = (String) o.getField("field");
      }
    }
    catch (RemoteException e)
    {
      GUI.setActionText(I18N.tr("Fehler beim Lesen der Liste."));
      this.values = new String[0];
    }
  }

  /**
   * @see de.willuhn.jameica.views.parts.Input#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent)
  {

    combo = new Combo(parent,SWT.BORDER | SWT.READ_ONLY);
    combo.setLayoutData(createGrid());
    int selected = 0;
    if (values == null || values.length == 0)
      values = new String[] {""};

    for (int i=0;i<values.length;++i)
    {
      combo.add((values[i] == null ? "" : values[i]));
      if (getValue().equals(values[i]))
        selected = i;
    }
    combo.select(selected);
  }

  /**
   * @see de.willuhn.jameica.views.parts.Input#getValue()
   */
  public String getValue()
  {
    return combo.getText();
  }
}

/*********************************************************************
 * $Log: SelectInput.java,v $
 * Revision 1.3  2003/11/22 20:43:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 * Revision 1.1  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 **********************************************************************/