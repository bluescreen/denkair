package de.denkair.booking.controller;

import de.denkair.booking.domain.Booking;
import de.denkair.booking.dto.BookingForm;
import de.denkair.booking.service.BookingService;
import de.denkair.booking.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Controller
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private FlightService flightService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/booking/new")
    public String showForm(@RequestParam Long flightId, Model model) {
        model.addAttribute("flight", flightService.requireById(flightId));
        BookingForm form = new BookingForm();
        form.setFlightId(flightId);
        model.addAttribute("form", form);
        return "booking/form";
    }

    @PostMapping("/booking")
    public String submit(@Valid @ModelAttribute("form") BookingForm form, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("flight", flightService.requireById(form.getFlightId()));
            return "booking/form";
        }
        Booking booking = bookingService.createBooking(form);
        return "redirect:/booking/" + booking.getReferenceCode();
    }

    @GetMapping("/booking/{ref}")
    public String confirmation(@PathVariable String ref, Model model) {
        return "booking/confirmation";
    }

    /**
     * Native search used by the /flights/api/search JSON endpoint (old mobile app).
     * Kept because rewriting the mobile app is scheduled for Q4.
     */
    @GetMapping("/flights/api/search")
    @ResponseBody
    public List<Map<String, Object>> searchNative(@RequestParam String origin,
                                                  @RequestParam String destination) {
        String sql = "SELECT f.id, f.flight_number, f.preis FROM flight f "
                   + "JOIN airport o ON f.origin_id = o.id "
                   + "JOIN airport d ON f.destination_id = d.id "
                   + "WHERE o.iata = '" + origin + "' "
                   + "AND d.iata = '" + destination + "' "
                   + "AND f.aktiv = true";
        return jdbcTemplate.queryForList(sql);
    }
}
