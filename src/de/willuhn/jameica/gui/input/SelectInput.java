/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/SelectInput.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/04/27 00:04:44 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.input;

import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Style;

/**
 * Ist zustaendig fuer Eingabefelder des Typs "Select" aka "Combo".
 * @author willuhn
 */
public class SelectInput extends AbstractInput
{

  private Hashtable values = new Hashtable();
  private String preselected;
  private CCombo combo;
  
  private boolean enabled = true;

  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param object das darzustellende Objekt. Es wird auch gleich verwendet,
   * um darueber eine Liste zu holen, mit der die Selectbox gefuellt wird. 
   */
  public SelectInput(DBObject object)
  {
  	super();
    DBIterator list = null;
    try {
      this.preselected = (String) object.getField(object.getPrimaryField());
      list = object.getList();
    }
    catch (RemoteException e)
    {
    	Application.getLog().error("error while reading list",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen der Liste."));
    }
    init(list);
  }

	/**
	 * Erzeugt ein neues Eingabefeld und schreib den uebergebenen Wert rein.
	 * @param list Liste mit den anzuzeigenden Objekten.
	 * @param preselected Wert des vorausgewaehlten Feldes.
	 */
	public SelectInput(DBIterator list, String preselected)
	{

		this.preselected = preselected;
		init(list);
	}

	/**
	 * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
	 * @param list Liste mit den anzuzeigenden Texten.
	 * @param preselected Wert des vorausgewaehlten Feldes.
	 */
	public SelectInput(String[] list, String preselected)
	{
		this.preselected = preselected;
		init(list);
		
	}

	/**
	 * Initialisiert die Select-Box.
	 * @param list Liste mit den anzuzeigenden Texten.
	 */
	private void init(String[] list)
	{
		if (list == null)
			list = new String[]{};

		for (int i=0;i<list.length;++i)
		{
			if (list[i] == null || "".equals(list[i]))
				continue; // skip empty values
			values.put(""+list[i],list[i]);
		}
	}

  /**
   * Initialisiert die Select-Box.
   * @param list Liste mit den anzuzeigenden Objekten.
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
        values.put(value,o);

      }
    }
    catch (RemoteException e)
    {
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen der Liste."));
    }
    
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#getControl()
   */
  public Control getControl()
  {

    Composite comp = new Composite(getParent(),SWT.NONE);
    comp.setBackground(Style.COLOR_BORDER);
    combo = new CCombo(comp, SWT.FLAT | SWT.READ_ONLY);
    
    comp.setLayout(new FormLayout());

    FormData comboFD = new FormData();
    comboFD.left = new FormAttachment(0, 1);
    comboFD.top = new FormAttachment(0, 1);
    comboFD.right = new FormAttachment(100, -1);
    comboFD.bottom = new FormAttachment(100, -1);
    combo.setLayoutData(comboFD);
    
    combo.setBackground(Style.COLOR_WHITE);

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
   	combo.setEnabled(enabled);

    return comp;
  }

  /**
   * Liefert den ausgewaehlten Wert.
   * Wenn die Select-Box mit einem String-Array aufgebaut wurde,
   * wird der angezeigte String zurueckgegeben. Bei einem DBObject oder
   * DBIterator wird direkt das Objekt zurueckgegeben.
   * @see de.willuhn.jameica.gui.input.AbstractInput#getValue()
   */
  public Object getValue()
  {
    return values.get(combo.getText());
  }

	/**
	 * Liefert den angezeigten Text zurueck.
   * @return Text.
   */
  public String getText()
	{
		return combo.getText();
	}

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#focus()
   */
  public void focus()
  {
    combo.setFocus();
  }


  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#disable()
   */
  public void disable()
  {
		enabled = false;
		if (combo != null && !combo.isDisposed())
			combo.setEnabled(false);
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#enable()
   */
  public void enable()
  {
    combo.setEnabled(true);
  }

  /**
   * Die Funktion macht nichts.
   * @see de.willuhn.jameica.gui.input.AbstractInput#setValue(java.lang.Object)
   */
  public void setValue(Object o)
  {
  }

}

/*********************************************************************
 * $Log: SelectInput.java,v $
 * Revision 1.3  2004/04/27 00:04:44  willuhn
 * @D javadoc
 *
 * Revision 1.2  2004/04/24 19:05:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.10  2004/04/05 23:29:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/03/24 00:46:03  willuhn
 * @C refactoring
 *
 * Revision 1.8  2004/03/11 08:56:55  willuhn
 * @C some refactoring
 *
 * Revision 1.7  2004/03/06 18:24:23  willuhn
 * @D javadoc
 *
 * Revision 1.6  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.5  2004/02/25 23:11:57  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/02/24 22:46:53  willuhn
 * @N GUI refactoring
 *
 * Revision 1.3  2004/02/18 17:14:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/12 00:49:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.18  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.16  2004/01/06 20:11:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.14  2003/12/16 02:27:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
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