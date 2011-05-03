/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/AbstractInput.java,v $
 * $Revision: 1.26 $
 * $Date: 2011/05/03 10:13:11 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.input;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.I18N;

/**
 * Basisklasse fuer Eingabefelder.
 * @author willuhn
 */
public abstract class AbstractInput implements Input
{
  final static I18N i18n = Application.getI18n();

  private final static Object PLACEHOLDER = new Object();

  private Composite parent = null;

  private String name        = null;
  private String comment     = null;
  private Label commentLabel = null;

  private Control control = null;
	private ArrayList listeners = new ArrayList();

  private String validChars = null;
  private String invalidChars = null;
  
  private boolean mandatory = false;
  
  private Object oldValue = PLACEHOLDER;

  /**
   * Liefert das Composite, in dem das Control gemalt werden soll.
   * @return das Composite, in dem das Control platziert wird.
   */
  protected Composite getParent()
  {
    return this.parent;
  }
  
  /**
   * Liefert die Stylebits (GridData-Settings), welche zum Erstellen des Widgets
   * verwendet werden.
   * @return die Style.Bits.
   */
  protected int getStyleBits()
  {
    return GridData.FILL_HORIZONTAL;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#addListener(org.eclipse.swt.widgets.Listener)
   */
  public void addListener(Listener l)
	{
		listeners.add(l);
	}

  /**
   * @see de.willuhn.jameica.gui.input.Input#setComment(java.lang.String)
   */
  public void setComment(String comment)
  {
    this.comment = comment;
		if (commentLabel != null && ! commentLabel.isDisposed() && this.comment != null)
		{
			commentLabel.setText(this.comment);
			commentLabel.redraw();
		}
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public final void paint(Composite parent)
  {
    paint(parent,240);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#paint(org.eclipse.swt.widgets.Composite, int)
   */
  public final void paint(Composite parent,int width)
  {
		boolean hasComment = this.comment != null;

    // neues Composite erstellen, welches Platz fuer den Kommentar laesst.
    this.parent = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(2, true);
    layout.marginHeight = 2;
    layout.marginWidth = 1;
    layout.horizontalSpacing = 5;
    layout.verticalSpacing = 0;
    this.parent.setLayout(layout);
    final GridData g = new GridData(getStyleBits());
    this.parent.setLayoutData(g);

    control = getControl();
    applyVerifier(control);
    
    if (control.getLayoutData() == null)
    {
      final GridData inputGrid = new GridData(getStyleBits());
      inputGrid.widthHint = hasComment ? width / 2 : width;
      inputGrid.horizontalSpan = hasComment ? 1 : 2;
      control.setLayoutData(inputGrid);
    }

    // den Kommentar hinten dran fuegen
    if (hasComment) {
      commentLabel = GUI.getStyleFactory().createLabel(this.parent,SWT.NONE);
      commentLabel.setText(this.comment);
      commentLabel.setForeground(Color.COMMENT.getSWTColor());
      commentLabel.setAlignment(SWT.LEFT);
      commentLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    // die Listener noch dran haengen
    Listener l = null;
    for (int i=0;i<listeners.size();++i)
    {
    	l = (Listener) listeners.get(i);
			control.addListener(SWT.Selection,l);
			control.addListener(SWT.FocusIn,l);
			control.addListener(SWT.FocusOut,l);
    }
    
    // Es kann sein, dass das Control ein Composite ist (z.Bsp. bei DialogInput)
    // Wenn es also aus mehren Elementen besteht, dann muessen wir
		// den Listener an alle haengen.
		if (control instanceof Composite)
		{
			Composite c = (Composite) control;
			Control[] children = c.getChildren();
			for (int j=0;j<children.length;++j)
			{
        for (int i=0;i<listeners.size();++i)
        {
          l = (Listener) listeners.get(i);
          children[j].addListener(SWT.Selection,l);
          children[j].addListener(SWT.FocusIn,l);
          children[j].addListener(SWT.FocusOut,l);
        }
        applyVerifier(children[j]);
			}
		}
    
    // Einmal manuell starten, damit es vor dem ersten
    // verify event ausgeloest wird
    update();
  }
  
  /**
   * Fuegt die Verifier dem Control hinzu.
   * @param control
   */
  private void applyVerifier(final Control control)
  {
    if (control == null || !(control instanceof Text))
      return;
    
    Listener updateCheck = new Listener() {
    
      public void handleEvent(Event event)
      {
        try
        {
          update();
        }
        catch (OperationCanceledException oce)
        {
          // ignore
        }
      }
    };
    DelayedListener dl = new DelayedListener(100,updateCheck);
    control.addListener(SWT.FocusOut,dl);
    control.addListener(SWT.Modify,dl);

    if ((validChars != null && validChars.length() > 0))
    {
      control.addListener(SWT.Verify, new Listener()
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
      control.addListener(SWT.Verify, new Listener()
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
   * @see de.willuhn.jameica.gui.input.Input#isMandatory()
   */
  public boolean isMandatory()
  {
    return this.mandatory && Application.getConfig().getMandatoryCheck();
  }

  private boolean inUpdate = false;
  
  /**
   * Wird immer dann aufgerufen, wenn eines der Controls des
   * Eingabe-Feldes aktualisiert wird. Hier kann dann z.Bsp.
   * geprueft werden, ob der Inhalt des Feldes korrekt ist
   * und ggf. die Hintergrund-Farbe angepasst werden.
   */
  void update() throws OperationCanceledException
  {
    if (inUpdate)
      throw new OperationCanceledException();

    try
    {
      inUpdate = true;

      if (this.control == null || this.control.isDisposed())
        return;

      if (!isEnabled())
        return;
      
      Object value = getValue();

      if (isMandatory() && (value == null || "".equals(value.toString())))
      {
        this.control.setBackground(Color.MANDATORY_BG.getSWTColor());
        return;
      }
      this.control.setBackground(Color.WIDGET_BG.getSWTColor());
    }
    finally
    {
      inUpdate = false;
    }
  }
  
  /**
   * @see de.willuhn.jameica.gui.input.Input#setMandatory(boolean)
   */
  public void setMandatory(boolean mandatory)
  {
    // Wenn das Control bereits gezeichnet wurde und
    // sich der Wert von mandatory aendern wird, rufen
    // wir gleich update() auf. Dann wird eine Aenderung
    // im laufenden Betrieb sofort sichtbar
    if (mandatory != this.mandatory &&
        this.control != null &&
        !this.control.isDisposed())
    {
      this.mandatory = mandatory;
      update();
    }
    else
    {
      // Ansonsten nur das Flag setzen
      this.mandatory = mandatory;
    }
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getName()
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setName(java.lang.String)
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#hasChanged()
   */
  public boolean hasChanged()
  {
    Object newValue = getValue();

    try
    {
      // Wir wurden noch nie aufgerufen
      if (oldValue == PLACEHOLDER || oldValue == newValue)
        return false;

      return newValue == null || !newValue.equals(oldValue);
    }
    finally
    {
      oldValue = newValue;
    }
    
  }
}

/*********************************************************************
 * $Log: AbstractInput.java,v $
 * Revision 1.26  2011/05/03 10:13:11  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.25  2010-08-24 22:43:56  willuhn
 * @N ImageInput - wollte Heiner in JVerein fuer Mitgliedsfotos haben
 *
 * Revision 1.24  2009/01/04 01:24:30  willuhn
 * @N Format-Funktion zum Uberschreiben der Anzeige von Elementen in SearchInput
 * @N AbstractInput#addListener ueberschreibbar
 *
 * Revision 1.23  2007/08/28 22:42:53  willuhn
 * @N Bei Aufruf von setMandatory() ggf. Farb-Aenderungen sofort durchfuehren, wenn sich Wert von "mandatory" geaendert hat und das Control bereits gezeichnet wurde
 *
 * Revision 1.22  2007/07/19 09:53:17  willuhn
 * @B removed debug output
 *
 * Revision 1.21  2007/07/17 16:25:05  willuhn
 * @N Schnelleres Updateverhalten
 *
 * Revision 1.20  2007/07/17 16:00:30  willuhn
 * @C Input-Validierung auch bei SWT.Modify
 *
 * Revision 1.19  2007/07/17 14:22:50  willuhn
 * @B update nicht bei jedem Paint-Event sondern nur bei Textaenderungen aufrufen
 *
 * Revision 1.18  2007/05/14 11:18:09  willuhn
 * @N Hoehe der Statusleiste abhaengig von DPI-Zahl und Schriftgroesse
 * @N Default-Schrift konfigurierbar und Beruecksichtigung dieser an mehr Stellen
 *
 * Revision 1.17  2007/04/26 11:19:48  willuhn
 * @N Generische Funktion "hasChanged()" zum Pruefen auf Aenderungen in Eingabe-Feldern
 *
 * Revision 1.16  2007/03/19 12:30:06  willuhn
 * @N Input can now have it's own label
 *
 * Revision 1.15  2007/01/23 15:52:10  willuhn
 * @C update() check for recursion
 * @N mandatoryCheck configurable
 *
 * Revision 1.14  2007/01/05 10:36:49  willuhn
 * @C Farbhandling - Jetzt aber!
 *
 * Revision 1.13  2006/12/28 15:35:52  willuhn
 * @N Farbige Pflichtfelder
 *
 * Revision 1.12  2006/11/30 23:48:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2006/10/06 16:00:48  willuhn
 * @B Bug 280
 *
 * Revision 1.10  2006/08/05 20:44:59  willuhn
 * @B Bug 256
 *
 * Revision 1.9  2005/07/11 18:12:39  web0
 * *** empty log message ***
 *
 * Revision 1.8  2004/07/21 23:54:53  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.7  2004/07/09 00:12:46  willuhn
 * @C Redesign
 *
 * Revision 1.6  2004/06/14 22:05:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/06/02 21:15:15  willuhn
 * @B win32 fixes in flat style
 * @C made ButtonInput more abstract
 *
 * Revision 1.4  2004/05/23 16:34:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.2  2004/04/21 22:28:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.2  2004/03/30 22:08:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/03/11 08:56:55  willuhn
 * @C some refactoring
 *
 * Revision 1.5  2004/03/06 18:24:23  willuhn
 * @D javadoc
 *
 * Revision 1.4  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.3  2004/02/24 22:46:53  willuhn
 * @N GUI refactoring
 *
 * Revision 1.2  2004/02/18 01:40:30  willuhn
 * @N new white style
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.15  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.13  2003/12/28 22:58:27  willuhn
 * @N synchronize mode
 *
 * Revision 1.12  2003/12/16 02:27:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.10  2003/12/10 00:47:12  willuhn
 * @N SearchDialog done
 * @N FatalErrorView
 *
 * Revision 1.9  2003/12/05 18:43:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2003/12/05 17:12:23  willuhn
 * @C SelectInput
 *
 * Revision 1.7  2003/12/01 21:22:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2003/12/01 20:28:58  willuhn
 * @B filter in DBIteratorImpl
 * @N InputFelder generalisiert
 *
 * Revision 1.5  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
 * Revision 1.4  2003/11/24 14:21:53  willuhn
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