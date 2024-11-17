package org.example.lms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    private String username, password;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    @FXML
    protected void onLoginButtonClick(ActionEvent event) throws IOException {
        username = usernameField.getText();
        password = passwordField.getText();

        AuthService authService = new AuthService();
        boolean isAuthenticated = authService.authenticate(username, password);

        if(isAuthenticated) {
            SceneSwitcher.switchScene(event, "dashboard.fxml", "Dashboard");
        }
        else {
            errorLabel.setText("Wrong password, try again.");
        }
    }

    @FXML
    protected void onRegisterButtonClick(ActionEvent event) throws IOException {
        SceneSwitcher.switchScene(event, "register.fxml", "Register");
    }
}