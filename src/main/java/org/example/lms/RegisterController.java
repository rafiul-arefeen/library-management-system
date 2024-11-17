package org.example.lms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import javafx.scene.control.TextField;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegisterController {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String username;
    private String password;
    private String hashedPassword;
    private String insertQuery = "INSERT INTO users (username, password_hash, name, email, phone_no) VALUES (?, ?, ?, ?, ?)";
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    @FXML
    protected void onRegisterButtonClick(ActionEvent event) throws IOException, NoSuchAlgorithmException {
        firstName = firstNameField.getText();
        lastName = lastNameField.getText();
        email = emailField.getText();
        phoneNumber = phoneNumberField.getText();
        username = usernameField.getText();
        password = passwordField.getText();

        hashedPassword = PasswordUtil.hashPassword(password);

        Connection connection = DatabaseConnection.connect();

        try{
            PreparedStatement stmnt = connection.prepareStatement(insertQuery);

            stmnt.setString(1, username);       // Username
            stmnt.setString(2, hashedPassword); // Hashed password
            stmnt.setString(3, firstName + " " + lastName);           // Name
            stmnt.setString(4, email);          // Email
            stmnt.setString(5, phoneNumber);

            int rowsInserted = stmnt.executeUpdate();

            if(rowsInserted > 0){
                System.out.println("New user created");

                SceneSwitcher.switchScene(event, "dashboard.fxml", "Dashboard");
                DatabaseConnection.disconnect(connection);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error inserting user: " + e.getMessage());
        }
    }

    @FXML
    protected void onLoginButtonClick(ActionEvent event) {
        SceneSwitcher.switchScene(event, "login.fxml", "LibraTrack");
    }
}
