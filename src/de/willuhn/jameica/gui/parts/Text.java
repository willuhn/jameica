/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Attic/Text.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/01/29 00:07:24 $
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.util.Style;

/**
 * Frei formatierbarer Text.
 */
public class Text
{

	private StringBuffer content = new StringBuffer();
	private boolean wrap = true;

  /**
   * ct.
   */
  public Text(String text)
  {
		appendText(text);
  }
  
  /**
   * 
   */
  public Text()
  {
  }

	/**
	 * Fuegt weiteren Text hinzu.
   * @param text anzuzeigender Text.
   */
  public void appendText(String text)
	{
		if (text == null)
			return;
		content.append(text);
	}

  /**
	 * Zeigt die angegebene Plaintext-Datei an.
   * @param file die PlainText-Datei.
   * @throws IOException Wenn beim Lesen der Datei Fehler auftreten.
   */
  public void setContent(File text) throws IOException
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
	 * Gibt an, ob Zeilenumbrueche automatisch gemacht werden sollen.
	 * Per Default ist die Option auf "true" gesetzt.
   * @param wrap Zeilenumbruch.
   */
  public void setWordWrap(boolean wrap)
	{
		this.wrap = wrap;
	}

	/**
	 * Malt den Text in das angegebene Composite.
   * @param parent Composite.
   */
  public void paint(Composite parent)
	{
		final GridLayout layout = new GridLayout();
		layout.marginHeight = 4;
		layout.marginWidth  = 4;
		final GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		Composite box = new Composite(parent, SWT.BORDER); // das brauchen wir fuer den Rand.
		box.setLayoutData(gridData);
		box.setLayout(layout);
		box.setBackground(Style.COLOR_WHITE);

		final StyledText stext = new StyledText(box,SWT.READ_ONLY | SWT.H_SCROLL);
 		stext.setBackground(Style.COLOR_WHITE);
		stext.setEditable(false);
		stext.setEnabled(false);
		stext.setWordWrap(wrap);
		stext.setLayoutData(new GridData(GridData.FILL_BOTH));
		stext.append(content.toString());
	}

}


/**********************************************************************
 * $Log: Text.java,v $
 * Revision 1.1  2004/01/29 00:07:24  willuhn
 * @N Text widget
 *
 **********************************************************************/