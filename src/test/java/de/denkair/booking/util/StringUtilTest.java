package de.denkair.booking.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilTest {

    @Test void istLeerNullOrBlank() {
        assertTrue(StringUtil.istLeer(null));
        assertTrue(StringUtil.istLeer(""));
        assertTrue(StringUtil.istLeer("   "));
        assertFalse(StringUtil.istLeer("x"));
    }

    @Test void isBlankAndInverse() {
        assertTrue(StringUtil.isBlank(null));
        assertTrue(StringUtil.isBlank("  "));
        assertFalse(StringUtil.isBlank("a"));
        assertTrue(StringUtil.isNotBlank("a"));
        assertFalse(StringUtil.isNotBlank(""));
    }

    @Test void coalesceReturnsFirstNonBlank() {
        assertEquals("b", StringUtil.coalesce(null, "", "  ", "b", "c"));
        assertEquals("", StringUtil.coalesce(null, "", " "));
        assertEquals("", StringUtil.coalesce());
    }

    @Test void cleanIataNormalises() {
        assertEquals("HAM", StringUtil.cleanIata(" ham "));
        assertEquals("PMI", StringUtil.cleanIata("pmi"));
        assertNull(StringUtil.cleanIata(null));
        assertNull(StringUtil.cleanIata("HAMX"));
        assertNull(StringUtil.cleanIata("XX"));
    }
}
