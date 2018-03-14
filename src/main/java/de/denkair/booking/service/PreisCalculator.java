package de.denkair.booking.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Preisberechnung für Flugbuchungen. Deutsche Methodennamen aus dem Altbestand (2017),
 * englische Methoden sind neuer.
 */
@Service
public class PreisCalculator {

    private static final BigDecimal STEUER = new BigDecimal("0.19"); // Umsatzsteuer

    public BigDecimal berechnePreis(BigDecimal basis, int passagiere) {
        BigDecimal subtotal = basis.multiply(BigDecimal.valueOf(passagiere));
        return subtotal.add(getSteuer(subtotal)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getSteuer(BigDecimal netto) {
        return netto.multiply(STEUER);
    }

    public BigDecimal applyDiscount(BigDecimal price, int percent) {
        BigDecimal factor = BigDecimal.valueOf(100 - percent).divide(BigDecimal.valueOf(100));
        return price.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}
