/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/FormTextPart.java,v $
 * $Revision: 1.13 $
 * $Date: 2007/05/14 11:18:09 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.parts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Freiformatierbarer Text.
 */
public class FormTextPart implements Part {

	private StringBuffer content 				= new StringBuffer();
	private ScrolledComposite container = null;
	private FormText text								= null;

	/**
	 * ct.
	 */
	public FormTextPart()
	{
	}

	/**
	 * ct.
	 * @param text der anzuzeigenden Text.
	 */
	public FormTextPart(String text)
	{
		setText(text);
	}
  
	/**
	 * ct.
	 * @param text die PlainText-Datei.
	 * @throws IOException Wenn beim Lesen der Datei Fehler auftreten.
	 */
	public FormTextPart(Reader text) throws IOException
	{
		setText(text);
	}

	/**
	 * Zeigt den Text aus der uebergebenen Datei an.
   * @param text anzuzeigender Text.
   * @throws IOException
   */
  public void setText(Reader text) throws IOException
	{
		BufferedReader br =  null;
		
		try {
			br = new BufferedReader(text);

			String thisLine = null;
			StringBuffer buffer = new StringBuffer();
			while ((thisLine =  br.readLine()) != null)
			{
				if (thisLine.length() == 0) // Leerzeile
				{
					buffer.append("\n\n");
					continue;
				}
				buffer.append(thisLine.trim() + " "); // Leerzeichen am Ende einfuegen.


			}

			content = buffer; // machen wir erst wenn die gesamte Datei gelesen werden konnte
			refresh();
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			try {
				br.close();
			}
			catch (Exception e) {}
		}
	}

	/**
	 * Zeigt den uebergebenen Hilfe-Text an.
	 * @param s anzuzeigender Hilfe-Text.
	 */
	public void setText(String s)
	{
		content = new StringBuffer(s);
		refresh();
	}


  /**
   */
  public void refresh()
	{
		if (text == null || content == null)
			return;
		String s = content.toString();
		boolean b = s != null && s.startsWith("<form>");
		text.setText(s == null ? "" : s,b,b);
		resize();
	}

	/**
   * Passt die Groesse des Textes an die Umgebung an.
   */
  private void resize()
	{
		if (text == null || container == null)
			return;
		text.setSize(text.computeSize(text.getParent().getClientArea().width,SWT.DEFAULT));
	}

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException {

		container = new ScrolledComposite(parent,SWT.H_SCROLL | SWT.V_SCROLL);
		container.setBackground(Color.BACKGROUND.getSWTColor());
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalIndent = 4;
		container.setLayoutData(gd);
		container.setLayout(SWTUtil.createGrid(1,true));
		container.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				resize();
			}
		});

		text = new FormText(container,SWT.WRAP);

		text.setBackground(Color.BACKGROUND.getSWTColor());
    text.setFont(Font.DEFAULT.getSWTFont());

		text.setColor("header",Color.COMMENT.getSWTColor());
    text.setColor("error",Color.ERROR.getSWTColor());
    text.setColor("success",Color.SUCCESS.getSWTColor());
		text.setFont("header", Font.H1.getSWTFont());

		container.setContent(text);
	
		HyperlinkSettings hs = new HyperlinkSettings(GUI.getDisplay());
		hs.setBackground(Color.WIDGET_BG.getSWTColor());
		hs.setForeground(Color.LINK.getSWTColor());
		hs.setActiveBackground(Color.WIDGET_BG.getSWTColor());
		hs.setActiveForeground(Color.LINK_ACTIVE.getSWTColor());

		text.setHyperlinkSettings(hs);

		text.addHyperlinkListener(new HyperlinkAdapter() {
      public void linkActivated(HyperlinkEvent e) {
     		Event ev = new Event();
     		ev.data = e.getHref();
        if (ev.data == null)
        {
          Logger.info("got hyperlink event, but data was null. nothing to do");
          return;
        }
        if (!(ev.data instanceof String))
        {
          Logger.info("got hyperlink event, but data is not a string, skipping");
          return;
        }
        String action = (String) ev.data;
        if (action.indexOf(".") == -1)
        {
          Logger.info("given href \"" + action + "\" doesn't look like a java class, skipping");
          return;
        }
        try
        {
          Logger.debug("trying to load class " + action);
          Class c = Application.getClassLoader().load(action);
          Action a = (Action) c.newInstance();
          a.handleAction(e);
        }
        catch (Throwable t)
        {
          Logger.error("error while executing action " + action,t);
        }
      }
    });

		refresh();
  }
}


/**********************************************************************
 * $Log: FormTextPart.java,v $
 * Revision 1.13  2007/05/14 11:18:09  willuhn
 * @N Hoehe der Statusleiste abhaengig von DPI-Zahl und Schriftgroesse
 * @N Default-Schrift konfigurierbar und Beruecksichtigung dieser an mehr Stellen
 *
 * Revision 1.12  2007/03/29 15:29:48  willuhn
 * @N Uebersichtlichere Darstellung der Systemstart-Meldungen
 *
 * Revision 1.11  2005/08/25 21:18:24  web0
 * @C changes accoring to findbugs eclipse plugin
 *
 * Revision 1.10  2005/08/15 13:15:32  web0
 * @C fillLayout removed
 *
 * Revision 1.9  2005/03/31 22:35:37  web0
 * @N flexible Actions fuer FormTexte
 *
 * Revision 1.8  2005/03/09 01:06:36  web0
 * @D javadoc fixes
 *
 * Revision 1.7  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/08/18 23:14:19  willuhn
 * @D Javadoc
 *
 * Revision 1.5  2004/07/31 15:03:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/07/09 00:12:46  willuhn
 * @C Redesign
 *
 * Revision 1.3  2004/05/23 16:34:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.1  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 **********************************************************************/