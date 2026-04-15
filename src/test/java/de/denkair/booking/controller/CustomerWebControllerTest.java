package de.denkair.booking.controller;

import de.denkair.booking.domain.Booking;
import de.denkair.booking.domain.Customer;
import de.denkair.booking.repository.BookingRepository;
import de.denkair.booking.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerWebController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "spring.thymeleaf.enabled=false")
class CustomerWebControllerTest {

    @Autowired MockMvc mvc;
    @MockBean CustomerRepository customerRepository;
    @MockBean BookingRepository bookingRepository;

    @Test
    void bookingsUsesFallbackEmailWhenAnonymous() throws Exception {
        Customer c = new Customer(); c.setEmail("kunde@example.de");
        when(customerRepository.findByEmail("kunde@example.de")).thenReturn(Optional.of(c));
        when(bookingRepository.findByCustomerOrderByCreatedAtDesc(any(Customer.class)))
                .thenReturn(Arrays.asList(new Booking(), new Booking()));

        mvc.perform(get("/customer/bookings"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/bookings"))
                .andExpect(model().attribute("customerEmail", "kunde@example.de"))
                .andExpect(model().attribute("bookings", hasSize(2)));
    }

    @Test
    void bookingsWhenCustomerUnknownReturnsEmptyList() throws Exception {
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        mvc.perform(get("/customer/bookings"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("bookings", hasSize(0)));
    }
}
