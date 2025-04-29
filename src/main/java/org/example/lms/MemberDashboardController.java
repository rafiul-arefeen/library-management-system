package org.example.lms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Label;

import java.io.IOException;

public class MemberDashboardController {

    @FXML
    private Button btnHomepage, btnRequestIssue, btnRequestReturn, btnLogout;

    @FXML
    private AnchorPane contentArea;

    @FXML
    private Label welcomeLabel;

    private Button activeButton = null; // Track the currently active button

    @FXML
    public void initialize() {
        String username = SessionManager.getUsername();
        welcomeLabel.setText(username);

        loadPage("UserHomepage.fxml"); // Default to homepage on load
        highlightButton(btnHomepage);
    }

    @FXML
    private void onSidebarOptionClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        highlightButton(clickedButton);

        String fxmlFile = "";
        if (clickedButton == btnHomepage) {
            fxmlFile = "UserHomepage.fxml";
        } else if (clickedButton == btnRequestIssue) {
            fxmlFile = "RequestIssue.fxml";
        } else if (clickedButton == btnRequestReturn) {
            fxmlFile = "ReturnBook.fxml";
        } else if (clickedButton == btnLogout) {
            // Navigate to login screen on logout
            SceneSwitcher.switchScene(event, "login.fxml", "Login");
            return;
        }

        loadPage(fxmlFile);
    }

    private void loadPage(String fxmlFile) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource(fxmlFile));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void highlightButton(Button clickedButton) {
        if (activeButton != null) {
            // Reset the previous active button's style
            activeButton.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #bdbdbd;");
        }

        // Set the clicked button as the active button
        activeButton = clickedButton;
        activeButton.setStyle("-fx-background-color: #4caf50; -fx-border-color: #388e3c; -fx-effect: innershadow(two-pass-box, rgba(0,0,0,0.2), 4, 0, 0, 2);");
    }
}
