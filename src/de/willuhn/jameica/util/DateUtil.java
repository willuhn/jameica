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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
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

import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Hilfsklasse zum Parsen von Datumsangaben.
 */
public class DateUtil
{
  /**
   * Das Default-Dateformat von Jameica.
   * Wird als DateTimeFormatter-Pattern �ber i18n angegeben.
   * z. B. deutsch: dd.MM.uuuu / englisch: dd/MM/uuuu
   * {@see <a href="https://docs.oracle.com/javase/8/docs/api/index.html?java/time/format/DateTimeFormatter.html">Java Docs: DateTimeFormatter</a>}
   */
  public static volatile DateFormat DEFAULT_FORMAT;
  private static volatile DateTimeFormatter defaultFormatter;  // moderne Version davon
  /**
   * Das Kurz-Dateformat von Jameica.
   * Wie {@link #DEFAULT_FORMAT}
   * z. B. deutsch: dd.MM.uu / englisch: dd/MM/uu
   */
  public static volatile DateFormat SHORT_FORMAT;
  private static volatile DateTimeFormatter shortFormatter;  // moderne Version davon

  static {
    initializeLocale(null, null, null);
  }

  /**
   * Wird diese Klasse getestet, existiert die Application-Instanz nicht
   * und wir k�nnen das Locale nicht aus der Config lesen.
   */
  private static void initializeLocale(Locale locale, String defaultDatePattern, String shortDatePattern) {
    if (locale == null) {
      try {
        locale = Application.getConfig().getLocale();
      } catch (NullPointerException e) {
        // Fallback, falls Application noch nicht initialisiert wurde (f�r Tests)
        locale = Locale.GERMANY;
      }
    }
    if (defaultDatePattern == null) {
      try {
        defaultDatePattern = Application.getI18n().tr("dd.MM.uuuu");
      } catch (NullPointerException e) {
        defaultDatePattern = "dd.MM.uuuu";
      }
    }
    if (shortDatePattern == null) {
      try {
        shortDatePattern = Application.getI18n().tr("dd.MM.uu");
      } catch (NullPointerException e) {
        shortDatePattern = "dd.MM.uu";
      }
    }

    DEFAULT_FORMAT = new SimpleDateFormat(
        createSimpleDateFormatPattern(defaultDatePattern), locale);
    defaultFormatter = DateTimeFormatter.ofPattern(defaultDatePattern, locale);

    SHORT_FORMAT = SimpleDateFormat.getDateInstance(DateFormat.SHORT, locale);
    shortFormatter = DateTimeFormatter.ofPattern(shortDatePattern, locale);
  }

  /**
   * �berschreibt das von der Config vorgegebene Locale (f�r Tests)
   * @implNote Diese Methode sollte nur f�r Tests verwendet werden und ist nicht f�r den Produktions-Betrieb gedacht.
   * @param locale Das Locale, das beim Parsen und Formatieren ber�cksichtigt wird
   * @param defaultDatePattern Das DateTimeFormatter-Pattern, das f�r die Standardformatierung verwendet wird
   * @param shortDatePattern Das DateTimeFormatter-Pattern, das f�r die kurze Formatierung verwendet wird
   */
  public static void setLocaleForTesting(Locale locale, String defaultDatePattern, String shortDatePattern) {
    initializeLocale(locale, defaultDatePattern, shortDatePattern);
  }

  /**
   * Eingabehilfe f�r Datumsfelder.<br>
   * Wie {@link #parseUserInput(String, DateTimeFormatter)}, nur dass ein String
   * zur�ckgegeben wird. Verwendet immer das {@link #DEFAULT_FORMAT}.
   * @param text zu parsender Text.
   * @return das vervollstaendigte Datum oder der Originalwert, wenn es nicht
   * geparst werden konnter.
   */
  public static String convert2Date(String text)
  {
    Optional<LocalDate> date = parseUserInput(text, defaultFormatter);
    return date
        .map(d -> d.format(defaultFormatter))
        .orElse(text);
  }

  /**
   * Eingabehilfe f�r Datumsfelder.<br>
   * Unterst�tzte Formate:<br>
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
   * Erzeugt aus einem DateTimeFormatter-Pattern ein SimpleDateFormat-Pattern.
   * @param dateTimeFormatterPattern das DateTimeFormatter-Pattern
   * @return das SimpleDateFormat-Pattern
   */
  private static String createSimpleDateFormatPattern(String dateTimeFormatterPattern) {
    // u (Jahr ohne �ra) im DateTimeFormatter entspricht y im SimpleDateFormat
    return dateTimeFormatterPattern.replace("u", "y");
  }
  
  /**
   * Versucht aus einem java.text.DateFormat einen java.time.format.DateTimeFormatter zu erstellen.
   * @param dtFormat Das DateFormat. Eine Konvertierung ist nur m�glich, wenn es
   *                 instanceof java.text.SimpleDateFormat ist. Andernfalls wird
   *                 das {@link DEFAULT_FORMAT} verwendet.
   * @return der DateTimeFormatter
   */
  public static DateTimeFormatter createDateTimeFormatter(DateFormat dtFormat) {
    if (dtFormat.equals(DEFAULT_FORMAT)) {
      return defaultFormatter;
    }
    if (dtFormat.equals(SHORT_FORMAT)) {
      return shortFormatter;
    }
    if (!(dtFormat instanceof SimpleDateFormat)) {
      // nur bei einem SimpleDateFormat existiert .toPattern()
      Logger.warn("Unable to create DateTimeFormatter from custom DateFormat. Using default Formatter");
      return defaultFormatter;
    }
    
    String sdPattern = ((SimpleDateFormat)dtFormat).toPattern();    
    String dateTimeFormatterPattern = sdPattern.replace("y", "u");
    
    return DateTimeFormatter.ofPattern(dateTimeFormatterPattern);
  }

  /**
   * Konvertiert ein java.time.LocalDate zu einem java.util.Date
   * @param localDate das LocalDate
   * @return das Date
   */
  public static Date localDate2Date(LocalDate localDate) {
    return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
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
    cal.setTime(date);
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
    cal.setTime(date);
    cal.set(Calendar.HOUR_OF_DAY,23);
    cal.set(Calendar.MINUTE,59);
    cal.set(Calendar.SECOND,59);
    cal.set(Calendar.MILLISECOND,999);
    return cal.getTime();
  }
}
