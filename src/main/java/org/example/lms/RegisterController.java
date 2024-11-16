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

public class RegisterController {
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    @FXML
    protected void onRegisterButtonClick(ActionEvent event) throws IOException {
        firstName = firstNameField.getText();
        lastName = lastNameField.getText();
        email = emailField.getText();
        username = usernameField.getText();
        password = passwordField.getText();

        // Handle adding new user logic here
    }

    @FXML
    protected void onLoginButtonClick(ActionEvent event) {
        try {
            // Load the new FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set the new scene
            stage.setScene(new Scene(root, 1280, 768)); // Set desired dimensions
            stage.setTitle("LibraTrack");

            // Show the updated stage
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
