/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/Input.java,v $
 * $Revision: 1.7 $
 * $Date: 2003/12/01 21:22:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.views.parts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.views.util.Style;

/**
 * Basisklasse fuer Eingabefelder.
 * @author willuhn
 */
public abstract class Input
{

  protected Composite parent = null;
  protected String comment = null;
  protected Label commentLabel = null;
  protected Listener commentListener = null;
  protected Control control = null;

  /**
   * Liefert den Wert des Eingabefelds.
   * @return Wert des Feldes.
   */
  public abstract String getValue();

  /**
   * Schreibt einen neuen Wert in das Eingabefeld.
   * @param value der neu anzuzeigende Wert.
   */
  public abstract void setValue(String value);

  /**
   * Liefert das eigentliche Eingabecontrol. Es muss von jeder
   * abgeleiteten Klasse implementiert werden und das Eingabe-Feld
   * zurueckliefern. Da der Implementierer das Composite benoetigt,
   * in dem das Control positioniert werden soll, kann er sich
   * der Methode getParent() in dieser Klasse bedienen.
   * @return das zu zeichnende Control.
   */
  public abstract Control getControl();

  /**
   * Liefert das Composite, in dem das Control gemalt werden soll.
   * @return das Composite, in dem das Control platziert wird.
   */
  protected Composite getParent()
  {
    return this.parent;
  }
  /**
   * Fuegt hinter das Eingabefeld noch einen Kommentar. Wenn sich dieser
   * abhaengig von der Eingabe aendern soll, muss ausserdem noch ein
   * Listener uebergeben werden, der die Aenderung vornimmt.
   * @param comment Kommentar.
   * @param commentListener optionaler Listener, der das Kommentarfeld aendern kann.
   */
  public void addComment(String comment, Listener commentListener)
  {
    this.comment = comment;
    this.commentListener = commentListener;
  }

  /**
   * Aktualisiert das Kommentarfeld.
   * @param comment neuer anzuzeigender Text.
   */
  public void updateComment(String comment)
  {
    if (comment == null || commentLabel == null)
      return;
    
    commentLabel.setText(comment);
    commentLabel.redraw();
  }
  /**
   * Positioniert und malt das Eingabefeld im uebergebenen Composite.
   * @param comment
   */
  public void paint(Composite parent)
  {
    if (this.comment != null)
    {

      // neues Composite erstellen, welches Platz fuer den Kommentar laesst.
      this.parent = new Composite(parent, SWT.NONE);
      GridLayout layout = new GridLayout(2, false);
      layout.marginHeight=0;
      layout.marginWidth=0;
      this.parent.setLayout(layout);
      final GridData g = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
      g.widthHint = 240;
      this.parent.setLayoutData(g);
      control = getControl();
      final GridData inputGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
      inputGrid.widthHint = 110;
      control.setLayoutData(inputGrid);
    }
    else {
      this.parent = parent;
      control = getControl();
      final GridData inputGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
      inputGrid.widthHint = 240;
      control.setLayoutData(inputGrid);
    }

    // den Kommentar hinten dran fuegen
    if (this.comment != null) {
      final GridData labelGrid = new GridData(GridData.FILL_HORIZONTAL);
      commentLabel = new Label(this.parent,SWT.NONE);
      commentLabel.setText(this.comment);
      commentLabel.setForeground(Style.COLOR_COMMENT);
      commentLabel.setAlignment(SWT.RIGHT);
      commentLabel.setLayoutData(labelGrid);
    }

    // den Kommentarlistener noch dran haengen
    if (commentListener != null)
    {
      control.addListener(SWT.Selection,commentListener);
      control.addListener(SWT.FocusIn,commentListener);
      control.addListener(SWT.FocusOut,commentListener);
    }
  }

  /**
   * Gibt diesem Eingabefeld den Focus.
   */
  public abstract void focus();
  
}

/*********************************************************************
 * $Log: Input.java,v $
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