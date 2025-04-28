package org.example.lms;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;

import java.sql.*;


public class ViewRequestsController {

    @FXML private TableView<IssueRequest> tableIssueRequests;
    @FXML private TableColumn<IssueRequest, Integer> colIssueMemberId, colIssueBookId;
    @FXML private TableColumn<IssueRequest, String> colIssueMemberName, colIssueBookName, colIssueRequestDate;

    @FXML private TableView<ReturnRequest> tableReturnRequests;
    @FXML private TableColumn<ReturnRequest, Integer> colReturnMemberId, colReturnBookId;
    @FXML private TableColumn<ReturnRequest, String> colReturnMemberName, colReturnBookName, colReturnRequestDate;

    @FXML private TextField txtMemberName, txtMemberEmail, txtMemberPhone;
    @FXML private TextField txtBookName, txtBookAuthor;
    @FXML private TextField txtPenaltyPerDay, txtOverdueDays, txtTotalPenalty;

    @FXML private Button btnAcceptIssue, btnAcceptReturn;

    private ObservableList<IssueRequest> issueRequestsList = FXCollections.observableArrayList();
    private ObservableList<ReturnRequest> returnRequestsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupIssueRequestsTable();
        setupReturnRequestsTable();
        loadIssueRequests();
        loadReturnRequests();

