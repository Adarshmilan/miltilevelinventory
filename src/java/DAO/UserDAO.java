package DAO;

import java.sql.*;
import DBUtil.java;

/**
 *
 * @author ADARSH MILAN
 */
public class UserDAO {
// SQL to authenticate and retrieve role/warehouse_id
    private static final String AUTH_SQL = 
            "SELECT user_id, name, email, role, warehouse_id FROM user WHERE email = ? AND password = ?";

    /**
     * Authenticates a user and retrieves their session data.
     * @return User object if credentials are valid, otherwise null.
     */
    public User authenticateUser(String email, String password) {
        User user = null;
        
        // Connection, PreparedStatement, and ResultSet are declared here 
        // and initialized within the try-with-resources block.
        try (Connection conn = DBUtil.getConnection(); // 2. CONNECTION ESTABLISHED via IMPORT
             PreparedStatement pstmt = conn.prepareStatement(AUTH_SQL)) {
            
            // Set parameters for the prepared statement
            pstmt.setString(1, email);
            pstmt.setString(2, password); // Note: In a real app, password must be hashed!

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Credentials verified, populate the User object
                    user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    
                    // Critical step: Retrieve warehouse_id only if needed
                    // Assumes a column named 'warehouse_id' exists in the 'user' table
                    user.setWarehouseId(rs.getInt("warehouse_id")); 
                }
            }
        } catch (SQLException e) {
            // Handle specific SQL exceptions (e.g., connection failure)
            System.err.println("SQL Error during authentication: " + e.getMessage());
            e.printStackTrace();
        }
        return user;
    }
}
