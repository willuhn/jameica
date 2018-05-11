/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.gui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.gui.GUI;

/**
 * Malt eine Standard-Ueberschrift in den Dialog.
 * @author willuhn
 */
public class Headline
{

  /**
   * Erzeugt eine neue Standardueberschrift im angegebenen Composite mit dem uebergebenen Namen.
   * @param parent das Composite in dem die Ueberschrift gemalt werden soll.
   * @param headline Name der Ueberschrift.
   */
  public Headline(Composite parent, String headline)
  {
    Label title = GUI.getStyleFactory().createLabel(parent, SWT.NONE);
    title.setText(headline);
    title.setLayoutData(new GridData());
    title.setFont(Font.H2.getSWTFont());
  }
}

/*********************************************************************
 * $Log: Headline.java,v $
 * Revision 1.5  2007/05/14 11:18:09  willuhn
 * @N Hoehe der Statusleiste abhaengig von DPI-Zahl und Schriftgroesse
 * @N Default-Schrift konfigurierbar und Beruecksichtigung dieser an mehr Stellen
 *
 * Revision 1.4  2004/06/10 20:56:54  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.3  2004/05/25 23:23:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.1  2004/04/12 19:15:59  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.2  2004/02/18 01:40:29  willuhn
 * @N new white style
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
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
 * Revision 1.1  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 **********************************************************************/