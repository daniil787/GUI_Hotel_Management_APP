package com.example.hotelmanagement;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddEmployeeDialogController {

    public TextField salaryField;
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> positionComboBox;
    @FXML
    private TextField addressField;
    @FXML
    private TextField phoneField;


    private boolean added;

    public boolean isAdded() {
        return added;
    }

    @FXML
    public void initialize() {
        loadProfessions();
    }

    private void loadProfessions() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "select Profesion from Profesion";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                positionComboBox.getItems().add(rs.getString("Profesion"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error loading professions: " + e.getMessage());
        }
    }

    @FXML
    private void onSaveClick() {
        String name = nameField.getText();
        String position = positionComboBox.getValue();
        String address = addressField.getText();
        String phone = phoneField.getText();
        double salary;
        try {
            salary = Double.parseDouble(salaryField.getText());
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Salary must be a number.");
            return;
        }

        if (name.isEmpty() || position.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            showAlert("Input Error", "All fields must be filled.");
            return;
        }
        if (!isPhoneNumberValid(phone)) {
            showAlert("Form Error", "Invalid phone number");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            String getIDProfessionQuery = "select ID_Profesion from Profesion where Profesion = ?";
            PreparedStatement getIDProfessionStmt = connection.prepareStatement(getIDProfessionQuery);
            getIDProfessionStmt.setString(1, position);
            ResultSet rs = getIDProfessionStmt.executeQuery();
            int professionId = 0;
            if (rs.next()) {
                professionId = rs.getInt("ID_Profesion");
            }

            String insertEmployeeQuery = "insert into Employee (Full_Name, Birth_Date, Address, Phone_Number, Salary, ID_Profesion) values (?, curdate(), ?, ?, ?, ?)";
            PreparedStatement insertEmployeeStmt = connection.prepareStatement(insertEmployeeQuery);
            insertEmployeeStmt.setString(1, name);
            insertEmployeeStmt.setString(2, address);
            insertEmployeeStmt.setString(3, phone);
            insertEmployeeStmt.setDouble(4, salary);
            insertEmployeeStmt.setInt(5, professionId);
            insertEmployeeStmt.executeUpdate();

            connection.commit();
            added = true;
            onCancelClick();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error adding employee: " + e.getMessage());
        }
    }

    @FXML
    private void onCancelClick() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
    private boolean isPhoneNumberValid(String phoneNumber) {
        return phoneNumber.matches("\\d{7}");
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
