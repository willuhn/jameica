/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/HelpView.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/03/03 22:27:10 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.views;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.Jameica;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.util.Style;
import de.willuhn.util.I18N;

/**
 * Eine View, in der Hilfe-Texte angezeigt werden.
 */
public class HelpView {

	private ScrolledComposite container;
	private CLabel title;
	private StyledText text;
	private Composite myParent;

  /**
   * ct.
   * @param parent Composite, in der die HelpView gemalt werden soll.
   */
  public HelpView(Composite parent) {

		I18N i18n = PluginLoader.getPlugin(Jameica.class).getResources().getI18N();
		///////////////////////////////
		// Eigenes Parent, damit wir ein GridLayout verwenden koennen
		myParent = new Composite(parent,SWT.BORDER);
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
			head.setBackground(Style.COLOR_WHITE);
			//
			///////////////////////////////
			
				///////////////////////////////
				// Der Titel selbst
				title = new CLabel(head,SWT.NONE);
				title.setBackground(Style.COLOR_WHITE);
				title.setLayoutData(new GridData(GridData.FILL_BOTH));
				title.setFont(Style.FONT_H2);
				title.setText(i18n.tr("Hilfe"));
		
				Label image = new Label(head,SWT.NONE);
				image.setImage(Style.getImage("gradient.gif"));
				title.setBackground(Style.COLOR_WHITE);
				image.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
				//
				///////////////////////////////
	
			///////////////////////////////
			// Der eigentliche Container fuer den Text.
			container = new ScrolledComposite(myParent,SWT.V_SCROLL | SWT.NONE);
			container.setBackground(Style.COLOR_BG);
			container.setLayout(new GridLayout());
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			text = new StyledText(container,SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
			text.setLayoutData(new GridData(GridData.FILL_BOTH));
			text.setBackground(Style.COLOR_BG);

			container.setContent(text);
			container.addListener(SWT.Resize, new Listener() {
				public void handleEvent(Event event) {
					refresh();
				}
			});
			//
			///////////////////////////////
  }

	/**
	 * Zeigt den uebergebenen Hilfe-Text an.
   * @param s anzuzeigender Hilfe-Text.
   */
  public void setText(String s)
	{
		text.setText(s);
		refresh();
	}

	private void refresh()
	{
		text.setSize(text.computeSize(text.getParent().getClientArea().width, SWT.DEFAULT));
	}

	/**
	 * Zeigt den Text aus dem uebergebenen Reader an.
   * @param reader auszulesender Reader.
   */
  public void setText(Reader reader)
	{
		BufferedReader br = null;
		String thisLine = null;
		StringBuffer all = new StringBuffer();

		try {
			br =  new BufferedReader(reader);
			while ((thisLine = br.readLine()) != null)
			{
				if (thisLine.length() == 0) // Leerzeile
				{
					all.append("\n\n");
					continue;
				}
				all.append(thisLine.trim() + " "); // Leerzeichen am Ende einfuegen.
			}
		}
		catch (IOException e)
		{
			Application.getLog().error("error while reading help text",e);
			return;
		}
		finally
		{
			try {
				br.close();
			}
			catch (Exception e) {}
		}
		setText(all.toString());
	}

}


/**********************************************************************
 * $Log: HelpView.java,v $
 * Revision 1.1  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 **********************************************************************/