/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/Attic/ViewDialog.java,v $
 * $Revision: 1.5 $
 * $Date: 2004/05/23 15:30:52 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.views.AbstractView;

/**
 * Dialog, der eine komplette AbstractView anzeigen kann, die
 * normalerweise im Content-Frame angezeigt wird.
 */
public class ViewDialog extends AbstractDialog {

	private AbstractView view = null;

  /**
   * Erzeugt einen neuen Dialog.
   * @param view die anzuzeigende View.
   * @param position Position.
   * @see AbstractDialog#POSITION_CENTER
   * @see AbstractDialog#POSITION_MOUSE
   */
  public ViewDialog(AbstractView view, int position) {
    super(position);
		this.view = view;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
	{
		// Die View zeigen wir nicht direkt in unserer View an, weil die
		// zu wenig Rahmen hat.
		Composite comp = new Composite(parent,SWT.NONE);
		comp.setBackground(Color.BACKGROUND.getSWTColor());
		comp.setLayout(new GridLayout());
		
		view.setParent(comp);
		
		// und fuer das unbind() setzen wir einen ShellListener
		addShellListener(new ShellListener() {
			public void shellClosed(ShellEvent e) {
				try {
					view.unbind();
				}
				catch (Exception ex) {/*useless*/}
			}
			public void shellActivated(ShellEvent e) {}
			public void shellDeactivated(ShellEvent e) {}
			public void shellDeiconified(ShellEvent e) {}
			public void shellIconified(ShellEvent e) {}
		});
		view.bind();
	}

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception {
    return null;
  }
}


/**********************************************************************
 * $Log: ViewDialog.java,v $
 * Revision 1.5  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.4  2004/03/06 18:24:24  willuhn
 * @D javadoc
 *
 * Revision 1.3  2004/02/24 22:46:53  willuhn
 * @N GUI refactoring
 *
 * Revision 1.2  2004/02/23 20:30:33  willuhn
 * @C refactoring in AbstractDialog
 *
 * Revision 1.1  2004/02/20 20:45:24  willuhn
 * *** empty log message ***
 *
 **********************************************************************/