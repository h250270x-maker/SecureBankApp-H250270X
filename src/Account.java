// Author: Kudzaishe Andrea | Reg No: H250512W | Secure Banking Application
import java.math.BigDecimal;
import java.math.RoundingMode;

/** A bank account. Balance is private and can only change through deposit/withdraw. */
public class Account {
    private final String accountNumber;
    private final String owner;
    private BigDecimal balance;

    public Account(String accountNumber, String owner, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.balance = balance.setScale(2, RoundingMode.UNNECESSARY);
    }

    public String getAccountNumber() { return accountNumber; }
    public String getOwner() { return owner; }
    public BigDecimal getBalance() { return balance; }

    public void deposit(BigDecimal amount) {
        if (amount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
        balance = balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (amount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (amount.compareTo(balance) > 0) throw new IllegalStateException("Insufficient funds");
        balance = balance.subtract(amount);
    }

    public String toFileLine() { return accountNumber + "|" + owner + "|" + balance.toPlainString(); }

    public static Account fromFileLine(String line) {
        String[] p = line.split("\\|");
        if (p.length != 3) throw new IllegalArgumentException("Corrupt account record");
        return new Account(p[0], p[1], new BigDecimal(p[2]));
    }
}
