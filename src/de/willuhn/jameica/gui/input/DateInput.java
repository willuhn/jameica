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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.dialogs.CalendarDialog;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;

/**
 * Fix und fertig konfiguriertes Eingabe-Feld fuer die Datumseingabe.
 * Das ist ein Meta-Input-Feld, welches sich unter der Haube aus einem
 * DialogInput und einem CalendarDialog zusammensetzt.
 */
public class DateInput implements Input
{
  private Map<String,Object> data = new HashMap<String,Object>();

  private final static Object PLACEHOLDER = new Object();
  
  private DateFormat format = DateUtil.DEFAULT_FORMAT;
  
  private DialogInput input     = null;
  private CalendarDialog dialog = null;
  private String name           = null;
  
  private Object oldValue = PLACEHOLDER;
  
  private boolean mandatory = false;
  
  /**
   * Konstruktor ohne Datumsangabe aber mit Default-Format.
   */
  public DateInput()
  {
    this(null);
  }
  
  /**
   * Konstruktor mit uebergebenem Datum und Default-Format.
   * @param date das Datum.
   */
  public DateInput(Date date)
  {
    this(date,null);
  }
  
  /**
   * Konstruktor mit uebergebenem Datum und Format.
   * @param date das Datum.
   * @param format das Format.
   */
  public DateInput(Date date, DateFormat format)
  {
    if (format != null)
      this.format = format;
    
    this.dialog = new MyCalendarDialog();
    
    if (date != null)
      this.dialog.setDate(date);
    
    this.dialog.addCloseListener(new Listener() {
    
      public void handleEvent(Event event)
      {
        if (event == null || event.data == null)
          return;
        input.setText(DateInput.this.format.format((Date) event.data));
      }
    });
    
    this.dialog.setTitle(Application.getI18n().tr("Datum"));
    this.dialog.setText(Application.getI18n().tr("Bitte wählen Sie das Datum aus"));
    this.input = new DialogInput(date == null ? null : this.format.format(date),this.dialog);
  }

  /**
   * Deaktiviert das komplette Control (Button und Text).
   */
  @Override
  public void disable()
  {
    this.input.disable();
  }

  /**
   * Aktiviert das komplette Control (Button und Text).
   */
  @Override
  public void enable()
  {
    this.input.enable();
  }

  @Override
  public void focus()
  {
    this.input.focus();
  }

  @Override
  public Control getControl()
  {
    return this.input.getControl();
  }

