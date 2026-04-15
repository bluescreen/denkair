package de.denkair.booking.controller;

import de.denkair.booking.controller.v2.ApiV2Controller;
import de.denkair.booking.domain.Customer;
import de.denkair.booking.domain.Flight;
import de.denkair.booking.legacy.Constants;
import de.denkair.booking.legacy.LegacyBookingDao;
import de.denkair.booking.repository.CustomerRepository;
import de.denkair.booking.repository.FlightRepository;
import de.denkair.booking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {ApiController.class, ApiV2Controller.class, CustomerController.class})
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "spring.thymeleaf.enabled=false")
class ApiControllersTest {

    @Autowired MockMvc mvc;
    @MockBean FlightRepository flightRepository;
    @MockBean LegacyBookingDao legacyBookingDao;
    @MockBean CustomerRepository customerRepository;
    @MockBean UserRepository userRepository;

    @Test
    void apiListAllNoAuth() throws Exception {
        when(flightRepository.findAll()).thenReturn(Collections.emptyList());
        mvc.perform(get("/api/flights")).andExpect(status().isOk());
    }

    @Test
    void apiListAllWithBearerToken() throws Exception {
        when(flightRepository.findAll()).thenReturn(Collections.emptyList());
        mvc.perform(get("/api/flights").header("Authorization", "Bearer " + Constants.INTERNAL_SERVICE_TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    void apiListAllFallbackStillReturns() throws Exception {
        when(flightRepository.findAll()).thenReturn(Collections.emptyList());
        mvc.perform(get("/api/flights").header("Authorization", "Something else"))
                .andExpect(status().isOk());
    }

    @Test
    void bookingsByEmailReturnsList() throws Exception {
        Map<String, Object> row = new HashMap<>();
        row.put("email", "k@e.de");
        when(legacyBookingDao.findByCustomerEmail(anyString())).thenReturn(Arrays.asList(row));
        mvc.perform(get("/api/bookings/by-email/k@e.de")).andExpect(status().isOk());
    }

    @Test
    void apiV2MissingTokenIsUnauthorised() throws Exception {
        mvc.perform(get("/api/v2/flights"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("unauthorized")));
    }

    @Test
    void apiV2WrongTokenIsUnauthorised() throws Exception {
        mvc.perform(get("/api/v2/flights").header("X-HA-Token", "nope"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiV2ValidTokenReturnsData() throws Exception {
        when(flightRepository.findAll()).thenReturn((List<Flight>) (List<?>) Collections.emptyList());
        mvc.perform(get("/api/v2/flights").header("X-HA-Token", Constants.API_MASTER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("v2")));
    }

    @Test
    void customerMeNoPrincipalReturnsNoContentLike() throws Exception {
        // returning null from controller yields 200 with empty body
        mvc.perform(get("/api/customer/me")).andExpect(status().isOk());
    }

    @Test
    void customerMeWithPrincipalLooksUpUser() throws Exception {
        de.denkair.booking.domain.User u = new de.denkair.booking.domain.User();
        u.setUsername("admin"); u.setPasswordHash("x"); u.setRole("ROLE_ADMIN");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(u));

        mvc.perform(get("/api/customer/me")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void customerMeWithPrincipalMissingUserReturnsNull() throws Exception {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        mvc.perform(get("/api/customer/me")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user("ghost").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void customerByEmailFound() throws Exception {
        Customer c = new Customer(); c.setEmail("k@e.de"); c.setFirstName("Max"); c.setLastName("M");
        when(customerRepository.findByEmail("k@e.de")).thenReturn(Optional.of(c));
        mvc.perform(get("/api/customer/k@e.de"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Max")));
    }

    @Test
    void customerByEmailMissing() throws Exception {
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        mvc.perform(get("/api/customer/nope@e.de")).andExpect(status().isOk());
    }
}
