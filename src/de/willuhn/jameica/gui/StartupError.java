/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/Attic/StartupError.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/01/05 15:18:08 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Diese Klasse zeigt einen Fehlerdialog an, wenn es beim Start der
 * Anwendung zu einem kritischen Fehler gekommen ist, der den Start
 * von Jameica verhindert.
 * 
 */
public class StartupError
{
	/**
	 * Zeigt den Text des Fehlers als SWT-Dialog an.
   * @param t
   */
  public static void show(Throwable t)
	{
		Display d = Display.getCurrent();
		if (d == null)
			d = new Display();
		final Shell s = new Shell();
		s.setLayout(new GridLayout());
		s.setText("Fehler");
		Label l = new Label(s,SWT.NONE);
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		l.setText(""+t.getMessage());

		Button b = new Button(s,SWT.BORDER);
		b.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		b.setText("OK");
		b.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				s.close();
			}
		});
		s.pack();
		s.open();
		while (!s.isDisposed()) {
			if (!d.readAndDispatch()) d.sleep();
		}
		try {
			s.dispose();
			d.dispose();
		}
		catch (Exception e2) {
			// useless
		}
	}
}


/**********************************************************************
 * $Log: StartupError.java,v $
 * Revision 1.2  2005/01/05 15:18:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/01/03 23:04:54  willuhn
 * @N separater StartupError Handler
 *
 **********************************************************************/