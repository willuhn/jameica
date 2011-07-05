/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/MultiInput.java,v $
 * $Revision: 1.6 $
 * $Date: 2011/07/05 17:35:38 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.util.Color;

/**
 * Erlaubt die Anzeige mehrerer Eingabefelder hinter einem Label.
 */
public class MultiInput implements Input
{
  /**
   * Context-Parameter fuer die Wichtung der Breite.
   */
  public final static String DATA_WEIGHT = "jameica.multiinput.weight";
  
  private Map<String,Object> data = new HashMap<String,Object>();

  private List<Input> inputs = new ArrayList<Input>();
  
  private Composite composite = null;
  private String name         = null;
  private String comment      = null;
  private Label commentLabel  = null;

  /**
   * ct.
   */
  public MultiInput()
  {
  }
  
  /**
   * ct.
   * @param inputs Liste von Eingabefeldern.
   */
  public MultiInput(Input... inputs)
  {
    if (inputs != null && inputs.length > 0)
    {
      for (Input i:inputs)
        this.add(i);
    }
  }
  
  /**
   * Fuegt ein Eingabe-Feld hinzu.
   * @param i das Eingabe-Feld.
   */
  public void add(Input i)
  {
    this.inputs.add(i);
  }
  
  /**
   * Deaktiviert alle enthaltenen Eingabefelder.
   * @see de.willuhn.jameica.gui.input.Input#disable()
   */
  public void disable()
  {
    this.setEnabled(false);
  }

  /**
   * Aktiviert alle enthaltenen Eingabefelder.
   * @see de.willuhn.jameica.gui.input.Input#enable()
   */
  public void enable()
  {
    this.setEnabled(true);
  }

  /**
   * Fokussiert das erste der enthaltenen Eingabefelder.
   * @see de.willuhn.jameica.gui.input.Input#focus()
   */
  public void focus()
  {
    // Das macht nur Sinn, wenn wir nur ein Element haben
    // Daher aktivieren wir grundsaetzlich nur das erste
    if (this.inputs.size() > 0)
      this.inputs.get(0).focus();
  }

  /**
   * Fuegt den Listener zu allen enthaltenen Eingabefeldern hinzu.
   * @see de.willuhn.jameica.gui.input.Input#addListener(org.eclipse.swt.widgets.Listener)
   */
  public void addListener(Listener l)
  {
    for (Input i:this.inputs)
      i.addListener(l);
  }

  /**
   * Liefert true, wenn sich mindestens eines der Eingabefelder geaendert hat.
   * @see de.willuhn.jameica.gui.input.Input#hasChanged()
   */
  public boolean hasChanged()
  {
    for (Input i:this.inputs)
    {
      if (i.hasChanged())
        return true;
    }
    return false;
  }

  /**
   * Liefert true, wenn mindestens eines der Eingabefelder Pflicht ist.
   * @see de.willuhn.jameica.gui.input.Input#isMandatory()
   */
  public boolean isMandatory()
  {
    for (Input i:this.inputs)
    {
      if (i.isMandatory())
        return true;
    }
    return false;
  }

