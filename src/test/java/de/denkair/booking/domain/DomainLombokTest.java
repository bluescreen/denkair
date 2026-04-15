package de.denkair.booking.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Light smoke tests on Lombok-generated accessors + Booking@PrePersist.
 * JaCoCo + lombok.config(addLombokGeneratedAnnotation=true) should exclude
 * the synthetic methods from coverage, but these assertions document the
 * contract anyway.
 */
class DomainLombokTest {

    @Test void bookingOnCreateSetsTimestamp() {
        Booking b = new Booking();
        assertNull(b.getCreatedAt());
        b.onCreate();
        assertNotNull(b.getCreatedAt());
    }

    @Test void flightAccessors() {
        Flight f = new Flight();
        f.setFlightNumber("HA1"); f.setPreis(new BigDecimal("100"));
        f.setSeatsAvailable(10); f.setDeparture(LocalDateTime.now());
        assertEquals("HA1", f.getFlightNumber());
        assertEquals(10, f.getSeatsAvailable());
        assertEquals(new BigDecimal("100"), f.getPreis());
        assertTrue(f.getAktiv());
    }

    @Test void airportAccessors() {
        Airport a = new Airport();
        a.setIata("HAM"); a.setName("Hamburg"); a.setCity("Hamburg"); a.setCountry("DE");
        assertEquals("HAM", a.getIata());
    }

    @Test void aircraftAccessors() {
        Aircraft a = new Aircraft();
        a.setTypeCode("A320"); a.setSeats(180); a.setRegistration("D-X");
        assertEquals("A320", a.getTypeCode());
        assertEquals(180, a.getSeats());
    }

    @Test void customerAccessors() {
        Customer c = new Customer();
        c.setFirstName("Max"); c.setLastName("M"); c.setEmail("a@b");
        assertEquals("Max", c.getFirstName());
    }

    @Test void userAccessors() {
        User u = new User();
        u.setUsername("admin"); u.setPasswordHash("x"); u.setRole("ROLE_ADMIN");
        assertEquals("admin", u.getUsername());
        assertTrue(u.getEnabled());
    }

    @Test void equalsAndHashCodeWorkOnFlight() {
        Flight a = new Flight(); Flight b = new Flight();
        assertEquals(a, b);
        a.setFlightNumber("HA1");
        assertNotEquals(a, b);
    }
}
