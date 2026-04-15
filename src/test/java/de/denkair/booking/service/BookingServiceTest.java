package de.denkair.booking.service;

import de.denkair.booking.domain.Airport;
import de.denkair.booking.domain.Booking;
import de.denkair.booking.domain.Customer;
import de.denkair.booking.domain.Flight;
import de.denkair.booking.dto.BookingForm;
import de.denkair.booking.legacy.PaymentService;
import de.denkair.booking.legacy.SabreGdsClient;
import de.denkair.booking.legacy.SapConnector;
import de.denkair.booking.repository.BookingRepository;
import de.denkair.booking.repository.CustomerRepository;
import de.denkair.booking.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private FlightRepository flightRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private PreisCalculator preisCalculator;
    @Mock private MailService mailService;
    @Mock private DiscountRules discountRules;
    @Mock private PaymentService paymentService;
    @Mock private SapConnector sapConnector;
    @Mock private SabreGdsClient sabre;

    @InjectMocks private BookingService service;

    private Flight flight;
    private BookingForm form;

    @BeforeEach
    void setUp() {
        Airport ham = new Airport(); ham.setIata("HAM");
        Airport pmi = new Airport(); pmi.setIata("PMI");
        flight = new Flight();
        flight.setId(1L);
        flight.setFlightNumber("HA4021");
        flight.setOrigin(ham);
        flight.setDestination(pmi);
        flight.setDeparture(LocalDateTime.now().plusDays(10));
        flight.setPreis(new BigDecimal("100.00"));
        flight.setSeatsAvailable(10);

        form = new BookingForm();
        form.setFlightId(1L);
        form.setFirstName("Max");
        form.setLastName("Mustermann");
        form.setEmail("max@example.de");
        form.setPhone("+49 1234");
        form.setPassengers(2);
    }

    private Map<String,Object> ok() {
        Map<String,Object> r = new HashMap<>();
        r.put("status", "succeeded");
        return r;
    }

    @Test
    void happyPathReturnsConfirmedBooking() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(customerRepository.findByEmail("max@example.de")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(preisCalculator.berechnePreis(any(), anyInt())).thenReturn(new BigDecimal("238.00"));
        when(discountRules.calcDiscountPercent(any())).thenReturn(BigDecimal.ZERO);
        when(discountRules.apply(any(), any())).thenReturn(new BigDecimal("238.00"));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentService.charge(any(), anyString())).thenReturn(ok());

        Booking b = service.createBooking(form);

        assertEquals("CONFIRMED", b.getStatus());
        assertEquals(2, b.getPassengers());
        assertTrue(b.getReferenceCode().startsWith("HA-"));
        assertEquals(new BigDecimal("238.00"), b.getTotalPreis());
        verify(mailService).sendBookingConfirmation(b);
        verify(sapConnector).postBooking(b);
        verify(sabre).pushInventory("HA4021", 8);
        assertEquals(8, flight.getSeatsAvailable());
    }

    @Test
    void reusesExistingCustomer() {
        Customer existing = new Customer();
        existing.setId(99L); existing.setEmail("max@example.de");
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(customerRepository.findByEmail("max@example.de")).thenReturn(Optional.of(existing));
        when(preisCalculator.berechnePreis(any(), anyInt())).thenReturn(new BigDecimal("238.00"));
        when(discountRules.calcDiscountPercent(any())).thenReturn(BigDecimal.ZERO);
        when(discountRules.apply(any(), any())).thenReturn(new BigDecimal("238.00"));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentService.charge(any(), anyString())).thenReturn(ok());

        Booking b = service.createBooking(form);

        assertSame(existing, b.getCustomer());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void unknownFlightThrows() {
        when(flightRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.createBooking(form));
    }

    @Test
    void seatShortageThrows() {
        flight.setSeatsAvailable(1);
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        assertThrows(IllegalStateException.class, () -> service.createBooking(form));
    }

    @Test
    void paymentFailureMarksCancelled() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(preisCalculator.berechnePreis(any(), anyInt())).thenReturn(new BigDecimal("200.00"));
        when(discountRules.calcDiscountPercent(any())).thenReturn(BigDecimal.ZERO);
        when(discountRules.apply(any(), any())).thenReturn(new BigDecimal("200.00"));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        Map<String,Object> fail = new HashMap<>();
        fail.put("status", "failed");
        when(paymentService.charge(any(), anyString())).thenReturn(fail);

        Booking b = service.createBooking(form);

        assertEquals("CANCELLED", b.getStatus());
        verify(mailService, never()).sendBookingConfirmation(any());
        verify(sapConnector, never()).postBooking(any());
    }

    @Test
    void testModeStatusPassesThrough() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(preisCalculator.berechnePreis(any(), anyInt())).thenReturn(new BigDecimal("100.00"));
        when(discountRules.calcDiscountPercent(any())).thenReturn(BigDecimal.ZERO);
        when(discountRules.apply(any(), any())).thenReturn(new BigDecimal("100.00"));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        Map<String,Object> testMode = new HashMap<>();
        testMode.put("status", "TEST_MODE");
        when(paymentService.charge(any(), anyString())).thenReturn(testMode);

        Booking b = service.createBooking(form);

        assertEquals("CONFIRMED", b.getStatus());
    }

    @Test
    void saferpayPendingPassesThrough() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(preisCalculator.berechnePreis(any(), anyInt())).thenReturn(new BigDecimal("500.00"));
        when(discountRules.calcDiscountPercent(any())).thenReturn(BigDecimal.ZERO);
        when(discountRules.apply(any(), any())).thenReturn(new BigDecimal("500.00"));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        Map<String,Object> sfp = new HashMap<>();
        sfp.put("status", "SAFERPAY_PENDING");
        when(paymentService.charge(any(), anyString())).thenReturn(sfp);

        Booking b = service.createBooking(form);
        assertEquals("CONFIRMED", b.getStatus());
    }

    @Test
    void sapFailureDoesNotFailBooking() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(preisCalculator.berechnePreis(any(), anyInt())).thenReturn(new BigDecimal("100.00"));
        when(discountRules.calcDiscountPercent(any())).thenReturn(BigDecimal.ZERO);
        when(discountRules.apply(any(), any())).thenReturn(new BigDecimal("100.00"));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentService.charge(any(), anyString())).thenReturn(ok());
        doThrow(new RuntimeException("sap down")).when(sapConnector).postBooking(any());

        Booking b = service.createBooking(form);
        assertEquals("CONFIRMED", b.getStatus());
    }

    @Test
    void mailFailureDoesNotFailBooking() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(preisCalculator.berechnePreis(any(), anyInt())).thenReturn(new BigDecimal("100.00"));
        when(discountRules.calcDiscountPercent(any())).thenReturn(BigDecimal.ZERO);
        when(discountRules.apply(any(), any())).thenReturn(new BigDecimal("100.00"));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentService.charge(any(), anyString())).thenReturn(ok());
        doThrow(new RuntimeException("smtp down")).when(mailService).sendBookingConfirmation(any());

        Booking b = service.createBooking(form);
        assertEquals("CONFIRMED", b.getStatus());
    }

    @Test
    void paymentExceptionIsSwallowed() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(preisCalculator.berechnePreis(any(), anyInt())).thenReturn(new BigDecimal("100.00"));
        when(discountRules.calcDiscountPercent(any())).thenReturn(BigDecimal.ZERO);
        when(discountRules.apply(any(), any())).thenReturn(new BigDecimal("100.00"));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentService.charge(any(), anyString())).thenThrow(new RuntimeException("boom"));

        Booking b = service.createBooking(form);
        assertEquals("CONFIRMED", b.getStatus());
    }

    @Test
    void referenceCodeMatchesExpectedAlphabet() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(preisCalculator.berechnePreis(any(), anyInt())).thenReturn(new BigDecimal("100.00"));
        when(discountRules.calcDiscountPercent(any())).thenReturn(BigDecimal.ZERO);
        when(discountRules.apply(any(), any())).thenReturn(new BigDecimal("100.00"));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentService.charge(any(), anyString())).thenReturn(ok());

        Booking b = service.createBooking(form);
        assertTrue(b.getReferenceCode().matches("HA-[A-Z2-9]{5}"),
                "reference code should match HA-[A-Z2-9]{5} but was " + b.getReferenceCode());
    }
}
