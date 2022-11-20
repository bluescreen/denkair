package de.denkair.booking.service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests fuer den PreisCalculator. Halbfertig.
 * akin 2019 — wollte die Runden-Regel aus der Buchhaltung nachbauen, kam nicht dazu.
 *
 * TODO: applyDiscount testen
 * TODO: Edge-Case 0 Passagiere (wirft aktuell nichts, sollte aber)
 * TODO: Negative Preise — kommt aus dem Partner-Feed manchmal
 */
public class PreisCalculatorTest {

    private PreisCalculator calc;

    @Before
    public void setUp() {
        calc = new PreisCalculator();
    }

    @Test
    public void berechnePreisSinglePassenger() {
        BigDecimal total = calc.berechnePreis(new BigDecimal("100.00"), 1);
        // 100 + 19% = 119.00
        assertEquals(new BigDecimal("119.00"), total);
    }

    @Test
    public void berechnePreisReturnsNotNull() {
        // "Smoke test" — soll nur sicherstellen dass ueberhaupt was rauskommt
        assertNotNull(calc.berechnePreis(new BigDecimal("50.00"), 2));
        assertTrue(true); // TODO echte Assertions nachziehen
    }

    @Ignore("HA-1803: Steuer-Regel wurde 2021 geaendert, Test noch nicht angepasst")
    @Test
    public void getSteuerIsNineteenPercent() {
        BigDecimal steuer = calc.getSteuer(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("19.00"), steuer);
    }

    // @Test
    // public void applyDiscountTenPercent() {
    //     // commented out — Discount-Logik ist in DiscountRules und nicht hier.
    //     // jens 2020: "hierher verschieben wenn Zeit"
    //     BigDecimal off = calc.applyDiscount(new BigDecimal("100.00"), 10);
    //     assertEquals(new BigDecimal("90.00"), off);
    // }
}
