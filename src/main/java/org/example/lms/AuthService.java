package org.example.lms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthService {
    private String query = "SELECT password_hash, isAdmin FROM users WHERE username = ?";

    /**
     * Authenticates a user and retrieves their role.
     *
     * @param username the username of the user
     * @param password the password provided by the user
     * @return "admin" if the user is an admin, "user" if a regular user, or null if authentication fails
     */
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
