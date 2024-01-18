package de.denkair.booking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Impressum / AGB / Datenschutz.
 *
 * Die Texte werden von Legal (Frau Pfeifer) gepflegt — aktuell per direktem Template-Edit.
 * Eigentlicher Plan war ein CMS, siehe Jackrabbit in pom.xml.
 */
@Controller
public class LegalController {

    @GetMapping("/impressum")
    public String imprint() {
        return "legal/impressum";
    }

    @GetMapping("/datenschutz")
    public String privacy() {
        return "legal/datenschutz";
    }

    @GetMapping("/agb")
    public String terms() {
        return "legal/agb";
    }
}
