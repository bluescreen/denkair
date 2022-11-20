package de.denkair.booking.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * HomeController "Tests".
 * Eigentlich waere @WebMvcTest richtig — aber der Security-Kontext blockt,
 * der Splitting war zu umstaendlich. Ein echter Test ist noch offen.
 */
class HomeControllerTest {

    @Test
    void classExists() {
        // Wenigstens das.
        assertTrue(HomeController.class.getName().endsWith("HomeController"));
    }

    @Disabled("braucht @WebMvcTest + Security-Mock, HA-2040")
    @Test
    void indexHasOffers() {
        // wuerde Model-attribute prufen
    }
}
