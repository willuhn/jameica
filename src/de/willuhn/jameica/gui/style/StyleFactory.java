/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.gui.style;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

/**
 * Diese Klasse ist fuer Styling-Kram in Jameica zustaendig.
 * @author willuhn
 */
public interface StyleFactory
{

  /**
	 * Erzeugt einen Button.
   * @param parent Parent.
   * @return Button.
   */
  public Button createButton(Composite parent);
  
  /**
   * Erzeugt eine Checkbox.
   * @param parent Parent.
   * @return Button.
   */
  public Button createCheckbox(Composite parent);

  /**
   * Erzeugt einen Text.
   * @param parent
   * @return Text.
   */
  public Text createText(Composite parent);

  /**
   * Erzeugt einen Text.
   * @param parent
   * @param style
   * @return Text.
   */
  public Text createText(Composite parent, int style);

  /**
   * Erzeugt ein mehrzeiliges Text-Feld.
   * @param parent
   * @return Text-Area.
   */
  public Text createTextArea(Composite parent);

  /**
	 * Erzeugt ein Label.
   * @param parent
   * @param style
   * @return Label.
   */
  public Label createLabel(Composite parent, int style);

	/**
	 * Erzeugt ein Combo.
   * @param parent Parent.
   * @param style
   * @return Combo.
   */
  public Combo createCombo(Composite parent, int style);
  
  /**
   * Erzeugt eine neue Tabelle.
   * @param parent Parent.
   * @param style
   * @return Table.
   */
  public Table createTable(Composite parent, int style);
  
  /**
   * Liefert den sprechenden Namen der Style-Factory.
   * @return Name der Style-Factory.
   */
  public String getName();

}
