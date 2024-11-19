package org.example.lms;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class UserHomepageController {

    @FXML
    private Label welcomeMessage;

    public void initialize() {
        // Set a default welcome message or fetch the logged-in user's name dynamically
        welcomeMessage.setText("Welcome to the User Dashboard!");
    }
}
