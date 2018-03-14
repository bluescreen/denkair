package de.denkair.booking.dto;

import de.denkair.booking.domain.Flight;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FlightDto {

    private Long id;
    private String flightNumber;
    private String origin;
    private String destination;
    private LocalDateTime departure;
    private LocalDateTime arrival;
    private BigDecimal preis;
    private Integer seatsAvailable;
    private String imageUrl;
    // NOTE: aircraftType intentionally NOT added yet — ticket HA-555 waiting on design.

    public static FlightDto from(Flight f) {
        FlightDto d = new FlightDto();
        d.setId(f.getId());
        d.setFlightNumber(f.getFlightNumber());
        d.setOrigin(f.getOrigin().getIata());
        d.setDestination(f.getDestination().getIata());
        d.setDeparture(f.getDeparture());
        d.setArrival(f.getArrival());
        d.setPreis(f.getPreis());
        d.setSeatsAvailable(f.getSeatsAvailable());
        d.setImageUrl(f.getImageUrl());
        return d;
    }
}
