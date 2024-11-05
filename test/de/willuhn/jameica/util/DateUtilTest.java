/**********************************************************************
 *
 * Copyright (c) 2024 Olaf Willuhn
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
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class DateUtilTest {
  private String[][] validDates;
  private String[] invalidDates;

  @Before
  public void setUp() {
    LocalDate today = LocalDate.now();
    int currentYear = today.getYear();
    int currentMonth = today.getMonthValue(); // 1 = Jan
    int currentDay = today.getDayOfMonth();
    int lastDayOfFebruary = LocalDate.of(currentYear, 3, 1).minusDays(1).getDayOfMonth();
    LocalDate todayPlus60days = today.plusDays(60);
    LocalDate todayMinus60days = today.minusDays(60);
    LocalDate todayPlus15Months = today.plusMonths(15);
    LocalDate todayMinus15Months = today.minusMonths(15);

    validDates = new String[][]{
        // input, expected output
        // without delimiter
        {"5", String.format("05.%02d.%d", currentMonth, currentYear)},
        {"04", String.format("04.%02d.%d", currentMonth, currentYear)},
        {"0304", String.format("03.04.%d", currentYear)},
        {"02032024", "02.03.2024"},
        // with delimiter
        {"5.", String.format("05.%02d.%d", currentMonth, currentYear)},
        {"05.", String.format("05.%02d.%d", currentMonth, currentYear)},
        {"8.9.", String.format("08.09.%d", currentYear)},
        {"08.9.", String.format("08.09.%d", currentYear)},
        {"8.09.", String.format("08.09.%d", currentYear)},
        {"7.8.29", "07.08.2029"},
        {"07.8.29", "07.08.2029"},
        {"7.08.29", "07.08.2029"},
        {"7.8.2029", "07.08.2029"},
        {"07.8.2029", "07.08.2029"},
        {"7.08.2029", "07.08.2029"},
        {"1/2/23", "01.02.2023"},
        // offsets from today
        {"h", String.format("%02d.%02d.%d", currentDay, currentMonth, currentYear)},
        {"t", String.format("%02d.%02d.%d", currentDay, currentMonth, currentYear)},
        {"-0", String.format("%02d.%02d.%d", currentDay, currentMonth, currentYear)},
        {"+60", String.format("%02d.%02d.%d", todayPlus60days.getDayOfMonth(), todayPlus60days.getMonthValue(), todayPlus60days.getYear())},
        {"-60", String.format("%02d.%02d.%d", todayMinus60days.getDayOfMonth(), todayMinus60days.getMonthValue(), todayMinus60days.getYear())},
        {"++15", String.format("%02d.%02d.%d", todayPlus15Months.getDayOfMonth(), todayPlus15Months.getMonthValue(), todayPlus15Months.getYear())},
        {"--15", String.format("%02d.%02d.%d", todayMinus15Months.getDayOfMonth(), todayMinus15Months.getMonthValue(), todayMinus15Months.getYear())},
        {" --15", String.format("%02d.%02d.%d", todayMinus15Months.getDayOfMonth(), todayMinus15Months.getMonthValue(), todayMinus15Months.getYear())},
        // auto-correct end of month
        {"30022024", "29.02.2024"},
        {"3002", String.format("%02d.02.%d", lastDayOfFebruary, currentYear)},
        {"30.02.2024", "29.02.2024"},
        // year in 2-digit notation: assume the last century if the year is more than 19 years from now
        {String.format("01.01.%02d", currentYear - 80 - 1900), String.format("01.01.%02d", currentYear - 80)},
        {String.format("01.01.%02d", currentYear - 81 - 1900), String.format("01.01.%02d", currentYear + 19)},
        // special cases
        {"01.012.2023", "01.12.2023"},
    };
    invalidDates = new String[]{
        // don't auto-correct or roll over
        "0113",
        "01132024",
        "01.13.2024",
        // don't parse time
        "01.02.2023 13:37",
        // don't ignore suffixes, even if the date is parseable
        "01.02.2023 invalid",
        "012",
        "123",
        "01023",
        // if a delimiter has been used for the day, the month also needs a delimiter
        "09.10",
        "9/10",
        // invalid offsets
        "1+3",
        "01.02.2023+h",
        "01.02.2023+4.5",
        "01.02.2023+4,5",
        "01.02.2023+4++",
        "01.02.2023+++",
        "01.02.2023+a++b",
        "01.02.2023+++3",
        "+",
        "+ ++ ",
        // offsets from date
        "t-1",
        "01.02.2023+4",
        "01.02.2023++1",
        "01.02.2023++1+4",
        "01.02.2023+4++1",
        "01.02.2023+4-4",
        "30.02.2024+1",
        "30.02.2024++1",
        // offsets from date (end of month)
        "30.04.2025++1",
        "31.05.2025--1",
        // special cases
        "",
        " ",
        "0",
        "00",
        "2023-02-01",
        "invalid",
    };
  }

  @Test
  public void convert2DateValid() throws Exception {
    DateUtil.setLocaleForTesting(Locale.GERMAN, "dd.MM.uuuu", "dd.MM.uu");
    String expected = String.format("input | converted%n");
    String actual = String.format("input | converted%n");
    for (int i = 0; i < validDates.length; i++) {
      String input = validDates[i][0];
      String result = DateUtil.convert2Date(input);
      expected += String.format ("%s | %s%n", input, validDates[i][1]);
      actual += String.format ("%s | %s%n", input, result);
    }
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void convert2DateInvalid() throws Exception {
    DateUtil.setLocaleForTesting(Locale.GERMAN, "dd.MM.uuuu", "dd.MM.uu");
    String expected = String.format("input | converted%n");
    String actual = String.format("input | converted%n");
    for (int i = 0; i < invalidDates.length; i++) {
      String input = invalidDates[i];
      String converted = DateUtil.convert2Date(input);
      expected += String.format ("%s | %s%n", input, input);
      actual += String.format ("%s | %s%n", input, converted);
    }
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void parseUserInputValid() throws Exception {
    DateUtil.setLocaleForTesting(Locale.GERMAN, "dd.MM.uuuu", "dd.MM.uu");

    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd.MM.uuuu");
    String expected = String.format("input | parsed%n");
    String actual = String.format("input | parsed%n");
    for (int i = 0; i < validDates.length; i++) {
      String input = validDates[i][0];
      expected += String.format ("%s | %s%n", input, validDates[i][1]);
      String result = DateUtil.parseUserInput(input, null)
          .map(date -> date.format(outputFormatter))
          .orElse("null");
      actual += String.format ("%s | %s%n", input, result);
    }
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void parseUserInputInvalid() throws Exception {
    DateUtil.setLocaleForTesting(Locale.GERMAN, "dd.MM.uuuu", "dd.MM.uu");
    String expected = String.format("input | parsingSucceeded%n");
    String actual = String.format("input | parsingSucceeded%n");
    for (int i = 0; i < invalidDates.length; i++) {
      String input = invalidDates[i];
      Optional<LocalDate> parsed = DateUtil.parseUserInput(input, null);
      expected += String.format("%s | %b%n", input, false);
      actual += String.format("%s | %b%n", input, parsed.isPresent());
    }
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void parseUserInputUsingCustomFormatter() throws Exception {
    DateUtil.setLocaleForTesting(Locale.GERMAN, "dd.MM.uuuu", "dd.MM.uu");
    String inputDate = "01---02//2023";

    Optional<LocalDate> parsedDate = DateUtil.parseUserInput(inputDate, null);
    Assert.assertTrue(parsedDate.isEmpty());

    DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("dd---MM//uuuu");
    parsedDate = DateUtil.parseUserInput(inputDate, customFormatter);
    Assert.assertEquals(Optional.of(LocalDate.of(2023, 2, 1)), parsedDate);
  }
  
  @Test
  public void createDateTimeFormatter() throws Exception {
    DateUtil.setLocaleForTesting(Locale.ITALIAN, "uuuu-MM-dd", "uu-MM-dd");
    
    Date dNow = new Date();
    LocalDate ldToday = LocalDate.now();
    
    DateFormat df;
    DateTimeFormatter dtf;
    
    // DEFAULT_FORMAT
    df = new SimpleDateFormat("yyyy-MM-dd");
    dtf = DateUtil.createDateTimeFormatter(df);
    Assert.assertEquals(df.format(dNow), dtf.format(ldToday));
    
    // SHORT_FORMAT
    df = new SimpleDateFormat("yy-MM-dd");
    dtf = DateUtil.createDateTimeFormatter(df);
    Assert.assertEquals(df.format(dNow), dtf.format(ldToday));
    
    // custom format
    df = new SimpleDateFormat("MM--yyyy...dd");
    dtf = DateUtil.createDateTimeFormatter(df);
    Assert.assertEquals(df.format(dNow), dtf.format(ldToday));
  }

  @Test
  public void localDate2Date() throws Exception {
    DateUtil.setLocaleForTesting(Locale.GERMAN, "dd.MM.uuuu", "dd.MM.uu");
    
    LocalDate ldToday = LocalDate.now();
    Date dNow = DateUtil.localDate2Date(ldToday);
    
    Assert.assertEquals(dNow, DateUtil.startOfDay(dNow));
    
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    String dNowFormatted = df.format(dNow);
    
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd");
    String ldTodayFormatted = ldToday.format(dtf);
    
    Assert.assertEquals(ldTodayFormatted, dNowFormatted);
  }
}
