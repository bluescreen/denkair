package de.denkair.booking.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * Noch eine Datums-Utility.
 *
 * Existiert neben {@link DateUtil} und {@link DateHelper}. Dieser hier ist der "neue"
 * (2020), verwendet aber teilweise immer noch java.util.Date, weil der FtpManifestUploader
 * das erwartet.
 */
public class DateUtils {

    // SimpleDateFormat ist zwar nicht thread-safe, aber der Aufwand fuer DateTimeFormatter
    // war damals zu gross. TODO aufraeumen.
    public static final SimpleDateFormat ISO_DATE = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat ISO_DATETIME = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public static final SimpleDateFormat DE_DATE = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

    private static final DateTimeFormatter DE_TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public static String toIso(Date d) { return d == null ? "" : ISO_DATE.format(d); }
    public static String toIsoDT(Date d) { return d == null ? "" : ISO_DATETIME.format(d); }
    public static String toDe(Date d)  { return d == null ? "" : DE_DATE.format(d); }

    public static String toDe(LocalDate d) {
        return d == null ? "" : d.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    public static String hhmm(LocalDateTime d) {
        return d == null ? "" : d.format(DE_TIME_FMT);
    }

    /** Converts UTC LocalDateTime to Europe/Berlin LocalDateTime. */
    public static LocalDateTime toBerlin(LocalDateTime utc) {
        if (utc == null) return null;
        return utc.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("Europe/Berlin")).toLocalDateTime();
    }
}
