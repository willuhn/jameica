/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/AbstractView.java,v $
 * $Revision: 1.4 $
 * $Date: 2003/11/20 03:48:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 ****************************************************************************/
package de.willuhn.jameica.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.GUI;
import de.willuhn.jameica.util.Style;
import de.willuhn.jameica.views.parts.LabelGroup;

/**
 * Basis-Klasse fuer alles Views.
 * @author willuhn
 */
public abstract class AbstractView
{
	private Label dotLine;
	Composite parent;
  Object currentObject;
  
  /**
   * Konstruktor.
   * @param o ein optionales Datenobjekt, welches in der View verarbeitet werden soll.
   */
  public AbstractView(Object o)
  {
    currentObject = o;
  }

  /**
   * Wird aufgerufen, wenn der Dialog geoeffnet wird. Diese Methode muss von abgeleiteteten
   * Klassen ueberschrieben werden, um dort den Content zu malen.
   */
  public abstract void bind();

  /**
   * Wird aufgerufen, wenn der Dialog verlassen wird. Diese Methode muss von abgeleiteten
   * Klassen ueberschrieben werden, um dort Aufraeumarbeiten vorzunehmen.
   */
  public abstract void unbind();

  /**
   * Setzt die Ueberschrift des Dialogs.
   * @param headline Name der Ueberschrift.
   */
  public void setHeadline(String headline)
	{
    Composite comp = new Composite(getParent(), SWT.NONE);
    comp.setLayout(new GridLayout());

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayoutData(gd);

    Label title = new Label(comp, SWT.NONE);
    title.setText(headline);
    title.setLayoutData(new GridData());

		title.setFont(Style.getFont("Verdana", 8, SWT.BOLD));
		title.setForeground(new Color(GUI.display,0,0,0));
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = 3;
    dotLine = new Label(comp, SWT.NONE);
    dotLine.setText("");
    dotLine.setLayoutData(data);


		dotLine.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					drawDottedLine(e);
				}
		}); 
	}
	
	/**
	 * Zeichnet eine gepunktete Linie unter die Ueberschrift.
	 * @param e PaintEvent
	 */
	private void drawDottedLine(PaintEvent e)
	{
		Point p = dotLine.getSize();
		e.gc.setLineWidth(1); 
		e.gc.setLineStyle(SWT.LINE_SOLID); 
		e.gc.setForeground(new Color(GUI.display, 125, 125, 125)); 
		for (int i=0; i<p.x;) {
			e.gc.drawLine(i,0,i,0);
			i=i+3;
		}
	}
	
	/**
   * Liefert das Composite, in dem der Dialog dargestellt werden soll.
   * @return
   */
  public Composite getParent()
	{
		return parent;
	}

	/**
   * Speichert das Composite, in dem der Dialog dargestellt werden soll.
   * @param parent
   */
  public void setParent(Composite parent)
	{
		this.parent = parent;
	}

	/**
   * Liefert das dieser View uebergebene Daten-Objekt zurueck. 
   * @return
   */
  public Object getCurrentObject()
	{
		return currentObject;
	}

  /**
   * Erzeugt eine Standard-Group fuer zweispaltige Dialoge (links Feldname, rechts Eingabefeld).
   * @param name
   * @return
   */
  protected LabelGroup createLabelGroup(String name)
  {
    Group group = new Group(parent, SWT.NONE);
    group.setText(name);
    group.setLayout(new GridLayout(2, false));
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    return new LabelGroup(group);
  }

}



/***************************************************************************
 * $Log: AbstractView.java,v $
 * Revision 1.4  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 * Revision 1.3  2003/10/29 00:41:27  willuhn
 * *** empty log message ***
 *
 ***************************************************************************/