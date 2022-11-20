package de.denkair.booking.service;

import de.denkair.booking.domain.Flight;
import de.denkair.booking.repository.FlightRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private FlightService flightService;

    @Before
    public void setUp() {
        when(flightRepository.searchFlights(anyString(), anyString(), any(), any()))
            .thenReturn(Collections.<Flight>emptyList());
    }

    @Test
    public void emptyResultIsEmpty() {
        assertTrue(flightService.search("HAM", "PMI", LocalDate.now()).isEmpty());
    }
}
