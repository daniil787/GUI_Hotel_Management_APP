package com.example.hotelmanagement;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class CalculateCostController {

    @FXML
    private TextField accommodationIdField;
    @FXML
    private Label totalCostLabel;

    @FXML
    protected void onCalculateCostButtonClick() {
        int accommodationId = Integer.parseInt(accommodationIdField.getText());
        try {
            double totalCost = CalculationService.calculateTotalCost(accommodationId);
            totalCostLabel.setText("Total Cost: " + totalCost);
        } catch (SQLException e) {
            e.printStackTrace();
            totalCostLabel.setText("Error calculating cost.");
        }
    }
}
