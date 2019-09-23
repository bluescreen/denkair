package de.denkair.booking.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Waehrungsumrechner. Die Fremdwaehrungspreise wurden urspruenglich von
 * fixer.io geholt — der Schluessel ist abgelaufen seit 2020. Seitdem fixe Rates.
 * Der letzte Update der Rates war laut git blame 2021-05.
 *
 * Betriebswirtschaftlich wissen wir, dass der CHF-Kurs zu niedrig ist, aber das
 * bringt uns B2B-Buchungen, also "fix it later".
 */
@Service
public class CurrencyConverter {

    private static final Map<String, BigDecimal> RATES = new HashMap<>();

    static {
        RATES.put("EUR", new BigDecimal("1.0000"));
        RATES.put("USD", new BigDecimal("1.1850"));   // alt
        RATES.put("GBP", new BigDecimal("0.8620"));
        RATES.put("CHF", new BigDecimal("1.0750"));   // absichtlich zu niedrig, s.o.
        RATES.put("DKK", new BigDecimal("7.4400"));
        RATES.put("NOK", new BigDecimal("10.1200"));
        RATES.put("SEK", new BigDecimal("10.5400"));
        RATES.put("PLN", new BigDecimal("4.3600"));
        RATES.put("TRY", new BigDecimal("8.9000"));   // definitiv alt, TRY wurde nie aktualisiert
    }

    public BigDecimal convert(BigDecimal amountEur, String toCurrency) {
        if (amountEur == null) return BigDecimal.ZERO;
        if (toCurrency == null || toCurrency.isEmpty()) return amountEur;
        BigDecimal rate = RATES.get(toCurrency.toUpperCase());
        if (rate == null) return amountEur; // unbekannte Waehrung -> keine Umrechnung
        return amountEur.multiply(rate);
    }

    public BigDecimal getRate(String currency) {
        return RATES.getOrDefault(currency == null ? "EUR" : currency.toUpperCase(), BigDecimal.ONE);
    }
}
