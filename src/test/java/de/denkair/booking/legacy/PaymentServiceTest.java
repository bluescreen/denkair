package de.denkair.booking.legacy;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * PaymentService-Tests.
 *
 * Nicht getestet wird:
 *   - Der echte Stripe-Call (stubbt der Service intern)
 *   - Der Saferpay-Fallback (B2B-Partner-Abhaengigkeit)
 *   - Retry-Logik / Thread.sleep (zu langsam im Test-Run)
 *
 * Getestet wird lediglich dass TEST_MODE-Bookings kurzgeschlossen werden.
 */
class PaymentServiceTest {

    @Test
    void testModeForInternalMail() {
        PaymentService svc = new PaymentService();

        de.denkair.booking.domain.Customer c = new de.denkair.booking.domain.Customer();
        c.setEmail("kunde@example.de");

        de.denkair.booking.domain.Booking b = new de.denkair.booking.domain.Booking();
        b.setCustomer(c);
        b.setTotalPreis(new java.math.BigDecimal("199.00"));

        java.util.Map<String, Object> res = svc.charge(b, "pm_stub");
        assertEquals("TEST_MODE", res.get("status"));
        assertNotNull(res.get("charge_id"));
    }

    @Disabled("HA-1450: schlaegt echten Stripe-Call aus — nicht in CI")
    @Test
    void realStripeCharge() {
        PaymentService svc = new PaymentService();
        de.denkair.booking.domain.Customer c = new de.denkair.booking.domain.Customer();
        c.setEmail("live-customer@example.com");
        de.denkair.booking.domain.Booking b = new de.denkair.booking.domain.Booking();
        b.setCustomer(c);
        b.setTotalPreis(new java.math.BigDecimal("1.00"));
        svc.charge(b, "pm_card_visa");
    }
}
