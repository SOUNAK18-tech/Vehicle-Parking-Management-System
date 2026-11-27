import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * =========================================================================================
 * VEHICLE NUMBER FORMATTER & VALIDATOR (VehicleNumberFormatter.java)
 * =========================================================================================
 * Implements strict, robust Indian vehicle registration number live-formatting and validation:
 *  - ZERO spaces
 *  - ZERO hyphens
 *  - ZERO special characters
 *  - Pure uppercase alphanumeric string adhering to the official 4-part Indian RTO standard:
 *
 * Structure Breakdown:
 *  Part 1: State Code (Two Letters)
 *    Indicates the State or Union Territory (e.g. WB, MH, DL, KA, TN, UP, GJ, HR).
 *    Validated against all official 36 Indian State and Union Territory codes.
 *
 *  Part 2: RTO District Code (Two Digits)
 *    Two-digit number representing the specific RTO/district (e.g. 01, 02, 12).
 *
 *  Part 3: Series Code (Zero to Three Letters)
 *    0 to 3 letters indicating the registration series (e.g. A, AB, BA, CAB).
 *    Letters 'I' and 'O' are strictly omitted to avoid confusion with digits 1 and 0.
 *
 *  Part 4: Unique Number (Four Digits)
 *    Sequential number from 0001 to 9999 assigned to the vehicle (with leading zeros).
 *    '0000' is strictly rejected.
 * =========================================================================================
 */
public class VehicleNumberFormatter {

    // Valid official 2-letter State & Union Territory codes of India
    public static final Set<String> INDIAN_STATE_CODES = new HashSet<>(Arrays.asList(
            "AN", "AP", "AR", "AS", "BR", "CG", "CH", "CT", "DD", "DL", "DN", "GA", "GJ",
            "HP", "HR", "JH", "JK", "KA", "KL", "LA", "LD", "MH", "ML", "MN", "MP", "MZ",
            "NL", "OD", "OR", "PB", "PY", "RJ", "SK", "TG", "TN", "TR", "TS", "UA", "UK",
            "UP", "WB"
    ));

    /**
     * Sanitizes and formats any raw input into standard Indian vehicle number format
     * without any spaces, hyphens, or special characters.
     * All alphabets are automatically converted to uppercase.
     */
    public static String format(String input) {
        if (input == null) return "";
        String clean = input.toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (clean.isEmpty()) return "";

        // 1. Check for Bharat (BH) Series: begins with 2 digits followed by 'BH'
        if (clean.matches("^\\d{2}BH.*")) {
            StringBuilder sb = new StringBuilder();
            sb.append(clean.substring(0, 2)); // 2-digit Year
            sb.append("BH");
            String rem = clean.substring(4);

            // 4-digit unique number
            int i = 0;
            while (i < rem.length() && Character.isDigit(rem.charAt(i)) && i < 4) i++;
            sb.append(rem.substring(0, i));
            rem = rem.substring(i);

            // 1-2 series letters
            int j = 0;
            while (j < rem.length() && Character.isLetter(rem.charAt(j)) && j < 2) {
                sb.append(rem.charAt(j++));
            }
            return sb.toString();
        }

        // 2. Standard State Series: SS DD [XXX] NNNN (Strictly No Spaces, No Hyphens)
        int idx = 0;
        StringBuilder sb = new StringBuilder();

        // Part 1: State Code (strictly up to 2 letters)
        while (idx < clean.length() && Character.isLetter(clean.charAt(idx)) && sb.length() < 2) {
            sb.append(clean.charAt(idx++));
        }
        if (sb.length() < 2) {
            return sb.toString();
        }

        // Part 2: RTO District Code (strictly up to 2 digits)
        int rtoCount = 0;
        while (idx < clean.length() && Character.isDigit(clean.charAt(idx)) && rtoCount < 2) {
            sb.append(clean.charAt(idx++));
            rtoCount++;
        }
        if (rtoCount < 2) {
            return sb.toString();
        }

        // Part 3: Series Code (0 to 3 letters)
        int seriesCount = 0;
        while (idx < clean.length() && Character.isLetter(clean.charAt(idx)) && seriesCount < 3) {
            sb.append(clean.charAt(idx++));
            seriesCount++;
        }

        // Part 4: Unique Registration Number (strictly up to 4 digits)
        int numCount = 0;
        while (idx < clean.length() && Character.isDigit(clean.charAt(idx)) && numCount < 4) {
            sb.append(clean.charAt(idx++));
            numCount++;
        }

        return sb.toString();
    }

