/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/style/StyleFactory.java,v $
 * $Revision: 1.5 $
 * $Date: 2005/02/01 17:15:19 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.style;

import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Button;
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
   * Erzeugt einen Text.
   * @param parent
   * @return Text.
   */
  public Text createText(Composite parent);

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
   * @return Combo.
   */
  public CCombo createCombo(Composite parent);
  
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

/*********************************************************************
 * $Log: StyleFactory.java,v $
 * Revision 1.5  2005/02/01 17:15:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/11/04 19:29:22  willuhn
 * @N TextAreaInput
 *
 * Revision 1.3  2004/06/17 22:07:12  willuhn
 * @C cleanup in tablePart and statusBar
 *
 * Revision 1.2  2004/06/14 22:05:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/06/03 00:24:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/05/23 18:15:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.19  2004/05/11 23:32:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/04/26 23:40:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/04/14 22:16:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/04/12 19:15:59  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.15  2004/04/01 22:07:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/04/01 19:06:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/04/01 00:23:24  willuhn
 * @N FontInput
 * @N ColorInput
 * @C improved ClassLoader
 * @N Tabs in Settings
 *
 * Revision 1.12  2004/03/29 23:20:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/03/11 08:56:56  willuhn
 * @C some refactoring
 *
 * Revision 1.10  2004/03/06 18:24:24  willuhn
 * @D javadoc
 *
 * Revision 1.9  2004/03/05 00:40:45  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.7  2004/02/22 20:05:21  willuhn
 * @N new Logo panel
 *
 * Revision 1.6  2004/02/20 01:25:06  willuhn
 * @N nice dialog
 * @N busy indicator
 * @N new status bar
 *
 * Revision 1.5  2004/02/18 20:28:45  willuhn
 * @N jameica now stores window position and size
 *
 * Revision 1.4  2004/02/18 17:14:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/02/18 01:40:30  willuhn
 * @N new white style
 *
 * Revision 1.2  2004/01/29 00:07:23  willuhn
 * @N Text widget
 *
 * Revision 1.1  2004/01/28 20:51:25  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.4  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.2  2003/12/05 18:43:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/11/23 19:26:27  willuhn
 * *** empty log message ***
 *
 **********************************************************************/