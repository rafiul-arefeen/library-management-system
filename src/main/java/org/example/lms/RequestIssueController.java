package org.example.lms;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RequestIssueController {

    @FXML
    private TableView<Book> tableBooks;
    @FXML
    private TableColumn<Book, Integer> colBookId;
    @FXML
    private TableColumn<Book, String> colBookName, colAuthorName, colCategory, colEdition, colLanguage;
    @FXML
    private TableColumn<Book, Integer> colQuantity;
    @FXML
    private TextField txtSearchBooks, txtBookName, txtBookAuthor, txtBookCategory, txtBookEdition, txtBookLanguage, txtBookQuantity;
    @FXML
    private Button btnRequestIssue;

    private ObservableList<Book> booksList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadBooks();
        setupBooksTable();
        setupBooksSearch();

        btnRequestIssue.setDisable(true);

        // Enable request button only when a book is selected
        tableBooks.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            btnRequestIssue.setDisable(newSelection == null);
            if (newSelection != null) {
                autoFillBookDetails(newSelection);
            }
        });

        setupTextFieldListeners();
    }

    public static class Book {
        private final int id;
        private final String name, author, category, edition, language;
        private final int quantity;

        public Book(int id, String name, String author, String category, String edition, String language, int quantity) {
            this.id = id;
            this.name = name;
            this.author = author;
            this.category = category;
            this.edition = edition;
            this.language = language;
            this.quantity = quantity;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getAuthor() {
            return author;
        }

        public String getCategory() {
            return category;
        }

        public String getEdition() {
            return edition;
        }

        public String getLanguage() {
            return language;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    private void loadBooks() {
        booksList.clear();
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement("SELECT * FROM books WHERE quantity > 0")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                booksList.add(new Book(
                        rs.getInt("book_id"),
                        rs.getString("book_name"),
                        rs.getString("author_name"),
                        rs.getString("category"),
                        rs.getString("edition"),
                        rs.getString("language"),
                        rs.getInt("quantity")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupBooksTable() {
        colBookId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBookName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAuthorName.setCellValueFactory(new PropertyValueFactory<>("author"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colEdition.setCellValueFactory(new PropertyValueFactory<>("edition"));
        colLanguage.setCellValueFactory(new PropertyValueFactory<>("language"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        tableBooks.setItems(booksList);

        tableBooks.setOnMouseClicked(event -> {
            Book selectedBook = tableBooks.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                btnRequestIssue.setDisable(false);
            }
        });
    }

    private void setupBooksSearch() {
        FilteredList<Book> filteredBooks = new FilteredList<>(booksList, b -> true);

        txtSearchBooks.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredBooks.setPredicate(book -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return book.getName().toLowerCase().contains(lowerCaseFilter) ||
                        book.getAuthor().toLowerCase().contains(lowerCaseFilter) ||
                        book.getCategory().toLowerCase().contains(lowerCaseFilter) ||
                        book.getLanguage().toLowerCase().contains(lowerCaseFilter) ||
                        String.valueOf(book.getId()).contains(lowerCaseFilter);
            });
        });

        tableBooks.setItems(filteredBooks);
    }

    private void autoFillBookDetails(Book selectedBook) {
        txtBookName.setText(selectedBook.getName());
        txtBookAuthor.setText(selectedBook.getAuthor());
        txtBookCategory.setText(selectedBook.getCategory());
        txtBookEdition.setText(selectedBook.getEdition());
        txtBookLanguage.setText(selectedBook.getLanguage());
        txtBookQuantity.setText(String.valueOf(selectedBook.getQuantity()));
    }

    private void setupTextFieldListeners() {
        txtBookName.textProperty().addListener((obs, oldValue, newValue) -> updateFieldsByInput());
        txtBookAuthor.textProperty().addListener((obs, oldValue, newValue) -> updateFieldsByInput());
        txtBookCategory.textProperty().addListener((obs, oldValue, newValue) -> updateFieldsByInput());
        txtBookEdition.textProperty().addListener((obs, oldValue, newValue) -> updateFieldsByInput());
        txtBookLanguage.textProperty().addListener((obs, oldValue, newValue) -> updateFieldsByInput());
    }

    private void updateFieldsByInput() {
        String bookName = txtBookName.getText().trim().toLowerCase();
        String author = txtBookAuthor.getText().trim().toLowerCase();
        String category = txtBookCategory.getText().trim().toLowerCase();
        String edition = txtBookEdition.getText().trim().toLowerCase();
        String language = txtBookLanguage.getText().trim().toLowerCase();

        for (Book book : booksList) {
            if (book.getName().toLowerCase().contains(bookName) &&
                    book.getAuthor().toLowerCase().contains(author) &&
                    book.getCategory().toLowerCase().contains(category) &&
                    book.getEdition().toLowerCase().contains(edition) &&
                    book.getLanguage().toLowerCase().contains(language)) {

                autoFillBookDetails(book);
                tableBooks.getSelectionModel().select(book);
                return;
            }
        }

        // Clear fields if no match is found
        clearBookFields();
    }

    private void clearBookFields() {
        txtBookQuantity.clear();
        tableBooks.getSelectionModel().clearSelection();
    }

    @FXML
    private void onRequestIssue(ActionEvent event) {
        Book selectedBook = tableBooks.getSelectionModel().getSelectedItem();

        if (selectedBook == null) {
            showAlert(Alert.AlertType.ERROR, "Selection Missing", "Please select a book.");
            return;
        }

        try (Connection connection = DatabaseConnection.connect()) {
            int loggedInMemberId = getMemberId(SessionManager.getUsername());

            // Check if book is already issued
            try (PreparedStatement checkStmt = connection.prepareStatement(
                    "SELECT * FROM issued_book_details WHERE book_id = ? AND member_id = ?")) {
                checkStmt.setInt(1, selectedBook.getId());
                checkStmt.setInt(2, loggedInMemberId);

                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    showAlert(Alert.AlertType.ERROR, "Already Issued", "You have already issued this book.");
                    return;
                }
            }

            // Check for duplicate requests
            try (PreparedStatement duplicateStmt = connection.prepareStatement(
                    "SELECT * FROM request_issue WHERE book_id = ? AND member_id = ?")) {
                duplicateStmt.setInt(1, selectedBook.getId());
                duplicateStmt.setInt(2, loggedInMemberId);

                ResultSet rs = duplicateStmt.executeQuery();
                if (rs.next()) {
                    showAlert(Alert.AlertType.ERROR, "Duplicate Request", "You have already requested this book.");
                    return;
                }
            }

            // Insert request
            try (PreparedStatement insertStmt = connection.prepareStatement(
                    "INSERT INTO request_issue (member_id, book_id) VALUES (?, ?)")) {
                insertStmt.setInt(1, loggedInMemberId);
                insertStmt.setInt(2, selectedBook.getId());
                insertStmt.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Success", "Request for issue has been submitted.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while requesting the book.");
        }
    }

    private int getMemberId(String username) {
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement("SELECT user_id FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Failed to fetch member ID for username: " + username);
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
