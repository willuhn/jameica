/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/style/Attic/StyleFactoryFlatImpl.java,v $
 * $Revision: 1.6 $
 * $Date: 2004/08/18 23:14:19 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.style;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableTree;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.internal.widgets.FormUtil;
import org.eclipse.ui.forms.widgets.Hyperlink;

import de.willuhn.jameica.gui.util.Color;

/**
 * Implementierung der Style-Factory im Flat-Style.
 */
public class StyleFactoryFlatImpl implements StyleFactory
{

  private static BorderPainter borderPainter;
  private static VisibilityHandler visibilityHandler;
  private static KeyboardHandler keyboardHandler;

	static
	{
		borderPainter = new BorderPainter();
		visibilityHandler = new VisibilityHandler();
		keyboardHandler = new KeyboardHandler();
	}

  /**
   * @see de.willuhn.jameica.gui.style.StyleFactory#createButton(org.eclipse.swt.widgets.Composite)
   */
  public Button createButton(Composite parent)
  {
		Button button = new Button(parent,SWT.PUSH | SWT.FLAT);
		adapt(button, true, true);
		return button;
  }

  /**
   * @see de.willuhn.jameica.gui.style.StyleFactory#createText(org.eclipse.swt.widgets.Composite)
   */
  public Text createText(Composite parent)
  {
  	Text text = new Text(parent, SWT.SINGLE);
  	text.setData(BorderPainter.KEY_DRAW_BORDER, BorderPainter.TEXT_BORDER);
		text.setForeground(Color.WIDGET_FG.getSWTColor());
		text.setBackground(Color.WIDGET_BG.getSWTColor());
		adapt(text, true, false);
		parent.addPaintListener(borderPainter);
		return text;
  }

  /**
   * @see de.willuhn.jameica.gui.style.StyleFactory#createCombo(org.eclipse.swt.widgets.Composite)
   */
  public CCombo createCombo(Composite parent)
	{
  	final CCombo combo = new CCombo(parent,SWT.READ_ONLY);
		combo.setData(BorderPainter.KEY_DRAW_BORDER, BorderPainter.TEXT_BORDER);
		combo.setForeground(Color.WIDGET_FG.getSWTColor());
		parent.addPaintListener(borderPainter);
		return combo;
	}

	/**
	 * @see de.willuhn.jameica.gui.style.StyleFactory#createTable(org.eclipse.swt.widgets.Composite, int)
	 */
	public Table createTable(Composite parent, int style)
	{
		Table table = new Table(parent, SWT.BORDER ^ style);
		table.setData(BorderPainter.KEY_DRAW_BORDER, BorderPainter.TEXT_BORDER);
		parent.addPaintListener(borderPainter);
		return table;
	}

  /**
   * @see de.willuhn.jameica.gui.style.StyleFactory#getName()
   */
  public String getName() {
    return "Flat-Look";
  }


	//////////////////////////////////////////////////////////////////////////////
	// Der folgende Code ist nahezu 1:1 aus SWT-Forms entnommen.
	// Grund: Bei deaktivierten Controls wurde kein Rahmen drum gemalt -
	// das ist unschoen.

	private void adapt(Control control, boolean trackFocus, boolean trackKeyboard) {
		if (trackFocus)
			control.addFocusListener(visibilityHandler);
		if (trackKeyboard)
			control.addKeyListener(keyboardHandler);
	}

	private static class VisibilityHandler extends FocusAdapter {
		/**
		 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
		 */
		public void focusGained(FocusEvent e) {
			Widget w = e.widget;
			if (w instanceof Control) {
				FormUtil.ensureVisible((Control) w);
			}
		}
	}
	private static class KeyboardHandler extends KeyAdapter {
		/**
		 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
		 */
		public void keyPressed(KeyEvent e) {
			Widget w = e.widget;
			if (w instanceof Control) {
				FormUtil.processKey(e.keyCode, (Control) w);
			}
		}
	}

	private static class BorderPainter implements PaintListener
	{

		/**
		 * Konstante fuer "Rahmen malen".
		 */
		public static final String KEY_DRAW_BORDER = "FormWidgetFactory.drawBorder";

    /**
     * Konstante fuer "Rahmen malen".
     */
		public static final String TREE_BORDER = "treeBorder";

    /**
     * Konstante fuer "Rahmen malen".
     */
		public static final String TEXT_BORDER = "textBorder";


		/**
		 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
		 */
		public void paintControl(PaintEvent event)
		{
			Composite composite = (Composite) event.widget;
			Control[] children = composite.getChildren();

			for (int i = 0; i < children.length; i++)
			{
				Control c = children[i];

				if (!c.isVisible()) continue;
				if (c instanceof Hyperlink) continue;

				boolean inactiveBorder = false;
				boolean textBorder = false;

				Object flag = c.getData(KEY_DRAW_BORDER);

				if (flag != null)
				{
					if (flag.equals(Boolean.FALSE)) continue;

					if (flag.equals(TREE_BORDER))
						inactiveBorder = true;
					else if (flag.equals(TEXT_BORDER))
						textBorder = true;
				}

				Rectangle b = c.getBounds();
				GC gc = event.gc;

				if (!inactiveBorder && (c instanceof Text || c instanceof CCombo || textBorder))
				{

					gc.setForeground(c.getBackground());
					gc.drawRectangle(b.x - 1, b.y - 1, b.width + 1, b.height + 1);

					gc.setForeground(Color.WIDGET_FG.getSWTColor());
					if (c instanceof CCombo)
						gc.drawRectangle(b.x - 1, b.y - 1, b.width + 1, b.height + 1);
					else
						gc.drawRectangle(b.x - 1, b.y - 2, b.width + 1, b.height + 3);

				}
				else if (inactiveBorder || c instanceof Table	|| c instanceof Tree || c instanceof TableTree)
				{

					gc.setForeground(Color.BORDER.getSWTColor());
					gc.drawRectangle(b.x - 1, b.y - 1, b.width + 2, b.height + 2);
				}
			}
		}
	}

}


/**********************************************************************
 * $Log: StyleFactoryFlatImpl.java,v $
 * Revision 1.6  2004/08/18 23:14:19  willuhn
 * @D Javadoc
 *
 * Revision 1.5  2004/06/24 21:32:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/06/17 22:07:12  willuhn
 * @C cleanup in tablePart and statusBar
 *
 * Revision 1.3  2004/06/14 22:05:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.1  2004/06/03 00:24:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/06/02 21:15:15  willuhn
 * @B win32 fixes in flat style
 * @C made ButtonInput more abstract
 *
 * Revision 1.3  2004/05/23 18:15:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/05/23 16:34:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 **********************************************************************/