  /**
   * Liefert ein Objekt vom Typ {@link Date} oder {@code null},
   * wenn das Datum nicht geparst werden konnte.
   */
  @Override
  public Object getValue()
  {
    // Wir liefern grundsaetzlich den Text aus dem Eingabe-Feld,
    // damit der User das Datum auch manuell eingeben kann.
    String inputText = this.input.getText();
    if (inputText == null || inputText.length() == 0)
      return null;

    String convertedText = DateUtil.convert2Date(inputText);
    try
    {
      Date d = this.format.parse(convertedText);
      
      // Bei der Gelegenheit schreiben wir auch gleich nochmal
      // das Datum schoen formatiert rein. Aber nur, wenn sich der
      // Wert geaendert hat. Sonst springt der Cursor unnoetig
      // an den Anfang des Eingabefeldes. Siehe BUGZILLA 1672
      String newText = this.format.format(d);
      if (!StringUtils.equals(inputText, newText))
        this.input.setText(newText);
      
      return d;
    }
    catch (ParseException e)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Ungültiges Datum: {0}",inputText),StatusBarMessage.TYPE_ERROR));
      return null;
    }
  }

  @Override
  public boolean isEnabled()
  {
    return this.input.isEnabled();
  }

  /**
   * Aktiviert oder deaktiviert das komplette Control (Button und Text).
   */
  @Override
  public void setEnabled(boolean enabled)
  {
    this.input.setEnabled(enabled);
  }

  /**
   * Aktiviert nur den Text.
   */
  public final void enableClientControl()
  {
    this.input.enableClientControl();
  }
  
  /**
   * Deaktiviert nur den Text.
   */
  public final void disableClientControl()
  {
    this.input.disableClientControl();
  }
  
  @Override
  public void setValue(Object value)
  {
    // Wenn nichts uebergeben wird, machen wir nur die Werte leer.
    if (value == null)
    {
      this.input.setValue(null);
      this.input.setText("");
      return;
    }
    
    // Wenn ein String uebergeben wurde, schreiben wir den rein
    if (value instanceof String)
    {
      this.input.setText((String)value);
      
      // Testen aber trotzdem noch, ob das Datum das korrekte Format hat
      try
      {
        this.format.parse((String)value);
      }
      catch (Exception e)
      {
        Logger.error("invalid date " + value,e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Ungültiges Datum: {0}",value.toString()),StatusBarMessage.TYPE_ERROR));
      }
      return;
    }
    
    // Wenn es ein Date ist, koennen wir es getrost uebernehmen
    if (value instanceof Date)
    {
      this.input.setText(this.format.format((Date)value));
    }
  }
  
  /**
   * Legt den anzuzeigenden Text auf dem Kalender-Dialog fest.
   * @param text Text auf dem Kalender-Dialog.
   */
  public void setText(String text)
  {
    this.dialog.setText(text);
  }
  
  /**
   * Legt den auf dem Kalender-Dialog anzuzeigenden Titel fest.
   * @param title der auf dem Dialog anzuzeigende Titel.
   */
  public void setTitle(String title)
  {
    this.dialog.setTitle(title);
  }

  @Override
  public final void addListener(Listener l)
  {
    this.input.addListener(l);
  }

  @Override
  public final void paint(Composite parent)
  {
    this.input.setMandatory(this.isMandatory());
    this.input.paint(parent);
    
    this.input.getControl().addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent e)
      {
        //Automatische Formatierung bei Focus-Lost des Eingabefeldes
        getValue();
      }
    });

  }

  @Override
  public final void paint(Composite parent, int width)
  {
    this.input.setMandatory(this.isMandatory());
    this.input.paint(parent,width);
  }

  @Override
  public void setComment(String comment)
  {
    this.input.setComment(comment);
  }

  @Override
  public boolean isMandatory()
  {
    return this.mandatory;
  }

  @Override
  public void setMandatory(boolean mandatory)
  {
    // Wenn das Control bereits gezeichnet wurde und
    // sich der Wert von mandatory aendern wird, rufen
    // wir gleich update() auf. Dann wird eine Aenderung
    // im laufenden Betrieb sofort sichtbar
    if (mandatory != this.mandatory &&
        this.input != null)
    {
      this.mandatory = mandatory;
      this.input.setMandatory(this.mandatory);
      this.input.update();
    }
    else
    {
      // Ansonsten nur das Flag setzen
      this.input.setMandatory(this.mandatory);
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
      if (o == null || !(o instanceof Label))
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
    Object newValue = this.input.getText();

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
  
  /**
   * Ueberschrieben, um ggf. das aktuelle Datum einzutragen
   */
  private class MyCalendarDialog extends CalendarDialog
  {

    /**
     * ct.
     */
    public MyCalendarDialog()
    {
      super(CalendarDialog.POSITION_MOUSE);
    }

    /**
     * @see de.willuhn.jameica.gui.dialogs.CalendarDialog#paint(org.eclipse.swt.widgets.Composite)
     */
    protected void paint(Composite parent) throws Exception
    {
      try
      {
        setDate((Date) getValue());
      }
      catch (Exception e)
      {
        // ignore
      }
      super.paint(parent);
    }
    
    
  }
}


/*********************************************************************
 * $Log: DateInput.java,v $
 * Revision 1.10  2011/05/11 08:42:07  willuhn
 * @N setData(String,Object) und getData(String) in Input. Damit koennen generische Nutzdaten im Eingabefeld gespeichert werden (siehe SWT-Widget)
 *
 * Revision 1.9  2011-01-20 17:13:24  willuhn
 * @C HBCIProperties#startOfDay und HBCIProperties#endOfDay nach Jameica in DateUtil verschoben
 **********************************************************************/