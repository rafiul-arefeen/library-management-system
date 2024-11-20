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
import java.util.Optional;

public class ManageBooksController {

    public Button btnAdd;
    public Button btnUpdate;
    public Button btnDelete;
    @FXML
    private TableView<Book> tableBooks;

    @FXML
    private TableColumn<Book, Integer> colId;
    @FXML
    private TableColumn<Book, String> colName;
    @FXML
    private TableColumn<Book, String> colAuthor;
    @FXML
    private TableColumn<Book, String> colCategory;
    @FXML
    private TableColumn<Book, String> colEdition;
    @FXML
    private TableColumn<Book, String> colLanguage;
    @FXML
    private TableColumn<Book, Integer> colQuantity;

    @FXML
    private TextField txtName;
    @FXML
    private TextField txtAuthor;
    @FXML
    private TextField txtCategory;
    @FXML
    private TextField txtEdition;
    @FXML
    private TextField txtLanguage;
    @FXML
    private TextField txtQuantity;
    @FXML
    private TextField txtSearch;

    private ObservableList<Book> booksList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadBooks();
        setupTable();
        setupSearchFunctionality();

        // Handle row selection to auto-fill fields
        tableBooks.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillFields(newSelection);
            }
        });

        addAutoFillListeners();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colEdition.setCellValueFactory(new PropertyValueFactory<>("edition"));
        colLanguage.setCellValueFactory(new PropertyValueFactory<>("language"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // Enable sorting for each column
        colId.setSortable(true);
        colName.setSortable(true);
        colAuthor.setSortable(true);
        colCategory.setSortable(true);
        colEdition.setSortable(true);
        colLanguage.setSortable(true);
        colQuantity.setSortable(true);

        // Set the data to the table
        tableBooks.setItems(booksList);

        tableBooks.setOnMouseClicked(event -> {
            ManageBooksController.Book selectedUser = tableBooks.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                fillFields(selectedUser);
            }
        });
    }


    private void loadBooks() {
        booksList.clear();
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement("SELECT * FROM books")) {

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

    private void addAutoFillListeners() {
        // Listen to all fields and trigger auto-fill when any is updated
        txtName.textProperty().addListener((observable, oldValue, newValue) -> autoFillQuantity());
        txtAuthor.textProperty().addListener((observable, oldValue, newValue) -> autoFillQuantity());
        txtCategory.textProperty().addListener((observable, oldValue, newValue) -> autoFillQuantity());
        txtEdition.textProperty().addListener((observable, oldValue, newValue) -> autoFillQuantity());
        txtLanguage.textProperty().addListener((observable, oldValue, newValue) -> autoFillQuantity());
    }

    private void autoFillQuantity() {
        String name = txtName.getText().trim();
        String author = txtAuthor.getText().trim();
        String category = txtCategory.getText().trim();
        String edition = txtEdition.getText().trim();
        String language = txtLanguage.getText().trim();

        if (!name.isEmpty() && !author.isEmpty() && !category.isEmpty() && !edition.isEmpty() && !language.isEmpty()) {
            for (Book book : booksList) {
                if (book.getName().equalsIgnoreCase(name) &&
                        book.getAuthor().equalsIgnoreCase(author) &&
                        book.getCategory().equalsIgnoreCase(category) &&
                        book.getEdition().equalsIgnoreCase(edition) &&
                        book.getLanguage().equalsIgnoreCase(language)) {
                    txtQuantity.setText(String.valueOf(book.getQuantity()));
                    return;
                }
            }
        }
    }

    private void fillFields(Book book) {
        txtName.setText(book.getName());
        txtAuthor.setText(book.getAuthor());
        txtCategory.setText(book.getCategory());
        txtEdition.setText(book.getEdition());
        txtLanguage.setText(book.getLanguage());
        txtQuantity.setText(String.valueOf(book.getQuantity()));
    }

    private void setupSearchFunctionality() {
        FilteredList<Book> filteredList = new FilteredList<>(booksList, p -> true);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(book -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                String regex = ".*" + lowerCaseFilter + ".*";

                return String.valueOf(book.getId()).matches(regex) ||
                        book.getName().toLowerCase().matches(regex) ||
                        book.getAuthor().toLowerCase().matches(regex) ||
                        book.getCategory().toLowerCase().matches(regex) ||
                        (book.getEdition() != null && book.getEdition().toLowerCase().matches(regex)) ||
                        book.getLanguage().toLowerCase().matches(regex) ||
                        String.valueOf(book.getQuantity()).matches(regex);
            });
        });

        tableBooks.setItems(filteredList);
    }

    @FXML
    private void onAdd(ActionEvent event) {
        String name = txtName.getText().trim();
        String author = txtAuthor.getText().trim();
        String category = txtCategory.getText().trim();
        String edition = txtEdition.getText().trim();
        String language = txtLanguage.getText().trim();
        String quantityText = txtQuantity.getText().trim();

        if (name.isEmpty() || author.isEmpty() || category.isEmpty() || edition.isEmpty() || language.isEmpty() || quantityText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Field(s) cannot be empty", "All fields must be filled out!");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Quantity must be a number!");
            return;
        }

        // Check if the book already exists
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement checkStmt = connection.prepareStatement(
                     "SELECT quantity FROM books WHERE book_name = ? AND author_name = ? AND category = ? AND edition = ? AND language = ?")) {

            checkStmt.setString(1, name);
            checkStmt.setString(2, author);
            checkStmt.setString(3, category);
            checkStmt.setString(4, edition); // Edition field is now mandatory
            checkStmt.setString(5, language);

            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                int existingQuantity = rs.getInt("quantity");

                // Prompt the user about the existing book
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Book Already Exists");
                alert.setHeaderText("A book with the same details already exists.");
                alert.setContentText("Existing Quantity: " + existingQuantity + ".\nDo you want to update the quantity?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // Update the quantity of the existing book
                    try (PreparedStatement updateStmt = connection.prepareStatement(
                            "UPDATE books SET quantity = ? WHERE book_name = ? AND author_name = ? AND category = ? AND edition = ? AND language = ?")) {

                        updateStmt.setInt(1, quantity);
                        updateStmt.setString(2, name);
                        updateStmt.setString(3, author);
                        updateStmt.setString(4, category);
                        updateStmt.setString(5, edition); // Edition field must be provided
                        updateStmt.setString(6, language);

                        updateStmt.executeUpdate();
                        showAlert(Alert.AlertType.INFORMATION, "Book Updated", "Book quantity updated successfully.");
                        loadBooks();
                        clearFields();
                    }
                }
                return; // Exit after handling the existing book
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while checking the book.");
            return;
        }

        // Add the new book
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(
                     "INSERT INTO books (book_name, author_name, category, edition, language, quantity) VALUES (?, ?, ?, ?, ?, ?)")) {

            stmt.setString(1, name);
            stmt.setString(2, author);
            stmt.setString(3, category);
            stmt.setString(4, edition); // Edition field must not be empty
            stmt.setString(5, language);
            stmt.setInt(6, quantity);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Book Added", "Book added successfully.");
                loadBooks();
                clearFields();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while adding the book.");
        }
    }


    @FXML
    private void onUpdate(ActionEvent event) {
        Book selectedBook = tableBooks.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert(Alert.AlertType.ERROR, "No Selection", "Please select a book to update.");
            return;
        }

        String name = txtName.getText().trim();
        String author = txtAuthor.getText().trim();
        String category = txtCategory.getText().trim();
        String edition = txtEdition.getText().trim();
        String language = txtLanguage.getText().trim();
        String quantityText = txtQuantity.getText().trim();

        if (name.isEmpty() || author.isEmpty() || category.isEmpty() || language.isEmpty() || quantityText.isEmpty() || edition.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Field(s) cannot be empty", "All fields must be filled out!");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Quantity must be a number!");
            return;
        }

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(
                     "UPDATE books SET book_name = ?, author_name = ?, category = ?, edition = ?, language = ?, quantity = ? WHERE book_id = ?")) {

            stmt.setString(1, name);
            stmt.setString(2, author);
            stmt.setString(3, category);
            stmt.setString(4, edition.isEmpty() ? null : edition);
            stmt.setString(5, language);
            stmt.setInt(6, quantity);
            stmt.setInt(7, selectedBook.getId());

            stmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Book Updated", "Book details updated successfully.");
            loadBooks();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while updating the book.");
        }
    }

    @FXML
    private void onDelete(ActionEvent event) {
        Book selectedBook = tableBooks.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert(Alert.AlertType.ERROR, "No Selection", "Please select a book to delete.");
            return;
        }

        // Check if the book is currently issued
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement checkStmt = connection.prepareStatement(
                     "SELECT COUNT(*) AS issued_count FROM issued_book_details WHERE book_id = ?")) {

            checkStmt.setInt(1, selectedBook.getId());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt("issued_count") > 0) {
                showAlert(Alert.AlertType.ERROR, "Cannot Delete", "The book is currently issued to a member and cannot be deleted.");
                return; // Exit the method if the book is issued
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while checking issued book details.");
            return;
        }

        // Proceed with deletion if the book is not issued
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this book?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try (Connection connection = DatabaseConnection.connect();
                 PreparedStatement stmt = connection.prepareStatement("DELETE FROM books WHERE book_id = ?")) {

                stmt.setInt(1, selectedBook.getId());
                stmt.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Book Deleted", "Book deleted successfully.");
                loadBooks();
                clearFields();

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while deleting the book.");
            }
        }
    }


    private void clearFields() {
        txtName.clear();
        txtAuthor.clear();
        txtCategory.clear();
        txtEdition.clear();
        txtLanguage.clear();
        txtQuantity.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner Book class
    public static class Book {
        private final int id;
        private final String name;
        private final String author;
        private final String category;
        private final String edition;
        private final String language;
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
}
