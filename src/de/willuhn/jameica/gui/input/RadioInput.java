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

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;

/**
 * Eingabefeld des Typs Radiobutton.
 */
public class RadioInput extends AbstractInput
{
  private static final Map<String,List<RadioInput>> groups = new HashMap<>();

  private String groupId  = null;
  private boolean focus   = false;
  private Button button   = null;
  private Object value    = null;
  private boolean enabled = true;

  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * 
   * Nachdem alle RadioInputs erzeugt wurden, muss einmal RadionInput.select(groupdId,value)
   * ausgefuehrt werden, um den Radiobutton zu aktivieren, der den derzeitigen Wert haelt.
   *
   * @param groupId frei zu vergebende ID der Gruppe, zu der der Radiobutton gehoert.
   * Radiobuttons werden ja fuer gewoehnlich immer in einer Gruppe von mehreren
   * verwendet, zwischen denen der User auswaehlen kann. Wird ein Radiobutton
   * aktiviert, wird der vorherige automatisch deaktiviert. Wenn man auf einem Dialog
   * mehrere solcher Gruppen hat, benoetigt Jameica hier eine eindeutige ID, um zu
   * erkennen, zu welcher Gruppe dieser Radiobutton gehoeren soll. Falls man jedoch nur
   * eine Gruppe Radiobuttons in einem Dialog benutzt, kann die ID auch NULL sein.
   * Dann gehoeren alle Radiobuttons automatisch zur selben Gruppe.
   * @param value true, wenn die Radiobox aktiviert werden soll.
   */
  public RadioInput(String groupId, Object value)
  {
    this.groupId = groupId;
    this.value   = value;

    // Zur Gruppe hinzutun
    List<RadioInput> group = groups.get(this.groupId);
    if (group == null)
    {
      group = new LinkedList<>();
      groups.put(this.groupId,group);
    }
    group.add(this);
  }
  
  /**
   * Aktiviert den Radiobutton in der angegebenen Gruppe, der den angegebenen Wert haelt.
   * @param groupId die ID der Gruppe.
   * @param value der Wert.
   */
  public static void select(String groupId, Object value)
  {
    List<RadioInput> list = groups.get(groupId);
    for (RadioInput input:list)
    {
      if (input.button == null || input.button.isDisposed())
        continue; // ist noch nicht gezeichnet oder nicht mehr aktiv

      try
      {
        input.button.setSelection(BeanUtil.equals(value,input.value));
      }
      catch (RemoteException re)
      {
        Logger.error("unable to compare objects",re);
      }
      input.button.redraw();
    }
  }
  
  /**
   * Liefert den Wert von dem Radiobutton aus der Gruppe, der derzeit aktiviert ist
   * oder NULL, wenn derzeit keiner aktiviert ist.
   * @param groupId die ID der Gruppe oder NULL, wenn keine explizite Gruppe angegeben wurde.
   * @return der Wert.
   */
  public static Object getValue(String groupId)
  {
    List<RadioInput> list = groups.get(groupId);
    for (RadioInput input:list)
    {
      if (input.button == null || input.button.isDisposed())
        continue; // ist noch nicht gezeichnet oder nicht mehr aktiv
      
      // Das ist der aktive.
      if (input.button.getSelection())
        return input.value;
    }
    return null;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getControl()
   */
  public Control getControl()
  {
		if (button != null)
			return button;
		
    button = new Button(getParent(), SWT.RADIO);

    Object tooltip = this.getData(DATAKEY_TOOLTIP);
    if (tooltip != null)
      this.button.setToolTipText(tooltip.toString());

    // Der Listener uebernimmt das Deselektieren der anderen Radiobuttons
    button.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        e.doit = false; // Rekursion vermeiden
        RadioInput.select(groupId,value);
      }
    });
    
    // Dispose-Listener zum Leer-Raeumen der Group
    // Das wird zwar redundant ausgefuehrt, weil es jeder ButtonInput
    // der Gruppe macht - ist aber nicht weiter wild.
    button.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        groups.remove(groupId);
      }
    });
    
    String name = this.getName();
    if (name != null)
      button.setText(name);
    
    if (this.focus)
      button.setFocus();
    
		button.setEnabled(enabled);
		
		if (isMandatory() && Application.getConfig().getMandatoryLabel())
      button.setForeground(Color.ERROR.getSWTColor());
    return button;
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#setName(java.lang.String)
   */
  @Override
  public void setName(String name)
  {
    super.setName(name);
    
    if (name != null && this.button != null && !this.button.isDisposed())
      this.button.setText(name);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    return this.value;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    this.value = value;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#focus()
   */
  public void focus()
  {
    this.focus = true;
    if (this.button != null && !this.button.isDisposed())
      button.setFocus();
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
    if (button == null || button.isDisposed())
      return enabled;
    return button.getEnabled();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
    if (button != null && !button.isDisposed())
      button.setEnabled(enabled);
  }

  /**
   * Leer ueberschrieben, weil wir hier keine Farbaenderungen wollen
   * @see de.willuhn.jameica.gui.input.AbstractInput#update()
   */
  @Override
  protected void update() throws OperationCanceledException
  {
  }
  
  
}

/*********************************************************************
 * $Log: RadioInput.java,v $
 * Revision 1.1  2011/08/31 10:51:37  willuhn
 * @N Endlich hat Jameica ein RadioInput ;)
 *
 **********************************************************************/