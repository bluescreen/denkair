package de.denkair.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PreisCalculatorTest {

    private PreisCalculator calc;

    @BeforeEach
    void setUp() {
        calc = new PreisCalculator();
    }

    @Test
    void berechnePreisSinglePassenger() {
        assertEquals(new BigDecimal("119.00"), calc.berechnePreis(new BigDecimal("100.00"), 1));
    }

    @Test
    void berechnePreisMultiplePassengers() {
        // 100 * 3 = 300 net + 19% = 357.00
        assertEquals(new BigDecimal("357.00"), calc.berechnePreis(new BigDecimal("100.00"), 3));
    }

    @Test
    void berechnePreisZeroPassengersIsZero() {
        // Documents current (buggy) behaviour: zero passengers => zero price, no exception.
        assertEquals(new BigDecimal("0.00"), calc.berechnePreis(new BigDecimal("100.00"), 0));
    }

    @Test
    void getSteuerIsNineteenPercent() {
        assertEquals(0, new BigDecimal("19.0000").compareTo(calc.getSteuer(new BigDecimal("100.00"))));
    }

    @Test
    void applyDiscountTenPercent() {
        // 100 * (100-10)/100 = 90
        assertEquals(0, new BigDecimal("90.00").compareTo(calc.applyDiscount(new BigDecimal("100.00"), 10)));
    }

    @Test
    void applyDiscountZeroUnchanged() {
        assertEquals(0, new BigDecimal("100.00").compareTo(calc.applyDiscount(new BigDecimal("100.00"), 0)));
    }

    @Test
    void applyDiscountHundredGivesZero() {
        assertEquals(0, BigDecimal.ZERO.compareTo(calc.applyDiscount(new BigDecimal("100.00"), 100)));
    }
}
