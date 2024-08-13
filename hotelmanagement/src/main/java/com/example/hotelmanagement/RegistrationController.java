package com.example.hotelmanagement;

import com.example.hotelmanagement.DatabaseConnection;
import com.example.hotelmanagement.LoginController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegistrationController {
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;

    @FXML
    protected void onRegisterButtonClick() {
        String fullName = fullNameField.getText();
        String phoneNumber = phoneNumberField.getText().strip();
        String login = loginField.getText().strip();
        String password = passwordField.getText().strip();

        if (fullName.isEmpty() || phoneNumber.isEmpty() || login.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Form Error!", "Please enter all fields");
            return;
        }

        if (!isPhoneNumberValid(phoneNumber)) {
            showAlert(Alert.AlertType.ERROR, "Form Error!", "Invalid phone number");
            return;
        }


        try (Connection connection = DatabaseConnection.getConnection()) {
            if (isUsernameExists(connection, login)) {
                showAlert(Alert.AlertType.ERROR, "Registration Error", "Username already exists");
                return;
            }

            String role = determineUserRole(connection, fullName, phoneNumber);
            if (role == null) {
                showAlert(Alert.AlertType.ERROR, "Registration Error", "Unable to register user with the provided details");
                return;
            }

            registerUser(connection, fullName, phoneNumber, login, password, role);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to register");
        }
    }

    private boolean isUsernameExists(Connection connection, String username) throws SQLException {
        String checkUsernameQuery = "select count(*) from User where Username = ?";
        try (PreparedStatement checkUsernameStmt = connection.prepareStatement(checkUsernameQuery)) {
            checkUsernameStmt.setString(1, username);
            try (ResultSet resultSet = checkUsernameStmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private String determineUserRole(Connection connection, String fullName, String phoneNumber) throws SQLException {
        String role = null;

        String checkEmployeeQuery = "select e.ID_Employee, p.Profesion from Employee e join Profesion p on e.ID_Profesion = p.ID_Profesion where e.Full_Name = ? and e.Phone_Number = ?";
        try (PreparedStatement checkEmployeeStmt = connection.prepareStatement(checkEmployeeQuery)) {
            checkEmployeeStmt.setString(1, fullName);
            checkEmployeeStmt.setString(2, phoneNumber);
            try (ResultSet employeeResult = checkEmployeeStmt.executeQuery()) {
                if (employeeResult.next()) {
                    String profession = employeeResult.getString("Profesion");
                    if ("Receptionist".equalsIgnoreCase(profession)) {
                        role = "Receptionist";
                    }
                }
            }
        }

        if (role == null) {

            String checkManagerQuery = "select Position from Manager where Full_Name = ? and Phone_Number = ?";
            try (PreparedStatement checkManagerStmt = connection.prepareStatement(checkManagerQuery)) {
                checkManagerStmt.setString(1, fullName);
                checkManagerStmt.setString(2, phoneNumber);
                try (ResultSet managerResult = checkManagerStmt.executeQuery()) {
                    if (managerResult.next()) {
                        String position = managerResult.getString("Position");
                        if ("Director".equalsIgnoreCase(position) || "Deputy Director".equalsIgnoreCase(position)) {
                            role = position;
                        }
                    }
                }
            }
        }

        return role;
    }

    private void registerUser(Connection connection, String fullName, String phoneNumber, String login, String password, String role) throws SQLException, IOException {
        String insertUserQuery = "insert into User (Username, Password, Role) values (?, ?, ?)";
        try (PreparedStatement insertUserStmt = connection.prepareStatement(insertUserQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            insertUserStmt.setString(1, login);
            insertUserStmt.setString(2, password);
            insertUserStmt.setString(3, role);
            insertUserStmt.executeUpdate();

            try (ResultSet generatedKeys = insertUserStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int newUserId = generatedKeys.getInt(1);
                    if ("Receptionist".equalsIgnoreCase(role)) {
                        updateEmployeeUserId(connection, fullName, phoneNumber, newUserId);
                    } else {
                        updateManagerUserId(connection, fullName, phoneNumber, newUserId);
                    }

                    showAlert(Alert.AlertType.INFORMATION, "Registration Successful!", "Welcome " + fullName);
                    backToMainWindow();
                }
            }
        }
    }

    private void updateEmployeeUserId(Connection connection, String fullName, String phoneNumber, int userId) throws SQLException {
        String updateEmployeeQuery = "update Employee set ID_User = ? where Full_Name = ? and Phone_Number = ?";
        try (PreparedStatement updateEmployeeStmt = connection.prepareStatement(updateEmployeeQuery)) {
            updateEmployeeStmt.setInt(1, userId);
            updateEmployeeStmt.setString(2, fullName);
            updateEmployeeStmt.setString(3, phoneNumber);
            updateEmployeeStmt.executeUpdate();
        }
    }

    private void updateManagerUserId(Connection connection, String fullName, String phoneNumber, int userId) throws SQLException {
        String updateManagerQuery = "update Manager set ID_User = ? where Full_Name = ? and Phone_Number = ?";
        try (PreparedStatement updateManagerStmt = connection.prepareStatement(updateManagerQuery)) {
            updateManagerStmt.setInt(1, userId);
            updateManagerStmt.setString(2, fullName);
            updateManagerStmt.setString(3, phoneNumber);
            updateManagerStmt.executeUpdate();
        }
    }

    @FXML
    protected void onBackButtonClick() throws IOException {
        backToMainWindow();
    }

    private boolean isPhoneNumberValid(String phoneNumber) {
        return phoneNumber.matches("\\d{7}");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void backToMainWindow() throws IOException {
        Stage stage = (Stage) fullNameField.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main-window.fxml"));

        if(LoginController.isUserLoggedIn()) {
            fxmlLoader = new FXMLLoader(getClass().getResource("main-continue.fxml"));
        }
        stage.setScene(new Scene(fxmlLoader.load()));
    }
}
