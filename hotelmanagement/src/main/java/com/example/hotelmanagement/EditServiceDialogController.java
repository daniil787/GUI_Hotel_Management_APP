package com.example.hotelmanagement;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EditServiceDialogController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;

    private Service service;
    private boolean updated;

    public void setService(Service service) {
        this.service = service;
        nameField.setText(service.getName());
        priceField.setText(String.valueOf(service.getPrice()));
    }

    public boolean isUpdated() {
        return updated;
    }

    @FXML
    private void onSaveClick() {
        String name = nameField.getText();
        double price;
        try {
            price = Double.parseDouble(priceField.getText());
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Price must be a number.");
            return;
        }

        if (name.isEmpty()) {
            showAlert("Input Error", "Please enter service name.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "update Service set Service = ?, Cost = ? where ID_Service = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            statement.setDouble(2, price);
            statement.setInt(3, service.getId());
            statement.executeUpdate();

            updated = true;
            onCancelClick();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error updating service: " + e.getMessage());
        }
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
