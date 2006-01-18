/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/AbstractView.java,v $
 * $Revision: 1.3 $
 * $Date: 2006/01/18 00:35:57 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 ****************************************************************************/
package de.willuhn.jameica.gui;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.util.ApplicationException;

/**
 * Basis-Klasse fuer alles Views.
 * @author willuhn
 */
public abstract class AbstractView
{

	private Object currentObject;
	private Composite parent;

  /**
   * Wird aufgerufen, wenn der Dialog geoeffnet wird.
   * Diese Methode muss von abgeleiteteten Klassen ueberschrieben werden, um dort den Content zu malen.
   * @throws Exception kann von der View geworfen werden, wenn ein Fehler
   * waehrend des Erstellens der View aufgetreten ist und die View diesen
   * Fehler nicht behandeln moechte. Die GUI uebernimmt das dann, indem Sie
   * stattdessen eine Fehlerseite mit dem Message-Text der Exception anzeigt.
   * Es ist also ratsam, in den Text der Exception etwas sinnvolles reinzuschreiben,
   * weil es dem Benutzer angezeigt wird.
   */
  public abstract void bind() throws Exception;

  /**
   * Wird aufgerufen, wenn der Dialog verlassen wird.
   * Diese Methode kann von abgeleiteten Klassen ueberschrieben werden, um dort Aufraeumarbeiten vorzunehmen.
   * @throws ApplicationException darf vom Dialog geworfen werden, wenn der neue Dialog
   * nicht gestartet werden soll. Z.Bsp. weil Daten noch nicht gespeichert worden oder
   * ein Vorgang noch in Bearbeitung ist.
   */
  public void unbind() throws ApplicationException
  {
  }
  
  /**
   * Kann von ableitenden Klassen ueberschrieben werden, um den Dialog neu zu laden.
   * @throws ApplicationException
   */
  public void reload() throws ApplicationException
  {
    
  }

	/**
   * Liefert das dieser View uebergebene Daten-Objekt zurueck. 
   * @return Liefert das Business-Objekt fuer das der Dialog zustaendig ist.
   */
  public final Object getCurrentObject()
	{
		return currentObject;
	}

	/**
	 * Speichert das zu dieser View gehoerende Daten-Objekt.
   * @param o das Business-Objekt.
   */
  public final void setCurrentObject(Object o)
	{
		this.currentObject = o;
	}

	/**
	 * Liefert das Composite, in dem der Dialog gemalt wird.
   * @return Parent-Composite.
   */
  public final Composite getParent()
	{
		return this.parent;
	}

	/**
	 * Speichert das Composite, in dem der Dialog gemalt werden soll.
	 * Wenn diese Funktion benutzt wird, muss sie zwingend vor
	 * bind() geschehen, da es sonst zu spaet ist ;).
   * @param p das Parent-Composite.
   */
  public final void setParent(Composite p)
	{
		this.parent = p;
	}
}



/***************************************************************************
 * $Log: AbstractView.java,v $
 * Revision 1.3  2006/01/18 00:35:57  web0
 * @N AbstractView#reload
 *
 * Revision 1.2  2004/10/29 16:16:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/08 13:38:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/02/22 20:05:21  willuhn
 * @N new Logo panel
 *
 * Revision 1.12  2004/02/20 20:45:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/01/28 20:51:25  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.10  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.8  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.7  2003/12/28 22:58:27  willuhn
 * @N synchronize mode
 *
 * Revision 1.6  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.5  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 * Revision 1.4  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 * Revision 1.3  2003/10/29 00:41:27  willuhn
 * *** empty log message ***
 *
 ***************************************************************************/