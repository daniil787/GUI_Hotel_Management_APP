package com.example.hotelmanagement;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

import java.io.IOException;

public class SearchRoomsController {
    @FXML
    private TextField roomTypeField;
    @FXML
    private TextField amenityField;

    @FXML
    private TableView<Room> roomsTable;
    @FXML
    private TableColumn<Room, Integer> roomNumberColumn;
    @FXML
    private TableColumn<Room, Integer> capacityColumn;
    @FXML
    private TableColumn<Room, String> roomTypeColumn;
    @FXML
    private TableColumn<Room, Double> costColumn;

    private ObservableList<Room> roomsList;

    @FXML
    private TableColumn<Room, String> amenitiesColumn;

    @FXML
    public void initialize() {
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("beds"));
        roomTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));
        amenitiesColumn.setCellValueFactory(new PropertyValueFactory<>("amenities"));

        roomsList = FXCollections.observableArrayList();
        loadRoomsFromDatabase();
        roomsTable.setItems(roomsList);
    }
    private void loadRoomsFromDatabase() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT \n" +
                    "    room.Room_Number, \n" +
                    "    room.Capacity, \n" +
                    "    room_type.Room_Type, \n" +
                    "    room_type.Cost, \n" +
                    "    COALESCE(GROUP_CONCAT(amenity.Amenity SEPARATOR ', '), 'None') AS Amenity \n" +
                    "FROM \n" +
                    "    room \n" +
                    "JOIN \n" +
                    "    room_type ON room.ID_Room_Type = room_type.ID_Room_Type \n" +
                    "LEFT JOIN \n" +
                    "    amenity_room ON room.Room_Number = amenity_room.ID_Room \n" +
                    "LEFT JOIN \n" +
                    "    amenity ON amenity_room.ID_Amenity = amenity.ID_Amenity \n" +
                    "WHERE \n" +
                    "    room.Room_Number NOT IN (\n" +
                    "        SELECT \n" +
                    "            booking.ID_Room \n" +
                    "        FROM \n" +
                    "            booking\n" +
                    "        UNION\n" +
                    "        SELECT\n" +
                    "            accommodation.ID_Room\n" +
                    "        FROM\n" +
                    "            accommodation\n" +
                    "    ) \n" +
                    "GROUP BY \n" +
                    "    room.Room_Number, \n" +
                    "    room.Capacity, \n" +
                    "    room_type.Room_Type, \n" +
                    "    room_type.Cost \n" +
                    "ORDER BY \n" +
                    "    room.Room_Number;\n";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                int roomNumber = resultSet.getInt("Room_Number");
                int beds = resultSet.getInt("Capacity");
                String type = resultSet.getString("Room_Type");
                double cost = resultSet.getDouble("Cost");
                String amenity = resultSet.getString("Amenity");

                Room room = new Room(roomNumber, beds, type, cost, amenity);
                roomsList.add(room);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load rooms from database.");
        }
    }

    public void searchRooms(String roomType, String amenity) {
        ObservableList<Room> filteredList = FXCollections.observableArrayList();

        for (Room room : roomsList) {
            boolean matchesType = roomType == null || roomType.isEmpty() || room.getType().toLowerCase().contains(roomType.toLowerCase());
            boolean matchesAmenity = amenity == null || amenity.isEmpty() || room.getAmenities().toLowerCase().contains(amenity.toLowerCase());
            if (matchesType && matchesAmenity) {
                filteredList.add(room);
            }
        }
        roomsTable.setItems(filteredList);
    }

    @FXML
    protected void onSearchRoomsButtonClick() {
        String roomType = roomTypeField.getText();
        String amenity = amenityField.getText();
        searchRooms(roomType, amenity);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void backToReceptionistMenu() throws IOException {
        Stage stage = (Stage) roomsTable.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("receptionist-main-form.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
    }
}

