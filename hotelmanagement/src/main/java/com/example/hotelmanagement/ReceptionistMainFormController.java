package com.example.hotelmanagement;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class ReceptionistMainFormController {

    private AvailableRoomsController availableRoomsController;
    @FXML
    private Label loginStatusLabel;

    @FXML
    private void initialize() {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("available-rooms.fxml"));
        try {
            fxmlLoader.load();
            availableRoomsController = fxmlLoader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUsername(String username) {
        loginStatusLabel.setText("Login done by: " + username);
    }

    @FXML
    protected void onViewAvailableRoomsButtonClick() throws IOException {
        if (availableRoomsController != null) {
            availableRoomsController.loadAvailableRooms();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load available rooms.");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("available-rooms.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        availableRoomsController = fxmlLoader.getController();
        availableRoomsController.loadAvailableRooms();
        Stage stage = (Stage) Stage.getWindows().filtered(window -> window.isShowing()).get(0);
        stage.setScene(scene);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    protected void onSearchRoomsButtonClick() throws IOException {
        Stage stage = (Stage) Stage.getWindows().filtered(window -> window.isShowing()).get(0);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("search-rooms.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
    }

    @FXML
    protected void onBookRoomButtonClick() throws IOException {
        Stage stage = (Stage) Stage.getWindows().filtered(window -> window.isShowing()).get(0);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("booking-form.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
    }

//    @FXML
//    protected void onCheckInButtonClick() throws IOException {
//        Stage stage = (Stage) Stage.getWindows().filtered(window -> window.isShowing()).get(0);
//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("check-in-form.fxml"));
//        Scene scene = new Scene(fxmlLoader.load());
//        stage.setScene(scene);
//    }

    @FXML
    protected void onBackButtonClick() throws IOException {
        Stage stage = (Stage) Stage.getWindows().filtered(window -> window.isShowing()).get(0);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main-continue.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
    }

    @FXML
    protected void onAccommodationButtonClick() throws IOException {
        Stage stage = (Stage) Stage.getWindows().filtered(window -> window.isShowing()).get(0);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("accommodation-form.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
    }

    @FXML
    protected void onServiceButtonClick() throws IOException {
        Stage stage = (Stage) Stage.getWindows().filtered(window -> window.isShowing()).get(0);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("service-form.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);


    }

    @FXML
    private void onPaymentButtonClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("payment-form.fxml"));
        Parent paymentForm = fxmlLoader.load();
        Stage stage = (Stage) Stage.getWindows().filtered(window -> window.isShowing()).get(0);
        Scene scene = new Scene(paymentForm);
        stage.setScene(scene);
    }
    @FXML
    protected void onExitButtonClick() {
        Stage stage = (Stage) Stage.getWindows().filtered(window -> window.isShowing()).get(0);
        stage.close();
    }
}
