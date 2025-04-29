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

public class ReturnBookController {

    // Issued Books Table
    @FXML private TableView<IssuedBook> tableIssuedBooks;
    @FXML private TableColumn<IssuedBook, Integer> colBookId;

    @FXML private TableColumn<IssuedBook, String> colBookName, colMemberUsername;
    @FXML private TableColumn<IssuedBook, LocalDate> colIssueDate, colDueDate;
    @FXML private TableColumn<IssuedBook, String> colStatus;
    @FXML private TextField txtSearchIssuedBooks;

    // Member and Book Details
    @FXML private TextField txtMemberUsername, txtMemberName, txtMemberEmail, txtMemberPhone;
    @FXML private TextField txtBookId, txtBookName, txtBookAuthor, txtBookCategory, txtBookEdition, txtBookLanguage, txtBookQuantity;
    @FXML private TextField txtPenaltyPerDay, txtOverdueDays, txtTotalPenalty;

    private ObservableList<IssuedBook> issuedBooksList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadIssuedBooks();
        setupIssuedBooksTable();
        setupIssuedBooksSearch();

        // Add listeners for dynamic penalty calculation
        txtPenaltyPerDay.textProperty().addListener((observable, oldValue, newValue) -> calculatePenalty());
        txtMemberUsername.textProperty().addListener((observable, oldValue, newValue) -> autoFillMemberDetails());
        txtBookId.textProperty().addListener((observable, oldValue, newValue) -> autoFillBookDetails());

