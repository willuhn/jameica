/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Attic/Input.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/03/03 22:27:10 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.parts;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.Jameica;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.util.Style;
import de.willuhn.util.I18N;

/**
 * Basisklasse fuer Eingabefelder.
 * @author willuhn
 */
public abstract class Input
{

	I18N i18n;

  private Composite parent = null;
  private String comment = null;
  private Label commentLabel = null;
  private Control control = null;
	private ArrayList listeners = new ArrayList();

	/**
   * Erzeugt ein neues Eingabe-Feld.
   */
  public Input()
	{
		i18n = PluginLoader.getPlugin(Jameica.class).getResources().getI18N();
	}

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
	 * Fuegt dem Eingabe-Feld einen Listener hinzu, der bei jedem Focus-Wechsel ausgeloest wird.
	 * Besteht das Eingabe-Feld aus mehreren Teilen (z.Bsp. bei SearchInput aus Eingabe-Feld
	 * + Knopf dahinter) dann wird der Listener bei Focus-Wechsel jedes dieser
	 * Teile ausgeloest.
   * @param l zu registrierender Listener.
   */
  public void addListener(Listener l)
	{
		listeners.add(l);
	}

  /**
   * Fuegt hinter das Eingabefeld noch einen Kommentar.
   * Existiert der Kommentar bereits, wird er gegen den neuen ersetzt.
   * Hinweis: Wird die Funktion nicht aufgerufen, bevor das Eingabe-Feld
   * gemalt wird, dann wird es auch nicht angezeigt. Denn vorm Malen
   * muss bekannt sein, ob es angezeigt werden soll, damit der Platz
   * dafuer reserviert werden kann.
   * @param comment Kommentar.
   */
  public void setComment(String comment)
  {
    this.comment = ""+comment;
		try {
			commentLabel.setText(this.comment); // wegen NullPointer
			commentLabel.redraw();
		}
		catch(Exception e)
		{
			// nicht schlimm. Das passiert, wenn das Eingabe-Feld noch
			// nicht gemalt wurde.
		}
  }

  /**
   * Positioniert und malt das Eingabefeld im uebergebenen Composite.
   * Es wird dabei mit einer vorgegebenen Standard-Breite gemalt.
   * @param parent Das Composite, in dem das Eingabefeld gemalt werden soll.
   */
  public void paint(Composite parent)
  {
    paint(parent,240);
  }

  /**
   * Positioniert und malt das Eingabefeld im uebergebenen Composite.
   * Es wird jedoch mit der angegebenen Breite gemalt.
   * @param parent Das Composite, in dem das Eingabefeld gemalt werden soll.
   * @param width Breite des Composites.
   */
  public void paint(Composite parent,int width)
  {
    if (this.comment != null)
    {

      // neues Composite erstellen, welches Platz fuer den Kommentar laesst.
      this.parent = new Composite(parent, SWT.NONE);
			this.parent.setBackground(Style.COLOR_BG);
      GridLayout layout = new GridLayout(2, false);
      layout.marginHeight=0;
      layout.marginWidth=0;
      this.parent.setLayout(layout);
      final GridData g = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
      g.widthHint = width;
      this.parent.setLayoutData(g);

      control = getControl();
      final GridData inputGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
      inputGrid.widthHint = width / 2;
      control.setLayoutData(inputGrid);
    }
    else {
      this.parent = parent;
      control = getControl();
      final GridData inputGrid = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
      inputGrid.widthHint = width;
      control.setLayoutData(inputGrid);
    }

    // den Kommentar hinten dran fuegen
    if (this.comment != null) {
      final GridData labelGrid = new GridData(GridData.FILL_HORIZONTAL);
      commentLabel = new Label(this.parent,SWT.NONE);
      commentLabel.setText(this.comment);
      commentLabel.setForeground(Style.COLOR_COMMENT);
			commentLabel.setBackground(Style.COLOR_BG);
      commentLabel.setAlignment(SWT.LEFT);
      commentLabel.setLayoutData(labelGrid);
    }

    // die Listener noch dran haengen
    Listener l = null;
    for (int i=0;i<listeners.size();++i)
    {
    	l = (Listener) listeners.get(i);
			control.addListener(SWT.Selection,l);
			control.addListener(SWT.FocusIn,l);
			control.addListener(SWT.FocusOut,l);
      
			// Es kann sein, dass das Control ein Composite ist (z.Bsp. bei SearchInput)
			// Wenn es also aus mehren Elementen besteht, dann muessen wir
			// den Listener an alle haengen.
			if (control instanceof Composite)
			{
				Composite c = (Composite) control;
				Control[] children = c.getChildren();
				for (int j=0;j<children.length;++j)
				{
					children[i].addListener(SWT.Selection,l);
					children[i].addListener(SWT.FocusIn,l);
					children[i].addListener(SWT.FocusOut,l);
				}
			}
    }
  }

  /**
   * Gibt diesem Eingabefeld den Focus.
   */
  public abstract void focus();
  
  /**
   * Deaktiviert das Eingabefeld.
   */
  public abstract void disable();
  
  /**
   * Aktiviert das Eingabefeld.
   */
  public abstract void enable();
}

/*********************************************************************
 * $Log: Input.java,v $
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