import java.sql.*;

class BookingModule extends Common {
    static void bookingMenu() {
        while (true) {
            System.out.println();
            printDoubleLine();
            System.out.println("  MODULE 2 â€” BOOKING & PRICING");
            printDoubleLine();
            System.out.println("  1. Create New Booking");
            System.out.println("  2. View All Bookings");
            System.out.println("  3. View Active Bookings");
            System.out.println("  4. Return Car (Close Booking)");
            System.out.println("  5. Search Booking by Customer");
            System.out.println("  0. Back to Main Menu");
            printLine();
            int ch = inputInt("  Select option: ");
            switch (ch) {
                case 1: createBooking();       break;
                case 2: viewAllBookings();     break;
                case 3: viewActiveBookings();  break;
                case 4: returnCar();           break;
                case 5: searchBooking();       break;
                case 0: return;
                default: System.out.println("  [!] Invalid option.");
            }
        }
    }

    static void createBooking() {
        System.out.println();
        printLine();
        System.out.println("  CREATE NEW BOOKING");
        printLine();

        // Show available cars
        System.out.println("  --- Available Cars ---");
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                "SELECT * FROM car WHERE status='Available' ORDER BY category, car_id"
            );
            boolean any = false;
            while (rs.next()) {
                any = true;
                System.out.printf("  [%d] %-12s %-18s %-8s Rs.%.0f/day%n",
                    rs.getInt("car_id"), rs.getString("brand"),
                    rs.getString("model"), rs.getString("category"),
                    rs.getDouble("daily_rate"));
            }
            rs.close(); st.close();
            if (!any) {
                System.out.println("  No cars available for booking right now.");
                pressEnter(); return;
            }
        } catch (SQLException e) {
            System.out.println("  [ERROR] " + e.getMessage()); return;
        }

        // Show customers
        System.out.println("\n  --- Registered Customers ---");
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM customer ORDER BY cust_id");
            boolean any = false;
            while (rs.next()) {
                any = true;
                System.out.printf("  [%d] %s  (Ph: %s)%n",
                    rs.getInt("cust_id"), rs.getString("full_name"), rs.getString("phone"));
            }
            rs.close(); st.close();
            if (!any) {
                System.out.println("  No customers registered. Please add a customer first.");
                pressEnter(); return;
            }
        } catch (SQLException e) {
            System.out.println("  [ERROR] " + e.getMessage()); return;
        }

        System.out.println();
        int custId = inputInt("  Enter Customer ID : ");
        int carId  = inputInt("  Enter Car ID      : ");

        // Validate IDs
        try {
            PreparedStatement pc = conn.prepareStatement("SELECT full_name FROM customer WHERE cust_id=?");
            pc.setInt(1, custId);
            ResultSet rc = pc.executeQuery();
            if (!rc.next()) {
                System.out.println("  [ERROR] Customer ID not found."); rc.close(); pc.close(); pressEnter(); return;
            }
            String custName = rc.getString("full_name");
            rc.close(); pc.close();

            PreparedStatement pcar = conn.prepareStatement(
                "SELECT brand, model, category, daily_rate, status FROM car WHERE car_id=?"
            );
            pcar.setInt(1, carId);
            ResultSet rcar = pcar.executeQuery();
            if (!rcar.next()) {
                System.out.println("  [ERROR] Car ID not found."); rcar.close(); pcar.close(); pressEnter(); return;
            }
            if (rcar.getString("status").equals("Not Available")) {
                System.out.println("  [ERROR] This car is currently not available.");
                rcar.close(); pcar.close(); pressEnter(); return;
            }
            String brand    = rcar.getString("brand");
            String model    = rcar.getString("model");
            String category = rcar.getString("category");
            double rate     = rcar.getDouble("daily_rate");
            rcar.close(); pcar.close();

            System.out.println("\n  Booking Date Format: YYYY-MM-DD");
            String startDate = input("  Booking Date      : ");
            String endDate   = input("  Return Date       : ");

            // Validate dates
            java.time.LocalDate start, end;
            try {
                start = java.time.LocalDate.parse(startDate);
                end   = java.time.LocalDate.parse(endDate);
            } catch (Exception e) {
                System.out.println("  [ERROR] Invalid date format. Use YYYY-MM-DD.");
                pressEnter(); return;
            }
            if (!end.isAfter(start)) {
                System.out.println("  [ERROR] Return date must be after booking date.");
                pressEnter(); return;
            }

            long days  = java.time.temporal.ChronoUnit.DAYS.between(start, end);
            double total = days * rate;

            // Show pricing breakdown
            System.out.println();
            printLine();
            System.out.println("  BOOKING SUMMARY");
            printLine();
            System.out.printf("  Customer   : %s (ID: %d)%n", custName, custId);
            System.out.printf("  Car        : %s %s [%s] (ID: %d)%n", brand, model, category, carId);
            System.out.printf("  Period     : %s  to  %s%n", startDate, endDate);
            System.out.printf("  Duration   : %d day(s)%n", days);
            System.out.printf("  Rate       : Rs. %.2f / day%n", rate);
            System.out.println("  â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€");
            System.out.printf("  TOTAL CHARGE : Rs. %.2f%n", total);
            printLine();

            String confirm = input("  Confirm booking? (yes/no): ");
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("  Booking cancelled."); pressEnter(); return;
            }

            // Insert booking
            PreparedStatement pb = conn.prepareStatement(
                "INSERT INTO booking (cust_id, car_id, booking_date, return_date, total_days, total_charge) " +
                "VALUES (?,?,?,?,?,?)"
            );
            pb.setInt(1, custId);
            pb.setInt(2, carId);
            pb.setDate(3, java.sql.Date.valueOf(startDate));
            pb.setDate(4, java.sql.Date.valueOf(endDate));
            pb.setLong(5, days);
            pb.setDouble(6, total);
            pb.executeUpdate();
            pb.close();

            // Update car status
            PreparedStatement pu = conn.prepareStatement(
                "UPDATE car SET status='Not Available' WHERE car_id=?"
            );
            pu.setInt(1, carId);
            pu.executeUpdate();
            pu.close();

            System.out.println("\n  [OK] Booking confirmed successfully!");
            System.out.println("  >> INSERT INTO booking ... executed.");
            System.out.println("  >> UPDATE car SET status='Not Available' WHERE car_id=" + carId + ";");

        } catch (SQLException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
        pressEnter();
    }


    static void viewAllBookings() {
        System.out.println();
        printLine();
        System.out.println("  ALL BOOKINGS â€” SELECT * FROM booking JOIN customer JOIN car;");
        printLine();
        try {
            String sql =
                "SELECT b.booking_id, c.full_name, cr.brand, cr.model, cr.category, " +
                "b.booking_date, b.return_date, b.total_days, b.total_charge, b.status " +
                "FROM booking b " +
                "JOIN customer c  ON b.cust_id = c.cust_id " +
                "JOIN car cr ON b.car_id  = cr.car_id " +
                "ORDER BY b.booking_id DESC";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            System.out.printf("  %-5s %-16s %-20s %-7s %-12s %-12s %-5s %-12s %-10s%n",
                "ID", "Customer", "Car", "Cat", "Start", "End", "Days", "Charge", "Status");
            printLine();
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("  %-5d %-16s %-20s %-7s %-12s %-12s %-5d Rs.%-9.0f %-10s%n",
                    rs.getInt("booking_id"),
                    rs.getString("full_name"),
                    rs.getString("brand") + " " + rs.getString("model"),
                    rs.getString("category"),
                    rs.getDate("booking_date").toString(),
                    rs.getDate("return_date").toString(),
                    rs.getInt("total_days"),
                    rs.getDouble("total_charge"),
                    rs.getString("status")
                );
            }
            if (!found) System.out.println("  No booking records found.");
            rs.close(); st.close();
        } catch (SQLException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
        pressEnter();
    }

    static void viewActiveBookings() {
        System.out.println();
        printLine();
        System.out.println("  ACTIVE BOOKINGS â€” status='Active'");
        printLine();
        try {
            String sql =
                "SELECT b.booking_id, c.full_name, cr.brand, cr.model, " +
                "b.booking_date, b.return_date, b.total_days, b.total_charge " +
                "FROM booking b " +
                "JOIN customer c  ON b.cust_id = c.cust_id " +
                "JOIN car cr ON b.car_id  = cr.car_id " +
                "WHERE b.status='Active' ORDER BY b.booking_id";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            System.out.printf("  %-5s %-16s %-22s %-12s %-12s %-5s %-12s%n",
                "ID", "Customer", "Car", "Start", "End", "Days", "Charge");
            printLine();
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("  %-5d %-16s %-22s %-12s %-12s %-5d Rs.%.0f%n",
                    rs.getInt("booking_id"),
                    rs.getString("full_name"),
                    rs.getString("brand") + " " + rs.getString("model"),
                    rs.getDate("booking_date").toString(),
                    rs.getDate("return_date").toString(),
                    rs.getInt("total_days"),
                    rs.getDouble("total_charge")
                );
            }
            if (!found) System.out.println("  No active bookings.");
            rs.close(); st.close();
        } catch (SQLException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
        pressEnter();
    }

    static void returnCar() {
        System.out.println();
        printLine();
        System.out.println("  RETURN CAR â€” CLOSE BOOKING");
        printLine();
        viewActiveBookings();
        int bookingId = inputInt("  Enter Booking ID to return: ");
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT b.*, c.full_name, cr.brand, cr.model, cr.car_id " +
                "FROM booking b " +
                "JOIN customer c  ON b.cust_id = c.cust_id " +
                "JOIN car cr ON b.car_id  = cr.car_id " +
                "WHERE b.booking_id=? AND b.status='Active'"
            );
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                System.out.println("  [ERROR] Active booking not found with ID " + bookingId);
                rs.close(); ps.close(); pressEnter(); return;
            }
            String custName  = rs.getString("full_name");
            String carName   = rs.getString("brand") + " " + rs.getString("model");
            int    carId     = rs.getInt("car_id");
            double charge    = rs.getDouble("total_charge");
            int    days      = rs.getInt("total_days");
            rs.close(); ps.close();

            System.out.println();
            System.out.println("  Customer   : " + custName);
            System.out.println("  Car        : " + carName);
            System.out.println("  Days       : " + days);
            System.out.printf( "  Amount Due : Rs. %.2f%n", charge);

            String confirm = input("\n  Confirm return? (yes/no): ");
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("  Cancelled."); pressEnter(); return;
            }

            PreparedStatement ub = conn.prepareStatement(
                "UPDATE booking SET status='Returned' WHERE booking_id=?"
            );
            ub.setInt(1, bookingId);
            ub.executeUpdate();
            ub.close();

            PreparedStatement uc = conn.prepareStatement(
                "UPDATE car SET status='Available' WHERE car_id=?"
            );
            uc.setInt(1, carId);
            uc.executeUpdate();
            uc.close();

            System.out.println("\n  [OK] Car returned. Booking closed.");
            System.out.println("  >> UPDATE booking SET status='Returned' WHERE booking_id=" + bookingId + ";");
            System.out.println("  >> UPDATE car SET status='Available' WHERE car_id=" + carId + ";");
            System.out.printf( "  >> Payment collected: Rs. %.2f%n", charge);

        } catch (SQLException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
        pressEnter();
    }

    static void searchBooking() {
        System.out.println();
        printLine();
        System.out.println("  SEARCH BOOKING BY CUSTOMER NAME");
        printLine();
        String keyword = input("  Enter customer name: ");
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT b.booking_id, c.full_name, cr.brand, cr.model, " +
                "b.booking_date, b.return_date, b.total_charge, b.status " +
                "FROM booking b " +
                "JOIN customer c  ON b.cust_id = c.cust_id " +
                "JOIN car cr ON b.car_id  = cr.car_id " +
                "WHERE c.full_name LIKE ? ORDER BY b.booking_id DESC"
            );
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            System.out.printf("  %-5s %-16s %-22s %-12s %-12s %-12s %-10s%n",
                "ID", "Customer", "Car", "Start", "End", "Charge", "Status");
            printLine();
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("  %-5d %-16s %-22s %-12s %-12s Rs.%-9.0f %-10s%n",
                    rs.getInt("booking_id"),
                    rs.getString("full_name"),
                    rs.getString("brand") + " " + rs.getString("model"),
                    rs.getDate("booking_date"),
                    rs.getDate("return_date"),
                    rs.getDouble("total_charge"),
                    rs.getString("status")
                );
            }
            if (!found) System.out.println("  No bookings found for '" + keyword + "'.");
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
        pressEnter();
    }

    // ============================================================
    //  MODULE 3 â€” CUSTOMER MANAGEMENT

}
