package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    // Get credentials from Environment Variables (for Cloud) or fallback to
    // Localhost (for Dev)
    private static String getUrl() {
        String dbUrl = System.getenv("DB_URL");
        return (dbUrl != null && !dbUrl.isEmpty()) ? dbUrl : "jdbc:mysql://localhost:3306/inventory";
    }

    private static String getUser() {
        String dbUser = System.getenv("DB_USER");
        return (dbUser != null && !dbUser.isEmpty()) ? dbUser : "root";
    }

    private static String getPassword() {
        String dbPass = System.getenv("DB_PASSWORD");
        return (dbPass != null) ? dbPass : "";
    }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            throw new RuntimeException("Failed to load JDBC driver.", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getUrl(), getUser(), getPassword());
    }

    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}