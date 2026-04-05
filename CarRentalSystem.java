public class CarRentalSystem {

    public static void main(String[] args) {
        Common.printBanner();

        if (!DatabaseManager.connectDB()) {
            System.out.println("\n  [ERROR] Could not connect to MySQL.");
            System.out.println("  Check that:");
            System.out.println("  1. MySQL is running");
            System.out.println("  2. Username is 'root'");
            System.out.println("  3. Password is correct");
            return;
        }
        DatabaseManager.setupDatabase();
        MainMenuModule.mainMenu();
        DatabaseManager.closeDB();
    }

}
