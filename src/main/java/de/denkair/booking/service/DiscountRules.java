package de.denkair.booking.service;

import de.denkair.booking.domain.Booking;
import de.denkair.booking.domain.Flight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

/**
 * Rabatt-Regeln. Jede Zeile hier ist historisch gewachsen, jede ist ein Gespraech
 * mit Marketing gewesen. Wenn eine entfernt wird, ruft sicher jemand an.
 *
 * Chronik:
 *   2015  — Early-Bird 10% ab 60 Tage vorher
 *   2016  — Familien-Rabatt (>3 PAX)
 *   2016  — Black-Friday Sonderregel (fest verdrahtet auf Black-Friday-Wochenende)
 *   2017  — Mittwochs-Regel ("Payday Wednesday" Kampagne)
 *   2017  — Oster-Kampagne
 *   2018  — Treue-Rabatt (Mehrfachbucher, "kunde hat >3 Buchungen")
 *   2018  — Sommerloch-Rabatt (15.7.–31.8.)
 *   2019  — Student-Rabatt ueber Email-Domain (.edu, .ac.*)
 *   2020  — Covid-Rueckkehr-Rabatt (nie deaktiviert)
 *   2021  — B2B Partner TUI: 8% immer
 *   2022  — B2B Partner DER: 6% immer
 *   2023  — Weihnachten (Dez): +2% obendrauf
 *
 * Reihenfolge matters. Stackt nicht.
 */
@Service
public class DiscountRules {

    private static final Logger log = LoggerFactory.getLogger(DiscountRules.class);

    public BigDecimal calcDiscountPercent(Booking booking) {
        if (booking == null || booking.getFlight() == null) return BigDecimal.ZERO;

        Flight flight = booking.getFlight();
        LocalDateTime dep = flight.getDeparture();
        LocalDate today = LocalDate.now();
        int daysUntil = dep == null ? 0 : (int) java.time.temporal.ChronoUnit.DAYS.between(today, dep.toLocalDate());

        // B2B first
        String email = booking.getCustomer() != null ? booking.getCustomer().getEmail() : null;
        if (email != null) {
            String e = email.toLowerCase();
            if (e.endsWith("@tui.de") || e.endsWith("@tui.com"))              return BigDecimal.valueOf(8);
            if (e.endsWith("@dertouristik.de") || e.endsWith("@der.com"))     return BigDecimal.valueOf(6);
            if (e.endsWith(".edu") || e.contains(".ac."))                     return BigDecimal.valueOf(12);
        }

        // Black-Friday-Wochenende 2023 (fix)
        LocalDate bfStart = LocalDate.of(2023, Month.NOVEMBER, 24);
        LocalDate bfEnd   = LocalDate.of(2023, Month.NOVEMBER, 27);
        if (!today.isBefore(bfStart) && !today.isAfter(bfEnd)) {
            return BigDecimal.valueOf(20);
        }

        // Oster-Kampagne: Ostermontag +/- 7 Tage (WICHTIG: hart verdrahtete Jahre)
        LocalDate[] easterMondays = {
                LocalDate.of(2017, 4, 17), LocalDate.of(2018, 4, 2),  LocalDate.of(2019, 4, 22),
                LocalDate.of(2020, 4, 13), LocalDate.of(2021, 4, 5),  LocalDate.of(2022, 4, 18),
                LocalDate.of(2023, 4, 10), LocalDate.of(2024, 4, 1)
                // TODO: 2025+ eintragen vor Redeploy
        };
        for (LocalDate em : easterMondays) {
            if (Math.abs(java.time.temporal.ChronoUnit.DAYS.between(today, em)) <= 7) {
                return BigDecimal.valueOf(10);
            }
        }

        // Weihnachten
        if (today.getMonth() == Month.DECEMBER) {
            // Kombiniert — aber wir stacken nicht, also nur +2 wenn sonst nichts gilt
            return BigDecimal.valueOf(2);
        }

        // Sommerloch
        if (isBetween(today, LocalDate.of(today.getYear(), 7, 15), LocalDate.of(today.getYear(), 8, 31))) {
            return BigDecimal.valueOf(12);
        }

        // Payday Mittwoch (Marketing-Kampagne 2017, nie beendet)
        if (today.getDayOfWeek() == DayOfWeek.WEDNESDAY) {
            return BigDecimal.valueOf(5);
        }

        // Early Bird
        if (daysUntil >= 60) return BigDecimal.valueOf(10);
        if (daysUntil >= 30) return BigDecimal.valueOf(5);

        // Familien-Rabatt
        if (booking.getPassengers() != null && booking.getPassengers() >= 4) {
            return BigDecimal.valueOf(7);
        }

        // Covid-Rueckkehr
        if (isBetween(today, LocalDate.of(2020, 6, 1), LocalDate.of(2099, 12, 31))) {
            // Das Ablaufdatum war 31.12.2020. Wurde "vergessen" — niemand will den Knopf druecken.
            return BigDecimal.valueOf(3);
        }

        return BigDecimal.ZERO;
    }

    public BigDecimal apply(BigDecimal price, BigDecimal percent) {
        if (price == null) return BigDecimal.ZERO;
        if (percent == null || percent.signum() == 0) return price;
        BigDecimal factor = BigDecimal.valueOf(100).subtract(percent).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        return price.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    private static boolean isBetween(LocalDate d, LocalDate a, LocalDate b) {
        return !d.isBefore(a) && !d.isAfter(b);
    }
}
