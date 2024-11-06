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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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

  private static final Object PLACEHOLDER = new Object();
  
  private DateFormat format = DateUtil.DEFAULT_FORMAT;
  private DateTimeFormatter dtFormatter;
  private DateUtil.DatePositions datePositions;
  
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
  public DateInput(Date date, DateFormat format) {
    if (format != null) {
      this.format = format;
    }
    this.dtFormatter = DateUtil.createDateTimeFormatter(this.format);
    this.datePositions = DateUtil.getDatePositions(this.dtFormatter);

    this.dialog = new MyCalendarDialog();

    if (date != null)
      this.dialog.setDate(date);

    this.dialog.setTitle(Application.getI18n().tr("Datum"));
    this.dialog.setText(Application.getI18n().tr("Bitte wählen Sie das Datum aus"));
    this.input = new DialogInput(date == null ? null : this.format.format(date), this.dialog);
    this.input.setData(DATAKEY_TOOLTIP, String.format(Application.getI18n().tr("DateInput.tooltip")));

    setupDialogCloseListener();
    setupFocusListener();
    setupKeyListener();
  }

  private void setupDialogCloseListener() {
    this.dialog.addCloseListener(new Listener() {
      @Override
      public void handleEvent(Event event) {
        if (event == null || event.data == null)
          return;
        input.setText(DateInput.this.format.format((Date) event.data));
      }
    });
  }
  
  private void setupFocusListener() {
    this.input.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent e) {
        //Automatische Formatierung bei Focus-Lost des Eingabefeldes
        parseAndUpdateInputText();
      }
    });
  }

  /**
   * Erlaubt das Ändern des Datums mittels Tastenkombination:<br>
   * ALT & BILD_HOCH: Tag +1<br>
   * ALT & BILD_RUNTER: Tag -1<br>
   * ALT & SHIFT & BILD_HOCH: Monat +1<br>
   * ALT & SHIFT & BILD_RUNTER: Monat -1
   */
  private void setupKeyListener() {
    this.input.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        boolean dayModifierPressed = e.stateMask == SWT.ALT;
        boolean monthModifierPressed = e.stateMask == (SWT.ALT | SWT.SHIFT);
        boolean plusPressed = e.keyCode == SWT.PAGE_UP;
        boolean minusPressed = e.keyCode == SWT.PAGE_DOWN;
        
        int dayOffset;
        int monthOffset;
        int selectionIndex;
        int selectionWidth;
        
        if (dayModifierPressed) {
          dayOffset = plusPressed ? 1 : (minusPressed ? -1 : 0);
          monthOffset = 0;
          selectionIndex = datePositions.dayIndex();
          selectionWidth = datePositions.dayWidth();
        } else if (monthModifierPressed) {
          dayOffset = 0;
          monthOffset = plusPressed ? 1 : (minusPressed ? -1 : 0);
          selectionIndex = datePositions.monthIndex();
          selectionWidth = datePositions.monthWidth();
        } else {
          return;
        }
        
        if(dayOffset == 0 && monthOffset == 0) {
          return;
        }
        
        parseAndUpdateInputText().ifPresent(date -> {
          LocalDate updatedDate = date.plusDays(dayOffset);
          updatedDate = DateUtil.addMonthsMaintainingEndOfMonth(updatedDate, monthOffset);
          setValue(updatedDate);
          input.setSelection(selectionIndex, selectionIndex + selectionWidth);
        });
        
        e.doit = false;  // Event nicht weiter propagieren
      }
    });
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
    Optional<LocalDate> date = parseAndUpdateInputText();
    return date
        .map(d -> DateUtil.localDate2Date(d))
        .orElse(null);
  }
  
  private Optional<LocalDate> parseAndUpdateInputText() {
    // Wir verwenden grundsaetzlich den Text aus dem Eingabe-Feld,
    // damit der User das Datum auch manuell eingeben kann.
    String inputText = this.input.getText();
    if (inputText == null || inputText.isBlank())
      return Optional.empty();
    
    Optional<LocalDate> date = DateUtil.parseUserInput(inputText, this.dtFormatter);

    if(date.isEmpty()) {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Ungültiges Datum: {0}",inputText),StatusBarMessage.TYPE_ERROR));
      return Optional.empty();
    }

    String parsedText = date
        .map(d -> d.format(this.dtFormatter))
        .orElse(inputText);
    
    if(!parsedText.equals(inputText)) {
      // Bei der Gelegenheit schreiben wir auch gleich nochmal
      // das Datum schoen formatiert rein. Aber nur, wenn sich der
      // Wert geaendert hat. Sonst springt der Cursor unnoetig
      // an den Anfang des Eingabefeldes. Siehe BUGZILLA 1672
      this.input.setText(parsedText);
    }
    return date;
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
    
    // Wenn es ein Date/LocalDate ist, koennen wir es getrost uebernehmen
    if (value instanceof Date)
    {
      this.input.setText(this.format.format((Date)value));
    }
    if (value instanceof LocalDate)
    {
      this.input.setText(((LocalDate)value).format(dtFormatter));
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