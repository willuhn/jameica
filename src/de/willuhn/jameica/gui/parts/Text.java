/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Attic/Text.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/03/19 01:44:01 $
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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.util.Style;

/**
 * Frei formatierbarer Text.
 */
public class Text
{

	private StringBuffer content = new StringBuffer();
	private boolean wrap = true;
	private StyledText stext = null;
	
	private boolean autoscroll = false; 

  /**
   * ct.
   * @param text der anzuzeigenden Text.
   */
  public Text(String text)
  {
		content = new StringBuffer(text);
  }
  
  /**
   * ct.
   */
  public Text()
  {
  }

	/**
	 * ct.
	 * @param text die PlainText-Datei.
	 * @throws IOException Wenn beim Lesen der Datei Fehler auftreten.
	 */
	public Text(File text) throws IOException
	{
		BufferedReader br =  null;
		
		try {
			br = new BufferedReader(new FileReader(text));

			String thisLine = null;
			StringBuffer buffer = new StringBuffer();
			while ((thisLine =  br.readLine()) != null)
			{
				buffer.append(thisLine + "\n");
			}

			content = buffer; // machen wir erst wenn die gesamte Datei gelesen werden konnte
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
	 * Definiert, ob der Text automatisch immer zu Ende scrollen soll.
	 * Sinnvoll fuer Log-Ausgaben.
   * @param b true, wenn gescrollt werden soll.
   */
  public void setAutoscroll(boolean b)
	{
		autoscroll = b;
	}

	/**
	 * Gibt an, ob Zeilenumbrueche automatisch gemacht werden sollen.
	 * Per Default ist die Option auf "true" gesetzt.
	 * @param wrap Zeilenumbruch.
	 */
	public void setWordWrap(boolean wrap)
	{
		this.wrap = wrap;
	}

	/**
	 * Fuegt weiteren Text hinzu.
   * @param text anzuzeigender Text.
   */
  public void appendText(String text)
	{
		if (text == null || text.length() == 0)
			return;

		if (stext == null || stext.isDisposed())
			return;

		stext.append(text);
		scroll();
	}

	/**
	 * Loescht den Inhalt des Textes.
	 */
	public void clear()
	{
		content = new StringBuffer();

		if (stext == null || stext.isDisposed())
			return;
		stext.setText("");
	}

  private void scroll()
	{
		if (stext == null || stext.isDisposed())
			return;

		if (autoscroll)
			stext.setTopIndex(stext.getLineCount());
	}

	/**
	 * Malt den Text in das angegebene Composite.
   * @param parent Composite.
   */
  public void paint(Composite parent)
	{
//		final GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		stext = new StyledText(parent,SWT.READ_ONLY | SWT.V_SCROLL);
 		stext.setBackground(Style.COLOR_BG);
		stext.setEditable(false);
		stext.setWordWrap(wrap);
		stext.setLayoutData(new GridData(GridData.FILL_BOTH));
		if (content != null)
			stext.append(content.toString());
	}

}


/**********************************************************************
 * $Log: Text.java,v $
 * Revision 1.4  2004/03/19 01:44:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/03/06 18:24:23  willuhn
 * @D javadoc
 *
 * Revision 1.2  2004/02/18 17:14:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/01/29 00:07:24  willuhn
 * @N Text widget
 *
 **********************************************************************/