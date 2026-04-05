import java.sql.*;
import java.util.Scanner;

class Common {
    // â”€â”€ DB CONFIG â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    static final String DB_URL  = "jdbc:mysql://localhost:3306/carrent_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    static final String DB_USER = "root";
    static final String DB_PASS = "Takurmukku@ 4";
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    static Connection conn;
    static Scanner sc = new Scanner(System.in);


    static void printBanner() {
        System.out.println("â•”â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•—");
        System.out.println("â•‘          CAR RENTAL BOOKING SYSTEM  v1.0                â•‘");
        System.out.println("â•‘     Java (J2SE) + MySQL + JDBC Console Application      â•‘");
        System.out.println("â•šâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•");
        System.out.println();
    }

    static void printLine() {
        System.out.println("â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€");
    }

    static void printDoubleLine() {
        System.out.println("â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•");
    }

    static String input(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    static int inputInt(String prompt) {
        while (true) {
            try {
                String s = input(prompt);
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Please enter a valid number.");
            }
        }
    }

    static double inputDouble(String prompt) {
        while (true) {
            try {
                String s = input(prompt);
                double d = Double.parseDouble(s);
                if (d <= 0) throw new NumberFormatException();
                return d;
            } catch (NumberFormatException e) {
                System.out.println("  [!] Please enter a valid positive number.");
            }
        }
    }

    static void pressEnter() {
        input("\n  Press Enter to continue...");
    }

}
