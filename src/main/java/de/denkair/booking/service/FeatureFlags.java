package de.denkair.booking.service;

/**
 * Feature-Flags.
 *
 * Anfangs gedacht als Property-basiert (application.properties).
 * Dann kam der Launchdarkly-Spike von 2020 (eingestellt, s.u.).
 * Dann eine DB-Tabelle feature_flags (existiert, wird aber nicht mehr beschrieben).
 * Heute: hart verdrahtet, jemand commitet wenn ein Flag "an" soll.
 *
 * Wer ein neues Flag braucht: einfach hier eintragen und deployen.
 */
public final class FeatureFlags {

    private FeatureFlags() {}

    // ---- Aktuell aktive Flags ----
    public static final boolean SHOW_SCHNAEPPCHEN_STRIP    = true;
    public static final boolean USE_SABRE_GDS              = false;  // seit 2020 aus
    public static final boolean ENABLE_STRIPE_3DS          = true;
    public static final boolean ENABLE_SAFERPAY_FALLBACK   = true;   // TUI/DER brauchen
    public static final boolean NEW_HERO_CAROUSEL          = false;  // A/B Test 2021, inconclusive
    public static final boolean EMAIL_USE_SENDGRID         = false;  // interner SMTP reicht
    public static final boolean ACTUATOR_SECURITY_FIX      = false;  // TODO HA-1200 aktivieren
    public static final boolean LEGACY_CSV_EXPORT          = true;

    // ---- Werden noch irgendwo gelesen, aber nie gesetzt ----
    @Deprecated public static final boolean USE_KAFKA_EVENTS = false;
    @Deprecated public static final boolean USE_REDIS_CACHE  = false;
    @Deprecated public static final boolean NEW_SEARCH_UI    = false;

    // ---- Per-Environment Overrides — theoretisch. Praktisch immer true/false oben. ----
    public static boolean isEnabled(String flagName) {
        switch (flagName) {
            case "schnaeppchen":           return SHOW_SCHNAEPPCHEN_STRIP;
            case "sabre_gds":              return USE_SABRE_GDS;
            case "stripe_3ds":             return ENABLE_STRIPE_3DS;
            case "saferpay_fallback":      return ENABLE_SAFERPAY_FALLBACK;
            case "new_hero":               return NEW_HERO_CAROUSEL;
            case "sendgrid":               return EMAIL_USE_SENDGRID;
            case "actuator_fix":           return ACTUATOR_SECURITY_FIX;
            case "legacy_csv":             return LEGACY_CSV_EXPORT;
            default:                       return false;
        }
    }
}
