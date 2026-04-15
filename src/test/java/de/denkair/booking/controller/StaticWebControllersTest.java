package de.denkair.booking.controller;

import de.denkair.booking.service.FlightService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Covers LegalController, LoginController, ServiceController, StaticContentController,
 * FlightControllerV2 — all pure view resolvers, no collaborators.
 */
@WebMvcTest(controllers = {
        LegalController.class,
        LoginController.class,
        ServiceController.class,
        StaticContentController.class,
        FlightControllerV2.class
})
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.thymeleaf.enabled=false",
        "spring.mvc.view.prefix=/WEB-INF/jsp/",
        "spring.mvc.view.suffix=.jsp"
})
class StaticWebControllersTest {

    @Autowired MockMvc mvc;
    @MockBean FlightService flightService;

    @Test void impressum() throws Exception { mvc.perform(get("/impressum")).andExpect(status().isOk()).andExpect(view().name("legal/impressum")); }
    @Test void datenschutz() throws Exception { mvc.perform(get("/datenschutz")).andExpect(status().isOk()).andExpect(view().name("legal/datenschutz")); }
    @Test void agb() throws Exception { mvc.perform(get("/agb")).andExpect(status().isOk()).andExpect(view().name("legal/agb")); }

    @Test void login() throws Exception { mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("auth/login")); }

    @Test void serviceOverview() throws Exception { mvc.perform(get("/service")).andExpect(status().isOk()).andExpect(view().name("service/overview")); }
    @Test void checkIn() throws Exception { mvc.perform(get("/service/check-in")).andExpect(status().isOk()).andExpect(view().name("service/check-in")); }
    @Test void gepaeck() throws Exception { mvc.perform(get("/service/gepaeck")).andExpect(status().isOk()).andExpect(view().name("service/gepaeck")); }
    @Test void meinFlug() throws Exception { mvc.perform(get("/service/mein-flug")).andExpect(status().isOk()).andExpect(view().name("service/mein-flug")); }

    @Test void kontakt() throws Exception { mvc.perform(get("/kontakt")).andExpect(status().isOk()).andExpect(view().name("contact")); }
    @Test void faq() throws Exception { mvc.perform(get("/faq")).andExpect(status().isOk()).andExpect(view().name("faq")); }
    @Test void karriere() throws Exception { mvc.perform(get("/karriere")).andExpect(status().isOk()).andExpect(view().name("karriere")); }

    @Test void flightV2Ping() throws Exception {
        mvc.perform(get("/v2/flights/ping"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/flights"));
    }
}
