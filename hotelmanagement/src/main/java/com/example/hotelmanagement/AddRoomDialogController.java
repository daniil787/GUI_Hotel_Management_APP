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

public class AddRoomDialogController {

    @FXML
    private TextField roomNumberField;
    @FXML
    private TextField bedsField;
    @FXML
    private ComboBox<String> typeComboBox;

    private boolean added;

    public boolean isAdded() {
        return added;
    }

    @FXML
    public void initialize() {
        loadRoomTypes();
    }

    private void loadRoomTypes() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "select Room_Type from Room_Type";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                typeComboBox.getItems().add(rs.getString("Room_Type"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error loading room types: " + e.getMessage());
        }
    }

    @FXML
    private void onSaveClick() {
        int roomNumber;
        try {
            roomNumber = Integer.parseInt(roomNumberField.getText());
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Room number must be an integer.");
            return;
        }

        int beds;
        try {
            beds = Integer.parseInt(bedsField.getText());
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Beds must be an integer.");
            return;
        }

        String type = typeComboBox.getValue();
        if (type == null || type.isEmpty()) {
            showAlert("Input Error", "All fields must be filled.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            String insertRoomQuery = "insert into Room (Room_Number, Capacity, ID_Room_Type) " +
                    "values (?, ?, (select ID_Room_Type from Room_Type where Room_Type = ?))";
            PreparedStatement insertRoomStmt = connection.prepareStatement(insertRoomQuery);
            insertRoomStmt.setInt(1, roomNumber);
            insertRoomStmt.setInt(2, beds);
            insertRoomStmt.setString(3, type);
            insertRoomStmt.executeUpdate();
            added = true;
            onCancelClick();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error adding room: " + e.getMessage());
        }
    }

    @FXML
    private void onCancelClick() {
        Stage stage = (Stage) roomNumberField.getScene().getWindow();
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
