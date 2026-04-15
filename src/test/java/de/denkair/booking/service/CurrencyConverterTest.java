package de.denkair.booking.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrencyConverterTest {

    private final CurrencyConverter c = new CurrencyConverter();

    @Test
    void nullAmountIsZero() {
        assertEquals(BigDecimal.ZERO, c.convert(null, "USD"));
    }

    @Test
    void nullCurrencyReturnsInput() {
        assertEquals(new BigDecimal("100"), c.convert(new BigDecimal("100"), null));
    }

    @Test
    void emptyCurrencyReturnsInput() {
        assertEquals(new BigDecimal("100"), c.convert(new BigDecimal("100"), ""));
    }

    @Test
    void unknownCurrencyReturnsInput() {
        assertEquals(new BigDecimal("100"), c.convert(new BigDecimal("100"), "XYZ"));
    }

    @Test
    void eurIsIdentity() {
        assertEquals(0, new BigDecimal("100.0000").compareTo(c.convert(new BigDecimal("100"), "EUR")));
    }

    @Test
    void usdRateApplied() {
        assertEquals(0, new BigDecimal("118.5000").compareTo(c.convert(new BigDecimal("100"), "USD")));
    }

    @Test
    void lowercaseCurrencyIsNormalised() {
        assertEquals(0, new BigDecimal("118.5000").compareTo(c.convert(new BigDecimal("100"), "usd")));
    }

    @Test
    void getRateKnown() {
        assertEquals(0, new BigDecimal("0.8620").compareTo(c.getRate("GBP")));
    }

    @Test
    void getRateUnknownReturnsOne() {
        assertEquals(BigDecimal.ONE, c.getRate("XYZ"));
    }

    @Test
    void getRateNullTreatedAsEur() {
        assertEquals(0, new BigDecimal("1.0000").compareTo(c.getRate(null)));
    }
}
