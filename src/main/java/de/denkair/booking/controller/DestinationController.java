package de.denkair.booking.controller;

import de.denkair.booking.domain.Airport;
import de.denkair.booking.domain.Flight;
import de.denkair.booking.repository.AirportRepository;
import de.denkair.booking.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Reiseziel-Landing-Pages.
 *
 * Jede Destination hat eine URL wie /ziele/palma-mallorca. Der Slug wird auf die
 * IATA-Tabelle abgebildet. Die redaktionellen Texte liegen hart im Controller,
 * weil das geplante Headless-CMS (2019) nie gekommen ist.
 */
@Controller
public class DestinationController {

    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private FlightRepository flightRepository;

    // slug -> IATA
    private static final Map<String, String> SLUG_TO_IATA = new HashMap<>();
    // slug -> {hero, teaser, highlights}
    private static final Map<String, String[]> CONTENT = new HashMap<>();

    static {
        SLUG_TO_IATA.put("palma-mallorca", "PMI");
        SLUG_TO_IATA.put("antalya",        "AYT");
        SLUG_TO_IATA.put("gran-canaria",   "LPA");
        SLUG_TO_IATA.put("kreta",          "HER");
        SLUG_TO_IATA.put("hurghada",       "HRG");
        SLUG_TO_IATA.put("faro",           "FAO");

        CONTENT.put("palma-mallorca", new String[]{
            "Palma de Mallorca — Sonne, Meer und Tapas.",
            "Die Balearen-Hauptstadt ist das Herz des deutschen Mittelmeer-Urlaubs. Direkt vom Flughafen nach Playa de Palma in 15 Minuten.",
            "Strände: Playa de Palma, Cala Major, Illetes|Altstadt: Kathedrale La Seu, Paseo del Borne|Ausflüge: Tramuntana-Gebirge, Valldemossa"
        });
        CONTENT.put("antalya", new String[]{
            "Antalya — Türkische Riviera.",
            "Endlose Sandstrände, antike Ruinen in Perge und Aspendos, pulsierende Altstadt Kaleiçi. Ganzjährig Badesaison.",
            "Strände: Konyaaltı, Lara Beach|Sehenswert: Düden-Wasserfälle, Antike Stadt Perge|Küche: Mezze, gegrillter Fisch am Hafen"
        });
        CONTENT.put("gran-canaria", new String[]{
            "Gran Canaria — Der Kontinent im Kleinen.",
            "Von den Dünen von Maspalomas bis zum Pinienwald im Hochland: Gran Canaria ist das ganze Jahr über warm und abwechslungsreich.",
            "Strände: Maspalomas, Playa del Inglés, Puerto Rico|Natur: Roque Nublo, Caldera de Bandama|Städte: Las Palmas, Teror"
        });
        CONTENT.put("kreta", new String[]{
            "Kreta — Wiege Europas.",
            "Die größte griechische Insel bietet Minoer-Paläste, raue Gebirge und türkisfarbene Buchten. Heraklion als Tor, Chania als Schmuckstück.",
            "Strände: Elafonisi, Balos, Falassarna|Antike: Knossos, Phaistos|Gebirge: Samaria-Schlucht"
        });
        CONTENT.put("hurghada", new String[]{
            "Hurghada — Rotes Meer trifft Wüste.",
            "Ganzjährige Badetemperaturen am Roten Meer, eines der besten Tauchreviere weltweit und Ausflüge in die Sahara.",
            "Tauchen: Giftun-Inseln, Abu Nuhas|Ausflüge: Luxor, Wüstensafari|Strände: Makadi Bay, Sahl Hasheesh"
        });
        CONTENT.put("faro", new String[]{
            "Faro — Sonnenküste Algarve.",
            "Faro ist das Tor zur Algarve: goldene Klippen, ruhige Lagunen, lebendige Fischerdörfer und das milde Atlantikklima.",
            "Strände: Praia de Faro, Ilha Deserta|Natur: Ria Formosa Naturpark|Städte: Tavira, Lagos, Albufeira"
        });
    }

    @GetMapping("/ziele")
    public String overview(Model model) {
        model.addAttribute("airports", airportRepository.findAll());
        model.addAttribute("slugMap", SLUG_TO_IATA);
        return "destinations/overview";
    }

    @GetMapping("/ziele/{slug}")
    public String detail(@PathVariable String slug, Model model) {
        String iata = SLUG_TO_IATA.get(slug);
        if (iata == null) {
            return "redirect:/ziele";
        }

        Optional<Airport> airportOpt = airportRepository.findByIata(iata);
        if (!airportOpt.isPresent()) {
            return "redirect:/ziele";
        }
        Airport airport = airportOpt.get();

        List<Flight> flights = flightRepository.findByAktivTrueOrderByDepartureAsc();
        flights.removeIf(f -> f.getDestination() == null || !iata.equals(f.getDestination().getIata()));
        flights.sort(Comparator.comparing(Flight::getPreis));

        BigDecimal from = flights.isEmpty() ? null : flights.get(0).getPreis();

        String[] content = CONTENT.getOrDefault(slug, new String[]{airport.getCity(), "", ""});
        String[] highlights = content[2].isEmpty() ? new String[0] : content[2].split("\\|");

        model.addAttribute("airport", airport);
        model.addAttribute("slug", slug);
        model.addAttribute("title", content[0]);
        model.addAttribute("teaser", content[1]);
        model.addAttribute("highlights", highlights);
        model.addAttribute("flights", flights);
        model.addAttribute("fromPrice", from);
        return "destinations/detail";
    }
}