    /**
     * Ensures the final unique registration number has 4 digits with leading zeros (e.g. 0001 to 9999).
     * No spaces or hyphens are present in the output.
     */
    public static String padRegistrationNumber(String input) {
        if (input == null || input.isEmpty()) return "";
        String s = format(input);
        if (s.length() < 5) return s;

        // Prefix is 2 letters state + 2 digits RTO
        String prefix = s.substring(0, 4);
        String rem = s.substring(4);

        int i = 0;
        while (i < rem.length() && Character.isLetter(rem.charAt(i))) {
            i++;
        }
        String series = rem.substring(0, i);
        String num = rem.substring(i);

        if (!num.isEmpty() && num.length() < 4) {
            try {
                num = String.format("%04d", Integer.parseInt(num));
            } catch (NumberFormatException ignored) {}
        }
        return prefix + series + num;
    }

    /**
     * Validates whether a vehicle registration number is complete, authentic, and well-formed.
     * Returns null if valid, or a clear, actionable error message if invalid.
     */
    public static String getValidationError(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "Please enter a valid Vehicle Registration Number.";
        }

        String rawClean = input.toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (rawClean.length() < 4) {
            return "Incomplete vehicle number. Expected standard Indian vehicle number (e.g. WB02AB1234).";
        }

        String formatted = padRegistrationNumber(rawClean);

        // 1. Check Bharat (BH) Series format: YYBHNNNNXX
        if (formatted.matches("^\\d{2}BH\\d{4}[A-Z]{1,2}$")) {
            String numStr = formatted.substring(4, 8);
            int num = Integer.parseInt(numStr);
            if (num < 1 || num > 9999) {
                return "Unique number must be between 0001 and 9999 ('0000' is not valid).";
            }
            String bhSeries = formatted.substring(8);
            if (bhSeries.contains("I") || bhSeries.contains("O")) {
                return "Invalid Series Code '" + bhSeries + "': Letters 'I' and 'O' are not permitted.";
            }
            return null; // Valid Bharat Series
        }

        // 2. Standard Indian State Series: SS DD [XXX] NNNN (No spaces, No hyphens)
        if (!formatted.matches("^[A-Z]{2}\\d{2}[A-Z]{0,3}\\d{4}$")) {
            return "Incomplete or invalid vehicle number!\n\n"
                    + "Expected Standard Indian Format (No spaces, hyphens, or symbols):\n"
                    + "  • Part 1: State Code (Two Letters, e.g. WB, MH, DL, KA)\n"
                    + "  • Part 2: RTO Code (Two Digits, e.g. 02, 12, 01)\n"
                    + "  • Part 3: Series Code (0 to 3 Letters, e.g. AB, C - no 'I'/'O')\n"
                    + "  • Part 4: Unique Number (Four Digits, e.g. 0001 to 9999)\n\n"
                    + "Examples: WB02AB1234  |  DL01C0001  |  MH121234  |  KA05MC5566";
        }

        // Validate Part 1: State Code
        String stateCode = formatted.substring(0, 2);
        if (!INDIAN_STATE_CODES.contains(stateCode)) {
            return "State code '" + stateCode + "' is not a recognized Indian State or UT code.\n\n"
                    + "Recognized examples: WB (West Bengal), MH (Maharashtra), DL (Delhi), KA (Karnataka),\n"
                    + "TN (Tamil Nadu), UP (Uttar Pradesh), GJ (Gujarat), HR (Haryana), etc.";
        }

        // Validate Part 3: Series Code (must not contain I or O)
        String series = formatted.substring(4, formatted.length() - 4);
        if (series.contains("I") || series.contains("O")) {
            return "Invalid Series Code '" + series + "': In Indian vehicle registrations, letters 'I' and 'O'\n"
                    + "are omitted to avoid confusion with digits '1' and '0'.";
        }

        // Validate Part 4: Unique Number (0001 to 9999, 0000 is rejected)
        String numStr = formatted.substring(formatted.length() - 4);
        try {
            int num = Integer.parseInt(numStr);
            if (num < 1 || num > 9999) {
                return "Unique number must be between 0001 and 9999 ('0000' is not valid).";
            }
        } catch (NumberFormatException e) {
            return "Invalid Unique Number: " + numStr;
        }

        return null; // 100% Valid!
    }

    /**
     * Quick boolean check for validity.
     */
    public static boolean isValid(String input) {
        return getValidationError(input) == null;
    }

    /**
     * Counts alphanumeric characters in a string.
     */
    public static int countAlphanumeric(String s) {
        if (s == null) return 0;
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLetterOrDigit(s.charAt(i))) count++;
        }
        return count;
    }
}
