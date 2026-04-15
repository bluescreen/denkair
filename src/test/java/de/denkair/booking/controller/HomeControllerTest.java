package de.denkair.booking.controller;

import de.denkair.booking.domain.Airport;
import de.denkair.booking.domain.Flight;
import de.denkair.booking.repository.AirportRepository;
import de.denkair.booking.repository.FlightRepository;
import de.denkair.booking.service.FlightService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "spring.thymeleaf.enabled=false")
class HomeControllerTest {

    @Autowired MockMvc mvc;
    @MockBean FlightService flightService;
    @MockBean AirportRepository airportRepository;
    @MockBean FlightRepository flightRepository;

    @Test
    void indexHasModelAttributes() throws Exception {
        Flight f1 = new Flight(); f1.setPreis(new BigDecimal("200"));
        Flight f2 = new Flight(); f2.setPreis(new BigDecimal("100"));
        when(flightService.topOffers(anyInt())).thenReturn(new java.util.ArrayList<>(Arrays.asList(f1, f2)));
        Airport a = new Airport(); a.setCity("Palma");
        when(airportRepository.findAll()).thenReturn(new java.util.ArrayList<>(Collections.singletonList(a)));
        when(flightRepository.count()).thenReturn(42L);

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("offers", hasSize(2)))
                .andExpect(model().attribute("totalFlights", 42L))
                .andExpect(model().attributeExists("airports", "dateFormatter", "search"));
    }

    @Test
    void homeAliasWorks() throws Exception {
        when(flightService.topOffers(anyInt())).thenReturn(new java.util.ArrayList<>());
        when(airportRepository.findAll()).thenReturn(new java.util.ArrayList<>());
        when(flightRepository.count()).thenReturn(0L);

        mvc.perform(get("/home")).andExpect(status().isOk()).andExpect(view().name("index"));
    }
}
