package de.denkair.booking.controller;

import de.denkair.booking.domain.Flight;
import de.denkair.booking.repository.AirportRepository;
import de.denkair.booking.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Angebote-Seite. Zeigt die guenstigsten aktiven Fluege gruppiert nach Reiseziel.
 *
 * Marketing will hier eigentlich eigene redaktionelle Inhalte, wurde aber nie geliefert.
 * Stattdessen: "die n guenstigsten Fluege" aus der Datenbank. HA-980.
 */
@Controller
public class OffersController {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private AirportRepository airportRepository;

    @RequestMapping(value = "/angebote", method = RequestMethod.GET)
    public String offers(Model model) {
        List<Flight> all = flightRepository.findByAktivTrueOrderByDepartureAsc();
        all.sort(Comparator.comparing(Flight::getPreis));

        BigDecimal cheapest = all.isEmpty() ? null : all.get(0).getPreis();

        model.addAttribute("offers", all);
        model.addAttribute("cheapest", cheapest);
        model.addAttribute("airports", airportRepository.findAll());
        return "offers";
    }
}
