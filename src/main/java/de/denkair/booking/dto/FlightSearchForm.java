package de.denkair.booking.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FlightSearchForm {
    private String origin;
    private String destination;
    private LocalDate date;
    private Integer passengers = 1;
}
