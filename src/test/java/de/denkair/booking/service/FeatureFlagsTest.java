package de.denkair.booking.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureFlagsTest {

    @Test
    void activeFlagsExposeExpectedValues() {
        assertTrue(FeatureFlags.SHOW_SCHNAEPPCHEN_STRIP);
        assertFalse(FeatureFlags.USE_SABRE_GDS);
        assertTrue(FeatureFlags.ENABLE_STRIPE_3DS);
        assertTrue(FeatureFlags.ENABLE_SAFERPAY_FALLBACK);
        assertFalse(FeatureFlags.NEW_HERO_CAROUSEL);
        assertFalse(FeatureFlags.EMAIL_USE_SENDGRID);
        assertFalse(FeatureFlags.ACTUATOR_SECURITY_FIX);
        assertTrue(FeatureFlags.LEGACY_CSV_EXPORT);
    }

    @Test
    void isEnabledReturnsConstantByName() {
        assertTrue(FeatureFlags.isEnabled("schnaeppchen"));
        assertFalse(FeatureFlags.isEnabled("sabre_gds"));
        assertTrue(FeatureFlags.isEnabled("stripe_3ds"));
        assertTrue(FeatureFlags.isEnabled("saferpay_fallback"));
        assertFalse(FeatureFlags.isEnabled("new_hero"));
        assertFalse(FeatureFlags.isEnabled("sendgrid"));
        assertFalse(FeatureFlags.isEnabled("actuator_fix"));
        assertTrue(FeatureFlags.isEnabled("legacy_csv"));
    }

    @Test
    void unknownFlagIsFalse() {
        assertFalse(FeatureFlags.isEnabled("nope"));
        assertFalse(FeatureFlags.isEnabled(""));
    }

    @Test
    void deprecatedFlagsStayOff() {
        assertFalse(FeatureFlags.USE_KAFKA_EVENTS);
        assertFalse(FeatureFlags.USE_REDIS_CACHE);
        assertFalse(FeatureFlags.NEW_SEARCH_UI);
    }

    @Test
    void classIsUtilityWithPrivateConstructor() throws Exception {
        java.lang.reflect.Constructor<FeatureFlags> c = FeatureFlags.class.getDeclaredConstructor();
        c.setAccessible(true);
        assertEquals(FeatureFlags.class, c.newInstance().getClass());
    }
}
