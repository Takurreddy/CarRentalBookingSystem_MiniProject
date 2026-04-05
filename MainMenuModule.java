class MainMenuModule extends Common {
    static void mainMenu() {
        while (true) {
            System.out.println();
            printDoubleLine();
            System.out.println("                     MAIN MENU");
            printDoubleLine();
            System.out.println("  1. Car Management");
            System.out.println("  2. Booking & Pricing");
            System.out.println("  3. Customer Management");
            System.out.println("  4. Reports");
            System.out.println("  0. Exit");
            printLine();
            int ch = inputInt("  Select option: ");
            switch (ch) {
                case 1: CarModule.carMenu();      break;
                case 2: BookingModule.bookingMenu();  break;
                case 3: CustomerModule.customerMenu(); break;
                case 4: ReportModule.reportMenu();   break;
                case 0:
                    System.out.println("\n  Goodbye! System closed.\n");
                    return;
                default:
                    System.out.println("  [!] Invalid option.");
            }
        }
    }

}
