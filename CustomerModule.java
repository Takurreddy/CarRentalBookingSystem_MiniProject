import java.sql.*;

class CustomerModule extends Common {
    static void customerMenu() {
        while (true) {
            System.out.println();
            printDoubleLine();
            System.out.println("  MODULE 3 â€” CUSTOMER MANAGEMENT");
            printDoubleLine();
            System.out.println("  1. Register New Customer");
            System.out.println("  2. View All Customers");
            System.out.println("  3. Search Customer");
            System.out.println("  4. Update Customer");
            System.out.println("  5. Delete Customer");
            System.out.println("  0. Back to Main Menu");
            printLine();
            int ch = inputInt("  Select option: ");
            switch (ch) {
                case 1: addCustomer();    break;
                case 2: viewCustomers();  break;
                case 3: searchCustomer(); break;
                case 4: updateCustomer(); break;
                case 5: deleteCustomer(); break;
                case 0: return;
                default: System.out.println("  [!] Invalid option.");
            }
        }
    }

    static void addCustomer() {
        System.out.println();
        printLine();
        System.out.println("  REGISTER NEW CUSTOMER");
        printLine();
        String name    = input("  Full Name      : ");
        String phone   = input("  Phone (10-dig) : ");
        String email   = input("  Email          : ");
        String license = input("  License No.    : ").toUpperCase();

        if (name.isEmpty() || phone.isEmpty() || license.isEmpty()) {
            System.out.println("  [ERROR] Name, Phone, and License are required.");
            pressEnter(); return;
        }
        if (!phone.matches("\\d{10}")) {
            System.out.println("  [ERROR] Phone must be exactly 10 digits.");
            pressEnter(); return;
        }
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO customer (full_name, phone, email, license_no) VALUES (?,?,?,?)"
            );
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, email);
            ps.setString(4, license);
            ps.executeUpdate();
            ps.close();
            System.out.println("\n  [OK] Customer registered successfully!");
            System.out.println("  >> INSERT INTO customer ... executed.");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("  [ERROR] Phone number already registered.");
        } catch (SQLException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
        pressEnter();
    }

    static void viewCustomers() {
        System.out.println();
        printLine();
        System.out.println("  ALL CUSTOMERS â€” SELECT * FROM customer;");
        printLine();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM customer ORDER BY cust_id");
            System.out.printf("  %-6s %-20s %-13s %-25s %-18s%n",
                "ID", "Name", "Phone", "Email", "License No.");
            printLine();
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("  %-6d %-20s %-13s %-25s %-18s%n",
                    rs.getInt("cust_id"),
                    rs.getString("full_name"),
                    rs.getString("phone"),
                    rs.getString("email") != null ? rs.getString("email") : "-",
                    rs.getString("license_no")
                );
            }
            if (!found) System.out.println("  No customers registered.");
            rs.close(); st.close();
        } catch (SQLException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
        pressEnter();
    }

    static void searchCustomer() {
        System.out.println();
        printLine();
        System.out.println("  SEARCH CUSTOMER");
        printLine();
        String keyword = input("  Enter name or phone: ");
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM customer WHERE full_name LIKE ? OR phone LIKE ?"
            );
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            System.out.printf("  %-6s %-20s %-13s %-25s %-18s%n",
                "ID", "Name", "Phone", "Email", "License No.");
            printLine();
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("  %-6d %-20s %-13s %-25s %-18s%n",
                    rs.getInt("cust_id"),
                    rs.getString("full_name"),
                    rs.getString("phone"),
                    rs.getString("email") != null ? rs.getString("email") : "-",
                    rs.getString("license_no")
                );
            }
            if (!found) System.out.println("  No customers found matching '" + keyword + "'.");
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
        pressEnter();
    }

    static void updateCustomer() {
        System.out.println();
        printLine();
        System.out.println("  UPDATE CUSTOMER");
        printLine();
        viewCustomers();
        int id = inputInt("  Enter Customer ID to update: ");
        try {
            PreparedStatement check = conn.prepareStatement("SELECT * FROM customer WHERE cust_id=?");
            check.setInt(1, id);
            ResultSet rs = check.executeQuery();
            if (!rs.next()) {
                System.out.println("  [ERROR] Customer ID not found.");
                check.close(); pressEnter(); return;
            }
            rs.close(); check.close();

            System.out.println("  1) Full Name   2) Phone   3) Email   4) License No.");
            int field = inputInt("  What to update: ");
            String col, newVal;
            switch (field) {
                case 1: col = "full_name";   newVal = input("  New Full Name  : "); break;
                case 2: col = "phone";       newVal = input("  New Phone      : ");
                    if (!newVal.matches("\\d{10}")) {
                        System.out.println("  [ERROR] Phone must be 10 digits."); pressEnter(); return;
                    } break;
                case 3: col = "email";       newVal = input("  New Email      : "); break;
                case 4: col = "license_no";  newVal = input("  New License No : ").toUpperCase(); break;
                default: System.out.println("  [!] Invalid choice."); pressEnter(); return;
            }
            if (newVal.isEmpty()) { System.out.println("  [ERROR] Value cannot be empty."); pressEnter(); return; }

            PreparedStatement ps = conn.prepareStatement(
                "UPDATE customer SET " + col + "=? WHERE cust_id=?"
            );
            ps.setString(1, newVal);
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
            System.out.println("  [OK] Customer updated successfully.");
        } catch (SQLException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
        pressEnter();
    }

    static void deleteCustomer() {
        System.out.println();
        printLine();
        System.out.println("  DELETE CUSTOMER");
        printLine();
        viewCustomers();
        int id = inputInt("  Enter Customer ID to delete: ");
        try {
            // Check active bookings
            PreparedStatement checkBook = conn.prepareStatement(
                "SELECT COUNT(*) FROM booking WHERE cust_id=? AND status='Active'"
            );
            checkBook.setInt(1, id);
            ResultSet rb = checkBook.executeQuery();
            rb.next();
            if (rb.getInt(1) > 0) {
                System.out.println("  [ERROR] Cannot delete customer with active bookings.");
                rb.close(); checkBook.close(); pressEnter(); return;
            }
            rb.close(); checkBook.close();

            PreparedStatement check = conn.prepareStatement("SELECT full_name FROM customer WHERE cust_id=?");
            check.setInt(1, id);
            ResultSet rc = check.executeQuery();
            if (!rc.next()) {
                System.out.println("  [ERROR] Customer ID not found.");
                rc.close(); check.close(); pressEnter(); return;
            }
            String name = rc.getString("full_name");
            rc.close(); check.close();

            String confirm = input("  Delete customer '" + name + "'? (yes/no): ");
            if (!confirm.equalsIgnoreCase("yes")) { System.out.println("  Cancelled."); pressEnter(); return; }

            PreparedStatement ps = conn.prepareStatement("DELETE FROM customer WHERE cust_id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
            System.out.println("  [OK] Customer '" + name + "' deleted.");
            System.out.println("  >> DELETE FROM customer WHERE cust_id=" + id + ";");
        } catch (SQLException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
        pressEnter();
    }

}
