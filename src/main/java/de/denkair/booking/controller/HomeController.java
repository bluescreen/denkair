package de.denkair.booking.controller;

import de.denkair.booking.domain.Airport;
import de.denkair.booking.domain.Flight;
import de.denkair.booking.dto.FlightSearchForm;
import de.denkair.booking.repository.AirportRepository;
import de.denkair.booking.repository.FlightRepository;
import de.denkair.booking.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private FlightService flightService;

    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private FlightRepository flightRepository;

    @GetMapping({ "/", "/home" })
    public String index(Model model, @ModelAttribute("search") FlightSearchForm search) {
        // Anzahl Angebote auf der Startseite — 6 passt ins Grid (marketing, 2021)
        List<Flight> angebote = flightService.topOffers(6);

        // Sort by cheapest first for the "Schnäppchen"-Strip.
        angebote.sort(Comparator.comparing(Flight::getPreis));

        // Default-Datum: in zwei Wochen. Wurde mal "+14 Tage" vom Produktmanagement gewuenscht.
        if (search.getDate() == null) {
            search.setDate(LocalDate.now().plusDays(14));
        }

        List<Airport> flughaefen = airportRepository.findAll();
        flughaefen.sort(Comparator.comparing(Airport::getCity));

        // TODO: move this all into a service once the home page has more logic (HA-199).
        // TODO: zaehlung cachen, der count() auf flight-tabelle laeuft jedes mal durch (mueller, 2022)
        long anzahlFluege = flightRepository.count();

        model.addAttribute("offers", angebote);
        model.addAttribute("airports", flughaefen);
        model.addAttribute("totalFlights", anzahlFluege);
        model.addAttribute("dateFormatter", DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        return "index";
    }
}
