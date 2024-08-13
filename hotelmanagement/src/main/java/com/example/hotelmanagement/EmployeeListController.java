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

public class EmployeeListController {

    @FXML
    private TableView<Employee> employeeTable;
    @FXML
    private TableColumn<Employee, Integer> idColumn;
    @FXML
    private TableColumn<Employee, String> nameColumn;
    @FXML
    private TableColumn<Employee, String> positionColumn;
    @FXML
    private TableColumn<Employee, String> addressColumn;
    @FXML
    private TableColumn<Employee, String> phoneColumn;
    @FXML
    private TableColumn<Employee, Double> salaryColumn;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button addButton;

    public ObservableList<Employee> employeeData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        salaryColumn.setCellValueFactory(new PropertyValueFactory<>("salary"));

        loadEmployeeData();

        editButton.setOnAction(event -> onEditEmployeeClick());
        deleteButton.setOnAction(event -> onDeleteEmployeeClick());
        addButton.setOnAction(event -> onAddEmployeeClick());
    }

    private void loadEmployeeData() {
        employeeData.clear();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "select e.ID_Employee, e.Full_Name, p.Profesion, e.Address, e.Phone_Number, e.Salary " +
                    "from Employee e " +
                    "join Profesion p ON e.ID_Profesion = p.ID_Profesion";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Employee employee = new Employee(
                        rs.getInt("ID_Employee"),
                        rs.getString("Full_Name"),
                        rs.getString("Profesion"),
                        rs.getString("Address"),
                        rs.getString("Phone_Number"),
                        rs.getDouble("Salary")
                );
                employeeData.add(employee);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading employee data: " + e.getMessage());
        }
        employeeTable.setItems(employeeData);
    }


    public void onDeleteEmployeeClick() {
        Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();
        if (selectedEmployee == null) {
            showAlert("No Selection", "Please select an employee to delete.");
            return;
        }

        boolean confirmed = showConfirmationDialog("Confirm Delete", "Are you sure you want to delete this employee?");
        if (!confirmed) {
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            //Check if the employee is referenced in the Service_Accommodation table
            String checkQuery = "select count(*) from Service_Accommodation where ID_Employee = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, selectedEmployee.getId());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                boolean deleteServicesConfirmed = showConfirmationDialog("Dependent Records Found",
                        "This employee is add to accommodation as service provider. " +
                                "Do you want to delete the related accommodation as well?");
                if (!deleteServicesConfirmed) {
                    showAlert("Deletion Canceled", "Employee deletion canceled due to existing accommodation.");
                    return;
                } else {
                    //delete related records from Service_Accommodation table
                    String deleteServicesQuery = "delete from Service_Accommodation where ID_Employee = ?";
                    PreparedStatement deleteServicesStmt = connection.prepareStatement(deleteServicesQuery);
                    deleteServicesStmt.setInt(1, selectedEmployee.getId());
                    deleteServicesStmt.executeUpdate();
                }
            }
            String deleteEmployeeQuery = "delete from Employee where ID_Employee = ?";
            PreparedStatement deleteEmployeeStmt = connection.prepareStatement(deleteEmployeeQuery);
            deleteEmployeeStmt.setInt(1, selectedEmployee.getId());
            deleteEmployeeStmt.executeUpdate();

            loadEmployeeData();
        } catch (SQLException e) {
            showAlert("Database Error", "Error deleting employee: " + e.getMessage());
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void onEditEmployeeClick() {
        Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();
        if (selectedEmployee == null) {
            showAlert("No Selection", "Please select an employee to edit.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("edit-employee-dialog.fxml"));
            Parent root = loader.load();
            EditEmployeeDialogController controller = loader.getController();
            controller.setEmployee(selectedEmployee);

            Stage stage = new Stage();
            stage.setTitle("Edit Employee");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isUpdated()) {
                loadEmployeeData();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("UI Error", "Error loading edit employee dialog: " + e.getMessage());
        }
    }
    private void onAddEmployeeClick() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("add-employee-dialog.fxml"));
                Parent root = loader.load();
                AddEmployeeDialogController controller = loader.getController();

                Stage stage = new Stage();
                stage.setTitle("Add Employee");
                stage.setScene(new Scene(root));
                stage.showAndWait();

                if (controller.isAdded()) {
                    loadEmployeeData();
                }
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("UI Error", "Error loading add employee dialog: " + e.getMessage());
            }
        }
    public boolean showConfirmationDialog(String title, String message) {
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
        Stage stage = (Stage) employeeTable.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("manager-main-form.fxml"));
        Parent mainWindow = fxmlLoader.load();
        Scene scene = new Scene(mainWindow);
        stage.setScene(scene);
    }
}
