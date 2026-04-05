import java.sql.*;

class ReportModule extends Common {
    static void reportMenu() {
        while (true) {
            System.out.println();
            printDoubleLine();
            System.out.println("  MODULE 4 â€” REPORTS & ANALYTICS");
            printDoubleLine();
            System.out.println("  1. Available Cars Report");
            System.out.println("  2. All Bookings Report");
            System.out.println("  3. Revenue Report");
            System.out.println("  4. Rental History by Customer");
            System.out.println("  5. System Summary");
            System.out.println("  0. Back to Main Menu");
            printLine();
            int ch = inputInt("  Select option: ");
            switch (ch) {
                case 1: reportAvailableCars();   break;
                case 2: BookingModule.viewAllBookings();        break;
                case 3: reportRevenue();          break;
                case 4: reportByCustomer();       break;
                case 5: reportSummary();          break;
                case 0: return;
                default: System.out.println("  [!] Invalid option.");
            }
        }
    }

    static void reportAvailableCars() {
        System.out.println();
        printLine();
        System.out.println("  REPORT: AVAILABLE CARS");
        printLine();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                "SELECT category, COUNT(*) as cnt, MIN(daily_rate) as min_rate, MAX(daily_rate) as max_rate " +
                "FROM car WHERE status='Available' GROUP BY category"
            );
            System.out.println("  Summary by Category:");
            while (rs.next()) {
                System.out.printf("  %-8s : %d car(s)  |  Rate: Rs.%.0f - Rs.%.0f / day%n",
                    rs.getString("category"), rs.getInt("cnt"),
                    rs.getDouble("min_rate"), rs.getDouble("max_rate"));
            }
            rs.close(); st.close();
            System.out.println();
        } catch (SQLException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
        CarModule.viewAvailableCars();
    }

    static void reportRevenue() {
        System.out.println();
        printLine();
        System.out.println("  REPORT: REVENUE ANALYSIS");
        printLine();
        try {
            Statement st = conn.createStatement();

            ResultSet r1 = st.executeQuery(
                "SELECT COUNT(*) as total, SUM(total_charge) as revenue FROM booking"
            );
            r1.next();
            System.out.printf("  Total Bookings  : %d%n", r1.getInt("total"));
            System.out.printf("  Total Revenue   : Rs. %.2f%n", r1.getDouble("revenue"));
            r1.close();

            ResultSet r2 = st.executeQuery(
                "SELECT COUNT(*) as cnt, SUM(total_charge) as rev FROM booking WHERE status='Active'"
            );
            r2.next();
            System.out.printf("  Active Bookings : %d  (Rs. %.2f pending)%n",
                r2.getInt("cnt"), r2.getDouble("rev"));
            r2.close();

            ResultSet r3 = st.executeQuery(
                "SELECT COUNT(*) as cnt, SUM(total_charge) as rev FROM booking WHERE status='Returned'"
            );
            r3.next();
            System.out.printf("  Returned        : %d  (Rs. %.2f collected)%n",
                r3.getInt("cnt"), r3.getDouble("rev"));
            r3.close();

            System.out.println();
            System.out.println("  Revenue by Category:");
            ResultSet r4 = st.executeQuery(
                "SELECT cr.category, COUNT(*) as cnt, SUM(b.total_charge) as rev " +
                "FROM booking b JOIN car cr ON b.car_id=cr.car_id " +
                "GROUP BY cr.category"
            );
            while (r4.next()) {
                System.out.printf("  %-8s : %d booking(s)  |  Rs. %.2f%n",
                    r4.getString("category"), r4.getInt("cnt"), r4.getDouble("rev"));
            }
            r4.close();
            st.close();
        } catch (SQLException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
        pressEnter();
    }

    static void reportByCustomer() {
        System.out.println();
        printLine();
        System.out.println("  REPORT: RENTAL HISTORY BY CUSTOMER");
        printLine();
        String keyword = input("  Enter customer name: ");
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT c.full_name, cr.brand, cr.model, b.booking_date, " +
                "b.return_date, b.total_days, b.total_charge, b.status " +
                "FROM booking b " +
                "JOIN customer c  ON b.cust_id = c.cust_id " +
                "JOIN car cr ON b.car_id  = cr.car_id " +
                "WHERE c.full_name LIKE ? ORDER BY b.booking_date DESC"
            );
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            System.out.printf("  %-18s %-22s %-12s %-12s %-5s %-12s %-10s%n",
                "Customer", "Car", "Start", "End", "Days", "Charge", "Status");
            printLine();
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("  %-18s %-22s %-12s %-12s %-5d Rs.%-9.0f %-10s%n",
                    rs.getString("full_name"),
                    rs.getString("brand") + " " + rs.getString("model"),
                    rs.getDate("booking_date"),
                    rs.getDate("return_date"),
                    rs.getInt("total_days"),
                    rs.getDouble("total_charge"),
                    rs.getString("status")
                );
            }
            if (!found) System.out.println("  No rental history found for '" + keyword + "'.");
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
        pressEnter();
    }

    static void reportSummary() {
        System.out.println();
        printDoubleLine();
        System.out.println("  SYSTEM SUMMARY REPORT");
        printDoubleLine();
        try {
            Statement st = conn.createStatement();

            ResultSet rc = st.executeQuery("SELECT COUNT(*) as t, SUM(status='Available') as a FROM car");
            rc.next();
            System.out.printf("  Total Cars      : %d  (%d available, %d booked)%n",
                rc.getInt("t"), rc.getInt("a"), rc.getInt("t") - rc.getInt("a"));
            rc.close();

            ResultSet rcu = st.executeQuery("SELECT COUNT(*) as t FROM customer");
            rcu.next();
            System.out.printf("  Customers       : %d registered%n", rcu.getInt("t"));
            rcu.close();

            ResultSet rb = st.executeQuery(
                "SELECT COUNT(*) as t, SUM(status='Active') as a, SUM(total_charge) as rev FROM booking"
            );
            rb.next();
            System.out.printf("  Total Bookings  : %d  (%d active)%n",
                rb.getInt("t"), rb.getInt("a"));
            System.out.printf("  Total Revenue   : Rs. %.2f%n", rb.getDouble("rev"));
            rb.close();

            st.close();
        } catch (SQLException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
        printDoubleLine();
        pressEnter();
    }

}
