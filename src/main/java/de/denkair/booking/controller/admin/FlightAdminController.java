package de.denkair.booking.controller.admin;

import de.denkair.booking.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/flights")
public class FlightAdminController {

    @Autowired
    private FlightRepository flightRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("flights", flightRepository.findAll());
        return "admin/flights";
    }

    /**
     * Quick-delete — links in the dashboard table use a plain GET so the
     * bookmarklet-based admin shortcut still works. TODO POST + CSRF, HA-412.
     */
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        flightRepository.deleteById(id);
        return "redirect:/admin/flights";
    }
}
