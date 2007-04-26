/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/Input.java,v $
 * $Revision: 1.7 $
 * $Date: 2007/04/26 11:19:48 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.input;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Part;

/**
 */
public interface Input extends Part
{

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
}

/**********************************************************************
 * $Log: Input.java,v $
 * Revision 1.7  2007/04/26 11:19:48  willuhn
 * @N Generische Funktion "hasChanged()" zum Pruefen auf Aenderungen in Eingabe-Feldern
 *
 * Revision 1.6  2007/03/19 12:30:06  willuhn
 * @N Input can now have it's own label
 *
 * Revision 1.5  2006/12/28 15:35:52  willuhn
 * @N Farbige Pflichtfelder
 *
 * Revision 1.4  2006/06/19 10:54:24  willuhn
 * @N neue Methode setEnabled(boolean) in Input
 * @N neue de_willuhn_util lib
 *
 * Revision 1.3  2005/08/22 13:31:52  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/07/11 18:12:39  web0
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/09 00:12:46  willuhn
 * @C Redesign
 *
 **********************************************************************/