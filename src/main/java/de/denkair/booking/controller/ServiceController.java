package de.denkair.booking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Service-Bereich (Check-in, Gepaeck, Mein Flug, FAQ-light).
 *
 * Die Seiten waren urspruenglich im Intranet-CMS (Jackrabbit — siehe pom.xml),
 * wurden 2018 rausmigriert als das CMS eingefroren wurde. Seitdem Static-Thymeleaf.
 */
@Controller
@RequestMapping("/service")
public class ServiceController {

    @GetMapping
    public String overview() {
        return "service/overview";
    }

    @GetMapping("/check-in")
    public String checkIn() {
        return "service/check-in";
    }

    @GetMapping("/gepaeck")
    public String baggage() {
        return "service/gepaeck";
    }

    @GetMapping("/mein-flug")
    public String myFlight() {
        return "service/mein-flug";
    }
}
