/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/WelcomeText.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/01/29 00:07:23 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica;

/**
 * Diese Klasse sammelt Texte, die auf der Startseite angezeigt werden sollen.
 */
public class WelcomeText
{

	private static StringBuffer content = new StringBuffer();

  /**
   * Fuegt weiteren Text zur Startseite hinzu.
   * @param text der anzuzeigende Text.
   */
  public static void addText(String text)
	{
		if (text == null)
			return;
		content.append(text);
	}

	/**
	 * Liefert den bis zum aktuellen Zeitpunkt gesammelten Welcome-Text.
   * @return Welcome-Text.
   */
  public static String getText()
	{
		return content.toString();
	}

}


/**********************************************************************
 * $Log: WelcomeText.java,v $
 * Revision 1.1  2004/01/29 00:07:23  willuhn
 * @N Text widget
 *
 **********************************************************************/