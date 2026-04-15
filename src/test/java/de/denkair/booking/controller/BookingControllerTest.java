package de.denkair.booking.controller;

import de.denkair.booking.domain.Booking;
import de.denkair.booking.domain.Flight;
import de.denkair.booking.dto.BookingForm;
import de.denkair.booking.service.BookingService;
import de.denkair.booking.service.FlightService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "spring.thymeleaf.enabled=false")
class BookingControllerTest {

    @Autowired MockMvc mvc;
    @MockBean BookingService bookingService;
    @MockBean FlightService flightService;
    @MockBean JdbcTemplate jdbcTemplate;

    @Test
    void showFormPopulatesModel() throws Exception {
        Flight f = new Flight(); f.setId(1L); f.setPreis(BigDecimal.TEN);
        when(flightService.requireById(1L)).thenReturn(f);

        mvc.perform(get("/booking/new").param("flightId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/form"))
                .andExpect(model().attributeExists("flight", "form"));
    }

    @Test
    void submitRedirectsOnSuccess() throws Exception {
        Booking b = new Booking(); b.setReferenceCode("HA-ABC12");
        when(bookingService.createBooking(any(BookingForm.class))).thenReturn(b);

        mvc.perform(post("/booking")
                        .param("flightId", "1")
                        .param("firstName", "Max")
                        .param("lastName", "M")
                        .param("email", "a@b.de")
                        .param("passengers", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/HA-ABC12"));
    }

    @Test
    void submitWithValidationErrorsRerendersForm() throws Exception {
        Flight f = new Flight(); f.setId(1L);
        when(flightService.requireById(anyLong())).thenReturn(f);

        mvc.perform(post("/booking").param("flightId", "1")) // missing firstName/lastName/email
                .andExpect(status().isOk())
                .andExpect(view().name("booking/form"));
    }

    @Test
    void confirmationView() throws Exception {
        mvc.perform(get("/booking/HA-ABC12"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/confirmation"));
    }

    @Test
    void nativeSearchExecutesSqlAndReturnsJson() throws Exception {
        Map<String, Object> row = new HashMap<>();
        row.put("id", 1L); row.put("flight_number", "HA4021"); row.put("preis", new BigDecimal("100"));
        when(jdbcTemplate.queryForList(anyString())).thenReturn(Arrays.asList(row));

        mvc.perform(get("/flights/api/search").param("origin", "HAM").param("destination", "PMI"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("HA4021")));
    }
}
