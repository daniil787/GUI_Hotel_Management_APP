package com.example.hotelmanagement;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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

public class LoginController {
    private static boolean isUserLoggedIn = false;
    private static String userRole;

    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;

    @FXML
    protected void onLoginButtonClick() {
        String username = loginField.getText().strip();
        String password = passwordField.getText().strip();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Form Error!", "Please enter login and password");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT Role FROM User WHERE Username = ? AND Password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                userRole = resultSet.getString("Role");
                showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Welcome, " + username + "!");
                loadDashboard(userRole, username);
                isUserLoggedIn = true;
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid login or password");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to login");
        }
    }

    @FXML
    protected void onBackButtonClick() throws IOException {
        Stage stage = (Stage) loginField.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main-window.fxml"));

        if (isUserLoggedIn) {
            fxmlLoader = new FXMLLoader(getClass().getResource("main-continue.fxml"));
        }
        stage.setScene(new Scene(fxmlLoader.load()));
    }

    private void loadDashboard(String role, String username) throws IOException {
        Stage stage = (Stage) loginField.getScene().getWindow();
        FXMLLoader fxmlLoader;

        if (role.equals("Director") || role.equals("Deputy Director")) {
            fxmlLoader = new FXMLLoader(getClass().getResource("manager-main-form.fxml"));
        } else {
            fxmlLoader = new FXMLLoader(getClass().getResource("receptionist-main-form.fxml"));
        }

        Parent dashboard = fxmlLoader.load();
        Scene scene = new Scene(dashboard);

        if (role.equals("Receptionist")) {
            ReceptionistMainFormController controller = fxmlLoader.getController();
            controller.setUsername(username);
        } else if (role.equals("Director") || role.equals("Deputy Director")) {
            ManagerMainFormController controller = fxmlLoader.getController();
            controller.setUsername(username);
        }

        stage.setScene(scene);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean isUserLoggedIn() {
        return isUserLoggedIn;
    }

    public static void setUserLoggedIn(boolean loggedIn) {
        isUserLoggedIn = loggedIn;
    }

    public static String getUserRole() {
        return userRole;
    }
}