  /**
   * Setzt das Pflicht-Flag bei allen enthaltenen Eingabefeldern.
   * @see de.willuhn.jameica.gui.input.Input#setMandatory(boolean)
   */
  public void setMandatory(boolean mandatory)
  {
    for (Input i:this.inputs)
      i.setMandatory(mandatory);
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
   * @see de.willuhn.jameica.gui.input.Input#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent)
  {
    this.paint(parent,240);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#paint(org.eclipse.swt.widgets.Composite, int)
   */
  public void paint(Composite parent, int width)
  {
    int size = this.inputs.size();
    if (this.comment != null)
      size++;

    for (Input i:this.inputs)
    {
      String name = i.getName();
      if (name != null)
        size++;
    }

    this.composite = new Composite(parent, SWT.NONE);
    final GridLayout layout = new GridLayout(size,false);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.horizontalSpacing = 5;
    layout.verticalSpacing = 0;
    this.composite.setLayout(layout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.widthHint = width;
    this.composite.setLayoutData(gd);

    for (Input i:this.inputs) {
      String name = i.getName();
      if (name != null && !(i instanceof CheckboxInput))
      {
        Label l = GUI.getStyleFactory().createLabel(this.composite,SWT.NONE);
        l.setText(name);
        l.setAlignment(SWT.RIGHT);
        l.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.HORIZONTAL_ALIGN_CENTER));
      }
      
      // Checken, ob wir einen Breiten-Hint haben
      Number weightHint = (Number) i.getData(DATA_WEIGHT);
      int len = (width / size) + 2; // 2 Pixel noch extra, damit das rechts besser abschliesst
      if (weightHint != null)
        i.paint(this.composite,(int) (len * weightHint.doubleValue()));
      else
        i.paint(this.composite,len);
    }
    
    // den Kommentar hinten dran fuegen
    if (this.comment != null) {
      commentLabel = GUI.getStyleFactory().createLabel(this.composite,SWT.NONE);
      commentLabel.setText(this.comment);
      commentLabel.setForeground(Color.COMMENT.getSWTColor());
      commentLabel.setAlignment(SWT.LEFT);
      commentLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

  }
  
  /**
   * @see de.willuhn.jameica.gui.input.Input#getControl()
   */
  public final Control getControl()
  {
    return this.composite;
  }


  /**
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    // Wir liefern eine Liste aller Ergebnisse zurueck
    List values = new ArrayList();
    for (Input i:this.inputs)
      values.add(i.getValue());
    return values;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#isEnabled()
   */
  public boolean isEnabled()
  {
    // Wir liefern nur dann true, wenn alle enabled sind
    for (Input i:this.inputs)
    {
      if (!i.isEnabled())
        return false;
    }
    return true;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled)
  {
    for (Input i:this.inputs)
      i.setEnabled(enabled);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (this.inputs.size() == 0)
      return;
    
    // Wenn es ein Array oder eine Liste ist, uebernehmen wir
    // die Werte so weit wie moeglich. Andernfalls kriegt
    // nur das erste Eingabe
    if (value instanceof Object[])
    {
      Object[] values = (Object[]) value;
      for (int i=0;i<values.length;++i)
      {
        if (i >= this.inputs.size()) // Keine Eingabefelder mehr uebrig
          return;
        
        this.inputs.get(i).setValue(values[i]);
      }
    }
    else if (value instanceof List)
    {
      List values = (List) value;
      for (int i=0;i<values.size();++i)
      {
        if (i >= this.inputs.size()) // Keine Eingabefelder mehr uebrig
          return;
        
        this.inputs.get(i).setValue(values.get(i));
      }
    }
    else
      this.inputs.get(0).setValue(value);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setData(java.lang.String, java.lang.Object)
   */
  public void setData(String key, Object data)
  {
    this.data.put(key,data);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getData(java.lang.String)
   */
  public Object getData(String key)
  {
    return this.data.get(key);
  }
}



/**********************************************************************
 * $Log: MultiInput.java,v $
 * Revision 1.6  2011/07/05 17:35:38  willuhn
 * @N Support fuer unterschiedlich breite Eingabefelder im MultiInput
 *
 * Revision 1.5  2011-05-11 08:42:07  willuhn
 * @N setData(String,Object) und getData(String) in Input. Damit koennen generische Nutzdaten im Eingabefeld gespeichert werden (siehe SWT-Widget)
 *
 * Revision 1.4  2011-05-03 10:13:11  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.3  2010-12-09 15:57:10  willuhn
 * @N Input-Label mit anzeigen, falls vorhanden
 *
 * Revision 1.2  2010-05-06 11:48:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2010/05/06 11:47:13  willuhn
 * @N Multi-Eingabefeld
 *
 **********************************************************************/