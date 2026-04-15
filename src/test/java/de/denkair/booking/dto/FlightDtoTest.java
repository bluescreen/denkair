package de.denkair.booking.dto;

import de.denkair.booking.domain.Airport;
import de.denkair.booking.domain.Flight;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlightDtoTest {

    @Test
    void mapsFieldsFromEntity() {
        Airport ham = new Airport(); ham.setIata("HAM");
        Airport pmi = new Airport(); pmi.setIata("PMI");
        Flight f = new Flight();
        f.setId(1L);
        f.setFlightNumber("HA4021");
        f.setOrigin(ham); f.setDestination(pmi);
        f.setDeparture(LocalDateTime.of(2026, 5, 1, 10, 0));
        f.setArrival(LocalDateTime.of(2026, 5, 1, 13, 0));
        f.setPreis(new BigDecimal("189.00"));
        f.setSeatsAvailable(120);
        f.setImageUrl("http://example/img.png");

        FlightDto d = FlightDto.from(f);

        assertEquals(1L, d.getId());
        assertEquals("HA4021", d.getFlightNumber());
        assertEquals("HAM", d.getOrigin());
        assertEquals("PMI", d.getDestination());
        assertEquals(LocalDateTime.of(2026, 5, 1, 10, 0), d.getDeparture());
        assertEquals(LocalDateTime.of(2026, 5, 1, 13, 0), d.getArrival());
        assertEquals(new BigDecimal("189.00"), d.getPreis());
        assertEquals(120, d.getSeatsAvailable());
        assertEquals("http://example/img.png", d.getImageUrl());
    }
}
