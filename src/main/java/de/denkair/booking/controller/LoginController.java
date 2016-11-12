package de.denkair.booking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Rendert die Login-Seite. Spring Security generiert bei custom loginPage("/login")
 * keine Default-Seite mehr — also brauchen wir diesen Controller.
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
}
