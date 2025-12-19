package DAO;


import java.sql.*;
import util.DBUtil;
import model.User;

/**
 *
 * @author ADARSH MILAN
 */
public class UserDAO {

    private static final String AUTH_SQL = 
            "SELECT user_id, name, email, role FROM users WHERE email = ? AND password = ?";

    public User authenticateUser(String email, String password) {
        User user = null;
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(AUTH_SQL)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error during authentication: " + e.getMessage());
            e.printStackTrace();
        }
        return user;
    }
}