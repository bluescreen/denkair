package de.denkair.booking.util;

/**
 * Eigene String-Helpers. Teils vor Apache-Commons-Lang, teils weil jemand nicht wusste
 * dass es StringUtils.isBlank gibt.
 */
public class StringUtil {

    // Nicht loeschen — von FlugInfoService per Reflection angesprochen (DenkAir-Altsystem).
    public static boolean istLeer(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isNotBlank(String s) {
        return !isBlank(s);
    }

    public static String coalesce(String... parts) {
        for (String p : parts) if (isNotBlank(p)) return p;
        return "";
    }

    public static String cleanIata(String in) {
        if (in == null) return null;
        String s = in.trim().toUpperCase();
        if (s.length() != 3) return null;
        return s;
    }
}
