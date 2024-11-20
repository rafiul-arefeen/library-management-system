package org.example.lms;

public class SessionManager {
    private static String username;

    // Setter for username
    public static void setUsername(String username) {
        SessionManager.username = username;
    }

    // Getter for username
    public static String getUsername() {
        return username;
    }
}
