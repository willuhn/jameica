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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.swt.PopupList;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;

/**
 * Erzeugt eine Such-Box, in der man Text eingaben kann.
 * Kann prima zur Erstellung eines Suchfeldes genutzt werden,
 * welches bei jeder Eingabe eines Zeichens eine Liste mit
 * Vorschlaegen anzeigen kann.
 * 
 * Beispiel fuer die Verwendung:
 * 
 * project = new SearchInput() {
 *   public List startSearch(String text) {
 *     try {
 *       DBService service = (DBService) Application.getServiceFactory().lookup(Plugin.class,"database");
 *       DBIterator result = service.createList(Project.class);
 *       if (text != null) {
 *         text = "%" + text + "%";
 *         result.addFilter("(name like ? or description like ?)", new Object[]{text,text});
 *       }
 *       return PseudoIterator.asList(result);
 *     }
 *     catch (Exception e) {
 *       Logger.error("unable to load project list",e);
 *       return null;
 *     }
 *   }
 * };
 * project.setValue(getTask().getProject());
 *
 * @author willuhn
 */
public class SearchInput extends AbstractInput
{
  /**
   * Das Default-Delay nach dessen Ablauf das Widget mit der Suche beginnen soll.
   * Angabe in Millisekunden.
   */
  public final static int DEFAULT_DELAY = 1000;
  
  // Fachdaten
  private String attribute    = null;
  private Object value        = null;
  
  // SWT-Daten
  private Text text           = null;
  private boolean enabled     = true;
  private String search       = null;
  private boolean focus       = false;
  private int maxLength       = 0;
  private int startAt         = 1;
  private int minWidth        = 0;
  
  private List<Listener> listeners = new ArrayList<Listener>();
  
  private int delay           = DEFAULT_DELAY;

  /**
   * Erzeugt eine neue Such-Box.
   */
  public SearchInput()
  {
    super();
    this.search = Application.getI18n().tr("Suche...");
  }
  
  /**
   * Legt einen abweichenden Text fest, der vor Eingabe der Suche angezeigt wird.
   * Standardmaessig wird "Suche..." verwendet. Mit dieser Funktion kann der
   * Wert geaendert werden.
   * @param text der anzuzeigende Text.
   */
  public void setSearchString(String text)
  {
    if (text != null)
      this.search = text;
  }

  /**
   * Legt den Namen des Attributes fest, welches von den Objekten angezeigt werden
   * soll. Bei herkoemmlichen Beans wird also ein Getter mit diesem Namen aufgerufen. 
   * Wird kein Attribut angegeben, wird bei Objekten des Typs <code>GenericObject</code>
   * der Wert des Primaer-Attributes angezeigt, andernfalls der Wert von <code>toString()</code>.
   * @param name Name des anzuzeigenden Attributes (muss im GenericObject
   * via getAttribute(String) abrufbar sein).
   */
  public void setAttribute(String name)
	{
		if (name != null)
			this.attribute = name;
	}
  
  /**
   * Definiert die maximal eingebbare Menge von Zeichen.
   * @param maxLength
   */
  public void setMaxLength(int maxLength)
  {
    this.maxLength = maxLength;
    if (this.text != null && !this.text.isDisposed())
      this.text.setTextLimit(this.maxLength);
  }
  
  /**
   * Legt fest, ab wieviel eingegebenen Zeichen die Suche starten soll.
   * @param length Mindest-Anzahl von Zeichen, ab der die Suche starten soll.
   * Werte <= 0 werden ignoriert.
   * Default: 1.
   */
  public void setStartAt(int length)
  {
    if (length > 0)
      this.startAt = length;
  }
  
  /**
   * Legt eine Mindest-Breite fuer die Ergebnisliste in Pixeln fest.
   * Wird kein Wert oder 0 angegeben, wird die Breite automatisch ermittelt.
   * @param width die Mindest-Breite der Ergebnisliste.
   */
  public void setMinWidth(int width)
  {
    if (width >= 0)
      this.minWidth = width;
  }
  
  /**
   * Legt ein abweichendes Delay fest.
   * @param millis das Delay.
   */
  public void setDelay(int millis)
  {
    if (millis > 0)
      this.delay = millis;
  }

  private boolean inSearch = false;