        // Default overdue days and penalty
        txtPenaltyPerDay.setText("10");
        txtOverdueDays.setText("0");
        txtTotalPenalty.setText("0");
    }

    // Issued Book Class
    public static class IssuedBook {
        private final int bookId; // Add bookId field
        private final String bookName;
        private final String memberUsername;
        private final LocalDate issueDate;
        private final LocalDate dueDate;
        private final String status;

        public IssuedBook(int bookId, String bookName, String memberUsername, LocalDate issueDate, LocalDate dueDate, String status) {
            this.bookId = bookId;
            this.bookName = bookName;
            this.memberUsername = memberUsername;
            this.issueDate = issueDate;
            this.dueDate = dueDate;
            this.status = status;
        }

        public int getBookId() {
            return bookId;
        }

        public String getBookName() {
            return bookName;
        }

        public String getMemberUsername() {
            return memberUsername;
        }

        public LocalDate getIssueDate() {
            return issueDate;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        public String getStatus() {
            return status;
        }
    }


    private void loadIssuedBooks() {
        issuedBooksList.clear();

        try (Connection connection = DatabaseConnection.connect()) {
            // Call the stored procedure to update the status
            try (CallableStatement callableStmt = connection.prepareCall("{CALL UpdateBookStatus()}")) {
                callableStmt.execute();
            }

            // Fetch the updated issued books
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT ibd.book_id, b.book_name, u.username, ibd.issue_date, ibd.due_date, ibd.status " +
                            "FROM issued_book_details ibd " +
                            "JOIN books b ON ibd.book_id = b.book_id " +
                            "JOIN users u ON ibd.member_id = u.user_id")) {

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    issuedBooksList.add(new IssuedBook(
                            rs.getInt("book_id"), // Include book_id
                            rs.getString("book_name"),
                            rs.getString("username"),
                            rs.getDate("issue_date").toLocalDate(),
                            rs.getDate("due_date").toLocalDate(),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void setupIssuedBooksTable() {
        colBookId.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        colBookName.setCellValueFactory(new PropertyValueFactory<>("bookName"));
        colMemberUsername.setCellValueFactory(new PropertyValueFactory<>("memberUsername"));
        colIssueDate.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tableIssuedBooks.setItems(issuedBooksList);

        // Listener for row selection
        tableIssuedBooks.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillDetailsFromIssuedBook(newSelection);
            }
        });

        // Mouse click listener to update on the same row click
        tableIssuedBooks.setOnMouseClicked(event -> {
            IssuedBook selectedBook = tableIssuedBooks.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                fillDetailsFromIssuedBook(selectedBook);
            }
        });
    }


    private void setupIssuedBooksSearch() {
        FilteredList<IssuedBook> filteredBooks = new FilteredList<>(issuedBooksList, b -> true);

        txtSearchIssuedBooks.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredBooks.setPredicate(book -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                return book.getBookName().toLowerCase().contains(lowerCaseFilter) ||
                        book.getMemberUsername().toLowerCase().contains(lowerCaseFilter);
            });
        });

        tableIssuedBooks.setItems(filteredBooks);
    }

    private void fillDetailsFromIssuedBook(IssuedBook issuedBook) {
        txtBookId.setText(String.valueOf(issuedBook.getBookId())); // Auto-fill book ID
        txtBookName.setText(issuedBook.getBookName());
        txtMemberUsername.setText(issuedBook.getMemberUsername());
        txtOverdueDays.setText(String.valueOf(calculateOverdueDays(issuedBook.getDueDate())));
        calculatePenalty();
    }


    private void autoFillBookDetails() {
        String bookIdText = txtBookId.getText().trim();
        if (bookIdText.isEmpty()) return;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement("SELECT * FROM books WHERE book_id = ?")) {

            stmt.setInt(1, Integer.parseInt(bookIdText));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                txtBookName.setText(rs.getString("book_name"));
                txtBookAuthor.setText(rs.getString("author_name"));
                txtBookCategory.setText(rs.getString("category"));
                txtBookEdition.setText(rs.getString("edition"));
                txtBookLanguage.setText(rs.getString("language"));
                txtBookQuantity.setText(String.valueOf(rs.getInt("quantity")));
            } else {
                clearBookFields();
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            clearBookFields();
        }
    }

    private void autoFillMemberDetails() {
        String username = txtMemberUsername.getText().trim();
        if (username.isEmpty()) return;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?")) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                txtMemberName.setText(rs.getString("name"));
                txtMemberEmail.setText(rs.getString("email"));
                txtMemberPhone.setText(rs.getString("phone_no"));
            } else {
                clearMemberFields();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            clearMemberFields();
        }
    }

    private int calculateOverdueDays(LocalDate dueDate) {
        return Math.max(0, LocalDate.now().compareTo(dueDate));
    }

    private void calculatePenalty() {
        String bookIdText = txtBookId.getText().trim();
        String memberUsername = txtMemberUsername.getText().trim();
        String penaltyPerDayText = txtPenaltyPerDay.getText().trim();

        if (bookIdText.isEmpty() || memberUsername.isEmpty() || penaltyPerDayText.isEmpty()) {
            txtTotalPenalty.setText("0");
            txtOverdueDays.setText("0");
            return;
        }

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT " +
                             "CASE WHEN CURDATE() > ibd.due_date THEN DATEDIFF(CURDATE(), ibd.due_date) ELSE 0 END AS overdue_days " +
                             "FROM issued_book_details ibd " +
                             "JOIN users u ON ibd.member_id = u.user_id " +
                             "WHERE ibd.book_id = ? AND u.username = ?")) {

            stmt.setInt(1, Integer.parseInt(bookIdText));
            stmt.setString(2, memberUsername);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int overdueDays = rs.getInt("overdue_days");
                txtOverdueDays.setText(String.valueOf(overdueDays));

                int penaltyPerDay = Integer.parseInt(penaltyPerDayText);
                txtTotalPenalty.setText(String.valueOf(overdueDays * penaltyPerDay));
            } else {
                txtTotalPenalty.setText("0");
                txtOverdueDays.setText("0");
            }
        } catch (SQLException | NumberFormatException e) {
            txtTotalPenalty.setText("0");
            txtOverdueDays.setText("0");
            e.printStackTrace();
        }
    }


    @FXML
    private void onReturnBook(ActionEvent event) {
        String bookIdText = txtBookId.getText().trim();
        String memberUsername = txtMemberUsername.getText().trim();

        if (bookIdText.isEmpty() || memberUsername.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing Details", "Please fill out all details before returning the book.");
            return;
        }

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement checkStmt = connection.prepareStatement(
                     "SELECT * FROM issued_book_details ibd " +
                             "JOIN users u ON ibd.member_id = u.user_id " +
                             "WHERE ibd.book_id = ? AND u.username = ?")) {

            checkStmt.setInt(1, Integer.parseInt(bookIdText));
            checkStmt.setString(2, memberUsername);

            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                showAlert(Alert.AlertType.ERROR, "Not Issued", "This book was not issued by this member.");
                return;
            }

            // Proceed with the return process
            try (PreparedStatement returnStmt = connection.prepareStatement(
                    "DELETE FROM issued_book_details WHERE book_id = ? AND member_id = ?")) {
                returnStmt.setInt(1, Integer.parseInt(bookIdText));
                returnStmt.setInt(2, rs.getInt("member_id"));
                returnStmt.executeUpdate();

                try (PreparedStatement updateBookStmt = connection.prepareStatement(
                        "UPDATE books SET quantity = quantity + 1 WHERE book_id = ?")) {
                    updateBookStmt.setInt(1, Integer.parseInt(bookIdText));
                    updateBookStmt.executeUpdate();
                }

                showAlert(Alert.AlertType.INFORMATION, "Success", "Book returned successfully.");
                loadIssuedBooks(); // Refresh issued books
                clearFields();
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while returning the book.");
        }
    }

    private void clearBookFields() {
        txtBookName.clear();
        txtBookAuthor.clear();
        txtBookCategory.clear();
        txtBookEdition.clear();
        txtBookLanguage.clear();
        txtBookQuantity.clear();
    }

    private void clearMemberFields() {
        txtMemberName.clear();
        txtMemberEmail.clear();
        txtMemberPhone.clear();
    }

    private void clearFields() {
        clearBookFields();
        clearMemberFields();
        txtPenaltyPerDay.clear();
        txtOverdueDays.clear();
        txtTotalPenalty.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
