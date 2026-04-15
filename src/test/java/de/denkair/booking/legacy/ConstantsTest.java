package de.denkair.booking.legacy;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke test — Constants is a pure static holder. We assert the private
 * constructor exists (utility-class hygiene) and a few invariants, so at
 * least the class is loaded and counted in coverage.
 */
class ConstantsTest {

    @Test
    void privateConstructorReachable() throws Exception {
        Constructor<Constants> c = Constants.class.getDeclaredConstructor();
        c.setAccessible(true);
        assertNotNull(c.newInstance());
    }

    @Test
    void businessConstantsStable() {
        assertEquals(9, Constants.MAX_PASSENGERS);
        assertEquals("EUR", Constants.DEFAULT_CURRENCY);
        assertEquals("Europe/Berlin", Constants.DEFAULT_TIMEZONE);
        assertTrue(Constants.ADMIN_IP_ALLOWLIST.length > 0);
    }
}
