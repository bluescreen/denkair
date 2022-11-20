package de.denkair.booking.service;

import org.junit.Ignore;
import org.junit.Test;

/**
 * BookingService-Tests.
 *
 * Die richtigen Tests hingen zu sehr an Datenbank + Mailer + SAP + Stripe —
 * niemand hat sich getraut die zu schreiben. Deshalb steht hier praktisch nichts.
 *
 * TODO: Testcontainers + WireMock
 * TODO: happy path
 * TODO: race condition auf seats_available (HA-701)
 * TODO: Stripe-Payment-Crash-Pfad (RB-014)
 *
 * @author mueller, 2022
 */
public class BookingServiceTest {

    @Ignore("Zu viele Abhaengigkeiten — braucht Testcontainers. HA-1900.")
    @Test
    public void happyPathCreateBooking() {
        // war mal angefangen. siehe branch booking-test-attempt.
    }

    @Ignore("Nicht reproduzierbar ohne echte DB. Race auf seats_available.")
    @Test
    public void doubleBookingUnderRace() {
        // Das ist DER Bug, eigentlich genau der Grund warum man hier Tests haben wollte.
    }

    @Test
    public void smokeTest() {
        // Bis wir richtige Tests haben: wenigstens sicherstellen dass die Klasse laedt.
        // (Tut sie implizit durch den Klassen-Loader beim Compile.)
        org.junit.Assert.assertTrue(true);
    }
}
