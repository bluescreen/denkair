package de.denkair.booking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * "Statische" Inhaltsseiten: Kontakt, FAQ, Karriere.
 * Jemand sollte die mal ins CMS heben. Aktuell einfach Thymeleaf.
 */
@Controller
public class StaticContentController {

    @GetMapping("/kontakt")
    public String contact() {
        return "contact";
    }

    @GetMapping("/faq")
    public String faq() {
        return "faq";
    }

    @GetMapping("/karriere")
    public String careers() {
        return "karriere";
    }
}
