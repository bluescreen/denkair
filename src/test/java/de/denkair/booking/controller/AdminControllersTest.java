package de.denkair.booking.controller;

import de.denkair.booking.controller.admin.AdminDashboardController;
import de.denkair.booking.controller.admin.FlightAdminController;
import de.denkair.booking.domain.Booking;
import de.denkair.booking.repository.BookingRepository;
import de.denkair.booking.repository.CustomerRepository;
import de.denkair.booking.repository.FlightRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AdminDashboardController.class, FlightAdminController.class})
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "spring.thymeleaf.enabled=false")
class AdminControllersTest {

    @Autowired MockMvc mvc;
    @MockBean FlightRepository flightRepository;
    @MockBean BookingRepository bookingRepository;
    @MockBean CustomerRepository customerRepository;

    @Test
    void dashboardShowsCounts() throws Exception {
        Booking b1 = new Booking(); b1.setCreatedAt(LocalDateTime.now());
        Booking b2 = new Booking(); b2.setCreatedAt(LocalDateTime.now().minusDays(1));
        when(flightRepository.count()).thenReturn(12L);
        when(bookingRepository.count()).thenReturn(7L);
        when(customerRepository.count()).thenReturn(5L);
        when(bookingRepository.findAll()).thenReturn(new ArrayList<>(Arrays.asList(b2, b1)));

        mvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attribute("totalFlights", 12L))
                .andExpect(model().attribute("totalBookings", 7L))
                .andExpect(model().attribute("totalCustomers", 5L))
                .andExpect(model().attribute("recentBookings", hasSize(2)));
    }

    @Test
    void flightAdminListRenders() throws Exception {
        when(flightRepository.findAll()).thenReturn(Collections.emptyList());
        mvc.perform(get("/admin/flights"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/flights"));
    }

    @Test
    void flightAdminDeleteRedirects() throws Exception {
        mvc.perform(get("/admin/flights/42/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/flights"));

        ArgumentCaptor<Long> cap = ArgumentCaptor.forClass(Long.class);
        verify(flightRepository).deleteById(cap.capture());
        org.junit.jupiter.api.Assertions.assertEquals(42L, cap.getValue());
    }
}
