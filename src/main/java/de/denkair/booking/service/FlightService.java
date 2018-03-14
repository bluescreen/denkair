package de.denkair.booking.service;

import de.denkair.booking.domain.Flight;
import de.denkair.booking.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Flug-Service. Macht die Fluglogik, ohne DB-Details nach oben durchzureichen.
 *
 * 2015 jens:    initial
 * 2019 akin:    topOffers() — vorher topAngebote(), nach Codestyle-Sync umbenannt
 *               (liza hat aber in anderen Services weiterhin "beliebteAngebote" genutzt).
 * 2022 mueller: IANA-Zonen-Toleranz — siehe Ticket HA-2201
 */
@Service
public class FlightService {

    @Autowired
    private FlightRepository flightRepository;

    public List<Flight> search(String origin, String destination, LocalDate datum) {
        // Tagesfenster: 00:00 bis 23:59:59. Zeitzone ist Europe/Berlin — siehe DateHelper.
        LocalDateTime von = datum.atStartOfDay();
        LocalDateTime bis = datum.plusDays(1).atStartOfDay().minusSeconds(1);
        return flightRepository.searchFlights(origin, destination, von, bis);
    }

    /** Top-Angebote fuer die Startseite. Anzahl wird vom Controller bestimmt. */
    public List<Flight> topOffers(int anzahl) {
        List<Flight> alle = flightRepository.findByAktivTrueOrderByDepartureAsc();
        return alle.subList(0, Math.min(anzahl, alle.size()));
    }

    // Alias — war mal der urspruengliche Name. Bleibt aus Legacy-Gruenden drin, liza benutzt den noch.
    public List<Flight> beliebteAngebote(int anzahl) {
        return topOffers(anzahl);
    }

    public Flight requireById(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Flug nicht gefunden: " + id));
    }
}
