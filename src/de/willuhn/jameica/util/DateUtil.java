/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.system.Application;

/**
 * Hilfsklasse zum Parsen von Datumsangaben.
 */
public class DateUtil
{
  /**
   * Das Default-Dateformat von Jameica.
   * Abhaengig vom Locale,
   * z. B. deutsch: dd.MM.yyyy / englisch: MMM d, yyyy
   */
  public static volatile DateFormat DEFAULT_FORMAT;
  /**
   * Das Kurz-Dateformat von Jameica.
   * Abhaengig vom Locale,
   * z. B. deutsch: dd.MM.yy / englisch: MM/dd/yyyy
   */
  public static volatile DateFormat SHORT_FORMAT;

  static {
    initializeLocale();
  }

  /**
   * Wird diese Klasse getestet, existiert die Application-Instanz nicht
   * und wir können das Locale nicht aus der Config lesen.
   */
  private static void initializeLocale() {
    Locale locale;
    try {
      locale = Application.getConfig().getLocale();
    } catch (NullPointerException e) {
      // Fallback, falls Application noch nicht initialisiert wurde (für Tests)
      locale = Locale.GERMANY;
    }
    DEFAULT_FORMAT = SimpleDateFormat.getDateInstance(DateFormat.DEFAULT, locale);
    SHORT_FORMAT = SimpleDateFormat.getDateInstance(DateFormat.SHORT, locale);
  }

