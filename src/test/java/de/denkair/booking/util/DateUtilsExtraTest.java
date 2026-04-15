package de.denkair.booking.util;

import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsExtraTest {

    private static Date d(String s) throws Exception {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(s);
    }

    @Test void toIsoAndToIsoDT() throws Exception {
        Date date = d("2024-07-15 12:30");
        assertEquals("2024-07-15", DateUtils.toIso(date));
        assertTrue(DateUtils.toIsoDT(date).startsWith("2024-07-15T"));
        assertEquals("", DateUtils.toIso(null));
        assertEquals("", DateUtils.toIsoDT(null));
    }

    @Test void toDeDateOverloadNull() {
        assertEquals("", DateUtils.toDe((Date) null));
        assertEquals("", DateUtils.toDe((LocalDate) null));
    }

    @Test void hhmmNullAndValue() {
        assertEquals("", DateUtils.hhmm(null));
        assertEquals("09:30", DateUtils.hhmm(LocalDateTime.of(2024, 1, 1, 9, 30)));
    }

    @Test void toBerlinShiftsFromUtc() {
        assertNull(DateUtils.toBerlin(null));
        LocalDateTime utc = LocalDateTime.of(2024, 7, 15, 10, 0);   // summer: Berlin = UTC+2
        LocalDateTime berlin = DateUtils.toBerlin(utc);
        assertEquals(12, berlin.getHour());
    }
}
