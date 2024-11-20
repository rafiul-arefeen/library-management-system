package org.example.lms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.Objects;

public class AdminDashboardController {

    @FXML
    private Button btnHomepage, btnManageBooks, btnIssueBook, btnReturnBook, btnViewRequests, btnManageMembers, btnLogout;

    @FXML
    private AnchorPane contentArea;

    @FXML
    private Label welcomeLabel;

    private Button activeButton = null; // Track the currently active button

    @FXML
    public void initialize() {
        String username = SessionManager.getUsername();
        welcomeLabel.setText(username);

        loadPage("Homepage.fxml"); // Default to homepage on load
        highlightButton(btnHomepage);
    }

    @FXML
    private void onSidebarOptionClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        highlightButton(clickedButton);

        String fxmlFile = "";
        if (clickedButton == btnHomepage) {
            fxmlFile = "Homepage.fxml";
        } else if (clickedButton == btnManageBooks) {
            fxmlFile = "ManageBooks.fxml";
        } else if (clickedButton == btnIssueBook) {
            fxmlFile = "IssueBook.fxml";
        } else if (clickedButton == btnReturnBook) {
            fxmlFile = "ReturnBook.fxml";
        } else if (clickedButton == btnViewRequests) {
            fxmlFile = "ViewRequests.fxml";
        } else if (clickedButton == btnManageMembers) {
            fxmlFile = "ManageMembers.fxml";
        } else if (clickedButton == btnLogout) {
            // Navigate to login screen on logout
            SceneSwitcher.switchScene(event, "login.fxml", "Login");
            return;
        }

        loadPage(fxmlFile);
    }

    private void loadPage(String fxmlFile) {
        try {
            Parent page = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlFile)));
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
        activeButton.setStyle("-fx-background-color: #9ccc65; -fx-border-color: #7cb342; -fx-effect: innershadow(two-pass-box, rgba(0,0,0,0.2), 4, 0, 0, 2);");
    }
}
