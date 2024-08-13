package com.example.hotelmanagement;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AvailableRoomsController {

    @FXML
    private TableView<Room> roomsTable;

    @FXML
    private TableColumn<Room, Integer> roomNumberColumn;
    @FXML
    private TableColumn<Room, Integer> bedsColumn;
    @FXML
    private TableColumn<Room, String> typeColumn;
    @FXML
    private TableColumn<Room, String> amenityColumn;
    @FXML
    private TableColumn<Room, Double> costColumn;

    @FXML
    public void initialize() {
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        bedsColumn.setCellValueFactory(new PropertyValueFactory<>("beds"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));
        amenityColumn.setCellValueFactory(new PropertyValueFactory<>("amenities"));
    }
    @FXML
    public void loadAvailableRooms() {

        roomsTable.getItems().clear();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "select room.Room_Number,  room.Capacity, room_type.Room_Type, " +
                    "room_type.Cost, coalesce(group_concat(amenity.Amenity separator ', '), 'None') as Amenity "
                    + "from room join room_type on room.ID_Room_Type = room_type.ID_Room_Type " +
                    "left join amenity_room on room.Room_Number = amenity_room.ID_Room " +
                    "left join amenity on amenity_room.ID_Amenity = amenity.ID_Amenity " +
                    "where room.Room_Number not in (select booking.ID_Room " +
                    "from booking union " +
                    "select accommodation.ID_Room from accommodation) " +
                    "group by room.Room_Number, room.Capacity, room_type.Room_Type, room_type.Cost " +
                    "order by room.Room_Number;";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                int roomNumber = resultSet.getInt("room_number");
                int beds = resultSet.getInt("capacity");

                String type = resultSet.getString("Room_Type");
                String amenity = resultSet.getString("Amenity");
                double price = resultSet.getDouble("cost");
                Room room = new Room(roomNumber, beds, type, price, amenity);


                roomsTable.getItems().add(room);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
       }

    @FXML
    private void backToReceptionistMenu() throws IOException {
        Stage stage = (Stage) roomsTable.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("receptionist-main-form.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
    }
}

