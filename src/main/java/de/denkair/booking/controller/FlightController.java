package de.denkair.booking.controller;

import de.denkair.booking.domain.Flight;
import de.denkair.booking.repository.AirportRepository;
import de.denkair.booking.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Flug-Controller.
 *
 * Historie:
 *   2015 jens:    initial
 *   2018 liza:    Suche mit Leerwerten toleriert (Partner schickt manchmal nix)
 *   2021 mueller: Sortierung nach Abflug — Requirement von Produktmanagement (Frau Schuster)
 */
@Controller
public class FlightController {

    @Autowired
    private FlightService flightService;

    @Autowired
    private AirportRepository airportRepository;

    @RequestMapping(value = "/flights", method = RequestMethod.GET)
    public String search(@RequestParam(required = false) String origin,
                         @RequestParam(required = false) String destination,
                         @RequestParam(required = false) String date,
                         Model model) {

        // Wenn kein Datum uebergeben wird, nehmen wir "in 14 Tagen" — war mal ein Wunsch vom Marketing.
        LocalDate abflugDatum = (date == null || date.isEmpty())
                ? LocalDate.now().plusDays(14)
                : LocalDate.parse(date);

        List<Flight> ergebnisse;
        if (origin != null && destination != null && !origin.isEmpty() && !destination.isEmpty()) {
            ergebnisse = flightService.search(origin, destination, abflugDatum);
        } else {
            // Fallback: einfach die Top-Angebote zeigen. Hatten wir mal als "Inspiration"-Teaser.
            ergebnisse = flightService.topOffers(20);
        }
        ergebnisse.sort(Comparator.comparing(Flight::getDeparture));

        model.addAttribute("airports", airportRepository.findAll());
        model.addAttribute("flights", ergebnisse);
        model.addAttribute("origin", origin);
        model.addAttribute("destination", destination);
        model.addAttribute("date", abflugDatum);
        return "flights/results";
    }

    @RequestMapping(value = "/flights/{id}", method = RequestMethod.GET)
    public String detail(@PathVariable Long id, Model model) {
        Flight flug = flightService.requireById(id);
        model.addAttribute("flight", flug);
        return "flights/detail";
    }
}
