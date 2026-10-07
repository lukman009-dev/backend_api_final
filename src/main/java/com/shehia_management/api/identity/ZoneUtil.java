package com.shehia_management.api.identity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * House numbers in this Shehia follow a fixed structural code:
 *
 *   SH/UW/A/123
 *   |  |  | |
 *   |  |  | +-- 3-digit house number on that zone (001-999, "000" not allowed)
 *   |  |  +---- zone letter (A, B, C, ...)
 *   |  +------- district code (fixed: UW)
 *   +---------- Shehia code (fixed: SH)
 *
 * Only the zone letter and the 3-digit number vary between residents; the
 * "SH/UW/" prefix is constant for this office. The zone letter doubles as
 * the unit staff are assigned to manage (see User.assignedZone).
 */
public final class ZoneUtil {

    public static final String PREFIX = "SH/UW/";

    // Zone letter (single A-Z) + 3 digit number that is not literally "000".
    public static final String HOUSE_NUMBER_REGEX = "^SH/UW/([A-Z])/(?!000)([0-9]{3})$";

    private static final Pattern PATTERN = Pattern.compile(HOUSE_NUMBER_REGEX);

    private ZoneUtil() {
    }

    public static boolean isValid(String houseNumber) {
        return houseNumber != null && PATTERN.matcher(houseNumber.trim().toUpperCase()).matches();
    }

    public static String normalize(String houseNumber) {
        return houseNumber == null ? null : houseNumber.trim().toUpperCase();
    }

    /** Extracts the zone letter (e.g. "A") from a valid house number, or null if it doesn't match the format. */
    public static String extractZone(String houseNumber) {
        if (houseNumber == null) return null;
        Matcher m = PATTERN.matcher(houseNumber.trim().toUpperCase());
        return m.matches() ? m.group(1) : null;
    }

    /** Extracts the 3-digit number (e.g. "123") from a valid house number, or null if it doesn't match the format. */
    public static String extractNumber(String houseNumber) {
        if (houseNumber == null) return null;
        Matcher m = PATTERN.matcher(houseNumber.trim().toUpperCase());
        return m.matches() ? m.group(2) : null;
    }

    public static String buildHouseNumber(String zone, String number) {
        return PREFIX + zone.trim().toUpperCase() + "/" + number.trim();
    }

    /** Prefix used to query/filter all residents belonging to a given zone, e.g. "SH/UW/A/". */
    public static String zonePrefix(String zone) {
        return PREFIX + zone.trim().toUpperCase() + "/";
    }

    public static boolean isValidZoneLetter(String zone) {
        return zone != null && zone.trim().matches("^[A-Za-z]$");
    }
}
