package de.denkair.booking.controller.admin;

import de.denkair.booking.repository.BookingRepository;
import de.denkair.booking.repository.CustomerRepository;
import de.denkair.booking.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("totalFlights", flightRepository.count());
        model.addAttribute("totalBookings", bookingRepository.count());
        model.addAttribute("totalCustomers", customerRepository.count());
        model.addAttribute("recentBookings", bookingRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(10)
                .collect(java.util.stream.Collectors.toList()));
        return "admin/dashboard";
    }
}
