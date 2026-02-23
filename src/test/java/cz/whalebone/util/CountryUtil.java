package cz.whalebone.util;

public final class CountryUtil {

    private CountryUtil() {}

    /**
     * Returns {@code true} if the last comma-separated segment of {@code birthPlace}
     * (trimmed, case-insensitive) matches {@code countryCode}.
     *
     * <p>Example: {@code endsWithCountryCode("Montreal, QC, CAN", "CAN") == true}</p>
     */
    public static boolean endsWithCountryCode(String birthPlace, String countryCode) {
        if (birthPlace == null || countryCode == null) return false;

        String s    = birthPlace.trim();
        String code = countryCode.trim().toUpperCase();
        if (s.isEmpty() || code.isEmpty()) return false;

        String[] parts = s.split(",");
        if (parts.length == 0) return false;

        String last = parts[parts.length - 1].trim().toUpperCase();
        return last.equals(code);
    }
}
