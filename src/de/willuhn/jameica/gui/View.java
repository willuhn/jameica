/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/View.java,v $
 * $Revision: 1.7 $
 * $Date: 2003/12/12 01:28:05 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 ****************************************************************************/
package de.willuhn.jameica.gui;



import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.gui.views.util.Style;

/**
 * Bildet das Content-Frame ab.
 * @author willuhn
 */
public class View
{
	private Composite view;
	private Composite content;

  /**
   * Erzeugt ein neues Content-Frame.
   */
  public View()
	{
		setLayout();
		setLogoPanel();
		cleanContent();
	}
	
  /**
   * Erzeugt das Layout.
   */
  private void setLayout()
	{
		view = new Composite(GUI.getShell(), SWT.NONE);
		view.setBackground(new Color(GUI.getDisplay(), 255, 255, 255));
		view.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		view.setLayout(layout);
	}
	
  /**
   * Setzt das Panel mit dem Logo rein.
   */
  private void setLogoPanel()
	{
		Label logoPanel = new Label(view, SWT.NONE);
		logoPanel.setImage(Style.getImage("logo.jpg"));
		logoPanel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
	}
	
  /**
   * Leert die Anzeige.
   * Wird beim Wechsel von einem Dialog auf den naechsten aufgerufen.
   */
  public void cleanContent()
	{
		if (content != null)
			content.dispose();

		content = new Composite(view, SWT.BORDER);
		GridLayout l = new GridLayout();
		l.marginHeight = 8;
		l.marginWidth = 8;
		content.setLayout(l);
		content.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

  /**
   * Aktualisiert die Anzeige.
   */
  public void refreshContent()
	{
		view.layout();
	}

  /**
   * Liefert das Composite, in das die anzuzeigenden Dialoge bitte ihre
   * Controls reinmalen sollen.
   * @return
   */
  public Composite getContent()
	{
		return content;
	}
}



/***************************************************************************
 * $Log: View.java,v $
 * Revision 1.7  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.5  2003/12/05 18:43:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2003/11/20 03:48:41  willuhn
 * @N first dialogues
 *
 * Revision 1.3  2003/11/13 00:37:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/10/29 00:41:26  willuhn
 * *** empty log message ***
 *
 ***************************************************************************/