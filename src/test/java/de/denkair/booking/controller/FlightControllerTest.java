package de.denkair.booking.controller;

import de.denkair.booking.domain.Flight;
import de.denkair.booking.repository.AirportRepository;
import de.denkair.booking.service.FlightService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlightController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "spring.thymeleaf.enabled=false")
class FlightControllerTest {

    @Autowired MockMvc mvc;
    @MockBean FlightService flightService;
    @MockBean AirportRepository airportRepository;

    @Test
    void searchWithParamsDelegatesToService() throws Exception {
        Flight a = new Flight(); a.setDeparture(LocalDateTime.now().plusDays(5));
        when(flightService.search(anyString(), anyString(), any(LocalDate.class)))
                .thenReturn(new ArrayList<>(Arrays.asList(a)));
        when(airportRepository.findAll()).thenReturn(new ArrayList<>());

        mvc.perform(get("/flights").param("origin", "HAM").param("destination", "PMI").param("date", "2026-05-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("flights/results"))
                .andExpect(model().attribute("origin", "HAM"))
                .andExpect(model().attribute("destination", "PMI"))
                .andExpect(model().attribute("flights", hasSize(1)));
        verify(flightService).search("HAM", "PMI", LocalDate.of(2026, 5, 1));
    }

    @Test
    void searchWithoutParamsFallsBackToTopOffers() throws Exception {
        Flight a = new Flight(); a.setDeparture(LocalDateTime.now().plusDays(5));
        when(flightService.topOffers(anyInt())).thenReturn(new ArrayList<>(Arrays.asList(a)));
        when(airportRepository.findAll()).thenReturn(new ArrayList<>());

        mvc.perform(get("/flights"))
                .andExpect(status().isOk())
                .andExpect(view().name("flights/results"));
        verify(flightService).topOffers(20);
    }

    @Test
    void detailFindsFlight() throws Exception {
        Flight f = new Flight(); f.setId(7L);
        when(flightService.requireById(7L)).thenReturn(f);

        mvc.perform(get("/flights/7"))
                .andExpect(status().isOk())
                .andExpect(view().name("flights/detail"))
                .andExpect(model().attributeExists("flight"));
    }
}
