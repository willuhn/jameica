/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/View.java,v $
 * $Revision: 1.17 $
 * $Date: 2004/05/23 15:30:52 $
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
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;

/**
 * Bildet das Content-Frame ab.
 * @author willuhn
 */
public class View
{
	private SashForm sash;

	private Composite view;
	private Composite content;
	private Composite snapin;
	private boolean snappedIn = false;

	private Composite parent;
	private CLabel title;
	private CLabel messages;
	private Canvas panelBg;
	

	/**
   * Erzeugt ein neues Content-Frame.
	 * @param parent
   */
  protected View(Composite parent)
	{
    this.parent = parent;
		init();
		cleanContent();
	}
	
  /**
   * Initialisiert das Layout der View.
   */
  private void init()
	{
		sash = new SashForm(parent,SWT.VERTICAL);
		sash.setLayout(new FillLayout());
		
		view = new Composite(sash, SWT.BORDER);
		view.setBackground(Color.WHITE.getSWTColor());
		view.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		view.setLayout(layout);
	
		snapin = new Composite(sash, SWT.BORDER);
		snapin.setLayoutData(new GridData(GridData.FILL_BOTH));
		snapin.setLayout(new FillLayout());
		sash.setMaximizedControl(view);

		panelBg = SWTUtil.getCanvas(view,SWTUtil.getImage("panel.bmp"), SWT.TOP | SWT.RIGHT);
		GridLayout layout2 = new GridLayout();
		layout2.marginHeight = 0;
		layout2.marginWidth = 0;
		layout2.horizontalSpacing = 0;
		layout2.verticalSpacing = 0;
		panelBg.setLayout(layout2);
		panelBg.setBackground(Color.WHITE.getSWTColor());

		title = new CLabel(panelBg,SWT.NONE);
		title.setFont(Font.H1.getSWTFont());
		title.setBackground(Color.WHITE.getSWTColor());
		title.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		Label sep = new Label(view,SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		messages = new CLabel(view,SWT.NONE);
		messages.setFont(Font.H2.getSWTFont());
		messages.setBackground(Color.BACKGROUND.getSWTColor());
		messages.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	}
	
  /**
   * Leert die Anzeige.
   * Wird beim Wechsel von einem Dialog auf den naechsten aufgerufen.
   */
  protected void cleanContent()
	{
		if (content != null)
			content.dispose();

		// Wir machen hier deshalb nicht nur ein layout() oder redraw()
		// weil wir wollen, dass die gesamte View disposed und entfernt
		// wird, bevor eine neue drauf kommt.
		content = new Composite(view, SWT.NONE);
		content.setBackground(Color.BACKGROUND.getSWTColor());
		content.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout l = new GridLayout();
		l.marginHeight = 6;
		l.marginWidth = 6;
		content.setLayout(l);
		messages.setText("");
		messages.layout();
	}

	/**
   * Das Snapin-Composite wird angezeigt.
   */
  public void snapIn()
	{
		sash.setMaximizedControl(null);
		sash.setWeights(new int[] {3,1});
		snappedIn = true;
	}

	/**
   * Das Snapin-Composite wird ausgeblendet.
   */
  public void snapOut()
	{
		sash.setMaximizedControl(view);
		snappedIn = false;
	}

	/**
	 * Prueft, ob das Snapin gerade angezeigt wird.
   * @return true, wenn es angezeigt wird.
   */
  public boolean snappedIn()
	{
		return snappedIn;
	}

	/**
	 * Liefert das SnapIn-Composite.
	 * Die Funktion liefert immer ein leeres Snapin. Wenn sich also vorher
	 * was drin befunden hat, wird es vorm erneuten Herausgeben geleert.
	 * Hinweis: Das Composite enthaelt ein FillLayout.
   * @return Snapin-Composite.
   */
  public Composite getSnapin()
	{
		SWTUtil.disposeChilds(snapin);
		return snapin;
	}

	/**
	 * Aktualisiert den Titel der View.
   * @param text anzuzeigender Titel.
   */
  public void setTitle(String text)
	{
		title.setText(text == null ? "" : " " + text);
		panelBg.layout();
	}

	/**
	 * Schreibt einen Fehlertext oben in die View.
   * @param text anzuzeigender Text. 
   */
  public void setErrorText(final String text)
	{
		GUI.getDisplay().asyncExec(new Runnable() {
			public void run() {
				messages.setText(text);
				messages.setForeground(Color.ERROR.getSWTColor());
				messages.layout();
			}
		});
	}

	/**
	 * Schreibt einen Erfolgstext oben in die View.
	 * @param text anzuzeigender Text. 
	 */
	public void setSuccessText(final String text)
	{
		GUI.getDisplay().asyncExec(new Runnable() {
			public void run() {
				messages.setText(text);
				messages.setForeground(Color.SUCCESS.getSWTColor());
				messages.layout();
			}
		});
	}

  /**
   * Aktualisiert die Anzeige.
   */
  protected void refreshContent()
	{
		view.layout();
	}

  /**
   * Liefert das Composite, in das die anzuzeigenden Dialoge bitte ihre Controls reinmalen sollen.
   * @return Composite, in das der Dialog seine Elemente reinmalen soll.
   */
  protected Composite getContent()
	{
		return content;
	}
}



/***************************************************************************
 * $Log: View.java,v $
 * Revision 1.17  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.16  2004/04/29 23:05:54  willuhn
 * @N new snapin feature
 *
 * Revision 1.15  2004/03/24 00:46:03  willuhn
 * @C refactoring
 *
 * Revision 1.14  2004/03/06 18:24:24  willuhn
 * @D javadoc
 *
 * Revision 1.13  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.12  2004/02/22 20:05:21  willuhn
 * @N new Logo panel
 *
 * Revision 1.11  2004/02/18 01:40:30  willuhn
 * @N new white style
 *
 * Revision 1.10  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.9  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
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