package com.example.hotelmanagement;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ManagerMainFormController {

    @FXML
    private Label loginStatusLabel;

    public void setUsername(String username) {
        loginStatusLabel.setText("Login done by: " + username);
    }

    @FXML
    private void onListEmployeesClick() throws IOException {
        show("employee-list.fxml");
    }

    @FXML
    private void onListRoomsClick() throws IOException {
        show("room-list.fxml");
    }

    @FXML
    private void onListServicesClick() throws IOException {
        show("service-list.fxml");
    }

    @FXML
    private void onListAmenitiesClick() throws IOException {
        show("amenity-list.fxml");
    }

    @FXML
    private void onMostPopularRoomTypeClick() throws IOException {
        show("popular-room-type.fxml");
    }

    @FXML
    private void onMostPopularServiceClick() throws IOException {
        show("popular-service.fxml");
    }

    @FXML
    private void onLogoutClick() throws IOException {
        Stage stage = (Stage) loginStatusLabel.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main-continue.fxml"));
        Parent mainWindow = fxmlLoader.load();
        Scene scene = new Scene(mainWindow);
        stage.setScene(scene);
    }

    private void show(String fxmlFile) throws IOException {
        Stage stage = (Stage) Stage.getWindows().filtered(window -> window.isShowing()).get(0);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    protected void onExitButtonClick() {
        Stage stage = (Stage) Stage.getWindows().filtered(window -> window.isShowing()).get(0);
        stage.close();
    }
}
