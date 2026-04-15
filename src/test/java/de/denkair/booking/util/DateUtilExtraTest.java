package de.denkair.booking.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateUtilExtraTest {

    @Test void formatDeDateTime() {
        Date d = Date.from(LocalDateTime.of(2024, 3, 14, 13, 45)
                .atZone(ZoneId.systemDefault()).toInstant());
        String s = DateUtil.formatDeDateTime(d);
        assertEquals("14.03.2024 13:45", s);
    }

    @Test void formatTime() {
        assertEquals("09:05", DateUtil.formatTime(LocalDateTime.of(2024, 1, 1, 9, 5)));
    }

    @Test void formatiereDatumAlias() {
        assertEquals("01.02.2024", DateUtil.formatiereDatum(LocalDate.of(2024, 2, 1)));
    }

    @Test void parseDeRoundTrip() throws Exception {
        LocalDate got = DateUtil.parseDe("15.07.2024 00:00");
        assertEquals(LocalDate.of(2024, 7, 15), got);
    }
}
