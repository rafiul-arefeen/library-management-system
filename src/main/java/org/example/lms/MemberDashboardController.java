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
    private Button activeButton = null; // Track the currently active button

    @FXML
    public void initialize() {
        String username = SessionManager.getUsername();
        //welcomeLabel.setText(username);

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
            fxmlFile = "RequestReturn.fxml";
        } else if (clickedButton == btnLogout) {
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
            activeButton.getStyleClass().remove("active-button"); // Remove old active class
        }

        activeButton = clickedButton;
        if (!activeButton.getStyleClass().contains("active-button")) {
            activeButton.getStyleClass().add("active-button"); // Add active class
        }
    }
}
