package org.example.lms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegisterController {

    private final String insertQuery = "INSERT INTO users (username, password_hash, name, email, phone_no) VALUES (?, ?, ?, ?, ?)";

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel; // Label to display error messages

    @FXML
    protected void onRegisterButtonClick(ActionEvent event) throws IOException, NoSuchAlgorithmException {
        String name = nameField.getText();
        String email = emailField.getText();
        String phoneNumber = phoneNumberField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Check if any field is empty
        if (name.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Field(s) cannot be empty!");
            return;
        }

        String hashedPassword = PasswordUtil.hashPassword(password);
        Connection connection = DatabaseConnection.connect();

        try {
            PreparedStatement stmnt = connection.prepareStatement(insertQuery);

            stmnt.setString(1, username);       // Username
            stmnt.setString(2, hashedPassword); // Hashed password
            stmnt.setString(3, name);           // Full name
            stmnt.setString(4, email);          // Email
            stmnt.setString(5, phoneNumber);    // Phone number

            int rowsInserted = stmnt.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("New user created successfully.");
                SessionManager.setUsername(username);
                SceneSwitcher.switchScene(event, "MemberDashboard.fxml", "Member Dashboard");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Error during registration. Please try again.");
        } finally {
            DatabaseConnection.disconnect(connection);
        }
    }

    @FXML
    protected void onLoginButtonClick(ActionEvent event) {
        SceneSwitcher.switchScene(event, "login.fxml", "LibraTrack");
    }
}
