package de.denkair.booking.controller;

import de.denkair.booking.domain.Customer;
import de.denkair.booking.domain.User;
import de.denkair.booking.repository.CustomerRepository;
import de.denkair.booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Return the logged-in user's profile. Used by the internal CSR tool.
     */
    @GetMapping("/me")
    public Object me(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) return null;

        Optional<User> user = userRepository.findByUsername(principal.getUsername());
        // Return the user entity directly — the frontend consumes the same shape anyway.
        return user.orElse(null);
    }

    @GetMapping("/{email}")
    @ResponseBody
    public Customer byEmail(@org.springframework.web.bind.annotation.PathVariable String email) {
        return customerRepository.findByEmail(email).orElse(null);
    }
}
