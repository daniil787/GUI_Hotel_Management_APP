package com.example.hotelmanagement;

import com.example.hotelmanagement.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

public class DateChangeDialogController {

    @FXML
    private DatePicker newCheckInDatePicker;
    @FXML
    private DatePicker newCheckOutDatePicker;

    private int guestId;
    private int roomId;
    private boolean confirmed = false;

    @FXML
    private void onCheckNewDates() {
        LocalDate newCheckInDate = newCheckInDatePicker.getValue();
        LocalDate newCheckOutDate = newCheckOutDatePicker.getValue();

        if (newCheckInDate == null || newCheckOutDate == null || newCheckOutDate.isBefore(newCheckInDate)) {
            showAlert("Input Error", "Please select valid check-in and check-out dates.");
            return;
        }

        if (isRoomAvailable(roomId, newCheckInDate, newCheckOutDate)) {
            confirmed = showConfirmationDialog("Dates Available", "Room is available for the selected dates. Confirm?");
            if (confirmed) {
                Stage stage = (Stage) newCheckInDatePicker.getScene().getWindow();
                stage.close();
            }
        } else {
            showAlert("No Availability", "Room is not available for the selected dates.");
        }
    }

    @FXML
    private void onCancel() {
        Stage stage = (Stage) newCheckInDatePicker.getScene().getWindow();
        stage.close();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public LocalDate getCheckInDate() {
        return newCheckInDatePicker.getValue();
    }

    public LocalDate getCheckOutDate() {
        return newCheckOutDatePicker.getValue();
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public void setOriginalDates(LocalDate checkInDate, LocalDate checkOutDate) {
        newCheckInDatePicker.setValue(checkInDate);
        newCheckOutDatePicker.setValue(checkOutDate);
    }

    private boolean isRoomAvailable(int roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT COUNT(*) FROM Accommodation WHERE ID_Room = ? AND ? < Check_Out_Date AND ? > Check_In_Date";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, roomId);
            statement.setDate(2, Date.valueOf(checkInDate));
            statement.setDate(3, Date.valueOf(checkOutDate));
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error checking room availability: " + e.getMessage());
        }
        return false;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
