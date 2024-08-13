package com.example.hotelmanagement;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BookingController {
    @FXML
    private TextField phoneNumberField;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField roomNumberField;
    @FXML
    private DatePicker birthDatePicker;
    @FXML
    private DatePicker settlementDatePicker;
    @FXML
    private DatePicker departureDatePicker;

    @FXML
    protected void onCheckInButtonClick() {
        String phoneNumber = phoneNumberField.getText();

        if (phoneNumber.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error","Empty Phone Number", "Please enter a phone number.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "select full_name, birth_date, address FROM Guest WHERE phone_number = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, phoneNumber);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        showAlert(Alert.AlertType.INFORMATION, "Success","Found guest", "Guest has already been added Data autofill completed");
                        fullNameField.setText(resultSet.getString("full_name"));
                        birthDatePicker.setValue(resultSet.getDate("birth_date").toLocalDate());
                        addressField.setText(resultSet.getString("address"));
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Warning","Not founded guest" ,"No guest found with phone number " + phoneNumber);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error","Database Error", "An error occurred while checking the guest");
        }
    }

    @FXML
    protected void onBookRoomButtonClick() {
        String phoneNumber = phoneNumberField.getText();
        String fullName = fullNameField.getText();
        String address = addressField.getText();
        LocalDate birthDate = birthDatePicker.getValue();
        String roomNumber = roomNumberField.getText();
        LocalDate settlementDate = settlementDatePicker.getValue();
        LocalDate departureDate = departureDatePicker.getValue();

        if (phoneNumber.isEmpty() || fullName.isEmpty() || address.isEmpty() || birthDate == null || roomNumber.isEmpty() || settlementDate == null || departureDate == null) {
            showAlert(Alert.AlertType.ERROR, "Error","Form Error!", "Please enter all fields");
            return;
        }

        if (!isPhoneNumberValid(phoneNumber)) {
            showAlert(Alert.AlertType.ERROR, "Error","Form Error!", "Invalid phone number");
            return;
        }
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            // Check if the guest already exists
            String checkGuestQuery = "SELECT ID_Guest FROM Guest WHERE Phone_Number = ?";
            try (PreparedStatement checkGuestStmt = connection.prepareStatement(checkGuestQuery)) {
                checkGuestStmt.setString(1, phoneNumber);
                try (ResultSet guestResult = checkGuestStmt.executeQuery()) {
                    int guestId;
                    if (guestResult.next()) {
                        guestId = guestResult.getInt("ID_Guest");
                    } else {
                        // Insert new guest
                        String insertGuestQuery = "INSERT INTO Guest (Full_Name, Address, Birth_Date, Phone_Number) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement insertGuestStmt = connection.prepareStatement(insertGuestQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                            insertGuestStmt.setString(1, fullName);
                            insertGuestStmt.setString(2, address);
                            insertGuestStmt.setDate(3, java.sql.Date.valueOf(birthDate));
                            insertGuestStmt.setString(4, phoneNumber);
                            insertGuestStmt.executeUpdate();

                            try (ResultSet generatedKeys = insertGuestStmt.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    guestId = generatedKeys.getInt(1);
                                } else {
                                    showAlert(Alert.AlertType.ERROR,  "Error", "Database Error", "An error occurred while processing the booking.");
                                    connection.rollback();
                                    return;
                                }
                            }
                        }
                    }

                    // Check if the room is available
                    String checkRoomQuery = "SELECT * FROM Booking WHERE ID_Room = ? AND (Date_Settlement BETWEEN ? AND ? OR Date_Departure BETWEEN ? AND ?)";
                    try (PreparedStatement checkRoomStmt = connection.prepareStatement(checkRoomQuery)) {
                        checkRoomStmt.setInt(1, Integer.parseInt(roomNumber));
                        checkRoomStmt.setDate(2, java.sql.Date.valueOf(settlementDate));
                        checkRoomStmt.setDate(3, java.sql.Date.valueOf(departureDate));
                        checkRoomStmt.setDate(4, java.sql.Date.valueOf(settlementDate));
                        checkRoomStmt.setDate(5, java.sql.Date.valueOf(departureDate));
                        try (ResultSet roomResult = checkRoomStmt.executeQuery()) {
                            if (roomResult.next()) {
                                showAlert(Alert.AlertType.ERROR, "Error", "Room Unavailable", "The room is not available for the selected dates");
                                connection.rollback();
                                return;
                            }
                        }
                    }

                    // Insert booking
                    String insertBookingQuery = "INSERT INTO Booking (ID_Room, ID_Guest, Date_Booking, Date_Settlement, Date_Departure, Date_Payment) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement insertBookingStmt = connection.prepareStatement(insertBookingQuery)) {
                        insertBookingStmt.setInt(1, Integer.parseInt(roomNumber));
                        insertBookingStmt.setInt(2, guestId);
                        insertBookingStmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                        insertBookingStmt.setDate(4, java.sql.Date.valueOf(settlementDate));
                        insertBookingStmt.setDate(5, java.sql.Date.valueOf(departureDate));
                        insertBookingStmt.setDate(6, java.sql.Date.valueOf(LocalDate.now()));
                        insertBookingStmt.executeUpdate();
                    }

                    connection.commit();
                    showAlert(Alert.AlertType.INFORMATION, "Success","Booking Successful", "Room booked successfully");
                    backToReceptionistDashboard();
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,  "Error", "Database Error", "Failed to book room.");
        }
    }

    @FXML
    protected void onBackButtonClick() throws IOException {
        backToReceptionistDashboard();
    }

    private boolean isPhoneNumberValid(String phoneNumber) {
        return phoneNumber.matches("\\d{7}");
    }

    private void showAlert(Alert.AlertType alertType, String title, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    private void backToReceptionistDashboard() throws IOException {
        Stage stage = (Stage) phoneNumberField.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("receptionist-main-form.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
    }
}
