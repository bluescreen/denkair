package de.denkair.booking.util;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DateHelperTest {

    @Test void parseLegacyIsoDate() {
        Date d = DateHelper.parseLegacy("2024-07-15");
        assertNotNull(d);
    }

    @Test void parseLegacyGermanDate() {
        Date d = DateHelper.parseLegacy("15.07.2024");
        assertNotNull(d);
    }

    @Test void parseLegacyGermanDateTime() {
        Date d = DateHelper.parseLegacy("15.07.2024 10:30");
        assertNotNull(d);
    }

    @Test void parseLegacyUnparseableIsNull() {
        assertNull(DateHelper.parseLegacy("kein datum"));
    }

    @Test void addDaysMovesForward() {
        Date d = new Date(0);
        Date plus2 = DateHelper.addDays(d, 2);
        assertEquals(2L * 86400000L, plus2.getTime());
    }

    @Test void addDaysNegative() {
        Date d = new Date(10L * 86400000L);
        Date minus1 = DateHelper.addDays(d, -1);
        assertEquals(9L * 86400000L, minus1.getTime());
    }
}
