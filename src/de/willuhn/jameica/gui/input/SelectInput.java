/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/SelectInput.java,v $
 * $Revision: 1.11 $
 * $Date: 2004/07/23 15:51:20 $
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
import java.util.Hashtable;

import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Control;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.Logger;

/**
 * Ist zustaendig fuer Eingabefelder des Typs "Select" aka "Combo".
 * @author willuhn
 */
public class SelectInput extends AbstractInput
{

  private GenericIterator list;
  private GenericObject preselected;
  private CCombo combo;

	private Hashtable values = new Hashtable();

  private boolean enabled = true;

  /**
   * Erzeugt eine neue Combo-Box und schreibt die Werte der uebergebenen Liste rein.
   * @param object das darzustellende Objekt. Es wird auch gleich verwendet,
   * um darueber eine Liste zu holen, mit der die Selectbox gefuellt wird. 
   * @param list Liste von Objekten.
   * @param preselected das Object, welches vorselektiert sein soll. Optional.
   */
  public SelectInput(GenericIterator list, GenericObject preselected)
  {
  	super();
  	this.list = list;
  	this.preselected = preselected;
  }

	/**
   * Erzeugt eine neue Combo-Box und schreibt die Werte der uebergebenen Liste rein.
   * @param list Liste der Werte.
   * @param preselected der String, welcher vorselektiert sein soll. Optional.
   */
  public SelectInput(String[] list, String preselected)
	{
		GenericObject[] s = new GenericObject[list.length];
		for (int i=0;i<list.length;++i)
		{
			s[i] = new StringObject(list[i]);
		}
		this.preselected = preselected == null ? null : new StringObject(preselected);
		this.list = PseudoIterator.fromArray(s);
	}

  /**
   * @see de.willuhn.jameica.gui.input.Input#getControl()
   */
  public Control getControl()
  {

		combo = GUI.getStyleFactory().createCombo(getParent());

    int selected = 0;
		int i = 0;
    try
    {
      while (list.hasNext())
      {
      	final GenericObject current = list.next();
      	String name = current.getAttribute(current.getPrimaryAttribute()).toString();
      
      	// Da CCombo und Combo nicht fuer jeden Eintrag Daten speichern
      	// koennen, muessen wir das selbst tun.
      	values.put(name,current);
      	combo.add(name);
      	if (preselected != null && current.equals(preselected))
      		selected = i;
      	++i;
      }
    }
    catch (RemoteException e)
    {
    	combo.add(Application.getI18n().tr("Fehler beim Laden der Daten"));
    	Logger.error("uanble to create combo box",e);
    }
    combo.select(selected);
   	combo.setEnabled(enabled);

    return combo;
  }

  /**
   * Liefert das ausgewaehlte GenericObject.
   * Folglich kann der Rueckgabewert direkt nach GenericObject gecastet werden.
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    Object o = values.get(combo.getText());
    if (o instanceof StringObject)
    {
    	try {
				return ((StringObject)o).getAttribute(null);
    	}
    	catch (RemoteException e)
    	{
    		Logger.error("unable to read from select box",e);
    	}
    }
    return o;
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
   * @see de.willuhn.jameica.gui.input.Input#focus()
   */
  public void focus()
  {
    combo.setFocus();
  }


  /**
   * @see de.willuhn.jameica.gui.input.Input#disable()
   */
  public void disable()
  {
		enabled = false;
		if (combo != null && !combo.isDisposed())
			combo.setEnabled(false);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#enable()
   */
  public void enable()
  {
    combo.setEnabled(true);
  }

  /**
   * Die Funktion macht nichts.
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object o)
  {
  }

	/**
	 * Kleine Hilfsklasse, um String-Werte ueber GenericObject abzubilden.
   */
  private class StringObject implements GenericObject
	{

		private String value;

		/**
		 * ct.
     * @param value
     */
    private StringObject(String value)
		{
			this.value = value;
		}

    /**
     * @see de.willuhn.datasource.rmi.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) throws RemoteException
    {
      return value;
    }

    /**
     * @see de.willuhn.datasource.rmi.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return value;
    }

    /**
     * @see de.willuhn.datasource.rmi.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "name";
    }

    /**
     * @see de.willuhn.datasource.rmi.GenericObject#equals(de.willuhn.datasource.rmi.GenericObject)
     */
    public boolean equals(GenericObject other) throws RemoteException
    {
    	if (other == null)
    		return false;
      return getID().equals(other.getID());
    }
	}

}

/*********************************************************************
 * $Log: SelectInput.java,v $
 * Revision 1.11  2004/07/23 15:51:20  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.10  2004/07/21 23:54:53  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.9  2004/07/09 00:12:47  willuhn
 * @C Redesign
 *
 * Revision 1.8  2004/06/30 20:58:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/06/18 19:47:17  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/06/17 00:05:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.4  2004/05/04 23:05:16  willuhn
 * *** empty log message ***
 *
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