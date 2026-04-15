package de.denkair.booking.controller;

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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OffersController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "spring.thymeleaf.enabled=false")
class OffersControllerTest {

    @Autowired MockMvc mvc;
    @MockBean FlightRepository flightRepository;
    @MockBean AirportRepository airportRepository;

    @Test
    void offersShowsCheapestFirst() throws Exception {
        Flight f1 = new Flight(); f1.setPreis(new BigDecimal("300"));
        Flight f2 = new Flight(); f2.setPreis(new BigDecimal("100"));
        when(flightRepository.findByAktivTrueOrderByDepartureAsc()).thenReturn(new ArrayList<>(Arrays.asList(f1, f2)));
        when(airportRepository.findAll()).thenReturn(new ArrayList<>());

        mvc.perform(get("/angebote"))
                .andExpect(status().isOk())
                .andExpect(view().name("offers"))
                .andExpect(model().attribute("cheapest", new BigDecimal("100")));
    }

    @Test
    void offersEmptyListHasNullCheapest() throws Exception {
        when(flightRepository.findByAktivTrueOrderByDepartureAsc()).thenReturn(new ArrayList<>());
        when(airportRepository.findAll()).thenReturn(new ArrayList<>());
        mvc.perform(get("/angebote"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("cheapest", org.hamcrest.Matchers.nullValue()));
    }
}
