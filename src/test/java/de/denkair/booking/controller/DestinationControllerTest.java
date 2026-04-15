package de.denkair.booking.controller;

import de.denkair.booking.domain.Airport;
import de.denkair.booking.domain.Flight;
import de.denkair.booking.repository.AirportRepository;
import de.denkair.booking.repository.FlightRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DestinationController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "spring.thymeleaf.enabled=false")
class DestinationControllerTest {

    @Autowired MockMvc mvc;
    @MockBean AirportRepository airportRepository;
    @MockBean FlightRepository flightRepository;

    @Test
    void overview() throws Exception {
        when(airportRepository.findAll()).thenReturn(new ArrayList<>());
        mvc.perform(get("/ziele"))
                .andExpect(status().isOk())
                .andExpect(view().name("destinations/overview"))
                .andExpect(model().attributeExists("airports", "slugMap"));
    }

    @Test
    void unknownSlugRedirects() throws Exception {
        mvc.perform(get("/ziele/atlantis"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ziele"));
    }

    @Test
    void knownSlugRendersDetail() throws Exception {
        Airport pmi = new Airport();
        pmi.setIata("PMI"); pmi.setCity("Palma");
        when(airportRepository.findByIata("PMI")).thenReturn(Optional.of(pmi));
        Flight f = new Flight();
        f.setPreis(new BigDecimal("150"));
        f.setDestination(pmi);
        when(flightRepository.findByAktivTrueOrderByDepartureAsc())
                .thenReturn(new ArrayList<>(Arrays.asList(f)));

        mvc.perform(get("/ziele/palma-mallorca"))
                .andExpect(status().isOk())
                .andExpect(view().name("destinations/detail"))
                .andExpect(model().attributeExists("airport", "slug", "title", "teaser", "highlights", "flights", "fromPrice"));
    }

    @Test
    void knownSlugButMissingAirportRedirects() throws Exception {
        when(airportRepository.findByIata(anyString())).thenReturn(Optional.empty());
        mvc.perform(get("/ziele/faro"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ziele"));
    }

    @Test
    void slugWithEmptyFlightsStillRenders() throws Exception {
        Airport lpa = new Airport();
        lpa.setIata("LPA"); lpa.setCity("Las Palmas");
        when(airportRepository.findByIata("LPA")).thenReturn(Optional.of(lpa));
        when(flightRepository.findByAktivTrueOrderByDepartureAsc()).thenReturn(new ArrayList<>());

        mvc.perform(get("/ziele/gran-canaria"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("fromPrice", org.hamcrest.Matchers.nullValue()));
    }
}
