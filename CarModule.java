import java.sql.*;

class CarModule extends Common {
    static void carMenu() {
        while (true) {
            System.out.println();
            printDoubleLine();
            System.out.println("  MODULE 1 â€” CAR MANAGEMENT");
            printDoubleLine();
            System.out.println("  1. Add New Car");
            System.out.println("  2. View All Cars");
            System.out.println("  3. View Available Cars");
            System.out.println("  4. Update Car Details");
            System.out.println("  5. Delete Car");
            System.out.println("  6. Search Car");
            System.out.println("  0. Back to Main Menu");
            printLine();
            int ch = inputInt("  Select option: ");
            switch (ch) {
                case 1: addCar();           break;
                case 2: viewAllCars();      break;
                case 3: viewAvailableCars();break;
                case 4: updateCar();        break;
                case 5: deleteCar();        break;
                case 6: searchCar();        break;
                case 0: return;
                default: System.out.println("  [!] Invalid option.");
            }
        }
    }

    static void addCar() {
        System.out.println();
        printLine();
        System.out.println("  ADD NEW CAR");
        printLine();
        String brand   = input("  Brand          : ");
        String model   = input("  Model          : ");
        if (brand.isEmpty() || model.isEmpty()) {
            System.out.println("  [ERROR] Brand and Model cannot be empty.");
            return;
        }
        System.out.println("  Category       : 1) AC   2) Non-AC");
        int catChoice  = inputInt("  Choose (1/2)   : ");
        String category= (catChoice == 1) ? "AC" : "Non-AC";
        double rate    = inputDouble("  Daily Rate (Rs): ");
        String reg     = input("  Reg. Number    : ").toUpperCase();
        if (reg.isEmpty()) {
            System.out.println("  [ERROR] Registration number cannot be empty.");
            return;
        }
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO car (brand, model, category, daily_rate, reg_number) VALUES (?,?,?,?,?)"
            );
            ps.setString(1, brand);
            ps.setString(2, model);
            ps.setString(3, category);
            ps.setDouble(4, rate);
            ps.setString(5, reg);
            ps.executeUpdate();
            ps.close();
            System.out.println("\n  [OK] Car added successfully!");
            System.out.println("  >> INSERT INTO car ... executed.");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("  [ERROR] Registration number already exists.");
        } catch (SQLException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
        pressEnter();
    }

    static void viewAllCars() {
        System.out.println();
        printLine();
        System.out.println("  ALL CARS â€” SELECT * FROM car;");
        printLine();
        try {
            Statement st  = conn.createStatement();
            ResultSet rs  = st.executeQuery("SELECT * FROM car ORDER BY car_id");
            System.out.printf("  %-6s %-12s %-18s %-8s %-10s %-15s %-14s%n",
                "ID", "Brand", "Model", "Cat", "Rate/Day", "Reg No.", "Status");
            printLine();
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("  %-6d %-12s %-18s %-8s Rs.%-7.0f %-15s %-14s%n",
                    rs.getInt("car_id"),
                    rs.getString("brand"),
                    rs.getString("model"),
                    rs.getString("category"),
                    rs.getDouble("daily_rate"),
                    rs.getString("reg_number"),
                    rs.getString("status")
                );
            }
            if (!found) System.out.println("  No car records found.");
            rs.close(); st.close();
        } catch (SQLException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
        pressEnter();
    }

    static void viewAvailableCars() {
        System.out.println();
        printLine();
        System.out.println("  AVAILABLE CARS â€” SELECT * FROM car WHERE status='Available';");
        printLine();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                "SELECT * FROM car WHERE status='Available' ORDER BY category, daily_rate"
            );
            System.out.printf("  %-6s %-12s %-18s %-8s %-12s %-15s%n",
                "ID", "Brand", "Model", "Cat", "Rate/Day", "Reg No.");
            printLine();
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("  %-6d %-12s %-18s %-8s Rs.%-9.0f %-15s%n",
                    rs.getInt("car_id"),
                    rs.getString("brand"),
                    rs.getString("model"),
                    rs.getString("category"),
                    rs.getDouble("daily_rate"),
                    rs.getString("reg_number")
                );
            }
            if (!found) System.out.println("  No cars currently available.");
            rs.close(); st.close();
        } catch (SQLException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
        pressEnter();
    }

    static void updateCar() {
        System.out.println();
        printLine();
        System.out.println("  UPDATE CAR DETAILS");
        printLine();
        viewAllCars();
        int id = inputInt("  Enter Car ID to update: ");
        try {
            PreparedStatement check = conn.prepareStatement("SELECT * FROM car WHERE car_id=?");
            check.setInt(1, id);
            ResultSet rs = check.executeQuery();
            if (!rs.next()) {
                System.out.println("  [ERROR] Car ID not found.");
                check.close(); pressEnter(); return;
            }
            System.out.println("  Current  ->  Brand: " + rs.getString("brand") +
                               " | Model: " + rs.getString("model") +
                               " | Rate: Rs." + rs.getDouble("daily_rate") +
                               " | Status: " + rs.getString("status"));
            rs.close(); check.close();

            System.out.println("\n  What to update?");
            System.out.println("  1) Brand   2) Model   3) Category   4) Daily Rate   5) Reg Number");
            int field = inputInt("  Choose: ");
            String col; String newVal;
            switch (field) {
                case 1: col = "brand";       newVal = input("  New Brand      : "); break;
                case 2: col = "model";       newVal = input("  New Model      : "); break;
                case 3: col = "category";
                    System.out.println("  1) AC  2) Non-AC");
                    newVal = (inputInt("  Choose: ") == 1) ? "AC" : "Non-AC"; break;
                case 4: col = "daily_rate";  newVal = String.valueOf(inputDouble("  New Rate (Rs)  : ")); break;
                case 5: col = "reg_number";  newVal = input("  New Reg No.    : ").toUpperCase(); break;
                default: System.out.println("  [!] Invalid choice."); pressEnter(); return;
            }
            if (newVal.isEmpty()) { System.out.println("  [ERROR] Value cannot be empty."); pressEnter(); return; }

            PreparedStatement ps = conn.prepareStatement(
                "UPDATE car SET " + col + "=? WHERE car_id=?"
            );
            ps.setString(1, newVal);
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
            System.out.println("  [OK] Car record updated successfully.");
            System.out.println("  >> UPDATE car SET " + col + "='" + newVal + "' WHERE car_id=" + id + ";");
        } catch (SQLException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
        pressEnter();
    }


    static void deleteCar() {
        System.out.println();
        printLine();
        System.out.println("  DELETE CAR");
        printLine();
        viewAllCars();
        int id = inputInt("  Enter Car ID to delete: ");
        try {
            PreparedStatement check = conn.prepareStatement(
                "SELECT * FROM car WHERE car_id=?"
            );
            check.setInt(1, id);
            ResultSet rs = check.executeQuery();
            if (!rs.next()) {
                System.out.println("  [ERROR] Car ID not found.");
                check.close(); pressEnter(); return;
            }
            if (rs.getString("status").equals("Not Available")) {
                System.out.println("  [ERROR] Cannot delete a car that is currently booked.");
                rs.close(); check.close(); pressEnter(); return;
            }
            rs.close(); check.close();

            String confirm = input("  Confirm delete? (yes/no): ");
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("  Cancelled."); pressEnter(); return;
            }
            PreparedStatement ps = conn.prepareStatement("DELETE FROM car WHERE car_id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
            System.out.println("  [OK] Car deleted successfully.");
            System.out.println("  >> DELETE FROM car WHERE car_id=" + id + ";");
        } catch (SQLException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
        pressEnter();
    }


    static void searchCar() {
        System.out.println();
        printLine();
        System.out.println("  SEARCH CAR");
        printLine();
        String keyword = input("  Enter brand or model to search: ");
        if (keyword.isEmpty()) { System.out.println("  [!] Search term cannot be empty."); pressEnter(); return; }
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM car WHERE brand LIKE ? OR model LIKE ?"
            );
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            System.out.println();
            System.out.printf("  %-6s %-12s %-18s %-8s %-10s %-15s %-14s%n",
                "ID", "Brand", "Model", "Cat", "Rate/Day", "Reg No.", "Status");
            printLine();
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("  %-6d %-12s %-18s %-8s Rs.%-7.0f %-15s %-14s%n",
                    rs.getInt("car_id"),
                    rs.getString("brand"),
                    rs.getString("model"),
                    rs.getString("category"),
                    rs.getDouble("daily_rate"),
                    rs.getString("reg_number"),
                    rs.getString("status")
                );
            }
            if (!found) System.out.println("  No cars found matching '" + keyword + "'.");
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
        pressEnter();
    }

    // ============================================================
    //  MODULE 2 â€” BOOKING & PRICING

}
