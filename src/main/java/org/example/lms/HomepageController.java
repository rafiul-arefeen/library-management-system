package org.example.lms;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HomepageController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label lblBooksIssued;

    @FXML
    private Label lblPendingRequests;

    @FXML
    private Label lblUsersWithPenalty;

    @FXML
    private Label lblTotalBooks;

    @FXML
    public void initialize() {
        String username = SessionManager.getUsername();  // assuming you are using SessionManager already
        welcomeLabel.setText("Welcome, " + username + "!");
        loadDashboardStatistics();
    }

    public void loadDashboardStatistics() {
        try (Connection connection = DatabaseConnection.connect()) {

            PreparedStatement issuedStmt = connection.prepareStatement(
                    "SELECT COUNT(*) AS issuedCount FROM issued_book_details");
            ResultSet issuedRs = issuedStmt.executeQuery();
            if (issuedRs.next()) {
                lblBooksIssued.setText(String.valueOf(issuedRs.getInt("issuedCount")));
            }

            PreparedStatement requestsStmt = connection.prepareStatement(
                    "SELECT (SELECT COUNT(*) FROM request_issue) + (SELECT COUNT(*) FROM request_return) AS totalRequests");
            ResultSet requestsRs = requestsStmt.executeQuery();
            if (requestsRs.next()) {
                lblPendingRequests.setText(String.valueOf(requestsRs.getInt("totalRequests")));
            }

            PreparedStatement penaltyStmt = connection.prepareStatement(
                    "SELECT COUNT(DISTINCT member_id) AS usersWithPenalty " +
                            "FROM issued_book_details " +
                            "WHERE DATEDIFF(CURDATE(), due_date) > 0");
            ResultSet penaltyRs = penaltyStmt.executeQuery();
            if (penaltyRs.next()) {
                lblUsersWithPenalty.setText(String.valueOf(penaltyRs.getInt("usersWithPenalty")));
            }

            PreparedStatement booksStmt = connection.prepareStatement(
                    "SELECT SUM(quantity) AS totalBooks FROM books");
            ResultSet booksRs = booksStmt.executeQuery();
            if (booksRs.next()) {
                lblTotalBooks.setText(String.valueOf(booksRs.getInt("totalBooks")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            lblBooksIssued.setText("Error");
            lblPendingRequests.setText("Error");
            lblUsersWithPenalty.setText("Error");
            lblTotalBooks.setText("Error");
        }
    }
}
