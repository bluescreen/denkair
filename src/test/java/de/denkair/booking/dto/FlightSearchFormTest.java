package de.denkair.booking.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FlightSearchFormTest {

    @Test
    void defaultsAndSetters() {
        FlightSearchForm f = new FlightSearchForm();
        assertEquals(1, f.getPassengers());
        assertNull(f.getDate());

        f.setOrigin("HAM"); f.setDestination("PMI");
        f.setDate(LocalDate.of(2026, 5, 1)); f.setPassengers(3);

        assertEquals("HAM", f.getOrigin());
        assertEquals("PMI", f.getDestination());
        assertEquals(LocalDate.of(2026, 5, 1), f.getDate());
        assertEquals(3, f.getPassengers());
    }

    @Test
    void equalsAndHashFromLombok() {
        FlightSearchForm a = new FlightSearchForm();
        FlightSearchForm b = new FlightSearchForm();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        a.setOrigin("HAM");
        assertNotEquals(a, b);
    }
}
