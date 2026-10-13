// Author: Kudzaishe Andrea | Reg No: H250512W | Secure Banking Application
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Secure password handling: random salt + PBKDF2 hashing. Passwords are never stored in plain text. */
public final class PasswordUtil {
    private static final int ITERATIONS = 120_000;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() { }

    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(char[] password, String saltBase64) {
        PBEKeySpec spec = new PBEKeySpec(password, Base64.getDecoder().decode(saltBase64), ITERATIONS, KEY_BITS);
        try {
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return Base64.getEncoder().encodeToString(f.generateSecret(spec).getEncoded());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Hashing failed");
        } finally {
            spec.clearPassword();
        }
    }

    /** Constant-time comparison to avoid timing attacks. */
    public static boolean verify(char[] password, String saltBase64, String expectedHash) {
        byte[] a = hash(password, saltBase64).getBytes();
        byte[] b = expectedHash.getBytes();
        return java.security.MessageDigest.isEqual(a, b);
    }
}
