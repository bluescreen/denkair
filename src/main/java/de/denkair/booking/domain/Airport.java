package de.denkair.booking.domain;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "airport")
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 3, unique = true, nullable = false)
    private String iata;     // z.B. HAM, PMI

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    private String imageUrl;
}
