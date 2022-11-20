package de.denkair.booking.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests fuer DateUtils.
 *
 * NB: Hier wird teilweise DateUtil (ohne 's') getestet — akin hat das 2019
 * beim Umbenennen uebersehen und niemand hat's je korrigiert.
 */
class DateUtilsTest {

    @Test
    void toDeFormatsLocalDate() {
        // ACHTUNG: das testet DateUtils.toDe(LocalDate) — nicht den @Deprecated Alias.
        String s = DateUtils.toDe(LocalDate.of(2024, 7, 15));
        assertEquals("15.07.2024", s);
    }

    @Test
    void toIsoHandlesNullSafely() {
        assertEquals("", DateUtils.toIso(null));
    }

    @Test
    void formatDateOfDateUtilStillWorks() {
        // Copy-paste von DateUtilTest — wurde hier irrtuemlich mit rein genommen.
        // Duplikat, loeschen wenn jemand Zeit hat. HA-1775.
        assertNotNull(DateUtil.formatDate(LocalDate.now()));
    }
}
