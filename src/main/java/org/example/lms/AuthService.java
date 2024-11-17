package org.example.lms;

import java.sql.*;

public class AuthService {
    private String query = "SELECT password_hash FROM users WHERE username = ?";

    public boolean authenticate(String username, String password) {
        try {
            Connection connection = DatabaseConnection.connect();
            PreparedStatement stmnt = connection.prepareStatement(query);

            stmnt.setString(1, username);
            ResultSet rs = stmnt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                DatabaseConnection.disconnect(connection);
                return storedHash.equals(PasswordUtil.hashPassword(password)); // Compare hashed passwords
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}

