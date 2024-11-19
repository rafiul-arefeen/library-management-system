package org.example.lms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private AuthService authService = new AuthService();

    @FXML
    protected void onLoginButtonClick(ActionEvent event) throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Check if fields are empty
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Field(s) cannot be empty!");
            return;
        }

        // Authenticate user and get role
        String role = authService.authenticate(username, password);

        if (role == null) {
            errorLabel.setText("Invalid username or password!");
        } else if (role.equals("admin")) {
            SceneSwitcher.switchScene(event, "AdminDashboard.fxml", "Admin Dashboard");
        } else {
            SceneSwitcher.switchScene(event, "MemberDashboard.fxml", "Member Dashboard");
        }
    }

    @FXML
    protected void onRegisterButtonClick(ActionEvent event) throws IOException {
        SceneSwitcher.switchScene(event, "register.fxml", "Register");
    }
}