  /**
   * Überschreibt das von der Config vorgegebene Locale (für Tests)
   * @param locale
   */
  public static void setLocaleForTesting(Locale locale) {
    DEFAULT_FORMAT = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
    SHORT_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT, locale);
  }

  /**
   * Eingabehilfe für Datumsfelder. Eine 1-2stellige Zahl wird als Tag des
   * aktuellen Monats interpretiert. Eine 4stellige Zahl als Tag und Monat des
   * laufenden Jahres.
   * @param text zu parsender Text.
   * @return das vervollstaendigte Datum oder der Originalwert, wenn es nicht
   * geparst werden konnter.
   */
  public static String convert2Date(String text)
  {
    int tag = 0;
    int monat = 0;
    
    // Eventuell mit Uhrzeit eingegeben. Wir lassen die Finger davon
    if (text.length() > 10)
      return text;

    // Datum im Format dd eingegeben. Wir vervollstaendigen mit aktuellem Monat und Jahr
    if (text.length() <= 2)
    {
      try
      {
        tag = Integer.parseInt(text);
        checkDay(tag);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, tag);
        return new DateFormatter(DEFAULT_FORMAT).format(cal.getTime());
      }
      catch (NumberFormatException e)
      {
        return text;
      }
    }
    
    // Datum im Format ddmm eingegeben. Wir parsen beides und vervollstaendigen mit dem aktuellen Jahr
    if (text.length() == 4)
    {
      try
      {
        tag = Integer.parseInt(text.substring(0, 2));
        monat = Integer.parseInt(text.substring(2));
        checkDay(tag);
        checkMonth(monat);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, tag);
        cal.set(Calendar.MONTH, monat - 1);
        return new DateFormatter(DEFAULT_FORMAT).format(cal.getTime());
      }
      catch (NumberFormatException e)
      {
        return text;
      }
    }
    
    // [BUGZILLA 1281] Ist vermutlich ddmmyyyy
    if (text.matches("^[0-9]{8}$"))
    {
      try
      {
        tag = Integer.parseInt(text.substring(0, 2));
        monat = Integer.parseInt(text.substring(2));
        int jahr = Integer.parseInt(text.substring(4,8));
        checkDay(tag);
        checkMonth(monat);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, tag);
        cal.set(Calendar.MONTH, monat - 1);
        cal.set(Calendar.YEAR, jahr);
        return new DateFormatter(DEFAULT_FORMAT).format(cal.getTime());
      }
      catch (NumberFormatException e)
      {
        return text;
      }
    }
    
    DateFormat df = DEFAULT_FORMAT;
    
    // dd.mm.yy
    if (text.matches("^[0-9]{1,2}\\.[0-9]{1,2}\\.[0-9]{2}$"))
      df = SHORT_FORMAT;

    try
    {
      // Checken, ob wir das Datum als Date-Objekt parsen koennen
      Date d = df.parse(text);
      
      // jepp, dann neu formatieren - u.a. mit fuehrenden Nullen und 4-stelligem Jahr
      return new DateFormatter(DEFAULT_FORMAT).format(d);
    }
    catch (Exception e)
    {
      // User hat Quatsch eingegeben
    }
    
    // Es wurde in keinem parse-faehigen Format eingegeben. Der String wird
    // wie eingegeben zurückgereicht.
    return text;
  }

  /**
   * Eingabehilfe für Datumsfelder.<br>
   * Unterstützte Formate:<br>
   * - Das per defaultFormatter angegebene Format<br>
   * - d | dd | ddMM | ddMMyy | ddMMyyyy<br>
   * - dd. | dd.MM. | dd.MM.yy | dd.MM.yyyy (dd und MM auch einstellig)<br>
   * - dd/ | dd/MM/ | dd/MM/yy | dd/MM/yyyy (dd und MM auch einstellig)<br>
   * - "h" (heute) | "t" (today)<br>
   * - +D | -D (heute plus/minus D Tage)<br>
   * - ++M | --M (heute plus/minus M Monate)<br>
   * Das Datum muss immer in der Reihenfolge "Tag - Monat - Jahr" angegeben werden (auch im defaultFormatter).
   * @param userInput Eingabetext
   * @param customFormatter Standardformat, das das Datumsfeld nutzt, um das Datum anzuzeigen
   * @return das geparste Datum als Optional
   */
  public static Optional<LocalDate> parseUserInput(String userInput, DateTimeFormatter customFormatter) {
    if (userInput == null || userInput.isBlank()) {
      return Optional.empty();
    }
    String strippedUserInput = userInput.strip();

    return Stream.<Supplier<Optional<LocalDate>>>of(
        () -> parseDateUsingFormatter(strippedUserInput, customFormatter),
        () -> parseDateUsingPatterns(strippedUserInput, "dd", "MM"),
        () -> parseDateUsingPatterns(strippedUserInput, "d.", "M."),
        () -> parseDateUsingPatterns(strippedUserInput, "d/", "M/"),
        () -> parseDateOffsets(strippedUserInput)
      )
      .map(Supplier::get)
      .filter(Optional::isPresent)
      .findFirst()
      .orElse(Optional.empty());
  }

  /**
   * Parst Datum mithilfe des angegebenen DateTimeFormatter
   * @param userInput das eingegebene Datum als String
   * @param formatter der DateTimeFormatter, mit dem das Datum geparst werden soll
   * @return das geparste Datum als Optional
   */
  private static Optional<LocalDate> parseDateUsingFormatter(String userInput, DateTimeFormatter formatter) {
    if (formatter == null) {
      return Optional.empty();
    }
    try {
      LocalDate parsedDate = LocalDate.parse(userInput, formatter);
      return Optional.of(parsedDate);
    } catch (DateTimeParseException e) {
      return Optional.empty();
    }
  }

  /**
   * Parst Datum in den Formaten {dayPattern}{monthPattern}{Jahr als 2- bis 4-stellige Zahl}
   * @param userInput das eingegebene Datum
   * @param dayPattern das DateTimeFormatter-Pattern, das auf den Tag matcht (z. B. "dd" oder "d.")
   * @param monthPattern das DateTimeFormatter-Pattern, das auf den Monat matcht (z. B. "MM" oder "M.")
   * @return das geparste Datum als Optional
   */
  private static Optional<LocalDate> parseDateUsingPatterns(String userInput, String dayPattern, String monthPattern) {
    if (userInput == null || userInput.isEmpty() ) {
      return Optional.empty();
    }
    if (userInput.length() == 1) {
      userInput = "0" + userInput;
    }
    DateTimeFormatter inputFormatter = new DateTimeFormatterBuilder()
        .appendPattern(dayPattern)
        .optionalStart()
        .appendPattern(monthPattern)
        .optionalStart()
        .appendValueReduced(ChronoField.YEAR, 2, 4, LocalDate.now().getYear() - 80)
        .optionalEnd()
        .optionalEnd()
        .parseDefaulting(ChronoField.MONTH_OF_YEAR, LocalDate.now().getMonthValue())
        .parseDefaulting(ChronoField.YEAR, LocalDate.now().getYear())
        .toFormatter();
    return parseDateUsingFormatter(userInput, inputFormatter);
  }

  /**
   * Parst das Datum als Offset zum heutigen Datum
   * "h" (heute) | "t" (today)
   * +D | -D (heute plus/minus D Tage)
   * ++M | --M (heute plus/minus M Monate)
   * @param userInput
   * @return das geparste Datum als Optional
   */
  private static Optional<LocalDate> parseDateOffsets(String userInput) {
    if (userInput == null || userInput.isEmpty()) {
      return Optional.empty();
    }
    if (userInput.equalsIgnoreCase("h") || userInput.equalsIgnoreCase("t")) {
      userInput = "+0"; // heute
    }

    String regex = "^((\\+{1,2}|-{1,2}))(\\d+)$";
    Pattern pattern = Pattern.compile(regex);

    Matcher matcher = pattern.matcher(userInput);
    if (!matcher.matches()) {
      return Optional.empty();
    }

    String prefix = matcher.group(1);
    String digits = matcher.group(3);
    int offset;
    try {
      offset = Integer.parseInt(digits);
    } catch (NumberFormatException e) {
      return Optional.empty();
    }

    Map<String, Function<Integer, LocalDate>> offsetFunctions = Map.of(
        "+", LocalDate.now()::plusDays,
        "++", LocalDate.now()::plusMonths,
        "-", LocalDate.now()::minusDays,
        "--", LocalDate.now()::minusMonths
    );

    return Optional
        .ofNullable(offsetFunctions.get(prefix))
        .map(func -> func.apply(offset));
  }

  /**
   * Prueft, ob sich der Tag innerhalb des erlaubten Bereichs befindet.
   * @param day der Tag.
   * @throws NumberFormatException
   */
  private static void checkDay(int day) throws NumberFormatException
  {
    if (day < 1 || day > 31)
    {
      throw new NumberFormatException();
    }
  }

  /**
   * Prueft, ob sich der Monat innerhalb des erlaubten Bereichs befindet.
   * @param month der Monat.
   * @throws NumberFormatException
   */
  private static void checkMonth(int month) throws NumberFormatException
  {
    if (month < 1 || month > 12)
    {
      throw new NumberFormatException();
    }
  }

  /**
   * Resettet die Uhrzeit eines Datums.
   * @param date das Datum.
   * @return das neue Datum.
   */
  public static Date startOfDay(Date date)
  {
    if (date == null)
      return null;
    
    Calendar cal = Calendar.getInstance();
    cal.setTime(date == null ? new Date() : date);
    cal.set(Calendar.HOUR_OF_DAY,0);
    cal.set(Calendar.MINUTE,0);
    cal.set(Calendar.SECOND,0);
    cal.set(Calendar.MILLISECOND,0);
    return cal.getTime();
  }

  /**
   * Setzt die Uhrzeit eines Datums auf 23:59:59.999.
   * @param date das Datum.
   * @return das neue Datum.
   */
  public static Date endOfDay(Date date)
  {
    if (date == null)
      return null;
    
    Calendar cal = Calendar.getInstance();
    cal.setTime(date == null ? new Date() : date);
    cal.set(Calendar.HOUR_OF_DAY,23);
    cal.set(Calendar.MINUTE,59);
    cal.set(Calendar.SECOND,59);
    cal.set(Calendar.MILLISECOND,999);
    return cal.getTime();
  }
}
