/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/Attic/ViewDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/02/20 20:45:24 $
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

import de.willuhn.jameica.gui.util.Style;
import de.willuhn.jameica.gui.views.AbstractView;

/**
 * Dialog, der eine komplette AbstractView anzeigen kann, die
 * normalerweise im Content-Frame angezeigt wird.
 */
public class ViewDialog extends AbstractDialog {

	private AbstractView view = null;

  /**
   * Erzeugt einen neuen Dialog.
   * @param position Position.
   * @see AbstractDialog#POSITION_CENTER
   * @see AbstractDialog#POSITION_MOUSE
   */
  public ViewDialog(AbstractView view, int position) {
    super(position);
		this.view = view;
  }

	/**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint()
   */
  public void paint() throws Exception
	{
		// Die View zeigen wir nicht direkt in unserer View an, weil die
		// zu wenig Rahmen hat.
		Composite comp = new Composite(getParent(),SWT.NONE);
		comp.setBackground(Style.COLOR_BG);
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
			public void shellActivated(ShellEvent e) {};
			public void shellDeactivated(ShellEvent e) {};
			public void shellDeiconified(ShellEvent e) {};
			public void shellIconified(ShellEvent e) {};
		});
		view.bind();
	}
}


/**********************************************************************
 * $Log: ViewDialog.java,v $
 * Revision 1.1  2004/02/20 20:45:24  willuhn
 * *** empty log message ***
 *
 **********************************************************************/