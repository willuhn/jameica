/**********************************************************************
 *
 * Copyright (c) 2016 Olaf Willuhn
 * GNU GPLv2
 *
 **********************************************************************/

package de.willuhn.jameica.gui.input;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

import de.willuhn.jameica.gui.Part;

/**
 * Basis-Interface fuer alle Eingabefelder in Jameica.
 */
public interface Input extends Part
{
  /**
   * Key fuer die Context-Information mit dem Tooltip.
   */
  public final static String DATAKEY_TOOLTIP = "datakey.tooltip";

  /**
   * Liefert den Wert des Eingabefelds.
   * @return Wert des Feldes.
   */
  public Object getValue();

  /**
   * Schreibt einen neuen Wert in das Eingabefeld.
   * @param value der neu anzuzeigende Wert.
   */
  public void setValue(Object value);
  
  /**
   * Prueft, ob sich der Wert von <code>getValue()</code> seit dem
   * letzten Aufruf von <code>hasChanged</code> geaendert hat.
   * @return true, wenn sich der Wert geaendert hat, sonstfalse.
   */
  public boolean hasChanged();

  /**
   * Liefert das eigentliche Eingabecontrol. Es muss von jeder
   * abgeleiteten Klasse implementiert werden und das Eingabe-Feld
   * zurueckliefern. Da der Implementierer das Composite benoetigt,
   * in dem das Control positioniert werden soll, kann er sich
   * der Methode getParent() in dieser Klasse bedienen.
   * @return das zu zeichnende Control.
   */
  public Control getControl();

  /**
   * Fuegt dem Eingabe-Feld einen Listener hinzu, der bei jedem Focus-Wechsel ausgeloest wird.
   * Besteht das Eingabe-Feld aus mehreren Teilen (z.Bsp. bei SearchInput aus Eingabe-Feld
   * + Knopf dahinter) dann wird der Listener bei Focus-Wechsel jedes dieser
   * Teile ausgeloest.
   * @param l zu registrierender Listener.
   */
  public void addListener(Listener l);

  /**
   * Fuegt hinter das Eingabefeld noch einen Kommentar.
   * Existiert der Kommentar bereits, wird er gegen den neuen ersetzt.
   * Hinweis: Wird die Funktion nicht aufgerufen, bevor das Eingabe-Feld
   * gemalt wird, dann wird es auch nicht angezeigt. Denn vorm Malen
   * muss bekannt sein, ob es angezeigt werden soll, damit der Platz
   * dafuer reserviert werden kann.
   * @param comment Kommentar.
   */
  public void setComment(String comment);

  /**
   * Positioniert und malt das Eingabefeld im uebergebenen Composite.
   * Es wird dabei mit einer vorgegebenen Standard-Breite gemalt.
   * @param parent Das Composite, in dem das Eingabefeld gemalt werden soll.
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent);

  /**
   * Positioniert und malt das Eingabefeld im uebergebenen Composite.
   * @param parent Das Composite, in dem das Eingabefeld gemalt werden soll.
   * @param width angegebene Breite.
   */
  public void paint(Composite parent,int width);

  /**
   * Gibt diesem Eingabefeld den Focus.
   */
  public void focus();

  /**
   * Deaktiviert das Eingabefeld.
   */
  public void disable();

  /**
   * Aktiviert das Eingabefeld.
   */
  public void enable();
  

  /**
   * Aktiviert oder deaktiviert das Eingabe-Feld.
   * @param enabled true, wenn es aktiv sein soll.
   */
  public void setEnabled(boolean enabled);
  
  /**
   * Prueft, ob das Eingabe-Feld aktiv ist.
   * @return true, wenn es aktiv ist.
   */
  public boolean isEnabled();
  
  /**
   * Legt fest, ob es sich um ein Pflichtfeld handelt.
   * @param mandatory true, wenn es ein Pflichtfeld ist.
   */
  public void setMandatory(boolean mandatory);
  
  /**
   * Prueft, ob es sich um ein Pflichtfeld handelt.
   * @return true, wenn es sich um ein Pflichfeld handelt.
   */
  public boolean isMandatory();
  
  /**
   * Speichert die Label-Bezeichnung des Eingabe-Feldes.
   * @param name Label-Bezeichnung.
   */
  public void setName(String name);
  
  /**
   * Liefert die Label-Bezeichnung des Eingabe-Feldes.
   * @return Label-Bezeichnung.
   */
  public String getName();
  
  /**
   * Speichert optionale Context-Daten.
   * @see Widget#setData(Object)
   * @param key freier Schluessel.
   * @param data beliebige Nutzdaten.
   */
  public void setData(String key, Object data);
  
  /**
   * Liefert die Context-Daten.
   * @param key der Schluessel.
   * @return die Contextdaten.
   */
  public Object getData(String key);
}
