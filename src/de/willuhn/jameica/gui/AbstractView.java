/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
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
    GUI.startView(this.getClass(),this.getCurrentObject());
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
  
  /**
   * Liefert einen Hilfetext fuer die View.
   * Normalerweise liegen die Hilfetexte in help/${locale} in Form
   * von Text-Dateien und werden automatisch geladen, wenn sie existieren.
   * Durch diese Funktion hier existiert eine Alternative, mit der
   * die Hilfetexte auch zur Laufzeit erzeugt werden koennen.
   * @return der Hilfetext fuer die View.
   */
  public String getHelp()
  {
    return null;
  }
  
  /**
   * Liefert true, wenn die View gebookmarkt werden kann.
   * Kann ueberschrieben werden, wenn nicht gewuenscht.
   * @return true, wenn die View gebookmarkt werden kann. Default ist true.
   */
  public boolean canBookmark()
  {
    return true;
  }
  
  /**
   * Liefert true, wenn an die View Attachments gehängt werden können.
   * Kann ueberschrieben werden, wenn nicht gewuenscht.
   * @return true, wenn an die View Attachments angehängt werden können. Default ist true.
   */
  public boolean canAttach()
  {
    return true;
  }
}
