// Author: Kudzaishe Andrea | Reg No: H250512W | Secure Banking Application
/** A bank customer. Only the salted hash of the password is kept. */
public class User {
    private final String username;
    private final String salt;
    private final String passwordHash;
    private int failedAttempts;
    private boolean locked;

    public User(String username, String salt, String passwordHash, int failedAttempts, boolean locked) {
        this.username = username;
        this.salt = salt;
        this.passwordHash = passwordHash;
        this.failedAttempts = failedAttempts;
        this.locked = locked;
    }

    public String getUsername() { return username; }
    public String getSalt() { return salt; }
    public String getPasswordHash() { return passwordHash; }
    public int getFailedAttempts() { return failedAttempts; }
    public boolean isLocked() { return locked; }

    public void recordFailure(int maxAttempts) {
        failedAttempts++;
        if (failedAttempts >= maxAttempts) locked = true;
    }

    public void resetFailures() { failedAttempts = 0; }

    public String toFileLine() {
        return username + "|" + salt + "|" + passwordHash + "|" + failedAttempts + "|" + locked;
    }

    public static User fromFileLine(String line) {
        String[] p = line.split("\\|");
        if (p.length != 5) throw new IllegalArgumentException("Corrupt user record");
        return new User(p[0], p[1], p[2], Integer.parseInt(p[3]), Boolean.parseBoolean(p[4]));
    }
}
