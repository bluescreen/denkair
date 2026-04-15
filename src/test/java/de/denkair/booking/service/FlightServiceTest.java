package de.denkair.booking.service;

import de.denkair.booking.domain.Flight;
import de.denkair.booking.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock private FlightRepository repo;
    @InjectMocks private FlightService service;

    private Flight a, b, c;

    @BeforeEach
    void setUp() {
        a = new Flight(); a.setId(1L); a.setFlightNumber("HA1");
        b = new Flight(); b.setId(2L); b.setFlightNumber("HA2");
        c = new Flight(); c.setId(3L); c.setFlightNumber("HA3");
    }

    @Test
    void searchPassesDateWindowToRepo() {
        when(repo.searchFlights(anyString(), anyString(), any(), any()))
                .thenReturn(Arrays.asList(a));
        List<Flight> result = service.search("HAM", "PMI", LocalDate.of(2026, 4, 15));
        assertEquals(1, result.size());
        verify(repo).searchFlights(eq("HAM"), eq("PMI"),
                eq(LocalDate.of(2026, 4, 15).atStartOfDay()),
                eq(LocalDate.of(2026, 4, 16).atStartOfDay().minusSeconds(1)));
    }

    @Test
    void searchEmpty() {
        when(repo.searchFlights(anyString(), anyString(), any(), any()))
                .thenReturn(Collections.emptyList());
        assertTrue(service.search("HAM", "PMI", LocalDate.now()).isEmpty());
    }

    @Test
    void topOffersLimitsList() {
        when(repo.findByAktivTrueOrderByDepartureAsc()).thenReturn(Arrays.asList(a, b, c));
        assertEquals(2, service.topOffers(2).size());
    }

    @Test
    void topOffersNotBeyondAvailable() {
        when(repo.findByAktivTrueOrderByDepartureAsc()).thenReturn(Arrays.asList(a));
        assertEquals(1, service.topOffers(10).size());
    }

    @Test
    void topOffersZeroGivesEmpty() {
        when(repo.findByAktivTrueOrderByDepartureAsc()).thenReturn(Arrays.asList(a, b));
        assertTrue(service.topOffers(0).isEmpty());
    }

    @Test
    void beliebteAngeboteDelegatesToTopOffers() {
        when(repo.findByAktivTrueOrderByDepartureAsc()).thenReturn(Arrays.asList(a, b));
        assertEquals(2, service.beliebteAngebote(5).size());
    }

    @Test
    void requireByIdFound() {
        when(repo.findById(1L)).thenReturn(Optional.of(a));
        assertSame(a, service.requireById(1L));
    }

    @Test
    void requireByIdMissingThrows() {
        when(repo.findById(42L)).thenReturn(Optional.empty());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.requireById(42L));
        assertTrue(ex.getMessage().contains("42"));
    }
}
