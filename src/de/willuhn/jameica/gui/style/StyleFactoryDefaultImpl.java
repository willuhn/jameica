/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/style/StyleFactoryDefaultImpl.java,v $
 * $Revision: 1.16 $
 * $Date: 2011/05/03 17:04:44 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.style;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.util.Font;

/**
 * Default-Implementierung der Style-Factory.
 */
public class StyleFactoryDefaultImpl implements StyleFactory
{

  /**
   * @see de.willuhn.jameica.gui.style.StyleFactory#createButton(org.eclipse.swt.widgets.Composite)
   */
  public Button createButton(Composite parent)
  {
    Button button = new Button(parent,SWT.PUSH);
    button.setFont(Font.DEFAULT.getSWTFont());
    return button;
  }

  /**
   * @see de.willuhn.jameica.gui.style.StyleFactory#createText(org.eclipse.swt.widgets.Composite)
   */
  public Text createText(Composite parent)
  {
    Text text = new Text(parent,SWT.BORDER | SWT.SINGLE);
    text.setFont(Font.DEFAULT.getSWTFont());
//    text.setForeground(Color.FOREGROUND.getSWTColor());
//    text.setBackground(Color.BACKGROUND.getSWTColor());
    return text;
  }

  /**
   * @see de.willuhn.jameica.gui.style.StyleFactory#createTextArea(org.eclipse.swt.widgets.Composite)
   */
  public Text createTextArea(Composite parent)
  {
    Text text = new Text(parent,SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
    text.setFont(Font.DEFAULT.getSWTFont());
//    text.setForeground(Color.FOREGROUND.getSWTColor());
//    text.setBackground(Color.BACKGROUND.getSWTColor());
    return text;
  }

  /**
   * @see de.willuhn.jameica.gui.style.StyleFactory#createCombo(org.eclipse.swt.widgets.Composite, int)
   */
  public Combo createCombo(Composite parent, int style)
  {
    Combo combo = new Combo(parent,SWT.BORDER | style);
    combo.setFont(Font.DEFAULT.getSWTFont());
    return combo;
  }

  /**
   * @see de.willuhn.jameica.gui.style.StyleFactory#createTable(org.eclipse.swt.widgets.Composite, int)
   */
  public Table createTable(Composite parent, int style)
  {
    Table t = new Table(parent, SWT.BORDER | style);
    t.setFont(Font.DEFAULT.getSWTFont());
    return t;
  }

  /**
   * @see de.willuhn.jameica.gui.style.StyleFactory#getName()
   */
  public String getName() {
    return "Default-Look";
  }

  /**
   * @see de.willuhn.jameica.gui.style.StyleFactory#createLabel(org.eclipse.swt.widgets.Composite, int)
   */
  public Label createLabel(Composite parent, int style)
  {
    Label label = new Label(parent,style);
    label.setFont(Font.DEFAULT.getSWTFont());
    return label;
  }

}


/**********************************************************************
 * $Log: StyleFactoryDefaultImpl.java,v $
 * Revision 1.16  2011/05/03 17:04:44  willuhn
 * @R Keine Hintergrundfarbe bei Buttons setzen
 *
 * Revision 1.15  2011-05-03 16:46:08  willuhn
 * @R Flatstyle entfernt - war eh nicht mehr zeitgemaess und rendere auf aktuellen OS sowieso haesslich
 * @C SelectInput verwendet jetzt Combo statt CCombo - das sieht auf den verschiedenen OS besser aus
 *
 * Revision 1.14  2011-05-03 10:13:11  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.13  2011-04-29 17:01:04  willuhn
 * @R undo
 *
 * Revision 1.11  2007-05-14 11:18:09  willuhn
 * @N Hoehe der Statusleiste abhaengig von DPI-Zahl und Schriftgroesse
 * @N Default-Schrift konfigurierbar und Beruecksichtigung dieser an mehr Stellen
 *
 * Revision 1.10  2006/12/28 15:57:56  willuhn
 * @C keine flachen Buttons in Default-Style
 *
 * Revision 1.9  2006/08/28 23:01:18  willuhn
 * @N Update auf SWT 3.2
 *
 * Revision 1.8  2006/08/05 20:44:59  willuhn
 * @B Bug 256
 *
 * Revision 1.7  2005/08/15 13:15:32  web0
 * @C fillLayout removed
 *
 * Revision 1.6  2005/02/01 17:15:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/11/04 19:29:22  willuhn
 * @N TextAreaInput
 *
 * Revision 1.4  2004/06/17 22:07:12  willuhn
 * @C cleanup in tablePart and statusBar
 *
 * Revision 1.3  2004/06/14 22:05:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.1  2004/06/03 00:24:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/06/02 21:15:15  willuhn
 * @B win32 fixes in flat style
 * @C made ButtonInput more abstract
 *
 * Revision 1.2  2004/05/23 18:15:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 **********************************************************************/