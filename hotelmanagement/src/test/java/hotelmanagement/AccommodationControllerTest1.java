package hotelmanagement;

import com.example.hotelmanagement.AccommodationController;
import com.example.hotelmanagement.DatabaseConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AccommodationControllerTest1 {

    private AccommodationController controller;
    private Connection connectionMock;
    private PreparedStatement statementMock;
    private ResultSet resultSetMock;

    @BeforeEach
    public void setUp() throws SQLException {
        controller = new AccommodationController();
        connectionMock = mock(Connection.class);
        statementMock = mock(PreparedStatement.class);
        resultSetMock = mock(ResultSet.class);

        controller.phoneNumberField = new javafx.scene.control.TextField();
        controller.fullNameField = new javafx.scene.control.TextField();
        controller.birthDateField = new javafx.scene.control.TextField();
        controller.addressField = new javafx.scene.control.TextField();


        DatabaseConnection dbConnectionMock = mock(DatabaseConnection.class);
        when(dbConnectionMock.getConnection()).thenReturn(connectionMock);
        controller.setDatabaseConnection(dbConnectionMock);
    }

    @Test
    public void testOnCheckInButtonClick_GuestExists() throws SQLException {
        String query = "select full_name, birth_date, address from Guest where phone_number = ?";
        when(connectionMock.prepareStatement(query)).thenReturn(statementMock);
        when(statementMock.executeQuery()).thenReturn(resultSetMock);

        when(resultSetMock.next()).thenReturn(true);
        when(resultSetMock.getString("full_name")).thenReturn("John Doe");
        when(resultSetMock.getDate("birth_date")).thenReturn(Date.valueOf("1980-01-01"));
        when(resultSetMock.getString("address")).thenReturn("123 Main St");


        controller.phoneNumberField.setText("5551234");

        controller.onCheckInButtonClick();

        verify(statementMock).setString(1, "5551234");

        assertEquals("John Doe", controller.fullNameField.getText());
        assertEquals("01.01.1980", controller.birthDateField.getText());
        assertEquals("123 Main St", controller.addressField.getText());
    }

    @Test
    public void testOnSubmitButtonClick_NewGuest() throws SQLException {
        String guestQuery = "select ID_Guest from Guest where phone_number = ?";
        String roomCheckQuery = "select count(*) from Accommodation where ID_Room = ? and (? between Date_Settlement and Date_Departure or ? between Date_Settlement and Date_Departure)";
        String insertGuestQuery = "insert into Guest (full_name, address, birth_date, phone_number) values (?, ?, ?, ?)";
        String costQuery = "select rt.Cost from Room r join Room_Type rt on r.ID_Room_Type = rt.ID_Room_Type where r.Room_Number = ?";
        String insertAccommodationQuery = "insert into Accommodation (ID_Guest, ID_Room, Date_Settlement, Date_Departure, Total_Cost, To_Pay) values (?, ?, ?, ?, ?, ?)";

        when(connectionMock.prepareStatement(anyString())).thenReturn(statementMock);
        when(statementMock.executeQuery()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(false);


        when(statementMock.getGeneratedKeys()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true);
        when(resultSetMock.getInt(1)).thenReturn(1);
    }
}