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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
  static final I18N i18n = Application.getI18n();

  private Map<String,Object> data = new HashMap<>();
  
  private static final Object PLACEHOLDER = new Object();

  private Composite parent = null;

  private String name        = null;
  private String comment     = null;
  private Label commentLabel = null;

  private Control control = null;
	private ArrayList<Listener> listeners = new ArrayList<>();

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
   * Liefert die Stylebits ({@link GridData}-Settings), welche zum Erstellen des Widgets
   * verwendet werden.
   * @return die Style.Bits.
   */
  public int getStyleBits()
  {
    return GridData.FILL_HORIZONTAL;
  }

  @Override
  public void addListener(Listener l)
	{
		listeners.add(l);
	}

  @Override
  public void setComment(String comment)
  {
    this.comment = comment;
		if (commentLabel != null && ! commentLabel.isDisposed() && this.comment != null)
		{
			commentLabel.setText(this.comment);
			commentLabel.redraw();
		}
  }

  @Override
  public final void paint(Composite parent)
  {
    paint(parent,240);
  }

  @Override
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
    	l = listeners.get(i);
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
          l = listeners.get(i);
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
    if (!(control instanceof Text))
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

    // Bei Einzeiligen Eingabefeldern kann in der Zwischenablage
    // ein Zeilenumbruch enthalten sein. Der darf zwar nicht im
    // Eingabefeld landen, sollte das Copy'n'Paste aber auch nicht
    // behindern - daher entfernen wir das
    final boolean single = ((control.getStyle() & SWT.SINGLE) != 0);
    if (single)
    {
      control.addListener(SWT.Verify, new Listener()
      {
        public void handleEvent(Event e)
        {
          e.text = e.text.replace("\n","").replace("\r","");
        }
      });
    }
    
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
   *
   * <p>Werden beide Funktionen {@link #setValidChars(String)} <b>und</b>
   * {@link #setInvalidChars(String)} benutzt, kann nur noch die verbleibende
   * Restmenge eingegeben werden. Das sind die Zeichen, die in validChars
   * angegeben und in invalidChars nicht enthalten sind.
   *
   * @param chars erlaubte Zeichen
   */
  public void setValidChars(String chars)
  {
    this.validChars = chars;
  }

  /**
   * Definiert eine Liste von Zeichen, die nicht eingegeben werden koennen.
   * Wird diese Funktion verwendet, dann duerfen die angegebenen Zeichen nicht
   * mehr verwendet werden.
   *
   * @param chars nicht-erlaubte Zeichen
   * @see #setValidChars(String)
   */
  public void setInvalidChars(String chars)
  {
    this.invalidChars = chars;
  }

  @Override
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
  protected void update() throws OperationCanceledException
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
      this.control.setBackground(null);
    }
    finally
    {
      inUpdate = false;
    }
  }
  
  @Override
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

  @Override
  public String getName()
  {
    return this.name;
  }

  @Override
  public void setName(String name)
  {
    this.name = name;

    // Label bei Bedarf aktualisieren
    if (this.name != null)
    {
      // Checken, ob wir ein Label haben
      Object o = this.getData("jameica.label");
      if (!(o instanceof Label))
        return;
      
      Label label = (Label) o;
      if (label.isDisposed())
        return;
      label.setText(this.name);
    }
  }

  @Override
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

  @Override
  public void setData(String key, Object data)
  {
    this.data.put(key,data);
  }

  @Override
  public Object getData(String key)
  {
    return this.data.get(key);
  }

}

/*********************************************************************
 * $Log: AbstractInput.java,v $
 * Revision 1.30  2011/09/26 11:18:42  willuhn
 * Zeilenumbruch aus einzeiligen Eingabefeldern beim Copy'n'Paste entfernen - das behindert sonst das Paste - siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=78495#78495
 *
 * Revision 1.29  2011-08-08 11:32:29  willuhn
 * @C AbstractInput#getStyleBits() public weil ...
 * @C ...vertikale Ausrichtung des Labels im Container nicht mehr hart mit "instanceof TextAreaInput" sondern anhand des Stylebits festlegen
 *
 * Revision 1.28  2011-08-08 10:45:05  willuhn
 * @C AbstractInput#update() ist jetzt "protected" (war package-private)
 *
 * Revision 1.27  2011-05-11 08:42:07  willuhn
 * @N setData(String,Object) und getData(String) in Input. Damit koennen generische Nutzdaten im Eingabefeld gespeichert werden (siehe SWT-Widget)
 *
 * Revision 1.26  2011-05-03 10:13:11  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 **********************************************************************/