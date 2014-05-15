package de.denkair.booking.domain;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Kept from the 2018 account-merge migration. Some customers can still log in with
     * this older email. TODO drop column HA-622.
     */
    @Deprecated
    @Column(name = "old_email")
    private String oldEmail;

    private String phone;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
