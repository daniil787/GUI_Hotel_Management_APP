package com.example.hotelmanagement;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;

public class AccommodationController {


    @FXML
    public TextField fullNameField;
    @FXML
    public TextField birthDateField;
    @FXML
    public TextField addressField;
    @FXML
    public TextField phoneNumberField;
    @FXML
    private TextField roomIdField;
    @FXML
    public DatePicker checkInDatePicker;
    @FXML
    public DatePicker checkOutDatePicker;
    // тимчасово
    private DatabaseConnection dbConnection;
    // тимчасово
    private String alertMessage;
    // тимчасово


    public DatabaseConnection getDbConnection() {
        return dbConnection;
    }

    // тимчасові гетери
    public TextField getPhoneNumberField() {
        return phoneNumberField;
    }

    public DatePicker getCheckInDatePicker() {
        return checkInDatePicker;
    }

    public TextField getAddressField() {
        return addressField;
    }

    public TextField getBirthDateField() {
        return birthDateField;
    }

    public TextField getFullNameField() {
        return fullNameField;
    }

    public String getAlertMessage() {
        return alertMessage;
    }


    public void setDatabaseConnection(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @FXML
    public void onCheckInButtonClick() {
        String phoneNumber = phoneNumberField.getText();
        if (phoneNumber.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Empty Phone Number", "Please enter a phone number.");
            return;
        }
        try (Connection connection = dbConnection.getConnection()) {
            String query = "select full_name, birth_date, address from Guest where phone_number = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, phoneNumber);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        showAlert(Alert.AlertType.INFORMATION, "Found", "Guest has already been added", "Data autofill completed");
                        fullNameField.setText(resultSet.getString("full_name"));
                        birthDateField.setText(resultSet.getDate("birth_date").toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                        addressField.setText(resultSet.getString("address"));
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Warning", "Guest Not Found", "No guest found with phone number " + phoneNumber);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database Error", "An error occurred while checking the guest.");
        }
    }

    private void fillBookingDates(int guestId) {
        // Fetch and fill the dates from the booking
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT Check_In_Date, Check_Out_Date FROM Booking WHERE ID_Guest = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, guestId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                checkInDatePicker.setValue(rs.getDate("Check_In_Date").toLocalDate());
                checkOutDatePicker.setValue(rs.getDate("Check_Out_Date").toLocalDate());
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error fetching booking dates: " + e.getMessage());
        }
    }


