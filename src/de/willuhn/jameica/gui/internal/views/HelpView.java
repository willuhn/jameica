/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/views/Attic/HelpView.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/10/08 13:38:20 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.internal.views;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Eine View, in der Hilfe-Texte angezeigt werden.
 */
public class HelpView {

	private FormTextPart text;

	private CLabel title;
	private Composite myParent;

  /**
   * ct.
   * @param parent Composite, in der die HelpView gemalt werden soll.
   */
  public HelpView(Composite parent) {

		I18N i18n = Application.getI18n();
		///////////////////////////////
		// Eigenes Parent, damit wir ein GridLayout verwenden koennen
		myParent = new Composite(parent,SWT.BORDER);
		myParent.setBackground(de.willuhn.jameica.gui.util.Color.BACKGROUND.getSWTColor());
		GridLayout myLayout = new GridLayout();
		myLayout.horizontalSpacing = 0;
		myLayout.verticalSpacing = 0;
		myLayout.marginHeight = 0;
		myLayout.marginWidth = 0;
		myParent.setLayout(myLayout);
		//
		///////////////////////////////
		
			///////////////////////////////
			// Titelleiste
			Composite head = new Composite(myParent,SWT.NONE);
			GridLayout headLayout = new GridLayout(2,true);
			headLayout.horizontalSpacing = 0;
			headLayout.verticalSpacing = 0;
			headLayout.marginHeight = 0;
			headLayout.marginWidth = 0;
			head.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			head.setLayout(headLayout);
			head.setBackground(new Color(GUI.getDisplay(),255,255,255));
			//
			///////////////////////////////
			
				///////////////////////////////
				// Der Titel selbst
				title = new CLabel(head,SWT.NONE);
				title.setBackground(new Color(GUI.getDisplay(),255,255,255));
				title.setLayoutData(new GridData(GridData.FILL_BOTH));
				title.setFont(Font.H2.getSWTFont());
				title.setText(i18n.tr("Hilfe"));
		
				Label image = new Label(head,SWT.NONE);
				image.setImage(SWTUtil.getImage("gradient.gif"));
				title.setBackground(new Color(GUI.getDisplay(),255,255,255));
				image.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
				//
				///////////////////////////////
	
			///////////////////////////////
			// Separator
			Label sep = new Label(myParent,SWT.SEPARATOR | SWT.HORIZONTAL);
			sep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			//
			///////////////////////////////

			///////////////////////////////
			// Der eigentliche Text.
			text = new FormTextPart();
			try {
				text.paint(myParent);
			}
			catch (Exception e)
			{
				Logger.error("unable to paint help text",e);
			}
			//
			///////////////////////////////
  }

	/**
	 * Zeigt den Text aus dem uebergebenen Reader an.
   * @param reader auszulesender Reader.
   * @throws IOException Wenn der Text aus dem Reader nicht gelesen werden kann.
   */
  public void setText(Reader reader) throws IOException
	{
		text.setText(reader);
	}

}


/**********************************************************************
 * $Log: HelpView.java,v $
 * Revision 1.1  2004/10/08 13:38:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/08/11 00:39:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.8  2004/06/30 20:58:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.6  2004/05/23 16:34:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.4  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.3  2004/03/30 22:08:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/03/04 00:26:34  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 **********************************************************************/