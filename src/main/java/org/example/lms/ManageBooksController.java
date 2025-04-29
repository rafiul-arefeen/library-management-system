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
    @FXML private TableView<Book> tableBooks;
    @FXML private TableColumn<Book, Integer> colId;
    @FXML private TableColumn<Book, String> colName, colAuthor, colCategory, colEdition, colLanguage;
    @FXML private TableColumn<Book, Integer> colQuantity;
    @FXML private TextField txtName, txtAuthor, txtCategory, txtEdition, txtLanguage, txtQuantity, txtSearch;

    private ObservableList<Book> booksList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadBooks();
        setupTable();
        setupSearchFunctionality();

        btnAdd.setDisable(true);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);

        // Handle row selection to auto-fill and enable Update/Delete
        tableBooks.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillFields(newSelection);
                btnUpdate.setDisable(false);
                btnDelete.setDisable(false);
                btnAdd.setDisable(true); // Disable Add when editing
            }
        });

        // Enable "Add" only when typing and no row is selected
        txtName.textProperty().addListener((obs, oldVal, newVal) -> updateAddButton());
        txtAuthor.textProperty().addListener((obs, oldVal, newVal) -> updateAddButton());
        txtCategory.textProperty().addListener((obs, oldVal, newVal) -> updateAddButton());
        txtEdition.textProperty().addListener((obs, oldVal, newVal) -> updateAddButton());
        txtLanguage.textProperty().addListener((obs, oldVal, newVal) -> updateAddButton());
        txtQuantity.textProperty().addListener((obs, oldVal, newVal) -> updateAddButton());
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colEdition.setCellValueFactory(new PropertyValueFactory<>("edition"));
        colLanguage.setCellValueFactory(new PropertyValueFactory<>("language"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        colId.setSortable(true);
        colName.setSortable(true);
        colAuthor.setSortable(true);
        colCategory.setSortable(true);
        colEdition.setSortable(true);
        colLanguage.setSortable(true);
        colQuantity.setSortable(true);

        tableBooks.setItems(booksList);
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

    private void setupSearchFunctionality() {
        FilteredList<Book> filteredList = new FilteredList<>(booksList, p -> true);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(book -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                String regex = ".*" + lowerCaseFilter + ".*";
                return String.valueOf(book.getId()).matches(regex)
                        || book.getName().toLowerCase().matches(regex)
                        || book.getAuthor().toLowerCase().matches(regex)
                        || book.getCategory().toLowerCase().matches(regex)
                        || (book.getEdition() != null && book.getEdition().toLowerCase().matches(regex))
                        || book.getLanguage().toLowerCase().matches(regex)
                        || String.valueOf(book.getQuantity()).matches(regex);
            });
        });

        tableBooks.setItems(filteredList);
    }

    private void updateAddButton() {
        boolean filled = !txtName.getText().trim().isEmpty()
                || !txtAuthor.getText().trim().isEmpty()
                || !txtCategory.getText().trim().isEmpty()
                || !txtEdition.getText().trim().isEmpty()
                || !txtLanguage.getText().trim().isEmpty()
                || !txtQuantity.getText().trim().isEmpty();

        boolean rowSelected = tableBooks.getSelectionModel().getSelectedItem() != null;
        btnAdd.setDisable(!filled || rowSelected);
    }

    private void fillFields(Book book) {
        txtName.setText(book.getName());
        txtAuthor.setText(book.getAuthor());
        txtCategory.setText(book.getCategory());
        txtEdition.setText(book.getEdition());
        txtLanguage.setText(book.getLanguage());
        txtQuantity.setText(String.valueOf(book.getQuantity()));
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

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(
                     "INSERT INTO books (book_name, author_name, category, edition, language, quantity) VALUES (?, ?, ?, ?, ?, ?)")) {

            stmt.setString(1, name);
            stmt.setString(2, author);
            stmt.setString(3, category);
            stmt.setString(4, edition);
            stmt.setString(5, language);
            stmt.setInt(6, quantity);

            stmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Book Added", "Book added successfully.");
            loadBooks();
            resetButtonsAndFields();
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
            stmt.setString(4, edition);
            stmt.setString(5, language);
            stmt.setInt(6, quantity);
            stmt.setInt(7, selectedBook.getId());

            stmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Book Updated", "Book details updated successfully.");
            loadBooks();
            resetButtonsAndFields();
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

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this book?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try (Connection connection = DatabaseConnection.connect();
                 PreparedStatement stmt = connection.prepareStatement("DELETE FROM books WHERE book_id = ?")) {

                stmt.setInt(1, selectedBook.getId());
                stmt.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Book Deleted", "Book deleted successfully.");
                loadBooks();
                resetButtonsAndFields();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while deleting the book.");
            }
        }
    }

    private void resetButtonsAndFields() {
        clearFields();
        btnAdd.setDisable(true);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        tableBooks.getSelectionModel().clearSelection();
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

        public int getId() { return id; }
        public String getName() { return name; }
        public String getAuthor() { return author; }
        public String getCategory() { return category; }
        public String getEdition() { return edition; }
        public String getLanguage() { return language; }
        public int getQuantity() { return quantity; }
    }
}
