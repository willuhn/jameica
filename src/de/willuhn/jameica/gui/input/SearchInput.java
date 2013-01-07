/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/SearchInput.java,v $
 * $Revision: 1.27 $
 * $Date: 2011/10/31 13:52:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
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
      popup.setItems((String[])items.toArray(new String[items.size()]));
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
          text.setForeground(Color.WIDGET_FG.getSWTColor());
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
        text.setForeground(Color.WIDGET_FG.getSWTColor());
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
        text.setForeground(Color.WIDGET_FG.getSWTColor());

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
    else
      color = Color.WIDGET_BG.getSWTColor();

    text.setBackground(color);
  }

}

/*********************************************************************
 * $Log: SearchInput.java,v $
 * Revision 1.27  2011/10/31 13:52:47  willuhn
 * @B SWT.FOCUSED wurde nur ausgeloest, wenn man mit der Maus in das Feld klickt, nicht aber bei TAB-Wechsel. Daher jetzt FOCUSED UND FocusIn
 *
 * Revision 1.26  2011-08-08 10:45:05  willuhn
 * @C AbstractInput#update() ist jetzt "protected" (war package-private)
 *
 * Revision 1.25  2011-06-09 10:59:15  willuhn
 * @B Musste leider einen Fork vom SWT-Custom-Widget PopupList machen - siehe Kommentar-Text im Fork
 *
 * Revision 1.24  2011-05-13 14:18:19  willuhn
 * besser so
 *
 * Revision 1.23  2011-05-13 14:15:38  willuhn
 * @C So, jetzt ist es richtig
 *
 * Revision 1.22  2011-05-13 14:11:17  willuhn
 * @B Fuehrte dazu, dass der Inhalt nicht angezeigt wird, wenn das Control den Focus hat
 *
 * Revision 1.21  2011-05-13 11:04:35  willuhn
 * @N Tuning ;)
 *
 * Revision 1.20  2011-05-13 10:10:34  willuhn
 * @B Text nur dann grau faerben und "Suche..." reinschreiben, wenn wir nicht schon den Focus haben. Ansonsten bleibt der Text ja grau und wir muessen das "Suche..." manuell entfernen
 *
 * Revision 1.19  2010-07-23 10:28:58  willuhn
 * @B Focus wurde unter bestimmten Umstaenden nicht ausgeloest. Das "Suche..." blieb dann im Textfeld stehen
 *
 * Revision 1.18  2010/01/18 22:59:02  willuhn
 * @B BUGZILLA 808
 *
 * Revision 1.17  2009/12/07 23:48:45  willuhn
 * @N Adress-Auswahl nun via Autosuggest UND Adress-Dialog moeglich
 *
 * Revision 1.16  2009/11/16 11:51:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2009/07/26 22:34:42  willuhn
 * @B BUGZILLA 743
 *
 * Revision 1.14  2009/05/19 10:20:50  willuhn
 * @C Suchtext soll auch entfernt werden koennen
 *
 * Revision 1.13  2009/05/17 21:51:56  willuhn
 * @N setSearchText(String) zum Aendern des Such-Textes (Heiners Wunsch)
 *
 * Revision 1.12  2009/05/12 22:25:21  willuhn
 * @R removed unused import
 *
 * Revision 1.11  2009/05/11 08:56:24  willuhn
 * @C Config-Parameter ersetzt gegen "setDelay(int)"
 *
 * Revision 1.10  2009/05/10 21:54:05  willuhn
 * @C Delay auf 1 Sekunde erhoeht und konfigurierbar gemacht (https://lists.berlios.de/pipermail/jameica-devel/2009-May/000001.html)
 *
 * Revision 1.9  2009/02/24 23:35:33  willuhn
 * @N setText()
 *
 * Revision 1.8  2009/02/24 23:23:46  willuhn
 * @N Maximale Textlaenge konfigurierbar
 *
 * Revision 1.7  2009/01/04 01:24:30  willuhn
 * @N Format-Funktion zum Uberschreiben der Anzeige von Elementen in SearchInput
 * @N AbstractInput#addListener ueberschreibbar
 *
 * Revision 1.6  2008/12/17 22:45:22  willuhn
 * @R t o d o  tag entfernt
 *
 * Revision 1.5  2008/02/14 12:06:36  willuhn
 * @N Korrektes Focus-Handling
 *
 * Revision 1.4  2007/07/31 14:36:13  willuhn
 * @N Beispiel-Code
 *
 * Revision 1.3  2007/07/31 14:32:48  willuhn
 * @N Neues Custom-Widget zum Suchen. Ist ein Textfeld, welches nach Eingabe einen Callback-Mechanismus fuer die Suche startet und die Ergebnisse dann in einer Dropdown-Box anbietet. Also ein Mix aus TextInput und dynamischem SelectInput
 *
 **********************************************************************/