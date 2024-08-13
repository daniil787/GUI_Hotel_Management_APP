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

public class RoomListController {

    @FXML
    private TableView<Room> roomTable;
    @FXML
    private TableColumn<Room, Integer> roomNumberColumn;
    @FXML
    private TableColumn<Room, String> typeColumn;
    @FXML
    private TableColumn<Room, Double> priceColumn;
    @FXML
    private TableColumn<Room, Integer> bedsColumn;
    @FXML
    private TableColumn<Room, String> amenitiesColumn;

    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button addButton;

    private ObservableList<Room> roomData = FXCollections.observableArrayList();

    public void initialize() {
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        bedsColumn.setCellValueFactory(new PropertyValueFactory<>("beds"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        amenitiesColumn.setCellValueFactory(new PropertyValueFactory<>("amenities"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));
        loadRoomData();

        editButton.setOnAction(event -> onEditRoomClick());
        deleteButton.setOnAction(event -> onDeleteRoomClick());
        addButton.setOnAction(event -> onAddRoomClick());
    }

    private void loadRoomData() {
        ObservableList<Room> roomList = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT room.Room_Number,  room.Capacity, room_type.Room_Type, " +
                    "room_type.Cost, COALESCE(GROUP_CONCAT(amenity.Amenity SEPARATOR ', '), 'None') AS Amenity "
                    + "FROM room JOIN room_type ON room.ID_Room_Type = room_type.ID_Room_Type " +
                    "LEFT JOIN amenity_room ON room.Room_Number = amenity_room.ID_Room " +
                    "LEFT JOIN amenity ON amenity_room.ID_Amenity = amenity.ID_Amenity " +
                    "WHERE room.Room_Number NOT IN (SELECT booking.ID_Room " +
                    "FROM booking UNION " +
                    "SELECT accommodation.ID_Room FROM accommodation) " +
                    "GROUP BY room.Room_Number, room.Capacity, room_type.Room_Type, room_type.Cost " +
                    "ORDER BY room.Room_Number;";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int roomNumber = resultSet.getInt("Room_Number");
                String type = resultSet.getString("Room_Type");
                double price = resultSet.getDouble("Cost");
                int beds = resultSet.getInt("Capacity");
                String amenities = resultSet.getString("Amenity");
                roomList.add(new Room(roomNumber, beds, type, price, amenities));
            }

            roomTable.setItems(roomList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error loading room data: " + e.getMessage());
        }
    }
    private void onAddRoomClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("add-room-dialog.fxml"));
            Parent root = loader.load();
            AddRoomDialogController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Add Room");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isAdded()) {
                loadRoomData();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("UI Error", "Error loading add room dialog: " + e.getMessage());
        }
    }

    private void onEditRoomClick() {
        Room selectedRoom = roomTable.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showAlert("No Selection", "Please select a room to edit.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("edit-room-dialog.fxml"));
            Parent root = loader.load();
            EditRoomDialogController controller = loader.getController();
            controller.setRoom(selectedRoom);

            Stage stage = new Stage();
            stage.setTitle("Edit Room");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isUpdated()) {
                loadRoomData();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("UI Error", "Error loading edit room dialog: " + e.getMessage());
        }
    }
    private void onDeleteRoomClick() {
        Room selectedRoom = roomTable.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showAlert("No Selection", "Please select a room to delete.");
            return;
        }

        boolean confirmed = showConfirmationDialog("Confirm Delete", "Are you sure you want to delete this room?");
        if (!confirmed) {
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {

             String checkQuery = "SELECT COUNT(*) FROM Amenity_Room WHERE ID_Room = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, selectedRoom.getRoomNumber());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                boolean deleteServicesConfirmed = showConfirmationDialog("Dependent Records Found",
                        "This room is referenced in the Amenity_Room table. " +
                                "Do you want to delete the related records as well?");
                if (!deleteServicesConfirmed) {
                    showAlert("Deletion Canceled", "Room deletion canceled due to existing references.");
                    return;
                } else {
                     String deleteServicesQuery = "DELETE FROM Amenity_Room WHERE ID_Room = ?";
                    PreparedStatement deleteServicesStmt = connection.prepareStatement(deleteServicesQuery);
                    deleteServicesStmt.setInt(1, selectedRoom.getRoomNumber());
                    deleteServicesStmt.executeUpdate();
                }
            }
            String deleteEmployeeQuery = "DELETE FROM Room WHERE Room_Number = ?";
            PreparedStatement deleteEmployeeStmt = connection.prepareStatement(deleteEmployeeQuery);
            deleteEmployeeStmt.setInt(1, selectedRoom.getRoomNumber());
            deleteEmployeeStmt.executeUpdate();

            loadRoomData();
        } catch (SQLException e) {
            showAlert("Database Error", "Error deleting room: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
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

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");

        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yesButton;
    }

    @FXML
    private void onLogoutClick() throws IOException {
        Stage stage = (Stage) roomTable.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("manager-main-form.fxml"));
        Parent mainWindow = fxmlLoader.load();
        Scene scene = new Scene(mainWindow);
        stage.setScene(scene);
    }
}
