/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/Tests.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/02/24 22:46:53 $
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
import org.eclipse.swt.widgets.Button;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.gui.dialogs.PINDialog;
import de.willuhn.util.ApplicationException;

/**
 */
public class Tests extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {
  	Button b = new Button(getParent(),SWT.FLAT);
  	b.setText("PIN");
  	b.addMouseListener(new MouseAdapter() {
      public void mouseUp(MouseEvent e) {
      	PINDialog pd = new PINDialog(PINDialog.POSITION_CENTER);
				try {
					pd.open();
				}
				catch (Exception e2)
				{
					Application.getLog().error(e2.getLocalizedMessage(),e2);
					GUI.setActionText(e2.getLocalizedMessage());
				}
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
 * Revision 1.2  2004/02/24 22:46:53  willuhn
 * @N GUI refactoring
 *
 * Revision 1.1  2004/02/22 20:05:21  willuhn
 * @N new Logo panel
 *
 **********************************************************************/