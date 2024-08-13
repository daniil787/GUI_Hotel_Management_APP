package com.example.hotelmanagement;

import com.example.hotelmanagement.DatabaseConnection;
import com.example.hotelmanagement.MainWindowController;
import com.example.hotelmanagement.Guest;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GuestFormController {
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField birthDateField;
    @FXML
    private TextField phoneNumberField;

    private MainWindowController mainWindowController;
    private Guest guest;

    public void setMainController(MainWindowController mainWindowController) {
        this.mainWindowController = mainWindowController;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
        if (guest != null) {
            fullNameField.setText(guest.getFullName());
            addressField.setText(guest.getAddress());
            birthDateField.setText(guest.getBirthDate());
            phoneNumberField.setText(guest.getPhoneNumber());
        }
    }

    @FXML
    protected void onSaveButtonClick() {
        String fullName = fullNameField.getText();
        String address = addressField.getText();
        String birthDate = birthDateField.getText();
        String phoneNumber = phoneNumberField.getText();

        try (Connection connection = DatabaseConnection.getConnection()) {
            if (guest == null) {

                String query = "insert into Guest (id_guest, full_name, address, birth_date, phone_number) values (?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, 1);
                preparedStatement.setString(2, fullName);
                preparedStatement.setString(3, address);
                preparedStatement.setString(4, birthDate);
                preparedStatement.setString(5, phoneNumber);
                preparedStatement.executeUpdate();
            } else {

                String query = "update Guest set  full_name = ?, address = ?, birth_date = ?, phone_number = ? where id_guest = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, fullName);
                preparedStatement.setString(2, address);
                preparedStatement.setString(3, birthDate);
                preparedStatement.setString(4, phoneNumber);
                preparedStatement.setInt(5, guest.getId());
                preparedStatement.executeUpdate();
            }

            Stage stage = (Stage) fullNameField.getScene().getWindow();
            stage.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
