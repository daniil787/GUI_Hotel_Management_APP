package com.example.hotelmanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CalculationService {

    public static double calculateTotalCost(int accommodationId) throws SQLException {
        double totalCost = 0;

        try (Connection connection = DatabaseConnection.getConnection()) {
            String roomCostQuery = "select rt.price, a.guest_count from Accommodation a join Room r on a.room_id = r.id join RoomType rt on r.type_id = rt.id where a.id = ?";
            PreparedStatement roomCostStatement = connection.prepareStatement(roomCostQuery);
            roomCostStatement.setInt(1, accommodationId);
            ResultSet roomCostResultSet = roomCostStatement.executeQuery();
            if (roomCostResultSet.next()) {
                double roomPrice = roomCostResultSet.getDouble("price");
                int guestCount = roomCostResultSet.getInt("guest_count");
                totalCost += roomPrice;
                if (guestCount > 1) {
                    totalCost += (roomPrice / 3) * (guestCount - 1);
                }
            }

            String serviceCostQuery = "select s.price, sa.quantity from Service_Accommodation sa join Service s on sa.service_id = s.id where sa.accommodation_id = ?";
            PreparedStatement serviceCostStatement = connection.prepareStatement(serviceCostQuery);
            serviceCostStatement.setInt(1, accommodationId);
            ResultSet serviceCostResultSet = serviceCostStatement.executeQuery();
            while (serviceCostResultSet.next()) {
                double servicePrice = serviceCostResultSet.getDouble("price");
                int quantity = serviceCostResultSet.getInt("quantity");
                totalCost += servicePrice * quantity;
            }
        }

        return totalCost;
    }
}
