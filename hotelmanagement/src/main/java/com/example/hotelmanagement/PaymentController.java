package com.example.hotelmanagement;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class PaymentController {

    @FXML
    private TextField accommodationIdTextField;
    @FXML
    private TableView<PaymentDetails> paymentTable;
    @FXML
    private TableColumn<PaymentDetails, Integer> idAccommodationColumn;
    @FXML
    private TableColumn<PaymentDetails, Integer> roomNumberColumn;
    @FXML
    private TableColumn<PaymentDetails, String> guestNamesColumn;
    @FXML
    private TableColumn<PaymentDetails, Double> toPayColumn;
    @FXML
    private Label totalAmountLabel;

    @FXML
    public void initialize() {
        idAccommodationColumn.setCellValueFactory(new PropertyValueFactory<>("idAccommodation"));
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        guestNamesColumn.setCellValueFactory(new PropertyValueFactory<>("guestNames"));
        toPayColumn.setCellValueFactory(new PropertyValueFactory<>("toPay"));
        loadPaymentDetails();
    }

    private void loadPaymentDetails() {
        paymentTable.getItems().clear();
        double totalCost = 0;

        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "select a.ID_Accommodation, r.Room_Number, g.Full_Name, a.To_Pay " +
                            "from Accommodation a " +
                            "join Room r on a.ID_Room = r.Room_Number " +
                            "join Guest g on a.ID_Guest = g.ID_Guest");

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int idAccommodation = resultSet.getInt("ID_Accommodation");
                int roomNumber = resultSet.getInt("Room_Number");
                String guestNames = resultSet.getString("Full_Name");
                double toPay = resultSet.getDouble("To_Pay");
                totalCost += toPay;


                PaymentDetails paymentDetails = new PaymentDetails(idAccommodation, roomNumber, guestNames, toPay);
                paymentTable.getItems().add(paymentDetails);
            }

            totalAmountLabel.setText(String.format("%.2f", totalCost));
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error loading payment details: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void onPayButtonClick() {
        String accommodationIdText = accommodationIdTextField.getText();
        if (accommodationIdText == null || accommodationIdText.trim().isEmpty()) {
            showAlert("Invalid Input", "Please enter an Accommodation ID.");
            return;
        }

        try {
            int accommodationID = Integer.parseInt(accommodationIdText);
            validateAndProcessPayment(accommodationID);
        } catch (NumberFormatException e) {
            showAlert("Invalid ID", "Please enter a valid Accommodation ID.");
        }
    }

    private void validateAndProcessPayment(int accommodationID) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "select To_Pay from Accommodation where ID_Accommodation = ?");
            statement.setInt(1, accommodationID);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                double toPay = resultSet.getDouble("To_Pay");

                if (toPay == 0) {
                    showAlert("Payment Status", "This accommodation has already been paid for.");
                } else {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                            "The amount to pay is " + toPay + ". Do you want to proceed?", ButtonType.YES, ButtonType.NO);
                    confirmAlert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            processPayment(accommodationID, toPay);
                        }
                    });
                }
            } else {
                showAlert("Invalid ID", "No accommodation found with the given ID.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error validating payment details: " + e.getMessage());
        }
    }

    private void processPayment(int accommodationID, double amountToPay) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement updateStatement = connection.prepareStatement(
                    "update Accommodation set To_Pay = 0 where ID_Accommodation = ?");
            updateStatement.setInt(1, accommodationID);
            updateStatement.executeUpdate();


            PreparedStatement insertPaymentStatement = connection.prepareStatement(
                    "insert into Payment (ID_Accommodation, Cost, Data_Payment) values (?, ?, ?)");
            insertPaymentStatement.setInt(1, accommodationID);
            insertPaymentStatement.setDouble(2, amountToPay);
            insertPaymentStatement.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
            insertPaymentStatement.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Payment Success", "Payment has been successfully processed.");
            loadPaymentDetails();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error processing payment: " + e.getMessage());
        }
    }
    @FXML
    private void onBackButtonClick() throws IOException {
        Stage stage = (Stage) accommodationIdTextField.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("receptionist-main-form.fxml"));
        Parent receptionistForm = fxmlLoader.load();
        Scene scene = new Scene(receptionistForm);
        stage.setScene(scene);
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
