package com.example.hotelmanagement;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddServiceDialogController {

    @FXML
    private TextField serviceNameField;
    @FXML
    private TextField costField;

    private boolean added;

    public boolean isAdded() {
        return added;
    }

    @FXML
    private void onSaveClick() {
        String serviceName = serviceNameField.getText();
        double cost;
        try {
            cost = Double.parseDouble(costField.getText());
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Cost must be a number.");
            return;
        }

        if (serviceName.isEmpty()) {
            showAlert("Input Error", "Service name must be filled.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            String insertServiceQuery = "insert into Service (Service, Cost) values (?, ?)";
            PreparedStatement insertServiceStmt = connection.prepareStatement(insertServiceQuery);
            insertServiceStmt.setString(1, serviceName);
            insertServiceStmt.setDouble(2, cost);
            insertServiceStmt.executeUpdate();

            added = true;
            onCancelClick();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error adding service: " + e.getMessage());
        }
    }

    @FXML
    private void onCancelClick() {
        Stage stage = (Stage) serviceNameField.getScene().getWindow();
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