        tableIssueRequests.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fillDetailsFromIssueRequest(newVal);
                toggleButtons("issue");
            }
        });

        tableReturnRequests.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fillDetailsFromReturnRequest(newVal);
                toggleButtons("return");
            }
        });

        txtPenaltyPerDay.textProperty().addListener((obs, oldVal, newVal) -> calculatePenalty());
    }

    private void setupIssueRequestsTable() {
        colIssueMemberId.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        colIssueMemberName.setCellValueFactory(new PropertyValueFactory<>("memberName"));
        colIssueBookId.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        colIssueBookName.setCellValueFactory(new PropertyValueFactory<>("bookName"));
        colIssueRequestDate.setCellValueFactory(new PropertyValueFactory<>("requestDate"));

        tableIssueRequests.setItems(issueRequestsList);
    }

    private void setupReturnRequestsTable() {
        colReturnMemberId.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        colReturnMemberName.setCellValueFactory(new PropertyValueFactory<>("memberName"));
        colReturnBookId.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        colReturnBookName.setCellValueFactory(new PropertyValueFactory<>("bookName"));
        colReturnRequestDate.setCellValueFactory(new PropertyValueFactory<>("requestDate"));

        tableReturnRequests.setItems(returnRequestsList);
    }

    private void loadIssueRequests() {
        issueRequestsList.clear();
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT ri.member_id, u.name AS member_name, ri.book_id, b.book_name, ri.request_date " +
                             "FROM request_issue ri " +
                             "JOIN users u ON ri.member_id = u.user_id " +
                             "JOIN books b ON ri.book_id = b.book_id")) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                issueRequestsList.add(new IssueRequest(
                        rs.getInt("member_id"),
                        rs.getString("member_name"),
                        rs.getInt("book_id"),
                        rs.getString("book_name"),
                        rs.getString("request_date")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadReturnRequests() {
        returnRequestsList.clear();
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT rr.id AS request_id, rr.member_id, u.name AS member_name, ibd.book_id, b.book_name, rr.request_date " +
                             "FROM request_return rr " +
                             "JOIN users u ON rr.member_id = u.user_id " +
                             "JOIN issued_book_details ibd ON rr.issued_id = ibd.id " +
                             "JOIN books b ON ibd.book_id = b.book_id")) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                returnRequestsList.add(new ReturnRequest(
                        rs.getInt("request_id"),
                        rs.getInt("member_id"),
                        rs.getString("member_name"),
                        rs.getInt("book_id"),
                        rs.getString("book_name"),
                        rs.getString("request_date")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fillDetailsFromIssueRequest(IssueRequest request) {
        txtMemberName.setText(request.getMemberName());
        txtBookName.setText(request.getBookName());
        clearPenaltyFields();
    }

    private void fillDetailsFromReturnRequest(ReturnRequest request) {
        txtMemberName.setText(request.getMemberName());
        txtBookName.setText(request.getBookName());
        calculatePenaltyForReturn(request);
    }

    private void calculatePenaltyForReturn(ReturnRequest request) {
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT DATEDIFF(CURDATE(), ibd.due_date) AS overdue_days " +
                             "FROM issued_book_details ibd " +
                             "WHERE ibd.book_id = ? AND ibd.member_id = ?")) {

            stmt.setInt(1, request.getBookId());
            stmt.setInt(2, request.getMemberId());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int overdueDays = Math.max(0, rs.getInt("overdue_days"));
                txtOverdueDays.setText(String.valueOf(overdueDays));
                calculatePenalty();
            } else {
                txtOverdueDays.setText("0");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void calculatePenalty() {
        try {
            int overdueDays = Integer.parseInt(txtOverdueDays.getText().trim());
            int penaltyPerDay = Integer.parseInt(txtPenaltyPerDay.getText().trim());
            txtTotalPenalty.setText(String.valueOf(overdueDays * penaltyPerDay));
        } catch (NumberFormatException e) {
            txtTotalPenalty.setText("0");
        }
    }

    private void clearPenaltyFields() {
        txtPenaltyPerDay.clear();
        txtOverdueDays.clear();
        txtTotalPenalty.clear();
    }

    private void toggleButtons(String type) {
        boolean isIssue = type.equals("issue");
        btnAcceptIssue.setDisable(!isIssue);
        btnAcceptReturn.setDisable(isIssue);
        txtPenaltyPerDay.setDisable(isIssue);
        txtOverdueDays.setDisable(isIssue);
        txtTotalPenalty.setDisable(isIssue);
    }

    @FXML
    private void onAcceptIssue(ActionEvent event) {
        IssueRequest selectedRequest = tableIssueRequests.getSelectionModel().getSelectedItem();

        if (selectedRequest == null) {
            showAlert(Alert.AlertType.ERROR, "No Request Selected", "Please select an issue request to proceed.");
            return;
        }

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement checkStmt = connection.prepareStatement(
                     "SELECT quantity FROM books WHERE book_id = ?")) {

            checkStmt.setInt(1, selectedRequest.getBookId());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt("quantity") <= 0) {
                showAlert(Alert.AlertType.ERROR, "Book Not Available", "The selected book is out of stock.");
                return;
            }

            try (PreparedStatement issueStmt = connection.prepareStatement(
                    "INSERT INTO issued_book_details (book_id, member_id, issue_date, due_date, status) " +
                            "VALUES (?, ?, CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY), 'On Time')")) {

                issueStmt.setInt(1, selectedRequest.getBookId());
                issueStmt.setInt(2, selectedRequest.getMemberId());
                issueStmt.executeUpdate();

                try (PreparedStatement updateBookStmt = connection.prepareStatement(
                        "UPDATE books SET quantity = quantity - 1 WHERE book_id = ?")) {
                    updateBookStmt.setInt(1, selectedRequest.getBookId());
                    updateBookStmt.executeUpdate();
                }

                try (PreparedStatement deleteRequestStmt = connection.prepareStatement(
                        "DELETE FROM request_issue WHERE member_id = ? AND book_id = ?")) {
                    deleteRequestStmt.setInt(1, selectedRequest.getMemberId());
                    deleteRequestStmt.setInt(2, selectedRequest.getBookId());
                    deleteRequestStmt.executeUpdate();
                }

                showAlert(Alert.AlertType.INFORMATION, "Success", "Book issued successfully.");
                loadIssueRequests();
                btnAcceptIssue.setDisable(true);

            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while processing the issue request.");


        }
    }

    @FXML
    private void onAcceptReturn(ActionEvent event) {
        ReturnRequest selectedRequest = tableReturnRequests.getSelectionModel().getSelectedItem();

        if (selectedRequest == null) {
            showAlert(Alert.AlertType.ERROR, "No Request Selected", "Please select a return request to proceed.");
            return;
        }

        int penaltyPerDay = 0; // Default to zero
        try {
            String penaltyStr = txtPenaltyPerDay.getText().trim();
            if (!penaltyStr.isEmpty()) {
                penaltyPerDay = Integer.parseInt(penaltyStr);
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Penalty per day must be a valid number.");
            return;
        }

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement checkStmt = connection.prepareStatement(
                     "SELECT ibd.id, ibd.book_id, DATEDIFF(CURRENT_DATE, ibd.due_date) AS overdue_days " +
                             "FROM issued_book_details ibd " +
                             "WHERE ibd.book_id = ? AND ibd.member_id = ?")) {

            checkStmt.setInt(1, selectedRequest.getBookId());
            checkStmt.setInt(2, selectedRequest.getMemberId());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                int issuedId = rs.getInt("id");
                int overdueDays = Math.max(0, rs.getInt("overdue_days"));
                int totalPenalty = overdueDays * penaltyPerDay;

                try (PreparedStatement deleteIssued = connection.prepareStatement(
                        "DELETE FROM issued_book_details WHERE id = ?")) {
                    deleteIssued.setInt(1, issuedId);
                    deleteIssued.executeUpdate();
                }

                try (PreparedStatement updateBookStmt = connection.prepareStatement(
                        "UPDATE books SET quantity = quantity + 1 WHERE book_id = ?")) {
                    updateBookStmt.setInt(1, selectedRequest.getBookId());
                    updateBookStmt.executeUpdate();
                }

                try (PreparedStatement deleteRequestStmt = connection.prepareStatement(
                        "DELETE FROM request_return WHERE id = ?")) {
                    deleteRequestStmt.setInt(1, selectedRequest.getRequestId());
                    deleteRequestStmt.executeUpdate();
                }

                showAlert(Alert.AlertType.INFORMATION, "Success", "Return processed. Penalty: " + totalPenalty);
                loadReturnRequests();
                btnAcceptReturn.setDisable(true);

            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while processing the return request.");
        }
    }


    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static class IssueRequest {
        private final int memberId;
        private final String memberName;
        private final int bookId;
        private final String bookName;
        private final String requestDate;

        public IssueRequest(int memberId, String memberName, int bookId, String bookName, String requestDate) {
            this.memberId = memberId;
            this.memberName = memberName;
            this.bookId = bookId;
            this.bookName = bookName;
            this.requestDate = requestDate;
        }

        public int getMemberId() { return memberId; }
        public String getMemberName() { return memberName; }
        public int getBookId() { return bookId; }
        public String getBookName() { return bookName; }
        public String getRequestDate() { return requestDate; }
    }

    public static class ReturnRequest {
        private final int requestId;
        private final int memberId;
        private final String memberName;
        private final int bookId;
        private final String bookName;
        private final String requestDate;

        public ReturnRequest(int requestId, int memberId, String memberName, int bookId, String bookName, String requestDate) {
            this.requestId = requestId;
            this.memberId = memberId;
            this.memberName = memberName;
            this.bookId = bookId;
            this.bookName = bookName;
            this.requestDate = requestDate;
        }

        public int getRequestId() { return requestId; }
        public int getMemberId() { return memberId; }
        public String getMemberName() { return memberName; }
        public int getBookId() { return bookId; }
        public String getBookName() { return bookName; }
        public String getRequestDate() { return requestDate; }
    }
}
