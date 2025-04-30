package org.example.lms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthService {
    private String query = "SELECT password_hash, isAdmin FROM users WHERE username = ?";


    public String authenticate(String username, String password) {
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmnt = connection.prepareStatement(query)) {

            stmnt.setString(1, username);
            ResultSet rs = stmnt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                boolean isAdmin = rs.getBoolean("isAdmin");

                // Verify the password
                if (PasswordUtil.hashPassword(password).equals(storedHash)) {
                    return isAdmin ? "admin" : "user";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Authentication failed
    }
}
