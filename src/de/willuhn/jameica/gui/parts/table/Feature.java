/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts.table;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Control;

import de.willuhn.jameica.gui.parts.AbstractTablePart;
import de.willuhn.jameica.gui.parts.ContextMenu;

/**
 * Gemeinsames Interface fuer Features, die zu einer Tabelle nachgeruestet werden koennen.
 */
public interface Feature
{
  /**
   * Liste der Events, auf die ein Feature reagieren kann.
   */
  public enum Event
  {
    /**
     * Wird ausgeloest, wenn die paint()-Funktion der Tabelle aufgerufen wird.
     */
    PAINT,
    
    /**
     * Wird ausgeloest, wenn die Anzeige explizit aktualisiert werden soll.
     */
    REFRESH,
    
    /**
     * Wird ausgeloest, wenn ein Datensatz hinzugefuegt wurde.
     */
    ADDED,
    
    /**
     * Wird ausgeloest, wenn ein Datensatz entfernt wurde.
     */
    REMOVED,
    
    /**
     * Wird ausgeloest, wenn die komplette Tabelle geleert wurde.
     */
    REMOVED_ALL,
  }
  
  /**
   * Hilfsklasse mit Meta-Informationen fuer das Feature.
   */
  public class Context
  {
    /**
     * Die SWT-Control selbst.
     */
    public Control control;
    
    /**
     * Das Context-Menu der Tabelle.
     */
    public ContextMenu menu;
    
    /**
     * Die Tabelle selbst.
     */
    public AbstractTablePart part;
    
    /**
     * Optionale Angabe des Datensatzes, auf den sich das Event bezieht.
     * Ist nur bei einigen Events gesetzt - z.Bsp. ADDED, REMOVED.
     */
    public Object data;
    
    /**
     * Generische Map, in der die von TablePart abgeleiteten Klassen beliebige
     * weitere Daten hinzufuegen koennen, die an das Feature weitergereicht
     * werden sollen.
     */
    public Map addon = new HashMap();
  }
  
  /**
   * Liefert true, wenn das Feature auf das angegebene Event reagieren soll.
   * @param e das Event.
   * @return true, wenn es auf das angegebene Event reagieren soll.
   */
  public boolean onEvent(Event e);
  
  /**
   * Wird aufgerufen, wenn das angegebene Event ausgeloest wurde.
   * @param e das Event.
   * @param ctx Context-Infos.
   */
  public void handleEvent(Event e, Context ctx);
}



/**********************************************************************
 * $Log$
 **********************************************************************/