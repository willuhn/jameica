/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.io.IOUtil;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Font;

/**
 * Simpler Text, der automatisch nach unten
 * scrollen kann und Auto-Wordwrap kann.
 */
public class TextPart implements Part
{

	private StringBuffer content = new StringBuffer();
	private boolean wrap = true;
	private StyledText stext = null;
  
  private Color background = null;
	
	private boolean autoscroll = false; 

  /**
   * ct.
   * @param text der anzuzeigenden Text.
   */
  public TextPart(String text)
  {
		content = new StringBuffer(text);
  }
  
  /**
   * ct.
   */
  public TextPart()
  {
  	content = new StringBuffer();
  }

	/**
	 * ct.
	 * @param text die PlainText-Datei.
	 * @throws IOException Wenn beim Lesen der Datei Fehler auftreten.
	 */
	public TextPart(File text) throws IOException
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
		  IOUtil.close(br);
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
   * @param color definierte Text-Farbe. Gilt nur fuer diese Zeile und
   * wird danach automatisch wieder zurueckgesetzt.
   */
  public void appendText(String text, Color color)
  {
    if (text == null || text.length() == 0)
      return;

    if (!text.endsWith("\n"))
      text += "\n";
    if (stext == null || stext.isDisposed())
    {
      content.append(text);
      return;
    }

    if (color != null)
      stext.setForeground(color.getSWTColor());
    stext.append(text);
    scroll();
    // Farbe wieder zuruecksetzen
    if (color != null)
      stext.setForeground(Color.FOREGROUND.getSWTColor());
  }

  /**
	 * Fuegt weiteren Text hinzu.
   * @param text anzuzeigender Text.
   */
  public void appendText(String text)
	{
    appendText(text,null);
	}

	/**
	 * Loescht den Inhalt des Textes.
	 */
	public void clear()
	{
		content = new StringBuffer();

    GUI.getDisplay().syncExec(new Runnable() {
      public void run()
      {
        if (stext == null || stext.isDisposed())
          return;
        stext.setText("");
      }
    });
	}

  private void scroll()
	{
		if (stext == null || stext.isDisposed())
			return;

		if (autoscroll)
			stext.setTopIndex(stext.getLineCount());
	}

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
	{
//		final GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		stext = new StyledText(parent,SWT.READ_ONLY | SWT.V_SCROLL);
    stext.setFont(Font.DEFAULT.getSWTFont());
    if (this.background != null)
 		  stext.setBackground(background.getSWTColor());
		stext.setEditable(false);
		stext.setWordWrap(wrap);
		stext.setLayoutData(new GridData(GridData.FILL_BOTH));
		if (content != null)
			stext.append(content.toString());
    scroll();
	}

  /**
   * Definiert die Hintergrundfarbe.
   * @param color
   */
  public void setBackground(Color color)
  {
    if (color == null)
      return;
    
    this.background = color;
    if (stext != null && !stext.isDisposed())
      stext.setBackground(this.background.getSWTColor());
  }
}


/**********************************************************************
 * $Log: TextPart.java,v $
 * Revision 1.11  2011/05/03 10:13:10  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.10  2011-04-26 12:09:17  willuhn
 * @B Potentielle Bugs gemaess Code-Checker
 *
 * Revision 1.9  2007/05/14 11:18:09  willuhn
 * @N Hoehe der Statusleiste abhaengig von DPI-Zahl und Schriftgroesse
 * @N Default-Schrift konfigurierbar und Beruecksichtigung dieser an mehr Stellen
 *
 * Revision 1.8  2005/08/08 17:07:38  web0
 * *** empty log message ***
 *
 * Revision 1.7  2005/07/26 22:58:34  web0
 * @N background task refactoring
 *
 * Revision 1.6  2005/06/03 17:14:41  web0
 * @N Livelog
 *
 * Revision 1.5  2004/10/25 17:59:15  willuhn
 * @N aenderbare Tabellen
 *
 * Revision 1.4  2004/07/09 00:12:46  willuhn
 * @C Redesign
 *
 * Revision 1.3  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.2  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.1  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
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