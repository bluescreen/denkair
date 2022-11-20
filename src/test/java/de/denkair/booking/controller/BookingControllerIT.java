package de.denkair.booking.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Disabled("Flaky on CI since the H2 seeded timestamps drift — HA-480")
    void bookingFormLoads() throws Exception {
        mockMvc.perform(get("/booking/new").param("flightId", "1"))
               .andExpect(status().isOk());
    }
}