    @FXML
    public void onSubmitButtonClick() {
        String fullName = fullNameField.getText();
        String birthDateStr = birthDateField.getText();
        String address = addressField.getText();
        String phoneNumber = phoneNumberField.getText();
        int roomId;
        try {
            roomId = Integer.parseInt(roomIdField.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Room ID", "Please enter a valid room ID.");
            return;
        }
        LocalDate checkInDate = checkInDatePicker.getValue();
        LocalDate checkOutDate = checkOutDatePicker.getValue();

        LocalDate birthDate;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            birthDate = LocalDate.parse(birthDateStr, formatter);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Birth Date", "Please enter a valid birth date in the format dd.MM.yyyy.");
            return;
        }

        if (!isPhoneNumberValid(phoneNumber )) {
            showAlert( Alert.AlertType.ERROR,"Error","Incorrect format !", "Invalid phone number");
            return;
        }
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            String roomCheckQuery = "select count(*) from Accommodation where ID_Room = ? and (? between Date_Settlement and Date_Departure or ? between Date_Settlement and Date_Departure)";
            try (PreparedStatement roomCheckStatement = connection.prepareStatement(roomCheckQuery)) {
                roomCheckStatement.setInt(1, roomId);
                roomCheckStatement.setDate(2, Date.valueOf(checkInDate));
                roomCheckStatement.setDate(3, Date.valueOf(checkOutDate));
                try (ResultSet roomCheckResultSet = roomCheckStatement.executeQuery()) {
                    if (roomCheckResultSet.next() && roomCheckResultSet.getInt(1) > 0) {
                        showAlert(Alert.AlertType.WARNING, "Warning", "Room Occupied", "Room ID " + roomId + " is currently occupied or reserved for the selected dates.");
                        return;
                    }
                }
            }

            int guestId = -1;
            String guestQuery = "select ID_Guest from Guest where phone_number = ?";
            try (PreparedStatement guestStatement = connection.prepareStatement(guestQuery)) {
                guestStatement.setString(1, phoneNumber);
                try (ResultSet guestResultSet = guestStatement.executeQuery()) {
                    if (guestResultSet.next()) {
                        guestId = guestResultSet.getInt("ID_Guest");
                    }
                }
            }

            if (guestId == -1) {
                String insertGuestQuery = "insert into Guest (full_name, address, birth_date, phone_number) values (?, ?, ?, ?)";
                try (PreparedStatement insertGuestStatement = connection.prepareStatement(insertGuestQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    insertGuestStatement.setString(1, fullName);
                    insertGuestStatement.setString(2, address);
                    insertGuestStatement.setDate(3, Date.valueOf(birthDate));
                    insertGuestStatement.setString(4, phoneNumber);
                    insertGuestStatement.executeUpdate();

                    try (ResultSet generatedKeys = insertGuestStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            guestId = generatedKeys.getInt(1);
                        }
                    }
                }
            }

            if (guestId != -1) {
                String accommodationCheckQuery = "select count(*) from Accommodation where ID_Guest = ? and (? between Date_Settlement and Date_Departure or ? between Date_Settlement and Date_Departure)";
                try (PreparedStatement accommodationCheckStatement = connection.prepareStatement(accommodationCheckQuery)) {
                    accommodationCheckStatement.setInt(1, guestId);
                    accommodationCheckStatement.setDate(2, Date.valueOf(checkInDate));
                    accommodationCheckStatement.setDate(3, Date.valueOf(checkOutDate));
                    try (ResultSet accommodationCheckResultSet = accommodationCheckStatement.executeQuery()) {
                        if (accommodationCheckResultSet.next() && accommodationCheckResultSet.getInt(1) > 0) {
                            showAlert(Alert.AlertType.WARNING, "Warning", "Date Conflict", "The guest has an existing booking that conflicts with the selected dates.");
                            return;
                        }
                    }
                }

                String roomTypeQuery = "select rt.Cost from Room r join Room_Type rt on r.ID_Room_Type = rt.ID_Room_Type where r.Room_Number = ?";
                double roomCost = 0;
                try (PreparedStatement roomTypeStatement = connection.prepareStatement(roomTypeQuery)) {
                    roomTypeStatement.setInt(1, roomId);
                    try (ResultSet roomTypeResultSet = roomTypeStatement.executeQuery()) {
                        if (roomTypeResultSet.next()) {
                            roomCost = roomTypeResultSet.getDouble("Cost");
                        }
                    }
                }

                long days = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
                double totalCost = (roomCost / 30) * days;

                String accommodationQuery = "insert into Accommodation (ID_Guest, ID_Room, Date_Settlement, Date_Departure, Total_Cost, To_Pay) values (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement accommodationStatement = connection.prepareStatement(accommodationQuery)) {
                    accommodationStatement.setInt(1, guestId);
                    accommodationStatement.setInt(2, roomId);
                    accommodationStatement.setDate(3, Date.valueOf(checkInDate));
                    accommodationStatement.setDate(4, Date.valueOf(checkOutDate));
                    accommodationStatement.setDouble(5, totalCost);
                    accommodationStatement.setDouble(6, totalCost);
                    accommodationStatement.executeUpdate();
                }

                connection.commit();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Accommodation Successful", "Guest has been successfully accommodated.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try (Connection connection = DatabaseConnection.getConnection()) {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
            }
            showAlert(Alert.AlertType.ERROR, "Error", "Database Error", "An error occurred while processing the accommodation.");
        }
    }

    private boolean hasBooking(int guestId, LocalDate checkInDate, LocalDate checkOutDate) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT COUNT(*) FROM Booking WHERE ID_Guest = ? AND ? < Check_Out_Date AND ? > Check_In_Date";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, guestId);
            statement.setDate(2, Date.valueOf(checkInDate));
            statement.setDate(3, Date.valueOf(checkOutDate));
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error checking booking: " + e.getMessage());
        }
        return false;
    }

    public boolean isPhoneNumberValid(String phoneNumber) {
        return phoneNumber.matches("\\d{7}");
    }

    @FXML
    protected void onBackButtonClick() throws IOException {
        backToReceptionistMenu();
    }

    @FXML
    private void backToReceptionistMenu() throws IOException {
        Stage stage = (Stage) phoneNumberField.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("receptionist-main-form.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showAlert(Alert.AlertType alertType, String title, String headerText, String contentText) {
        // тимчасово
        alertMessage = contentText;
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

}
