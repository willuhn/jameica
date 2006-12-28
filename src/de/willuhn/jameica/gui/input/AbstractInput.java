/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/AbstractInput.java,v $
 * $Revision: 1.13 $
 * $Date: 2006/12/28 15:35:52 $
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

import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Basisklasse fuer Eingabefelder.
 * @author willuhn
 */
public abstract class AbstractInput implements Input
{

	I18N i18n;

  private Composite parent = null;

  private String comment = null;
  private Label commentLabel = null;

  private Control control = null;
	private ArrayList listeners = new ArrayList();

  private String validChars = null;
  private String invalidChars = null;
  
  private boolean mandatory = false;

	/**
   * Erzeugt ein neues Eingabe-Feld.
   */
  public AbstractInput()
	{
		i18n = Application.getI18n();
	}

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
  public final void addListener(Listener l)
	{
		listeners.add(l);
	}

  /**
   * @see de.willuhn.jameica.gui.input.Input#setComment(java.lang.String)
   */
  public final void setComment(String comment)
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
		this.parent.setBackground(Color.BACKGROUND.getSWTColor());
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
    
    final GridData inputGrid = new GridData(getStyleBits());
    inputGrid.widthHint = hasComment ? width / 2 : width;
    inputGrid.horizontalSpan = hasComment ? 1 : 2;
    control.setLayoutData(inputGrid);

    // den Kommentar hinten dran fuegen
    if (hasComment) {
      commentLabel = new Label(this.parent,SWT.NONE);
      commentLabel.setText(this.comment);
      commentLabel.setForeground(Color.COMMENT.getSWTColor());
      commentLabel.setBackground(Color.BACKGROUND.getSWTColor());
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
  }
  
  /**
   * Fuegt die Verifier dem Control hinzu.
   * @param control
   */
  private void applyVerifier(Control control)
  {
    if (control == null || !(control instanceof Text))
      return;
    
    if (mandatory && isEnabled())
      control.addListener(SWT.Paint,new MandatoryListener(control));

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
    return this.mandatory;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setMandatory(boolean)
   */
  public void setMandatory(boolean mandatory)
  {
    this.mandatory = mandatory;
  }
  
  private class MandatoryListener implements Listener
  {
    private Control myControl = null;
    
    /**
     * @param c
     */
    private MandatoryListener(Control c)
    {
      this.myControl = c;
    }
    
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event)
    {
      if (!isEnabled() || myControl == null || myControl.isDisposed())
        return;
      
      Object value = getValue();
      if (mandatory && (value == null || "".equals(value.toString())))
        myControl.setBackground(Color.MANDATORY_BG.getSWTColor());
      else
        myControl.setBackground(Color.WIDGET_BG.getSWTColor());
    }
    
  }


}

/*********************************************************************
 * $Log: AbstractInput.java,v $
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