/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/SelectInput.java,v $
 * $Revision: 1.12 $
 * $Date: 2003/12/10 23:51:55 $
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
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;

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

  private Hashtable values = new Hashtable();
  private String preselected;
  private Combo combo;

  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param object das darzustellende Objekt. Es wird auch gleich verwendet,
   * um darueber eine Liste zu holen, mit der die Selectbox gefuellt wird. 
   * @param preselected Wert des vorausgewaehlten Feldes.
   */
  public SelectInput(DBObject object)
  {
    DBIterator list = null;
    try {
      this.preselected = (String) object.getField(object.getPrimaryField());
      list = object.getList();
    }
    catch (RemoteException e)
    {
      GUI.setActionText(I18N.tr("Fehler beim Lesen der Liste."));
    }
    init(list);
  }

  /**
   * Initialisiert die Select-Box.
   * @param list
   */
  private void init(DBIterator list)
  {
    try
    {
      if (list == null)
        throw new RemoteException();

      DBObject o = null;
      while (list.hasNext())
      {
        o = list.next();
        String value = o.getField(o.getPrimaryField()).toString();
        if (value == null || "".equals(value))
          continue; // skip empty values
        values.put(value,o.getID());

      }
    }
    catch (RemoteException e)
    {
      GUI.setActionText(I18N.tr("Fehler beim Lesen der Liste."));
    }
    
  }

  /**
   * Erzeugt ein neues Eingabefeld und schreib den uebergebenen Wert rein.
   * @param list eine Liste von Objekten.
   * @param preselected Wert des vorausgewaehlten Feldes.
   */
  public SelectInput(DBIterator list, String preselected)
  {

    this.preselected = preselected;
    init(list);
  }

  /**
   * @see de.willuhn.jameica.views.parts.Input#getControl()
   */
  public Control getControl()
  {

    combo = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);

    int selected = 0;

    Enumeration e = values.keys();
    int i = 0;
    while (e.hasMoreElements())
    {
      String value = (String) e.nextElement();
      combo.add(value);
      if (preselected != null && preselected.equals(value))
        selected = i;
      ++i;
    }
    combo.select(selected);

    return combo;
  }

  /**
   * @see de.willuhn.jameica.views.parts.Input#getValue()
   */
  public String getValue()
  {
    return (String) values.get(combo.getText());
  }
  /**
   * @see de.willuhn.jameica.views.parts.Input#focus()
   */
  public void focus()
  {
    combo.setFocus();
  }

  /**
   * @see de.willuhn.jameica.views.parts.Input#setValue(java.lang.String)
   */
  public void setValue(String value)
  {
  }
}

/*********************************************************************
 * $Log: SelectInput.java,v $
 * Revision 1.12  2003/12/10 23:51:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2003/12/05 17:12:23  willuhn
 * @C SelectInput
 *
 * Revision 1.10  2003/12/01 21:22:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2003/12/01 20:28:58  willuhn
 * @B filter in DBIteratorImpl
 * @N InputFelder generalisiert
 *
 * Revision 1.8  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
 * Revision 1.7  2003/11/24 17:27:50  willuhn
 * @N Context menu in table
 *
 * Revision 1.6  2003/11/24 14:21:53  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2003/11/24 11:51:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2003/11/23 19:26:27  willuhn
 * *** empty log message ***
 *
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