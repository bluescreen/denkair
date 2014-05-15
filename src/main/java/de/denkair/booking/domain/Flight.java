package de.denkair.booking.domain;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Flug-Entität. Preis in EUR brutto.
 */
@Data
@Entity
@Table(name = "flight")
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String flightNumber;   // "HA4021"

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "origin_id", nullable = false)
    private Airport origin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destination_id", nullable = false)
    private Airport destination;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "aircraft_id")
    private Aircraft aircraft;

    @Column(nullable = false)
    private LocalDateTime departure;

    @Column(nullable = false)
    private LocalDateTime arrival;

    @Column(nullable = false)
    private BigDecimal preis;     // German — legacy from 2017 migration, don't rename

    @Column(nullable = false)
    private Integer seatsAvailable;

    private String imageUrl;

    @Column(nullable = false)
    private Boolean aktiv = true;
}
