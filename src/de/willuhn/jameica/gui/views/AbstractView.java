/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/AbstractView.java,v $
 * $Revision: 1.3 $
 * $Date: 2003/10/29 00:41:27 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 ****************************************************************************/
package de.willuhn.jameica.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.GUI;
import de.willuhn.jameica.util.SWTFactory;
import de.willuhn.jameica.util.Style;

public abstract class AbstractView
{
	private Label dotLine;
	Composite parent;
  Object currentObject;
  
  public AbstractView(Object o)
  {
    currentObject = o;
  }

  public abstract void bind();

  public abstract void unbind();

  public void setHeadline(String headline)
	{
		Composite comp = SWTFactory.getComposite(getParent(), new GridLayout());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = 5;
		comp.setLayoutData(gd);
		Label title = SWTFactory.getLabel(comp, headline, new GridData());
		title.setFont(Style.getFont("Verdana", 8, SWT.BOLD));
		title.setForeground(new Color(GUI.display,0,0,0));
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = 3;
		// gepunktete Linie
		dotLine = SWTFactory.getLabel(comp, "", data);
		dotLine.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					drawDottedLine(e);
				}
		}); 
	}
	
	/**
	 * Malt eine gepunktete Linie auf ein Label.
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
   * Liefert das dieser View uebergebene Objekt zurueck. 
   * @return
   */
  public Object getCurrentObject()
	{
		return currentObject;
	}

	public void addGlobalListener(Composite c)
	{
		final Control[] controls = c.getChildren();
		for (int i=0;i<controls.length;++i)
		{
			controls[i].addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					onGlobalChange();
				}
			});
			if (controls[i] instanceof Composite)
				addGlobalListener((Composite) controls[i]);
		}
	}

	public void onGlobalChange()
	{
	}
}



/***************************************************************************
 * $Log: AbstractView.java,v $
 * Revision 1.3  2003/10/29 00:41:27  willuhn
 * *** empty log message ***
 *
 ***************************************************************************/