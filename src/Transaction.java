// Author: Kudzaishe Andrea | Reg No: H250512W | Secure Banking Application
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Immutable record of one deposit or withdrawal. */
public final class Transaction {
    public enum Type { DEPOSIT, WITHDRAW }

    private final String accountNumber;
    private final Type type;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final LocalDateTime time;

    public Transaction(String accountNumber, Type type, BigDecimal amount, BigDecimal balanceAfter, LocalDateTime time) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.time = time;
    }

    public String getAccountNumber() { return accountNumber; }
    public Type getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public LocalDateTime getTime() { return time; }

    public String toFileLine() {
        return accountNumber + "|" + type + "|" + amount.toPlainString() + "|" + balanceAfter.toPlainString() + "|" + time;
    }

    public static Transaction fromFileLine(String line) {
        String[] p = line.split("\\|");
        if (p.length != 5) throw new IllegalArgumentException("Corrupt transaction record");
        return new Transaction(p[0], Type.valueOf(p[1]), new BigDecimal(p[2]), new BigDecimal(p[3]), LocalDateTime.parse(p[4]));
    }

    @Override
    public String toString() {
        return String.format("%s | %-8s | %12s | balance: %12s", time.withNano(0), type, amount.toPlainString(), balanceAfter.toPlainString());
    }
}
