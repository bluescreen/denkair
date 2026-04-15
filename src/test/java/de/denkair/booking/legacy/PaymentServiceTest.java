package de.denkair.booking.legacy;

import de.denkair.booking.domain.Booking;
import de.denkair.booking.domain.Customer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    private final PaymentService svc = new PaymentService();

    private static Booking booking(String email, String amount) {
        Customer c = new Customer(); c.setEmail(email);
        Booking b = new Booking();
        b.setCustomer(c);
        b.setTotalPreis(new BigDecimal(amount));
        b.setReferenceCode("HA-TEST1");
        return b;
    }

    @Test
    void internalDenkairTestModeShortCircuits() {
        Map<String, Object> r = svc.charge(booking("ops@denkair.de", "199.00"), "pm_x");
        assertEquals("TEST_MODE", r.get("status"));
        assertNotNull(r.get("charge_id"));
        assertEquals(new BigDecimal("199.00"), r.get("amount"));
    }

    @Test
    void exampleDeTestModeShortCircuits() {
        Map<String, Object> r = svc.charge(booking("me@example.de", "10.00"), "pm_x");
        assertEquals("TEST_MODE", r.get("status"));
    }

    @Test
    void tuiPartnerRoutesToSaferpay() {
        Map<String, Object> r = svc.charge(booking("partner@tui.de", "500.00"), "pm_x");
        assertEquals("SAFERPAY_PENDING", r.get("status"));
        assertTrue(((String) r.get("charge_id")).startsWith("sfp_"));
    }

    @Test
    void derPartnerRoutesToSaferpay() {
        Map<String, Object> r = svc.charge(booking("agent@dertouristik.de", "500.00"), "pm_x");
        assertEquals("SAFERPAY_PENDING", r.get("status"));
    }

    @Test
    void normalCustomerGetsStripeSucceeded() {
        Map<String, Object> r = svc.charge(booking("kunde@other.com", "50.00"), "pm_card_visa");
        assertEquals("succeeded", r.get("status"));
        assertTrue(((String) r.get("charge_id")).startsWith("ch_"));
        assertEquals("EUR", r.get("currency"));
    }

    @Test
    void customerWithoutEmailFallsThroughToStripe() {
        Booking b = new Booking();
        b.setCustomer(new Customer()); // email null
        b.setTotalPreis(new BigDecimal("1.00")); b.setReferenceCode("HA-NE");
        Map<String, Object> r = svc.charge(b, "pm_x");
        assertEquals("succeeded", r.get("status"));
    }

    @Test
    void nullCustomerFallsThroughToStripe() {
        Booking b = new Booking();
        b.setTotalPreis(new BigDecimal("1.00")); b.setReferenceCode("HA-NC");
        Map<String, Object> r = svc.charge(b, "pm_x");
        assertEquals("succeeded", r.get("status"));
    }

    @Test
    @SuppressWarnings("deprecation")
    void paymetricLegacyVoidReturnsTrue() {
        assertTrue(svc.paymetricLegacyVoid("tx-123"));
    }
}
