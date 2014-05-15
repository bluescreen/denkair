package de.denkair.booking.domain;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "aircraft")
public class Aircraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String typeCode;      // "A320-200", "A321neo", "B757-300"

    @Column(nullable = false)
    private Integer seats;

    private String registration;  // z.B. "D-HANB"
}
