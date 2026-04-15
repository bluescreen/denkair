package de.denkair.booking.repository;

import de.denkair.booking.domain.Airport;
import de.denkair.booking.domain.Booking;
import de.denkair.booking.domain.Customer;
import de.denkair.booking.domain.Flight;
import de.denkair.booking.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RepositoriesTest {

    @Autowired TestEntityManager em;
    @Autowired FlightRepository flightRepo;
    @Autowired AirportRepository airportRepo;
    @Autowired BookingRepository bookingRepo;
    @Autowired CustomerRepository customerRepo;
    @Autowired UserRepository userRepo;

    private Airport ham;
    private Airport pmi;
    private Flight f1;
    private Flight f2;
    private Customer customer;

    @BeforeEach
    void setUp() {
        ham = airport("HAM", "Hamburg");
        pmi = airport("PMI", "Palma");
        em.persist(ham);
        em.persist(pmi);

        f1 = flight("HA4021", ham, pmi, LocalDateTime.of(2026, 5, 1, 10, 0), true);
        f2 = flight("HA4022", ham, pmi, LocalDateTime.of(2026, 5, 1, 18, 0), true);
        Flight inactive = flight("HA9999", ham, pmi, LocalDateTime.of(2026, 5, 1, 12, 0), false);
        em.persist(f1);
        em.persist(f2);
        em.persist(inactive);

        customer = new Customer();
        customer.setFirstName("Max"); customer.setLastName("M"); customer.setEmail("max@e.de");
        em.persist(customer);
        em.flush();
    }

    private Airport airport(String iata, String name) {
        Airport a = new Airport();
        a.setIata(iata); a.setName(name); a.setCity(name); a.setCountry("DE");
        return a;
    }

    private Flight flight(String number, Airport o, Airport d, LocalDateTime dep, boolean aktiv) {
        Flight f = new Flight();
        f.setFlightNumber(number); f.setOrigin(o); f.setDestination(d);
        f.setDeparture(dep); f.setArrival(dep.plusHours(3));
        f.setPreis(new BigDecimal("199.00"));
        f.setSeatsAvailable(100); f.setAktiv(aktiv);
        return f;
    }

    @Test
    void airportFindByIata() {
        assertTrue(airportRepo.findByIata("HAM").isPresent());
        assertFalse(airportRepo.findByIata("XXX").isPresent());
    }

    @Test
    void flightSearchExcludesInactiveAndMatchesWindow() {
        List<Flight> hits = flightRepo.searchFlights("HAM", "PMI",
                LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(2026, 5, 1, 23, 59));
        assertEquals(2, hits.size());
        assertEquals("HA4021", hits.get(0).getFlightNumber()); // ordered by departure ASC
    }

    @Test
    void flightSearchOutsideWindowIsEmpty() {
        List<Flight> hits = flightRepo.searchFlights("HAM", "PMI",
                LocalDateTime.of(2027, 1, 1, 0, 0),
                LocalDateTime.of(2027, 1, 2, 0, 0));
        assertTrue(hits.isEmpty());
    }

    @Test
    void findByAktivTrueOrdersAscending() {
        List<Flight> all = flightRepo.findByAktivTrueOrderByDepartureAsc();
        assertEquals(2, all.size());
        assertEquals("HA4021", all.get(0).getFlightNumber());
    }

    @Test
    @SuppressWarnings("deprecation")
    void findeNachFlugnummerMatches() {
        List<Flight> hits = flightRepo.findeNachFlugnummer("HA4021");
        assertEquals(1, hits.size());
    }

    @Test
    void customerFindByEmail() {
        assertTrue(customerRepo.findByEmail("max@e.de").isPresent());
        assertFalse(customerRepo.findByEmail("nope@e.de").isPresent());
    }

    @Test
    void bookingRoundTripByReferenceCode() {
        Booking b = new Booking();
        b.setReferenceCode("HA-ABC12"); b.setFlight(f1); b.setCustomer(customer);
        b.setPassengers(2); b.setTotalPreis(new BigDecimal("400.00")); b.setStatus("CONFIRMED");
        em.persistAndFlush(b);

        Optional<Booking> back = bookingRepo.findByReferenceCode("HA-ABC12");
        assertTrue(back.isPresent());
        assertNotNull(back.get().getCreatedAt(), "createdAt should be set by @PrePersist");
    }

    @Test
    void bookingFindByCustomerVariants() {
        Booking b = new Booking();
        b.setReferenceCode("HA-QWERT"); b.setFlight(f1); b.setCustomer(customer);
        b.setPassengers(1); b.setTotalPreis(new BigDecimal("119.00")); b.setStatus("CONFIRMED");
        em.persistAndFlush(b);

        assertEquals(1, bookingRepo.findByCustomer(customer).size());
        assertEquals(1, bookingRepo.findAllByCustomer(customer).size());
        assertEquals(1, bookingRepo.findByCustomerOrderByCreatedAtDesc(customer).size());
    }

    @Test
    void userFindByUsername() {
        User u = new User();
        u.setUsername("admin"); u.setPasswordHash("x"); u.setRole("ROLE_ADMIN");
        em.persistAndFlush(u);

        assertTrue(userRepo.findByUsername("admin").isPresent());
        assertFalse(userRepo.findByUsername("ghost").isPresent());
    }
}
