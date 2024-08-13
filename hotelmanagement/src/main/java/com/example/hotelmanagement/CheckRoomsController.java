package com.example.hotelmanagement;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CheckRoomsController {

    @FXML
    private ListView<String> roomsListView;

    @FXML
    public void initialize() {
        loadRooms();
    }

    private void loadRooms() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "select * from Room";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String roomInfo = "Room " + resultSet.getInt("room_number") +
                        " - Type: " + resultSet.getString("room_type") +
                        " - Price: $" + resultSet.getDouble("cost");
                roomsListView.getItems().add(roomInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
