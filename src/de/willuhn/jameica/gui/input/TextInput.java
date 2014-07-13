/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/TextInput.java,v $
 * $Revision: 1.23 $
 * $Date: 2011/10/05 16:50:31 $
 * $Author: willuhn $
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
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.GUI;

/**
 * Ist zustaendig fuer Standard-Eingabefelder.
 * @author willuhn
 */
public class TextInput extends AbstractInput<Object>
{

  protected Text text;
  private String value;
  private String hint;
  private boolean enabled = true;
  private boolean focus = false;

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
    this(value,maxLength,null);
  }

  /**
   * Erzeugt ein neues Eingabefeld und schreib den uebergebenen Wert rein.
   * @param value anzuzeigender Wert.
   * @param maxLength maximale Anzahl von Zeichen.
   * @param hint Hinweis-Text, der als Hint im Eingabefeld angezeigt werden soll.
   */
  public TextInput(String value, int maxLength, String hint)
  {
    this.value     = value;
    this.maxLength = maxLength;
    this.hint      = hint;
  }

  /**
   * Definiert die maximal eingebbare Menge von Zeichen.
   * @param maxLength
   */
  public void setMaxLength(int maxLength)
  {
    this.maxLength = maxLength;
    if (this.text != null && !this.text.isDisposed())
      this.text.setTextLimit(this.maxLength);
  }
  
  /**
   * Definiert einen Hinweis-Text, der als Hint im Eingabefeld angezeigt werden soll.
   * @param hint der Hinweis-Text.
   */
  public void setHint(String hint)
  {
    this.hint = hint;
    if (this.hint != null && this.text != null && !this.text.isDisposed())
      this.text.setMessage(this.hint);
  }
  
  /**
   * Liefert einen Hinweis-Text, der als Hint im Eingabefeld angezeigt werden soll.
   * @return der Hinweis-Text.
   */
  public String getHint()
  {
    return this.hint;
  }

  /**
   * Erzeugt das Text-Widget.
   * Ist eine extra Funktion damit es zum Beispiel von TextAreaInput
   * ueberschriebn werden kann.
   * @return das Text-Widget.
   */
  Text getTextWidget()
  {
    if (text == null)
      text = GUI.getStyleFactory().createText(getParent());
    return text;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getControl()
   */
  public Control getControl()
  {
    if (text != null)
      return text;

    text = getTextWidget();

    if (maxLength > 0)
      text.setTextLimit(maxLength);
    
    if (this.hint != null)
      text.setMessage(this.hint);

    this.setEnabledInternal(enabled);
    text.setText((value == null ? "" : value));
    if (this.focus)
      text.setFocus();
    return text;
  }

  /**
   * Liefert den angezeigten Text als String.
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    if (text == null || text.isDisposed())
      return value;
    return text.getText();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    String s = value == null ? null : value.toString();
    this.value = s;

    if (this.text != null && !this.text.isDisposed())
    {
      this.text.setText(s == null ? "" : s);
      this.text.redraw();
    }
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#focus()
   */
  public void focus()
  {
    this.focus = true;
    if (text != null && !text.isDisposed())
      text.setFocus();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#disable()
   */
  public void disable()
  {
    setEnabled(false);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#enable()
   */
  public void enable()
  {
    setEnabled(true);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#isEnabled()
   */
  public boolean isEnabled()
  {
    return enabled;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
    if (this.text != null && !this.text.isDisposed())
    {
      this.setEnabledInternal(enabled);
      this.update();
    }
  }
  
  /**
   * Markiert das Control als editierbar oder nicht.
   * @param enabled true, wenn 
   */
  private void setEnabledInternal(boolean enabled)
  {
    // Wenn das Control Scrollbalken enthaelt, duerfen
    // wir nicht "setEnabled" verwenden, weil man dann nicht
    // mehr scrollen koennte. Bei einzeiligen Eingabefeldern
    // sieht "setEnabled(false)" aber besser aus, weil da der
    // Hintergrund grau gefaerbt wird.
    int style = this.text.getStyle();
    if ((style & SWT.V_SCROLL) != 0 || (style & SWT.H_SCROLL) != 0)
      this.text.setEditable(enabled);
    else
      this.text.setEnabled(enabled);
  }
}

/*********************************************************************
 * $Log: TextInput.java,v $
 * Revision 1.23  2011/10/05 16:50:31  willuhn
 * @N Text-Hints in Eingabefeldern - funktioniert nur in einzeiligen Feldern
 *
 * Revision 1.22  2011-08-05 11:21:12  willuhn
 * @B Wenn das Eingabefeld Scrollbalken hat (beim abgeleiteten TextAreaInput z.Bsp.) darf nicht "setEnabled(false)" verwendet werden sondern "setEditable(false)", weil sonst auch das Scrollen nicht mehr moeglich ist
 *
 * Revision 1.21  2009/02/18 00:43:21  willuhn
 * @N maxlength live aktualisieren, wenn Control bereits gezeichnet ist
 *
 * Revision 1.20  2008/02/29 01:12:30  willuhn
 * @N Erster Code fuer neues Backup-System
 * @N DirectoryInput
 * @B Fixes an FileInput, TextInput
 *
 * Revision 1.19  2008/02/14 12:06:36  willuhn
 * @N Korrektes Focus-Handling
 *
 * Revision 1.18  2007/01/05 10:36:49  willuhn
 * @C Farbhandling - Jetzt aber!
 *
 * Revision 1.17  2006/12/28 15:35:52  willuhn
 * @N Farbige Pflichtfelder
 *
 * Revision 1.16  2006/10/06 16:00:48  willuhn
 * @B Bug 280
 *
 * Revision 1.15  2006/06/19 10:54:24  willuhn
 * @N neue Methode setEnabled(boolean) in Input
 * @N neue de_willuhn_util lib
 *
 * Revision 1.14  2005/10/06 14:10:11  web0
 * @B NPE
 *
 * Revision 1.13  2005/09/01 16:40:04  web0
 * *** empty log message ***
 *
 * Revision 1.12  2005/08/22 13:31:52  web0
 * *** empty log message ***
 *
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