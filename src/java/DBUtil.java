import java.sql.*;
public class DBUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/inventory";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        // Load the MySQL driver (needed for older JVMs, good practice)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}