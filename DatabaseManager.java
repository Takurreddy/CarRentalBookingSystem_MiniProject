import java.sql.*;

class DatabaseManager extends Common {
    static boolean connectDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Connect without DB first to create it if needed
            Connection tempConn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                DB_USER, DB_PASS
            );
            Statement st = tempConn.createStatement();
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS carrent_db");
            st.close();
            tempConn.close();
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("\n  [OK] Connected to MySQL as 'root' â€” database: carrent_db");
            return true;
        } catch (Exception e) {
            System.out.println("\n  [FAIL] " + e.getMessage());
            return false;
        }
    }

    static void closeDB() {
        try { if (conn != null) conn.close(); } catch (SQLException e) {}
    }

    static void setupDatabase() {
        try {
            Statement st = conn.createStatement();

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS car (" +
                "  car_id       INT AUTO_INCREMENT PRIMARY KEY," +
                "  brand        VARCHAR(50)  NOT NULL," +
                "  model        VARCHAR(80)  NOT NULL," +
                "  category     ENUM('AC','Non-AC') NOT NULL," +
                "  daily_rate   DECIMAL(10,2) NOT NULL," +
                "  reg_number   VARCHAR(20)  NOT NULL UNIQUE," +
                "  status       ENUM('Available','Not Available') DEFAULT 'Available'" +
                ")"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS customer (" +
                "  cust_id      INT AUTO_INCREMENT PRIMARY KEY," +
                "  full_name    VARCHAR(100) NOT NULL," +
                "  phone        VARCHAR(15)  NOT NULL UNIQUE," +
                "  email        VARCHAR(100)," +
                "  license_no   VARCHAR(30)  NOT NULL" +
                ")"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS booking (" +
                "  booking_id   INT AUTO_INCREMENT PRIMARY KEY," +
                "  cust_id      INT NOT NULL," +
                "  car_id       INT NOT NULL," +
                "  booking_date DATE NOT NULL," +
                "  return_date  DATE NOT NULL," +
                "  total_days   INT NOT NULL," +
                "  total_charge DECIMAL(10,2) NOT NULL," +
                "  status       ENUM('Active','Returned') DEFAULT 'Active'," +
                "  FOREIGN KEY (cust_id) REFERENCES customer(cust_id)," +
                "  FOREIGN KEY (car_id)  REFERENCES car(car_id)" +
                ")"
            );

            st.close();
            System.out.println("  [OK] Database schema verified (tables: car, customer, booking).");
        } catch (SQLException e) {
            System.out.println("[ERROR] Schema setup: " + e.getMessage());
        }
    }

}
