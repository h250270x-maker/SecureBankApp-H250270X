// Author: Kudzaishe Andrea | Reg No: H250512W | Secure Banking Application
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

/** Business logic: authentication, accounts, transactions, and saving/loading data. */
public class Bank {
    public static final int MAX_FAILED_ATTEMPTS = 3;
    private static final String USERS = "users.txt";
    private static final String ACCOUNTS = "accounts.txt";
    private static final String TRANSACTIONS = "transactions.txt";

    private final FileStorage storage;
    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Account> accounts = new LinkedHashMap<>();
    private final List<Transaction> transactions = new ArrayList<>();
    private final SecureRandom random = new SecureRandom();

    public Bank(FileStorage storage) {
        this.storage = storage;
        load();
    }

    // ---------- persistence ----------
    private void load() {
        for (String l : storage.readLines(USERS)) {
            try { User u = User.fromFileLine(l); users.put(u.getUsername(), u); }
            catch (RuntimeException e) { System.out.println("Skipped a corrupt user record."); }
        }
        for (String l : storage.readLines(ACCOUNTS)) {
            try { Account a = Account.fromFileLine(l); accounts.put(a.getAccountNumber(), a); }
            catch (RuntimeException e) { System.out.println("Skipped a corrupt account record."); }
        }
        for (String l : storage.readLines(TRANSACTIONS)) {
            try { transactions.add(Transaction.fromFileLine(l)); }
            catch (RuntimeException e) { System.out.println("Skipped a corrupt transaction record."); }
        }
    }

    private void saveUsers() {
        List<String> l = new ArrayList<>();
        for (User u : users.values()) l.add(u.toFileLine());
        storage.writeLines(USERS, l);
    }

    private void saveAccounts() {
        List<String> l = new ArrayList<>();
        for (Account a : accounts.values()) l.add(a.toFileLine());
        storage.writeLines(ACCOUNTS, l);
    }

    private void saveTransactions() {
        List<String> l = new ArrayList<>();
        for (Transaction t : transactions) l.add(t.toFileLine());
        storage.writeLines(TRANSACTIONS, l);
    }

    // ---------- authentication ----------
    /** Returns an error message, or null on success. */
    public String register(String username, char[] password) {
        if (!InputValidator.isValidUsername(username))
            return "Username must be 3-20 characters: letters, digits or underscore.";
        if (!InputValidator.isStrongPassword(password))
            return "Password must be 8+ chars with upper, lower, digit and special character.";
        if (users.containsKey(username)) return "Username is not available.";
        String salt = PasswordUtil.generateSalt();
        users.put(username, new User(username, salt, PasswordUtil.hash(password, salt), 0, false));
        saveUsers();
        return null;
    }

    /** Returns the logged-in username, or null. Generic error messages avoid revealing which part was wrong. */
    public String login(String username, char[] password) {
        User u = users.get(username);
        if (u == null) {
            // hash anyway so response time does not reveal whether the username exists
            PasswordUtil.hash(password, PasswordUtil.generateSalt());
            return null;
        }
        if (u.isLocked()) return null;
        if (PasswordUtil.verify(password, u.getSalt(), u.getPasswordHash())) {
            u.resetFailures();
            saveUsers();
            return u.getUsername();
        }
        u.recordFailure(MAX_FAILED_ATTEMPTS);
        saveUsers();
        return null;
    }

    public boolean isLocked(String username) {
        User u = users.get(username);
        return u != null && u.isLocked();
    }

    // ---------- accounts ----------
    public Account createAccount(String owner) {
        String number;
        do {
            number = String.format("ACC%08d", random.nextInt(100_000_000));
        } while (accounts.containsKey(number));
        Account a = new Account(number, owner, new BigDecimal("0.00"));
        accounts.put(number, a);
        saveAccounts();
        return a;
    }

    public List<Account> getAccountsOf(String owner) {
        List<Account> out = new ArrayList<>();
        for (Account a : accounts.values()) if (a.getOwner().equals(owner)) out.add(a);
        return out;
    }

    /** Only returns the account if it belongs to the logged-in user (access control). */
    public Account getOwnedAccount(String owner, String accountNumber) {
        if (!InputValidator.isValidAccountNumber(accountNumber)) return null;
        Account a = accounts.get(accountNumber);
        return (a != null && a.getOwner().equals(owner)) ? a : null;
    }

    // ---------- transactions ----------
    public void deposit(Account a, BigDecimal amount) {
        a.deposit(amount);
        transactions.add(new Transaction(a.getAccountNumber(), Transaction.Type.DEPOSIT, amount, a.getBalance(), LocalDateTime.now()));
        saveAccounts();
        saveTransactions();
    }

    public void withdraw(Account a, BigDecimal amount) {
        a.withdraw(amount);
        transactions.add(new Transaction(a.getAccountNumber(), Transaction.Type.WITHDRAW, amount, a.getBalance(), LocalDateTime.now()));
        saveAccounts();
        saveTransactions();
    }

    public List<Transaction> historyOf(Account a) {
        List<Transaction> out = new ArrayList<>();
        for (Transaction t : transactions) if (t.getAccountNumber().equals(a.getAccountNumber())) out.add(t);
        return out;
    }
}
