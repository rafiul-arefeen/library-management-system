package org.example.lms;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomepageController {

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        // Set a welcome message or any default content
        welcomeLabel.setText("Welcome to the Admin Homepage!");
    }

    // Additional methods to handle actions on the homepage
    public void loadDashboardStatistics() {
        // Example method to load some stats (if applicable)
        System.out.println("Loading dashboard statistics...");
    }
}
