package com.example.hotelmanagement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.util.Optional;

public class ServiceListController {

    @FXML
    private TableView<Service> serviceTable;
    @FXML
    private TableColumn<Service, Integer> idColumn;
    @FXML
    private TableColumn<Service, String> nameColumn;
    @FXML
    private TableColumn<Service, Double> priceColumn;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button addButton;

    private ObservableList<Service> serviceData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        loadServiceData();

        editButton.setOnAction(event -> onEditServiceClick());
        deleteButton.setOnAction(event -> onDeleteServiceClick());
        addButton.setOnAction(event -> onAddServiceClick());
    }

    private void loadServiceData() {
        serviceData.clear();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT ID_Service, Service, Cost FROM Service";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Service service = new Service(
                        rs.getInt("ID_Service"),
                        rs.getString("Service"),
                        rs.getDouble("Cost")
                );
                serviceData.add(service);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading service data: " + e.getMessage());
        }
        serviceTable.setItems(serviceData);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void onAddServiceClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("add-service-dialog.fxml"));
            Parent root = loader.load();
            AddServiceDialogController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Add Service");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isAdded()) {
                loadServiceData();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("UI Error", "Error loading add service dialog: " + e.getMessage());
        }
    }
    private void onDeleteServiceClick() {
        Service selectedService = serviceTable.getSelectionModel().getSelectedItem();
        if (selectedService == null) {
            showAlert("No Selection", "Please select a service to delete.");
            return;
        }

        boolean confirmed = showConfirmationDialog("Confirm Delete", "Are you sure you want to delete this service?");
        if (!confirmed) {
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {

            String checkQuery = "SELECT COUNT(*) FROM Service_Accommodation WHERE ID_Service = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, selectedService.getId());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                showAlert("Deletion Error", "Cannot delete this service because it is referenced in the Service_Accommodation table.");
                return;
            }
            String deleteServiceQuery = "DELETE FROM Service WHERE ID_Service = ?";
            PreparedStatement deleteServiceStmt = connection.prepareStatement(deleteServiceQuery);
            deleteServiceStmt.setInt(1, selectedService.getId());
            deleteServiceStmt.executeUpdate();

            loadServiceData();
        } catch (SQLException e) {
            showAlert("Database Error", "Error deleting service: " + e.getMessage());
        }
    }

    @FXML
    private void onEditServiceClick() {
        Service selectedService = serviceTable.getSelectionModel().getSelectedItem();
        if (selectedService == null) {
            showAlert("Selection Error", "Please select a service to edit.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("edit-service-dialog.fxml"));
            Parent root = loader.load();
            EditServiceDialogController controller = loader.getController();
            controller.setService(selectedService);

            Stage stage = new Stage();
            stage.setTitle("Edit Service");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isUpdated()) {
                loadServiceData();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to load the edit dialog.");
        }
    }

    private boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");

        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yesButton;
    }

    @FXML
    private void onLogoutClick() throws IOException {
        Stage stage = (Stage) serviceTable.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("manager-main-form.fxml"));
        Parent mainWindow = fxmlLoader.load();
        Scene scene = new Scene(mainWindow);
        stage.setScene(scene);
    }

}
