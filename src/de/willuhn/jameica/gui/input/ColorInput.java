/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.gui.input;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;

/**
 * Eingabefeld, zur Auswahl einer Farbe.
 * @author willuhn
 */
public class ColorInput extends ButtonInput
{
  private Text label        = null;
	private Color color       = null;
  private boolean forground = false;

  /**
   * Erzeugt ein neues ColorInput mit der angegebenen Farbe.
   * @param color initial anzuzeigende Farbe.
   * @param foreground true, wenn es sich um eine Vordergrundfarbe handelt.
   */
  public ColorInput(Color color, boolean foreground)
  {
    this.color = color;
    this.forground = foreground;
    addButtonListener(new Listener()
    {
      public void handleEvent(Event event)
      {
				Logger.debug("starting color choose dialog");
				ColorDialog cd = new ColorDialog(GUI.getShell());
        cd.setRGB(ColorInput.this.color.getRGB());
        cd.setText(Application.getI18n().tr("Bitte wählen Sie die Farbe aus"));
				setValue(cd.open());
				label.forceFocus(); // das muessen wir machen, damit der CommentListener ausgeloest wird
      }
    });
  }

  /**
   * @see de.willuhn.jameica.gui.input.ButtonInput#getClientControl(org.eclipse.swt.widgets.Composite)
   */
  public Control getClientControl(Composite parent)
  {
    label = GUI.getStyleFactory().createText(parent);
    label.setEditable(false);
    if (forground)
    {
      label.setForeground(color);
    }
    else
    {
      label.setBackground(color);
    }
    label.setText("the quick brown fox jumps over the lazy dog");
    return label;
  }

  /**
   * Liefert die aktuell ausgewaehlte Farbe.
   *
   * @return Rueckgabewert vom Typ {@link Color}.
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    return color;
  }

  /**
   * Erwartet ein Objekt vom Typ {@link Color}.
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (value == null)
      return;

    if (value instanceof RGB)
      this.color = new Color(GUI.getDisplay(),(RGB)value); 
    else if (value instanceof Color)
			this.color = (Color) value;
      
    if (this.label == null || this.label.isDisposed())
      return;

    if (forground)
    {
      label.setForeground(color);
    }
    else
    {
      label.setBackground(color);
    }
    label.redraw();
  }
  
  /**
   * Leer ueberschrieben, weil wir hier keine Farbaenderungen wollen
   * @see de.willuhn.jameica.gui.input.AbstractInput#update()
   */
  protected void update() throws OperationCanceledException
  {
  }

}

/*********************************************************************
 * $Log: ColorInput.java,v $
 * Revision 1.16  2011/08/08 10:45:05  willuhn
 * @C AbstractInput#update() ist jetzt "protected" (war package-private)
 *
 * Revision 1.15  2011-05-03 16:46:08  willuhn
 * @R Flatstyle entfernt - war eh nicht mehr zeitgemaess und rendere auf aktuellen OS sowieso haesslich
 * @C SelectInput verwendet jetzt Combo statt CCombo - das sieht auf den verschiedenen OS besser aus
 *
 **********************************************************************/