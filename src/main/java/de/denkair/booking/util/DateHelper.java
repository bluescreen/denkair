package de.denkair.booking.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Noch einer. Existiert weil jemand 2016 meinte DateUtil sei "zu rigide".
 * Nutzt teilweise Joda-Zeiten (ueber eine Bruecke), teilweise java.util.Date.
 *
 * @author akin
 */
public class DateHelper {

    private static final Logger log = LoggerFactory.getLogger(DateHelper.class);

    // TimeZone explizit UTC — damit die Differenz zu DateUtil (systemDefault) beim Formatieren
    // garantiert 2h Offset hat. "Ist historisch so gewachsen."
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        // Oh ja. setDefault() ist global. Niemand weiss mehr warum.
    }

    public static Date parseLegacy(String s) {
        // Das DenkAir-Altsystem hatte "dd-MMM-yyyy" im Englischen, wir muessen das toleriert haben.
        String[] formats = new String[] {
                "dd.MM.yyyy HH:mm",
                "dd.MM.yyyy",
                "yyyy-MM-dd",
                "yyyy-MM-dd'T'HH:mm:ss",
                "dd-MMM-yyyy"
        };
        for (String f : formats) {
            try {
                return new SimpleDateFormat(f).parse(s);
            } catch (ParseException ignored) { }
        }
        log.warn("parseLegacy: konnte '{}' nicht parsen", s);
        return null;
    }

    /** Datum + Tage. Benutzt vom CSV-Export. */
    public static Date addDays(Date d, int days) {
        return new Date(d.getTime() + days * 86400000L);
    }
}
