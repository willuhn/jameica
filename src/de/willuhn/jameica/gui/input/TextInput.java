/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/TextInput.java,v $
 * $Revision: 1.11 $
 * $Date: 2005/05/30 14:25:40 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.input;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.GUI;

/**
 * Ist zustaendig fuer Standard-Eingabefelder.
 * @author willuhn
 */
public class TextInput extends AbstractInput
{

  protected Text text;
  private String value;
  private boolean enabled = true;
  private String validChars = null;
	private String invalidChars = null;

	private int maxLength = 0;

  /**
   * Erzeugt ein neues Eingabefeld und schreib den uebergebenen Wert rein.
   * @param value anzuzeigender Wert.
   */
  public TextInput(String value)
  {
    this(value,0);
  }

	/**
	 * Erzeugt ein neues Eingabefeld und schreib den uebergebenen Wert rein.
	 * @param value anzuzeigender Wert.
	 * @param maxLength maximale Anzahl von Zeichen.
	 */
	public TextInput(String value, int maxLength)
	{
		this.value = value;
		this.maxLength = maxLength;
	}

	/**
	 * Definiert die maximal eingebbare Menge von Zeichen.
   * @param maxLength
   */
  public void setMaxLength(int maxLength)
	{
		this.maxLength = maxLength;
	}

	/**
	 * Definiert eine Liste von Zeichen, die eingegeben werden koennen.
	 * Wird diese Funktion verwendet, dann duerfen nur noch die hier
	 * angegebenen Zeichen eingegeben werden.
	 * Werden beide Funktionen <code>setValidChars</code> <b>und</b>
	 * <code>setInvalidChars</code> benutzt, kann nur noch die verbleibende
	 * Restmenge eingegeben werden. Das sind die Zeichen, die in validChars
	 * angegeben und in invalidChars nicht enthalten sind. 
   * @param chars
   */
  public void setValidChars(String chars)
	{
		this.validChars = chars;
	}

	/**
	 * Definiert eine Liste von Zeichen, die nicht eingegeben werden koennen.
	 * Wird diese Funktion verwendet, dann duerfen die angegebenen Zeichen nicht
	 * mehr verwendet werden.
   * @param chars
   */
  public void setInvalidChars(String chars)
	{
		this.invalidChars = chars;
	}

  /**
   * Erzeugt das Text-Widget.
   * Ist eine extra Funktion damit es zum Beispiel von TextAreaInput
   * ueberschriebn werden kann.
   * @return das Text-Widget.
   */
  Text getTextWidget()
  {
    return GUI.getStyleFactory().createText(getParent());
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getControl()
   */
  public Control getControl()
  {
    text = getTextWidget();

    if (maxLength > 0)
      text.setTextLimit(maxLength);

		text.setEnabled(enabled);
    text.setText((value == null ? "" : value));

		if ((validChars != null && validChars.length() > 0))
		{
			text.addListener(SWT.Verify, new Listener()
			{
				public void handleEvent(Event e)
				{
					char[] chars = e.text.toCharArray();
					for (int i=0; i<chars.length; i++) {
						if (validChars.indexOf(chars[i]) == -1) // eingegebenes Zeichen nicht enthalten
						{
							e.doit = false;
							return;
						}
					}
				}
			});
		}

		if ((invalidChars != null && invalidChars.length() > 0))
		{
			text.addListener(SWT.Verify, new Listener()
			{
				public void handleEvent(Event e)
				{
					char[] chars = e.text.toCharArray();
					for (int i=0; i<chars.length; i++) {
						if (invalidChars.indexOf(chars[i]) != -1) // eingegebenes Zeichen enthalten
						{
							e.doit = false;
							return;
						}
					}
				}
			});
		}
    return text;
  }

  /**
   * Liefert den angezeigten Text als String.
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    return text == null ? null : text.getText();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (value == null)
      return;
    this.text.setText(value.toString());
    this.text.redraw();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#focus()
   */
  public void focus()
  {
    text.setFocus();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#disable()
   */
  public void disable()
  {
  	enabled = false;
  	if (text != null && !text.isDisposed())
	    text.setEnabled(false);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#enable()
   */
  public void enable()
  {
  	enabled = true;
  	if (text != null && !text.isDisposed())
	    text.setEnabled(true);
  }
}

/*********************************************************************
 * $Log: TextInput.java,v $
 * Revision 1.11  2005/05/30 14:25:40  web0
 * @B NPE
 *
 * Revision 1.10  2004/11/04 19:29:22  willuhn
 * @N TextAreaInput
 *
 * Revision 1.9  2004/11/01 23:11:18  willuhn
 * @N setValidChars und setInvalidChars in TextInput
 *
 * Revision 1.8  2004/10/15 20:06:08  willuhn
 * @N added maxLength to TextInput
 * @N double comma check in DecimalInput
 *
 * Revision 1.7  2004/07/09 00:12:47  willuhn
 * @C Redesign
 *
 * Revision 1.6  2004/06/18 19:47:17  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/06/02 21:15:15  willuhn
 * @B win32 fixes in flat style
 * @C made ButtonInput more abstract
 *
 * Revision 1.4  2004/05/26 23:23:23  willuhn
 * @N Timeout fuer Messages in Statusbars
 *
 * Revision 1.3  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.2  2004/04/27 00:04:44  willuhn
 * @D javadoc
 *
 * Revision 1.1  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.6  2004/03/25 00:45:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/03/16 23:59:40  willuhn
 * @N 2 new Input fields
 *
 * Revision 1.4  2004/03/11 08:56:55  willuhn
 * @C some refactoring
 *
 * Revision 1.3  2004/03/06 18:24:23  willuhn
 * @D javadoc
 *
 * Revision 1.2  2004/02/18 17:14:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.10  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.9  2003/12/16 02:27:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.7  2003/12/01 21:22:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2003/12/01 20:28:58  willuhn
 * @B filter in DBIteratorImpl
 * @N InputFelder generalisiert
 *
 * Revision 1.5  2003/11/24 14:21:53  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2003/11/24 11:51:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/11/22 20:43:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 * Revision 1.1  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 **********************************************************************/