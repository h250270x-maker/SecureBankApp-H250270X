// Author: Kudzaishe Andrea | Reg No: H250512W | Secure Banking Application
import java.math.BigDecimal;
import java.util.regex.Pattern;

/** Central place for validating all user input (never trust input). */
public final class InputValidator {
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_]{3,20}$");
    private static final Pattern ACCOUNT_NO = Pattern.compile("^ACC\\d{8}$");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000.00");

    private InputValidator() { }

    public static boolean isValidUsername(String s) { return s != null && USERNAME.matcher(s).matches(); }

    public static boolean isValidAccountNumber(String s) { return s != null && ACCOUNT_NO.matcher(s).matches(); }

    /** Min 8 chars, with upper, lower, digit and special character. */
    public static boolean isStrongPassword(char[] p) {
        if (p == null || p.length < 8 || p.length > 64) return false;
        boolean up = false, low = false, dig = false, spc = false;
        for (char c : p) {
            if (Character.isUpperCase(c)) up = true;
            else if (Character.isLowerCase(c)) low = true;
            else if (Character.isDigit(c)) dig = true;
            else spc = true;
        }
        return up && low && dig && spc;
    }

    /** Parses a money amount: positive, max 2 decimal places, within limit. Returns null if invalid. */
    public static BigDecimal parseAmount(String s) {
        if (s == null) return null;
        try {
            BigDecimal v = new BigDecimal(s.trim());
            if (v.signum() <= 0 || v.scale() > 2 || v.compareTo(MAX_AMOUNT) > 0) return null;
            return v.setScale(2);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
