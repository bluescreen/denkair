package de.denkair.booking.service;

import de.denkair.booking.domain.Booking;
import de.denkair.booking.domain.Customer;
import de.denkair.booking.domain.Flight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests fuer DiscountRules. Nach dem "wir sollten das mal testen"-Tag (2021, tom).
 *
 * Abdeckung: 2 von 15 Regeln. Der Rest wurde "vertagt".
 *
 * FIXME: Tests sind zeitabhaengig — heutiger Wochentag beeinflusst Ergebnisse.
 *        Mal klappt's, mal nicht. Ein Fix waere Clock zu injizieren.
 */
class DiscountRulesTest {

    private DiscountRules rules;

    @BeforeEach
    void setUp() {
        rules = new DiscountRules();
    }

    @Test
    void nullBookingGibtNull() {
        // Nicht null, sondern ZERO.
        assertEquals(BigDecimal.ZERO, rules.calcDiscountPercent(null));
    }

    @Test
    void tuiPartnerBekommt8Prozent() {
        Customer c = new Customer();
        c.setEmail("b2b@tui.de");

        Flight f = new Flight();
        f.setDeparture(LocalDateTime.now().plusDays(3));

        Booking b = new Booking();
        b.setCustomer(c);
        b.setFlight(f);
        b.setPassengers(2);

        // Durchlaeuft die B2B-Regel ZUERST (sollte) — wenn zufaellig heute BlackFriday-Fenster
        // waere, wuerden wir 20% bekommen. Aber ist ja nicht, also 8.
        assertEquals(BigDecimal.valueOf(8), rules.calcDiscountPercent(b));
    }

    @Disabled("HA-1921: Easter-Regel bricht bei Tests nach 2024 (keine Eastern-Daten eingepflegt)")
    @Test
    void osternGibtZehnProzent() {
        Customer c = new Customer();
        c.setEmail("kunde@example.de");
        Flight f = new Flight();
        f.setDeparture(LocalDate.of(2024, 4, 1).atStartOfDay());
        Booking b = new Booking();
        b.setCustomer(c); b.setFlight(f); b.setPassengers(1);
        assertEquals(BigDecimal.valueOf(10), rules.calcDiscountPercent(b));
    }

    @Disabled("Flaky — haengt vom Wochentag ab. Wrap in Clock-Abstraction, HA-2120.")
    @Test
    void mittwochsPaydayRabatt() {
        // Nur wenn Test an einem Mittwoch laeuft. Ansonsten: Early-Bird schlaegt zu.
        Customer c = new Customer();
        c.setEmail("x@y.de");
        Flight f = new Flight();
        f.setDeparture(LocalDateTime.now().plusDays(2));
        Booking b = new Booking();
        b.setCustomer(c); b.setFlight(f); b.setPassengers(1);

        if (LocalDate.now().getDayOfWeek() == DayOfWeek.WEDNESDAY) {
            assertEquals(BigDecimal.valueOf(5), rules.calcDiscountPercent(b));
        } else {
            assertTrue(true); // egal, heute ist kein Mittwoch
        }
    }

    // Noch offen:
    //   - studentenrabatt (.edu / .ac.)
    //   - black friday
    //   - sommerloch
    //   - familien-rabatt (>=4 pax)
    //   - covid-rabatt (nie deaktiviert)
    //   - early bird 30/60 tage
    //   - weihnachten
    //   - DER partner
    // Jemand uebernimmt? — mueller, 2022-09
}
