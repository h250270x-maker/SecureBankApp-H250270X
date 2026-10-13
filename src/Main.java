import java.io.Console;
import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner IN = new Scanner(System.in);
    private static final Console CONSOLE = System.console();
    private static Bank bank;

    public static void main(String[] args) {
        bank = new Bank(new FileStorage("data"));
        System.out.println("=== Secure Banking Application ===");
        while (true) {
            System.out.println("\n1. Login\n2. Register\n3. Exit");
            switch (ask("Choose: ")) {
                case "1": doLogin(); break;
                case "2": doRegister(); break;
                case "3": System.out.println("Goodbye."); return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static String ask(String prompt) {
        System.out.print(prompt);
        return IN.hasNextLine() ? IN.nextLine().trim() : "3";
    }

    private static char[] askPassword(String prompt) {
        if (CONSOLE != null) return CONSOLE.readPassword(prompt);
        System.out.print(prompt);
        return IN.hasNextLine() ? IN.nextLine().toCharArray() : new char[0];
    }

    private static void doRegister() {
        String user = ask("Choose username: ");
        char[] pw = askPassword("Choose password: ");
        String err = bank.register(user, pw);
        java.util.Arrays.fill(pw, ' ');
        System.out.println(err == null ? "Registration successful. You can now log in." : err);
    }

    private static void doLogin() {
        String user = ask("Username: ");
        char[] pw = askPassword("Password: ");
        String who = bank.login(user, pw);
        java.util.Arrays.fill(pw, ' ');
        if (who == null) {
            System.out.println(bank.isLocked(user)
                    ? "Account locked after too many failed attempts."
                    : "Invalid username or password.");
            return;
        }
        System.out.println("Welcome, " + who + "!");
        userMenu(who);
    }

    private static void userMenu(String user) {
        while (true) {
            System.out.println("\n1. Create new account\n2. View balances\n3. Deposit\n4. Withdraw\n5. Transaction history\n6. Logout");
            switch (ask("Choose: ")) {
                case "1":
                    Account a = bank.createAccount(user);
                    System.out.println("Account created: " + a.getAccountNumber());
                    break;
                case "2": showBalances(user); break;
                case "3": transact(user, true); break;
                case "4": transact(user, false); break;
                case "5": showHistory(user); break;
                case "6": System.out.println("Logged out."); return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void showBalances(String user) {
        List<Account> list = bank.getAccountsOf(user);
        if (list.isEmpty()) { System.out.println("You have no accounts yet."); return; }
        for (Account a : list) System.out.println(a.getAccountNumber() + "  balance: " + a.getBalance().toPlainString());
    }

    private static Account pickAccount(String user) {
        Account a = bank.getOwnedAccount(user, ask("Account number: "));
        if (a == null) System.out.println("Account not found.");
        return a;
    }

    private static void transact(String user, boolean deposit) {
        Account a = pickAccount(user);
        if (a == null) return;
        BigDecimal amt = InputValidator.parseAmount(ask("Amount: "));
        if (amt == null) { System.out.println("Invalid amount (positive, max 2 decimals, up to 1,000,000)."); return; }
        try {
            if (deposit) bank.deposit(a, amt); else bank.withdraw(a, amt);
            System.out.println("Success. New balance: " + a.getBalance().toPlainString());
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }
    }

    private static void showHistory(String user) {
        Account a = pickAccount(user);
        if (a == null) return;
        List<Transaction> h = bank.historyOf(a);
        if (h.isEmpty()) System.out.println("No transactions yet.");
        for (Transaction t : h) System.out.println(t);
    }
}