  /**
   * Ersetzt alle Elemente der Selectbox gegen die aus der uebergebenen Liste.
   * @param list
   */
  private void setList(List list)
  {
    if (inSearch || this.text == null || this.text.isDisposed())
      return;

    // Nichts gefunden
    if (list == null || list.size() == 0)
      return;

    try
    {
      // Liste von Strings fuer die Anzeige in der Popup-Box.
      List<String> items  = new ArrayList<String>();
      List values = new ArrayList();
      
      for (Object object:list)
      {
        if (object == null)
          continue;

        // Anzuzeigenden Text ermitteln
        String text = format(object);
        if (text == null)
          continue;
        items.add(text);
        values.add(object);
      }

      Point location = this.text.toDisplay(this.text.getLocation());
      Rectangle rect = this.text.getClientArea();

      PopupList popup = new PopupList(GUI.getShell());
      popup.setItems(items.toArray(new String[items.size()]));
      popup.setMinimumWidth(this.minWidth);
      int selected = popup.open(new Rectangle(location.x, rect.y + location.y + rect.height, rect.width, 0));

      if (selected >= 0) // ist -1, wenn nichts ausgewaehlt wurde
        this.setValue(values.get(selected));
    }
    catch (Exception e)
    {
      Logger.error("unable to create combo box",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Laden der Daten..."),StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Formatiert die Bean passend fuer die Anzeige in der Combo-Box.
   * @param bean die Bean.
   * @return String mit dem anzuzeigenden Wert.
   */
  protected String format(Object bean)
  {
    if (bean == null)
      return null;
    try
    {
      if (this.attribute == null || this.attribute.length() == 0)
        return BeanUtil.toString(bean);

      Object value = BeanUtil.get(bean,this.attribute);
      return value == null ? null : value.toString();
    }
    catch (RemoteException re)
    {
      Logger.error("unable to format object",re);
      return null;
    }
  }

  /**
   * Diese Funktion sollte ueberschrieben werden, wenn die Liste
   * der Vorschlaege bei Eingabe von Suchbegriffen aktualisiert werden soll.
   * Die Standardimplementierung macht schlicht keine Suche sondern
   * laesst alles, wie es ist.
   * @param text der momentan eingegebene Suchtext.
   * @return eine neue Liste mit den als Suchvorschlaegen anzuzeigenden Objekten.
   * Die Funktion kann sowohl null als auch eine leere Liste zurueckgeben,
   * wenn nichts gefunden wurde.
   */
  public List startSearch(String text)
  {
    return null;
  }
  
  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#addListener(org.eclipse.swt.widgets.Listener)
   */
  public void addListener(Listener l)
  {
    this.listeners.add(l);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getControl()
   */
  public Control getControl()
  {
    if (this.text != null && !this.text.isDisposed())
      return this.text;

    this.text = GUI.getStyleFactory().createText(getParent());

    Object tooltip = this.getData(DATAKEY_TOOLTIP);
    if (tooltip != null)
      this.text.setToolTipText(tooltip.toString());

    String display = this.value == null ? null : format(this.value);
    if (display == null)
      display = this.search;
    
    // Wenn wir bereits den Focus haben, darf das "Suche..." nicht
    // mehr drin stehen
    if (this.value == null && this.focus)
      display = "";
    
    this.text.setText(display);

    // "Suche..." grau einfaerben - aber nur, wenn wir keinen Focus haben
    if ((!this.focus && this.value == null) || !enabled)
      this.text.setForeground(Color.COMMENT.getSWTColor());
    
    this.text.setEnabled(enabled);
    this.text.setEditable(enabled);

    if (maxLength > 0)
      text.setTextLimit(maxLength);

    final Listener focusIn = new Listener() {
      public void handleEvent(Event event)
      {
        if (text == null || text.isDisposed())
          return;

        // Text "Suche..." entfernen, wenn man reinklickt
        String s = text.getText();
        if (s != null && s.equals(search))
        {
          text.setText("");
          text.setForeground(Color.FOREGROUND.getSWTColor());
        }
      }
    };
    this.text.addListener(SWT.FOCUSED,focusIn);
    this.text.addListener(SWT.FocusIn,focusIn);
    this.text.addListener(SWT.FocusOut,new Listener() {
      public void handleEvent(Event event)
      {
        if (text == null || text.isDisposed())
          return;

        // Text "Suche..." eintragen, wenn nichts drin steht
        String s = text.getText();
        if (s == null || s.length() == 0)
        {
          text.setText(search);
          text.setForeground(Color.COMMENT.getSWTColor());
        }
      }
    });

    if (this.focus)
      this.text.setFocus();

    // Loest die Suche aus
    final Listener listener = new Listener()
    {
      public void handleEvent(Event event)
      {
        if (text == null || text.isDisposed())
          return;

        // Bei Escape loesen wir nicht aus
        // Damit muessen wir jetzt nicht mehr vergleichen,
        // ob sich der Text geaendert hat. Denn mit ESC wird das
        // Popup geschlossen - da das allerdings bis hierhin durchgereicht
        // wird, waere das Poup sonst in einem Loop immer wieder aufgegangen.
        // Jetzt nicht mehr ;)
        if (event.keyCode == SWT.ESC)
          return;
        
        if (inSearch)
        {
          inSearch = false;
          return;
        }

        String newText = text.getText();
        if (newText == null || newText.length() <= startAt)
          return; // Noch kein Suchbegriff - keine Suche

        if (newText.equals(search))
          return; // Nach "Suche..." suchen wir nicht

        List newList = startSearch(newText);
        setList(newList);
      }
    
    };
    this.text.addListener(SWT.KeyUp, new DelayedListener(this.delay,listener));
    
    // Bei Enter loesen wir sofort aus. Ohne auf das Timeout zu warten
    this.text.addListener(SWT.Traverse,new Listener() {
      public void handleEvent(Event event)
      {
        if (event.detail == SWT.TRAVERSE_RETURN)
          listener.handleEvent(event);
      }
    });

    return this.text;
  }

  /**
   * Liefert das aktuelle Objekt.
   * Das ist entweder das ausgewaehlte aus der letzten Suche oder das
   * initial uebergebene.
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    if (this.text == null || this.text.isDisposed())
      return this.value;
    String s = text.getText();
    if (s == null || s.length() == 0 || s.equals(this.search))
      return null;
    return value;
  }

	/**
	 * Liefert den derzeit angezeigten Text zurueck.
   * @return Text.
   */
  public String getText()
	{
    if (this.text == null || this.text.isDisposed())
      return null;
		String s = this.text.getText();
    if (s == null || s.length() == 0)
      return s;
    if (s.equals(this.search))
      return null;
    return s;
	}

  /**
   * Speichert den anzuzeigenden Text.
   * @param s Text.
   */
  public void setText(String s)
  {
    if (s != null && this.text != null && !this.text.isDisposed())
    {
      this.text.setText("");
      this.text.insert(s);
    }
  }


  
  /**
   * @see de.willuhn.jameica.gui.input.Input#focus()
   */
  public void focus()
  {
    this.focus = true;
    
    if (text != null && !text.isDisposed())
      text.setFocus();
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
   * @see de.willuhn.jameica.gui.input.Input#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
    if (text != null && !text.isDisposed())
    {
      text.setEnabled(enabled);
      if (enabled)
        text.setForeground(Color.FOREGROUND.getSWTColor());
      else
        text.setForeground(Color.COMMENT.getSWTColor());
    }
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object o)
  {
    this.value = o;
    
    if (this.text != null && !this.text.isDisposed())
    {
      // Das "setText" loest eine erneute Suche aus. Daher
      // ueberpringen wir die naechste
      this.inSearch = true;

      String s = format(this.value);
      this.text.setText(s == null ? "" : s);
      
      if (s != null && !s.equals(this.search))
        text.setForeground(Color.FOREGROUND.getSWTColor());

      if (this.listeners.size() > 0)
      {
        Event e = new Event();
        e.data = this.value;
        e.text = s;
        for (Listener l:this.listeners)
        {
          l.handleEvent(e);
        }
      }
    }
  }
  
  /**
   * @see de.willuhn.jameica.gui.input.Input#isEnabled()
   */
  public boolean isEnabled()
  {
    return enabled;
  }
  
  /**
   * BUGZILLA 743
   * @see de.willuhn.jameica.gui.input.AbstractInput#update()
   */
  protected void update() throws OperationCanceledException
  {
    super.update();

    // ueberschrieben, weil getValue() das Objekt zurueckliefert.
    // Wir pruefen hier aber auch, ob lediglich ein Text drin steht.
    if (text == null || text.isDisposed())
      return;
    
    String s = text.getText();

    org.eclipse.swt.graphics.Color color = null;
    
    if (isMandatory() && (s == null || s.length() == 0 || s.equals(this.search)))
      color = Color.MANDATORY_BG.getSWTColor();

    text.setBackground(color);
  }

}
