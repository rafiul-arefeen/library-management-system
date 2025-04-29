package org.example.lms;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserHomepageController {

    @FXML private Label welcomeMessage;
    @FXML private Label lblIssuedCount;
    @FXML private Label lblPenaltyAmount;

    public void initialize() {
        String username = SessionManager.getUsername();  // same as in RequestReturnController
        welcomeMessage.setText("Welcome, " + username + "!");
        loadUserStats(username);
    }

    private void loadUserStats(String username) {
        try (Connection connection = DatabaseConnection.connect()) {

            // Get member_id
            int userId = getMemberId(username);

            // 1. Count issued books
            PreparedStatement countIssuedStmt = connection.prepareStatement(
                    "SELECT COUNT(*) AS issuedCount FROM issued_book_details WHERE member_id = ?");
            countIssuedStmt.setInt(1, userId);
            ResultSet rs1 = countIssuedStmt.executeQuery();
            if (rs1.next()) {
                lblIssuedCount.setText(rs1.getInt("issuedCount") + "");
            }

            // 2. Calculate total penalty
            PreparedStatement penaltyStmt = connection.prepareStatement(
                    "SELECT SUM(GREATEST(DATEDIFF(CURDATE(), due_date), 0) * 10) AS totalPenalty " +
                            "FROM issued_book_details WHERE member_id = ?");
            penaltyStmt.setInt(1, userId);
            ResultSet rs2 = penaltyStmt.executeQuery();
            if (rs2.next()) {
                int penalty = rs2.getInt("totalPenalty");
                lblPenaltyAmount.setText(penalty + "");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            lblIssuedCount.setText("Error");
            lblPenaltyAmount.setText("Error");
        }
    }

    private int getMemberId(String username) {
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement("SELECT user_id FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("user_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Failed to find user ID for username: " + username);
    }
}
