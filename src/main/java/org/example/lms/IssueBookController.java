package org.example.lms;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;

public class IssueBookController {

    // Books Table
    @FXML private TableView<Book> tableBooks;
    @FXML private TableColumn<Book, Integer> colBookId;
    @FXML private TableColumn<Book, String> colBookName, colAuthorName, colCategory, colEdition, colLanguage;
    @FXML private TableColumn<Book, Integer> colQuantity;
    @FXML private TextField txtSearchBooks;

    // Members Table
    @FXML private TableView<User> tableMembers;
    @FXML private TableColumn<User, Integer> colMemberId;
    @FXML private TableColumn<User, String> colUsername, colMemberName, colMemberEmail, colPhone;
    @FXML private TextField txtSearchMembers;

    // Book Details
    @FXML private TextField txtBookName, txtBookAuthor, txtBookCategory, txtBookEdition, txtBookLanguage, txtBookQuantity;

    // Member Details
    @FXML private TextField txtMemberUsername, txtMemberName, txtMemberEmail, txtMemberPhone;

    @FXML private DatePicker datePickerDueDate;

    private ObservableList<Book> booksList = FXCollections.observableArrayList();
    private ObservableList<User> membersList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        datePickerDueDate.setValue(java.time.LocalDate.now().plusDays(14));

        datePickerDueDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(java.time.LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                // Disable past dates
                setDisable(empty || date.isBefore(java.time.LocalDate.now()));
            }
        });

        loadBooks();
        setupBooksTable();
        setupBooksSearch();

        loadMembers();
        setupMembersTable();
        setupMembersSearch();

        txtMemberUsername.setOnAction(event -> checkAndFillByUsername());
        txtMemberUsername.focusedProperty().addListener((obs, oldFocus, newFocus) -> {
            if (!newFocus) {
                checkAndFillByUsername();
            }
        });

        txtMemberEmail.setOnAction(event -> checkAndFillByEmail());
        txtMemberEmail.focusedProperty().addListener((obs, oldFocus, newFocus) -> {
            if (!newFocus) {
                checkAndFillByEmail();
            }
        });

        addAutoFillListeners();
    }

    // Book and User Classes
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

    public static class User {
        private final int id;
        private final String username;
        private final String name;
        private final String email;
        private final String phone;

        public User(int id, String username, String name, String email, String phone) {
            this.id = id;
            this.username = username;
            this.name = name;
            this.email = email;
            this.phone = phone;
        }

        public int getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }
    }

    private void addAutoFillListeners() {
        // Listen to all fields and trigger auto-fill when any is updated
        txtBookName.textProperty().addListener((observable, oldValue, newValue) -> autoFillQuantity());
        txtBookAuthor.textProperty().addListener((observable, oldValue, newValue) -> autoFillQuantity());
        txtBookCategory.textProperty().addListener((observable, oldValue, newValue) -> autoFillQuantity());
        txtBookEdition.textProperty().addListener((observable, oldValue, newValue) -> autoFillQuantity());
        txtBookLanguage.textProperty().addListener((observable, oldValue, newValue) -> autoFillQuantity());
    }

    private void autoFillQuantity() {
        String name = txtBookName.getText().trim();
        String author = txtBookAuthor.getText().trim();
        String category = txtBookCategory.getText().trim();
        String edition = txtBookEdition.getText().trim();
        String language = txtBookLanguage.getText().trim();

        if (!name.isEmpty() && !author.isEmpty() && !category.isEmpty() && !edition.isEmpty() && !language.isEmpty()) {
            for (IssueBookController.Book book : booksList) {
                if (book.getName().equalsIgnoreCase(name) &&
                        book.getAuthor().equalsIgnoreCase(author) &&
                        book.getCategory().equalsIgnoreCase(category) &&
                        book.getEdition().equalsIgnoreCase(edition) &&
                        book.getLanguage().equalsIgnoreCase(language)) {
                    txtBookQuantity.setText(String.valueOf(book.getQuantity()));
                    return;
                }
            }
            txtBookQuantity.clear(); // Clear quantity if no match is found
        }
    }

    private void checkAndFillByUsername() {
        String username = txtMemberUsername.getText().trim(); // Trim any extra spaces

        if (username.isEmpty()) return; // Skip if the field is empty

        for (IssueBookController.User user : membersList) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                fillMemberDetails(user); // Populate the fields if a match is found
                break;
            }
        }
    }

    private void checkAndFillByEmail() {
        String email = txtMemberEmail.getText().trim(); // Trim any extra spaces

        if (email.isEmpty()) return; // Skip if the field is empty

        for (IssueBookController.User user : membersList) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                fillMemberDetails(user); // Populate the fields if a match is found
                return; // Exit the method once a match is found
            }
        }

        // If no match is found, do nothing (email remains as entered)

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

        colBookId.setSortable(true);
        colBookName.setSortable(true);
        colAuthorName.setSortable(true);
        colCategory.setSortable(true);
        colEdition.setSortable(true);
        colLanguage.setSortable(true);
        colQuantity.setSortable(true);

        tableBooks.setItems(booksList);

        tableBooks.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) fillBookDetails(newSelection);
        });

        tableBooks.setOnMouseClicked(event -> {
            Book selectedBook = tableBooks.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                fillBookDetails(selectedBook);
            }
        });
    }

    private void setupBooksSearch() {
        FilteredList<Book> filteredBooks = new FilteredList<>(booksList, b -> true);

        txtSearchBooks.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredBooks.setPredicate(book -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true; // Show all books if the search is empty
                }

                String lowerCaseFilter = newValue.toLowerCase();
                String regex = ".*" + lowerCaseFilter + ".*";

                // Match the search query with all columns
                return String.valueOf(book.getId()).matches(regex) ||
                        book.getName().toLowerCase().matches(regex) ||
                        book.getAuthor().toLowerCase().matches(regex) ||
                        book.getCategory().toLowerCase().matches(regex) ||
                        (book.getEdition() != null && book.getEdition().toLowerCase().matches(regex)) ||
                        book.getLanguage().toLowerCase().matches(regex) ||
                        String.valueOf(book.getQuantity()).matches(regex);
            });
        });

        tableBooks.setItems(filteredBooks);
    }


    private void fillBookDetails(Book book) {
        txtBookName.setText(book.getName());
        txtBookAuthor.setText(book.getAuthor());
        txtBookCategory.setText(book.getCategory());
        txtBookEdition.setText(book.getEdition());
        txtBookLanguage.setText(book.getLanguage());
        txtBookQuantity.setText(String.valueOf(book.getQuantity()));
    }

    private void loadMembers() {
        membersList.clear();
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE isAdmin = 0")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                membersList.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone_no")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupMembersTable() {
        colMemberId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colMemberName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colMemberEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        colMemberId.setSortable(true);
        colUsername.setSortable(true);
        colMemberName.setSortable(true);
        colMemberEmail.setSortable(true);
        colPhone.setSortable(true);

        tableMembers.setItems(membersList);
        tableMembers.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) fillMemberDetails(newSelection);
        });

        tableMembers.setOnMouseClicked(event -> {
            User selectedUser = tableMembers.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                fillMemberDetails(selectedUser);
            }
        });

    }

    private void setupMembersSearch() {
        FilteredList<User> filteredMembers = new FilteredList<>(membersList, u -> true);

        txtSearchMembers.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredMembers.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true; // Show all members if the search is empty
                }

                String lowerCaseFilter = newValue.toLowerCase();
                String regex = ".*" + lowerCaseFilter + ".*";

                // Match the search query with all columns
                return String.valueOf(user.getId()).matches(regex) ||
                        user.getUsername().toLowerCase().matches(regex) ||
                        user.getName().toLowerCase().matches(regex) ||
                        user.getEmail().toLowerCase().matches(regex) ||
                        (user.getPhone() != null && user.getPhone().toLowerCase().matches(regex));
            });
        });

        tableMembers.setItems(filteredMembers);
    }


    private void fillMemberDetails(User user) {
        txtMemberUsername.setText(user.getUsername());
        txtMemberName.setText(user.getName());
        txtMemberEmail.setText(user.getEmail());
        txtMemberPhone.setText(user.getPhone());
    }

    @FXML
    private void onIssueBook(ActionEvent event) {
        Book selectedBook = tableBooks.getSelectionModel().getSelectedItem();
        User selectedUser = tableMembers.getSelectionModel().getSelectedItem();

        if (selectedBook == null || selectedUser == null) {
            showAlert(Alert.AlertType.ERROR, "Selection Missing", "Please select a book and a member.");
            return;
        }

        // Check for valid due date
        if (datePickerDueDate.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Missing Due Date", "Please select a due date.");
            return;
        }

        // Preemptive check for book availability (to improve user experience)
        if (selectedBook.getQuantity() <= 0) {
            showAlert(Alert.AlertType.ERROR, "Book Not Available", "The selected book is not available for issuing.");
            return;
        }

        // Check if the book is already issued to the user
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement checkStmt = connection.prepareStatement(
                     "SELECT * FROM issued_book_details WHERE book_id = ? AND member_id = ?")) {
            checkStmt.setInt(1, selectedBook.getId());
            checkStmt.setInt(2, selectedUser.getId());

            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                showAlert(Alert.AlertType.ERROR, "Already Issued", "This book is already issued to this user.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while checking existing issues.");
            return;
        }

        // Attempt to issue the book
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement issueStmt = connection.prepareStatement(
                     "INSERT INTO issued_book_details (book_id, member_id, issue_date, due_date, status) VALUES (?, ?, CURRENT_DATE, ?, 'On Time')")) {
            issueStmt.setInt(1, selectedBook.getId());
            issueStmt.setInt(2, selectedUser.getId());
            issueStmt.setDate(3, java.sql.Date.valueOf(datePickerDueDate.getValue())); // Use selected due date
            issueStmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Book issued successfully.");
            loadBooks(); // Refresh available books
        } catch (SQLException e) {
            if (e.getMessage().contains("Book is not available for issuing")) {
                showAlert(Alert.AlertType.ERROR, "Book Not Available", "The selected book is not available for issuing.");
            } else {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while issuing the book.");
            }
        }
    }



    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
