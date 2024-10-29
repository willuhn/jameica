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

import java.time.LocalDate;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;


public class DateUtilTest {

    @Test
    public void convert2Date() throws Exception {
        DateUtil.setLocaleForTesting(Locale.GERMAN);
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue(); // 1 = Jan
        int currentDay = today.getDayOfMonth();
        int lastDayOfFebruary = LocalDate.of(currentYear, 3, 1).minusDays(1).getDayOfMonth();
        String[][] testStrings = {
                // input, expected output
                // === valid dates ===
                {"01.02.2023", "01.02.2023"},
                {"02032024", "02.03.2024"},
                {"0304", String.format("03.04.%d", currentYear)},
                {"04", String.format("04.%02d.%d", currentMonth, currentYear)},
                {"5", String.format("05.%02d.%d", currentMonth, currentYear)},
                {"6.7.2028", "06.07.2028"},
                {"7.8.29", "07.08.2029"},
                //{"8.9.", String.format("08.09.%d", currentYear)},
                // special shortcuts
                //{"h", String.format("%02d.%02d.%d", currentDay, currentMonth, currentYear)},
                //{"g", },
                //{"+60", },
                //{"-60", },
                //{"++15", },
                //{"--15", },
                //{"3002", String.format("%02d.02.%d", lastDayOfFebruary, currentYear)},
                //{"30022024", "29.02.2024"},
                //{"30.02.2024", "29.02.2024"},
                // === invalid dates ===
                {"0113", "0113"},
                {"01132024", "01132024"},
                //{"01.13.2024", "01.13.2024"},
                {"01.012.2023", "01.012.2023"},
                {"01.02.2023 13:37", "01.02.2023 13:37"},
                {"01.02.2023 invalid", "01.02.2023 invalid"},
                {"123", "123"},
                {"0", "0"},
                {"01023", "01023"},
                {"09.10", "09.10"},
                {"2023-02-01", "2023-02-01"},
                {"invalid", "invalid"},
        };
        for (int i = 0; i < testStrings.length; i++) {
            Assert.assertEquals(
                    testStrings[i][1], // expected
                    DateUtil.convert2Date(testStrings[i][0])  // actual
            );
        }
    }
}
