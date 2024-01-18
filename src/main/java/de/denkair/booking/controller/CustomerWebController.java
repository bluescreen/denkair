package de.denkair.booking.controller;

import de.denkair.booking.domain.Booking;
import de.denkair.booking.domain.Customer;
import de.denkair.booking.repository.BookingRepository;
import de.denkair.booking.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Web-Ansicht "Meine Buchungen". Die /api/customer/me API laeuft separat
 * in {@link CustomerController} — das ist die HTML-Seite.
 *
 * Wurde 2020 gebaut nachdem der Callcenter ins Portal verschoben wurde.
 * Der UserDetailsService faellt auf den username "kunde@example.de" zurueck
 * (HA-1102 — siehe TODO.md).
 */
@Controller
@RequestMapping("/customer")
public class CustomerWebController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/bookings")
    public String bookings(Principal principal, Model model) {
        String email = (principal != null) ? principal.getName() : "kunde@example.de";

        Optional<Customer> customer = customerRepository.findByEmail(email);
        List<Booking> list = customer
                .map(c -> bookingRepository.findByCustomerOrderByCreatedAtDesc(c))
                .orElse(Collections.emptyList());

        model.addAttribute("bookings", list);
        model.addAttribute("customerEmail", email);
        return "customer/bookings";
    }
}
