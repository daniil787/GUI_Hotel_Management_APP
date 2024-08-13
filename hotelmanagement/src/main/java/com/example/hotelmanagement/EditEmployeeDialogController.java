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

public class EditEmployeeDialogController {

    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> positionComboBox;
    @FXML
    private TextField addressField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField salaryField;

    private Employee selectedEmployee;
    private boolean updated;
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
    public void setEmployee(Employee employee) {
        this.selectedEmployee = employee;
        nameField.setText(employee.getName());
        positionComboBox.setValue(employee.getPosition());
        addressField.setText(employee.getAddress());
        phoneField.setText(employee.getPhone());
        salaryField.setText(String.valueOf(employee.getSalary()));
    }

    public boolean isUpdated() {
        return updated;
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

            String updateEmployeeQuery = "update Employee SET Full_Name = ?, Address = ?, Phone_Number = ?, Salary = ?, ID_Profesion = ? where ID_Employee = ?";
            PreparedStatement updateEmployeeStmt = connection.prepareStatement(updateEmployeeQuery);
            updateEmployeeStmt.setString(1, name);
            updateEmployeeStmt.setString(2, address);
            updateEmployeeStmt.setString(3, phone);
            updateEmployeeStmt.setDouble(4, salary);
            updateEmployeeStmt.setInt(5, professionId);
            updateEmployeeStmt.setInt(6, selectedEmployee.getId());
            updateEmployeeStmt.executeUpdate();

            connection.commit();
            updated = true;
            onCancelClick();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error updating employee: " + e.getMessage());
        }
    }
    private boolean isPhoneNumberValid(String phoneNumber) {
        return phoneNumber.matches("\\d{7}");
    }

    @FXML
    private void onCancelClick() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
