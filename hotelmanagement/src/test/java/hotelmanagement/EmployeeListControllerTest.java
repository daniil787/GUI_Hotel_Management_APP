package hotelmanagement;

import com.example.hotelmanagement.Employee;
import com.example.hotelmanagement.EmployeeListController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeListControllerTest {

    @Mock
    private TableView<Employee> employeeTable;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private EmployeeListController controller;

    private ObservableList<Employee> employeeData;

    @BeforeEach
    public void setUp() {
        employeeData = FXCollections.observableArrayList(
                new Employee(1, "John Doe", "Manager", "123 Main St", "555-5555", 50000.0)
        );

        when(employeeTable.getSelectionModel().getSelectedItem()).thenReturn(employeeData.get(0));
        controller.employeeData = employeeData;
    }

    @Test
    public void testOnDeleteEmployeeClick_EmployeeNotSelected() {
        when(employeeTable.getSelectionModel().getSelectedItem()).thenReturn(null);

        controller.onDeleteEmployeeClick();

        verifyNoInteractions(connection, preparedStatement, resultSet);

    }

    @Test
    public void testOnDeleteEmployeeClick_EmployeeDeletedSuccessfully() throws Exception {

        Employee employee = employeeData.get(0);
        when(controller.showConfirmationDialog(anyString(), anyString())).thenReturn(true);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(0);


        doNothing().when(preparedStatement).executeUpdate();

        controller.onDeleteEmployeeClick();

        verify(preparedStatement, times(1)).setInt(1, employee.getId());
        verify(preparedStatement, times(2)).executeUpdate();

    }

    @Test
    public void testOnDeleteEmployeeClick_EmployeeHasDependentRecords() throws Exception {
        Employee employee = employeeData.get(0);
        when(controller.showConfirmationDialog(anyString(), anyString())).thenReturn(true);
        when(controller.showConfirmationDialog(eq("Dependent Records Found"), anyString())).thenReturn(true);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);


        doNothing().when(preparedStatement).executeUpdate();

        controller.onDeleteEmployeeClick();

        verify(preparedStatement, times(3)).setInt(1, employee.getId());
        verify(preparedStatement, times(3)).executeUpdate();
    }

    @Test
    public void testOnDeleteEmployeeClick_DeletionCancelled() throws Exception {
        Employee employee = employeeData.get(0);
        when(controller.showConfirmationDialog(anyString(), anyString())).thenReturn(false);

        controller.onDeleteEmployeeClick();

        verify(preparedStatement, never()).executeUpdate();
        verify(connection, never()).prepareStatement(anyString());
    }
}
