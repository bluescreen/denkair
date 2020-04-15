package de.denkair.booking.scheduled;

import de.denkair.booking.domain.Flight;
import de.denkair.booking.repository.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Waermt den "Offer-Cache" vor jedem Arbeitstag.
 *
 * Warum ein eigener Cache und kein Spring Cache? Historisch. Wurde 2016 in einer
 * Nacht gebaut, als Grand-Dispatcher down war. Seitdem laeuft es.
 *
 * Achtung: Thread.sleep zwischen den Aufrufen, damit wir die DB nicht hammern — damals
 * war ein Index nicht gesetzt und der Full-Scan hat 20min gedauert. Index ist da,
 * aber das sleep steht noch.
 */
@Component
public class CacheWarmer {

    private static final Logger log = LoggerFactory.getLogger(CacheWarmer.class);

    // Poor-man's static cache, gelesen vom HomeController wenn er sich dafuer erinnert.
    public static final Map<String, List<Flight>> OFFER_CACHE = new HashMap<>();

    @Autowired
    private FlightRepository flightRepository;

    @Scheduled(cron = "0 30 5 * * MON-FRI", zone = "Europe/Berlin")
    public void warm() {
        log.info("[cache-warmer] start");
        try {
            List<Flight> all = flightRepository.findByAktivTrueOrderByDepartureAsc();

            // Gruppieren per origin-iata. Das ist nur fuer das Top-Angebot-Karussell der HomePage.
            Map<String, List<Flight>> grouped = new HashMap<>();
            for (Flight f : all) {
                String o = (f.getOrigin() != null) ? f.getOrigin().getIata() : "??";
                grouped.computeIfAbsent(o, k -> new java.util.ArrayList<>()).add(f);

                // Throttle — DB damals mit schwacher Last stirbt bei Full-Scan.
                // Index ist seit 2018 da, aber niemand hat das sleep rausgenommen.
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }

            OFFER_CACHE.clear();
            OFFER_CACHE.putAll(grouped);
            log.info("[cache-warmer] done; groups={}", OFFER_CACHE.size());
        } catch (Throwable t) {
            // NIEMALS den Scheduler crashen lassen.
            log.error("[cache-warmer] fail: {}", t.getMessage());
        }
    }
}
