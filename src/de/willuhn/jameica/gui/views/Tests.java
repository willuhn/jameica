/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/Tests.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/04/01 00:23:24 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 */
public class Tests extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

			Button b = new Button(getParent(),SWT.BORDER);
  		b.setText("Test");
	  	b.addMouseListener(new MouseAdapter() {
	      public void mouseUp(MouseEvent e) {
	      	ColorDialog cd = new ColorDialog(GUI.getShell());
	      	RGB foo = cd.open();
	      	System.out.println(foo);
	      }
    });
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {

  }

}


/**********************************************************************
 * $Log: Tests.java,v $
 * Revision 1.4  2004/04/01 00:23:24  willuhn
 * @N FontInput
 * @N ColorInput
 * @C improved ClassLoader
 * @N Tabs in Settings
 *
 * Revision 1.3  2004/02/25 23:11:57  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/24 22:46:53  willuhn
 * @N GUI refactoring
 *
 * Revision 1.1  2004/02/22 20:05:21  willuhn
 * @N new Logo panel
 *
 **********************************************************************/