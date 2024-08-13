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
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceController {
    @FXML
    private TableView<ServiceRecord> serviceTableView;
    @FXML
    private TableColumn<ServiceRecord, Integer> accommodationIdColumn;
    @FXML
    private TableColumn<ServiceRecord, Integer> roomNumberColumn;
    @FXML
    private TableColumn<ServiceRecord, String> guestNamesColumn;
    @FXML
    private TableColumn<ServiceRecord, String> servicesColumn;
    @FXML
    private TextField accommodationIdField;
    @FXML
    private ComboBox<String> serviceComboBox;
    @FXML
    private ComboBox<String> employeeComboBox;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;

    private Connection connection;

    public void initialize() {
        try {
            connection = DatabaseConnection.getConnection();
            loadServiceComboBox();
            loadServiceRecords();
            setupTableColumns();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Connection Error", "Failed to connect to the database");
        }
    }

    private void loadServiceComboBox() {
        ObservableList<String> services = FXCollections.observableArrayList();
        String query = "SELECT Service FROM Service";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                services.add(resultSet.getString("Service"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        serviceComboBox.setItems(services);
    }

    private void loadServiceRecords() {
        ObservableList<ServiceRecord> serviceRecords = FXCollections.observableArrayList();
        String query = "select A.ID_Accommodation, A.ID_Room, G.Full_Name, " +
                "(Select Group_Concat(S.Service SEPARATOR ', ') from Service_Accommodation SA " +
                "join Service S ON SA.ID_Service = S.ID_Service where SA.ID_Accommodation = A.ID_Accommodation) as Services " +
                "from Accommodation A " +
                "join Guest G ON A.ID_Guest = G.ID_Guest";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                int accommodationId = resultSet.getInt("ID_Accommodation");
                int roomNumber = resultSet.getInt("ID_Room");
                String guestNames = resultSet.getString("Full_Name");
                String services = resultSet.getString("Services");

                serviceRecords.add(new ServiceRecord(accommodationId, roomNumber, guestNames, services));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        serviceTableView.setItems(serviceRecords);
    }

    private void setupTableColumns() {
        accommodationIdColumn.setCellValueFactory(new PropertyValueFactory<>("accommodationId"));
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        guestNamesColumn.setCellValueFactory(new PropertyValueFactory<>("guestNames"));
        servicesColumn.setCellValueFactory(new PropertyValueFactory<>("services"));
    }

    @FXML
    private void onServiceComboBoxAction() {
        String selectedService = serviceComboBox.getSelectionModel().getSelectedItem();
        updateEmployeeComboBox(selectedService);
    }

    private void updateEmployeeComboBox(String service) {
        List<String> providers = new ArrayList<>();
        String query = "SELECT E.Full_Name FROM Employee E " +
                "JOIN Profesion P ON E.ID_Profesion = P.ID_Profesion " +
                "JOIN Service_Profesion SP ON P.ID_Profesion = SP.ID_Profesion " +
                "JOIN Service S ON SP.ID_Service = S.ID_Service " +
                "WHERE S.Service = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, service);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    providers.add(resultSet.getString("Full_Name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        employeeComboBox.setItems(FXCollections.observableArrayList(providers));
    }

    @FXML
    protected void onAddServiceButtonClick() {
        String accommodationIdText = accommodationIdField.getText();
        String service = serviceComboBox.getValue();
        String employee = employeeComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (accommodationIdText == null || accommodationIdText.isEmpty() || service == null || service.isEmpty() ||
                employee == null || employee.isEmpty() || startDate == null || endDate == null) {
            showAlert(Alert.AlertType.ERROR, "Form Error!", "Please enter all fields");
            return;
        }

        int accommodationId = Integer.parseInt(accommodationIdText);

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO Service_Accommodation (ID_Service, ID_Accommodation, ID_Employee, Date_Serv_Begin, Date_Serv_End) " +
                    "VALUES ((SELECT ID_Service FROM Service WHERE Service = ?), ?, " +
                    "(SELECT ID_Employee FROM Employee WHERE Full_Name = ?), ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, service);
                statement.setInt(2, accommodationId);
                statement.setString(3, employee);
                statement.setDate(4, java.sql.Date.valueOf(startDate));
                statement.setDate(5, java.sql.Date.valueOf(endDate));

                statement.executeUpdate();
            }
            updateAccommodationCost(accommodationId);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Service added successfully");
            loadServiceComboBox();
            loadServiceRecords();

            accommodationIdField.clear();
            serviceComboBox.setValue(null);
            employeeComboBox.setValue(null);
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add service");
        }
    }

    private void updateAccommodationCost(int accommodationId) {
        String query = "SELECT SUM(S.Cost) as TotalServiceCost " +
                "FROM Service_Accommodation SA " +
                "JOIN Service S ON SA.ID_Service = S.ID_Service " +
                "WHERE SA.ID_Accommodation = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, accommodationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    double totalServiceCost = resultSet.getDouble("TotalServiceCost");

                    String updateQuery = "UPDATE Accommodation SET Total_Cost = Total_Cost  + ?, To_Pay = To_Pay + ? WHERE ID_Accommodation = ?";
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                        updateStatement.setDouble(1, totalServiceCost);
                        updateStatement.setDouble(2, totalServiceCost);
                        updateStatement.setInt(3, accommodationId);
                        updateStatement.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update accommodation cost");
        }
    }

    @FXML
    protected void onBackButtonClick() throws IOException {
        Stage stage = (Stage) accommodationIdField.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("receptionist-main-form.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class ServiceRecord {
        private final int accommodationId;
        private final int roomNumber;
        private final String guestNames;
        private final String services;

        public ServiceRecord(int accommodationId, int roomNumber, String guestNames, String services) {
            this.accommodationId = accommodationId;
            this.roomNumber = roomNumber;
            this.guestNames = guestNames;
            this.services = services;
        }

        public int getAccommodationId() {
            return accommodationId;
        }

        public int getRoomNumber() {
            return roomNumber;
        }

        public String getGuestNames() {
            return guestNames;
        }

        public String getServices() {
            return services;
        }
    }
}
