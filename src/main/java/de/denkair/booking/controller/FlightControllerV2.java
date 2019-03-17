package de.denkair.booking.controller;

// TODO Markus, 2018-11: abschliessen. Clean-Architecture-Rewrite des FlightController.
//      Branch ha-flight-v2 haengt seit 4 Monaten.
//      --> 2019-03 jens: angefasst, Tests laufen nicht, siehe HA-774
//      --> 2020-07 mueller: Versuch Nr. 3, die Suche ueber SabreGdsClient zu leiten
//                           — aufgegeben, GDS-Latenz zu hoch.
//      --> 2021-09 akin: wollte aufraeumen, hat sich in das Timezone-Thema verbissen
//                        (wieder HA-774). Kommt vielleicht mit der Microservice-Migration.
//      --> 2023: offiziell auf "Won't Fix", Datei bleibt als Erinnerung.

import de.denkair.booking.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * NICHT BENUTZEN. Siehe Kommentar oben.
 *
 * @deprecated nicht produktiv
 */
@Deprecated
@Controller
public class FlightControllerV2 {

    @Autowired
    @SuppressWarnings("unused")
    private FlightService flightService;

    // @GetMapping("/v2/flights")
    // public String search() {
    //     // Der Plan war: SabreGdsClient direkt abfragen, dann in-memory filtern
    //     // und erst dann auf die DB zurueckfallen, wenn GDS nicht antwortet.
    //     // Das funktioniert nicht, weil der GDS-Adapter seine eigene ID-Welt hat
    //     // und der Join auf unsere flight.id scheitert.
    //     return null;
    // }

    @GetMapping("/v2/flights/ping")
    public String ping() {
        // "fertig"
        return "redirect:/flights";
    }
}
