package org.example.lms;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.time.LocalDate;

public class RequestReturnController {

    @FXML private TableView<IssuedBook> tableIssuedBooks;
    @FXML private TableColumn<IssuedBook, Integer> colBookId;
    @FXML private TableColumn<IssuedBook, String> colBookName;
    @FXML private TableColumn<IssuedBook, LocalDate> colIssueDate, colDueDate;
    @FXML private TableColumn<IssuedBook, String> colStatus;
    @FXML private TextField txtSearchIssuedBooks;

    @FXML private Button btnRequestReturn;

    private ObservableList<IssuedBook> issuedBooksList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadIssuedBooks();
        setupTable();
        setupSearch();

        tableIssuedBooks.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            btnRequestReturn.setDisable(newSelection == null);
        });

        btnRequestReturn.setDisable(true);
    }

    public static class IssuedBook {
        private final int bookId;
        private final String bookName;
        private final LocalDate issueDate;
        private final LocalDate dueDate;
        private final String status;
        private final int issuedId;

        public IssuedBook(int issuedId, int bookId, String bookName, LocalDate issueDate, LocalDate dueDate, String status) {
            this.issuedId = issuedId;
            this.bookId = bookId;
            this.bookName = bookName;
            this.issueDate = issueDate;
            this.dueDate = dueDate;
            this.status = status;
        }

        public int getIssuedId() { return issuedId; }
        public int getBookId() { return bookId; }
        public String getBookName() { return bookName; }
        public LocalDate getIssueDate() { return issueDate; }
        public LocalDate getDueDate() { return dueDate; }
        public String getStatus() { return status; }
    }

    private void loadIssuedBooks() {
        issuedBooksList.clear();
        String username = SessionManager.getUsername();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT ibd.id, ibd.book_id, b.book_name, ibd.issue_date, ibd.due_date, ibd.status " +
                             "FROM issued_book_details ibd " +
                             "JOIN books b ON ibd.book_id = b.book_id " +
                             "JOIN users u ON ibd.member_id = u.user_id " +
                             "WHERE u.username = ?")) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                issuedBooksList.add(new IssuedBook(
                        rs.getInt("id"),
                        rs.getInt("book_id"),
                        rs.getString("book_name"),
                        rs.getDate("issue_date").toLocalDate(),
                        rs.getDate("due_date").toLocalDate(),
                        rs.getString("status")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupTable() {
        colBookId.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        colBookName.setCellValueFactory(new PropertyValueFactory<>("bookName"));
        colIssueDate.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tableIssuedBooks.setItems(issuedBooksList);
    }

    private void setupSearch() {
        FilteredList<IssuedBook> filteredList = new FilteredList<>(issuedBooksList, p -> true);

        txtSearchIssuedBooks.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(book -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String filter = newValue.toLowerCase();
                return book.getBookName().toLowerCase().contains(filter) ||
                        String.valueOf(book.getBookId()).contains(filter) ||
                        book.getStatus().toLowerCase().contains(filter);
            });
        });

        tableIssuedBooks.setItems(filteredList);
    }

    @FXML
    private void onRequestReturn(ActionEvent event) {
        IssuedBook selectedBook = tableIssuedBooks.getSelectionModel().getSelectedItem();

        if (selectedBook == null) {
            showAlert(Alert.AlertType.ERROR, "No Book Selected", "Please select a book to request return.");
            return;
        }

        try (Connection connection = DatabaseConnection.connect()) {
            int memberId = getMemberId(SessionManager.getUsername());

            // Check for duplicate return request
            try (PreparedStatement checkStmt = connection.prepareStatement(
                    "SELECT * FROM request_return WHERE issued_id = ? AND member_id = ?")) {
                checkStmt.setInt(1, selectedBook.getIssuedId());
                checkStmt.setInt(2, memberId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    showAlert(Alert.AlertType.ERROR, "Already Requested", "You’ve already requested to return this book.");
                    return;
                }
            }

            // Insert return request
            try (PreparedStatement insertStmt = connection.prepareStatement(
                    "INSERT INTO request_return (member_id, issued_id, request_date) VALUES (?, ?, CURRENT_DATE)")) {
                insertStmt.setInt(1, memberId);
                insertStmt.setInt(2, selectedBook.getIssuedId());
                insertStmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Request Submitted", "Your return request has been submitted.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while requesting the return.");
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
        throw new RuntimeException("Failed to find member ID for username: " + username);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
