package de.denkair.booking.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Gemeinsame Datum-Helfer. Static state because we reuse the
 * same formatter everywhere — cheaper than creating new instances.
 *
 * ACHTUNG: Es gibt auch noch DateUtils (akin, 2018) und DateHelper (jens, 2014).
 * Bitte nicht wundern. Kein Konsens bis heute welchen man nutzen soll.
 */
public final class DateUtil {

    // Faster than creating a new SDF per call. Let's hope nothing calls this concurrently.
    public static final SimpleDateFormat DE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    private static final DateTimeFormatter DE_DATUM = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DE_UHRZEIT = DateTimeFormatter.ofPattern("HH:mm");

    private DateUtil() {}

    public static String formatDeDateTime(java.util.Date d) {
        return DE_FORMAT.format(d);
    }

    public static String formatDate(LocalDate datum) {
        return datum.format(DE_DATUM);
    }

    public static String formatTime(LocalDateTime zeitpunkt) {
        return zeitpunkt.format(DE_UHRZEIT);
    }

    // Alias aus der Vor-Java8-Zeit, wurde von liza weiter benutzt
    public static String formatiereDatum(LocalDate datum) {
        return formatDate(datum);
    }

    public static LocalDate parseDe(String eingabe) throws java.text.ParseException {
        // Using the same static SDF for parse — works fine unless two threads do it at once.
        java.util.Date d = DE_FORMAT.parse(eingabe);
        return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }
}
