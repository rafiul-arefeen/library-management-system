package org.example.lms;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ManageMembersController {

    public Button btnAdd;
    public Button btnUpdate;
    public Button btnDelete;
    public TextField txtSearch;
    @FXML
    private TableView<User> tableMembers;

    @FXML
    private TableColumn<User, Integer> colId;
    @FXML
    private TableColumn<User, String> colUsername;
    @FXML
    private TableColumn<User, String> colName;
    @FXML
    private TableColumn<User, String> colEmail;
    @FXML
    private TableColumn<User, String> colPhone;

    @FXML
    private TextField txtUsername;
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtPhone;

    private ObservableList<User> membersList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadMembers();
        setupTable();

        // Handle row selection to auto-fill fields
        tableMembers.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillFields(newSelection);
            }
        });

        // Add search functionality
        setupSearchFunctionality();

        // Trigger auto-fill when Enter is pressed
        txtUsername.setOnAction(event -> checkAndFillByUsername());
        txtUsername.focusedProperty().addListener((obs, oldFocus, newFocus) -> {
            if (!newFocus) {
                checkAndFillByUsername();
            }
        });

        txtEmail.setOnAction(event -> checkAndFillByEmail());
        txtEmail.focusedProperty().addListener((obs, oldFocus, newFocus) -> {
            if (!newFocus) {
                checkAndFillByEmail();
            }
        });
    }

    private void setupSearchFunctionality() {
        // Wrap the membersList in a FilteredList
        FilteredList<User> filteredList = new FilteredList<>(membersList, p -> true);

        // Add a listener to the search text field
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(user -> {
                // If search text is empty, show all users
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Use regex to match the search query with all columns
                String lowerCaseFilter = newValue.toLowerCase();
                String regex = ".*" + lowerCaseFilter + ".*";

                // Match against all columns
                return String.valueOf(user.getId()).toLowerCase().matches(regex) ||
                        user.getUsername().toLowerCase().matches(regex) ||
                        user.getName().toLowerCase().matches(regex) ||
                        user.getEmail().toLowerCase().matches(regex) ||
                        user.getPhone().toLowerCase().matches(regex);
            });
        });

        // Bind the filtered list to the table
        tableMembers.setItems(filteredList);
    }


    private void checkAndFillByUsername() {
        String username = txtUsername.getText().trim(); // Trim any extra spaces

        if (username.isEmpty()) return; // Skip if the field is empty

        for (User user : membersList) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                fillFields(user); // Populate the fields if a match is found
                break;
            }
        }
    }

    private void checkAndFillByEmail() {
        String email = txtEmail.getText().trim(); // Trim any extra spaces

        if (email.isEmpty()) return; // Skip if the field is empty

        for (User user : membersList) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                fillFields(user); // Populate the fields if a match is found
                return; // Exit the method once a match is found
            }
        }

        // If no match is found, do nothing (email remains as entered)

    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // Enable sorting for each column
        colId.setSortable(true);
        colUsername.setSortable(true);
        colName.setSortable(true);
        colEmail.setSortable(true);
        colPhone.setSortable(true);

        // Set the data to the table
        tableMembers.setItems(membersList);

        tableMembers.setOnMouseClicked(event -> {
            ManageMembersController.User selectedUser = tableMembers.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                fillFields(selectedUser);
            }
        });
    }


    private void loadMembers() {
        membersList.clear();
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement("SELECT user_id, username, name, email, phone_no FROM users WHERE isAdmin = 0")) {

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

    private void fillFields(User user) {
        txtUsername.setText(user.getUsername());
        txtName.setText(user.getName());
        txtEmail.setText(user.getEmail());
        txtPhone.setText(user.getPhone());
    }

    @FXML
    private void onAdd(ActionEvent event) throws NoSuchAlgorithmException {
        String username = txtUsername.getText().trim();
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();

        // Check if any field is empty
        if (username.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Field(s) cannot be empty", "All fields must be filled out!");
            return;
        }

        // Default password to "password" and hash it
        String password = "password"; // Default password
        String hashedPassword = PasswordUtil.hashPassword(password); // Assuming PasswordUtil is available for hashing

        // Check if the user already exists
        if (userExists(username) || emailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "User exists", "The user already exists!");
        } else {
            try (Connection connection = DatabaseConnection.connect();
                 PreparedStatement stmt = connection.prepareStatement("INSERT INTO users (username, password_hash, name, email, phone_no) VALUES (?, ?, ?, ?, ?)")) {

                stmt.setString(1, username);       // Username
                stmt.setString(2, hashedPassword); // Hashed password
                stmt.setString(3, name);           // Full name
                stmt.setString(4, email);          // Email
                stmt.setString(5, phone);    // Phone number

                int rowsInserted = stmt.executeUpdate();

                if (rowsInserted > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "User Added", "User added successfully.");
                    loadMembers(); // Reload members table
                    clearFields(); // Clear input fields
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while adding the user.");
            }
        }
    }



    @FXML
    private void onUpdate(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();

        // Validate that username and email are not empty
        if (username.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing Fields", "Both username and email must be entered.");
            return;
        }

        // Check if the user exists with the given username
        User existingUser = membersList.stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);

        if (existingUser == null) {
            showAlert(Alert.AlertType.ERROR, "User Not Found", "Cannot update, no user found with the given username.");
            return;
        }

        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();

        // Validate other fields
        if (name.isEmpty() || phone.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing Fields", "Name and phone number must be filled out.");
            return;
        }

        // Check if the new email is already taken by another user
        boolean emailTaken = membersList.stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email) && user.getId() != existingUser.getId());

        if (emailTaken) {
            showAlert(Alert.AlertType.ERROR, "Email Already Taken", "The email address is already associated with another user.");
            return;
        }

        // Update the user details
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement("UPDATE users SET username = ?, name = ?, email = ?, phone_no = ? WHERE user_id = ?")) {

            stmt.setString(1, username);
            stmt.setString(2, name);
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.setInt(5, existingUser.getId());
            stmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "User Updated", "User details updated successfully.");
            loadMembers(); // Refresh the table data

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while updating the user.");
        }
    }


    @FXML
    private void onDelete(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();

        // Validate username and email fields
        if (username.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing Fields", "Both username and email must be entered.");
            return;
        }

        // Check if the user exists
        User userToDelete = membersList.stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username) && user.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);

        if (userToDelete == null) {
            showAlert(Alert.AlertType.ERROR, "User Not Found", "Cannot delete, user with the given username and email does not exist.");
            return;
        }

        // Check if the user has any issued books
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement checkStmt = connection.prepareStatement(
                     "SELECT COUNT(*) AS issued_count FROM issued_book_details WHERE member_id = ?")) {

            checkStmt.setInt(1, userToDelete.getId());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt("issued_count") > 0) {
                showAlert(Alert.AlertType.ERROR, "Cannot Delete", "The member has issued books that have not been returned.");
                return; // Exit the method if books are still issued
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while checking issued books.");
            return;
        }

        // Proceed with deletion if no issued books
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this user?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try (Connection connection = DatabaseConnection.connect();
                 PreparedStatement stmt = connection.prepareStatement("DELETE FROM users WHERE user_id = ?")) {

                stmt.setInt(1, userToDelete.getId());
                stmt.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "User Deleted", "User deleted successfully.");
                loadMembers(); // Refresh the table
                clearFields();

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while deleting the user.");
            }
        }
    }




    private boolean userExists(String username) {
        return membersList.stream().anyMatch(user -> user.getUsername().equals(username));
    }

    private boolean emailExists(String email) {
        return membersList.stream().anyMatch(user -> user.getEmail().equals(email));
    }

    private void clearFields() {
        txtUsername.clear();
        txtName.clear();
        txtEmail.clear();
        txtPhone.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner User class
    public static class User {
        private int id;
        private String username;
        private String name;
        private String email;
        private String phone;

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
}
