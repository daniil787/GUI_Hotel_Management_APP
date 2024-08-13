package hotelmanagement;

import com.example.hotelmanagement.AccommodationController;
import com.example.hotelmanagement.DatabaseConnection;
import javafx.scene.control.TextField;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccommodationControllerTest {

    @InjectMocks
    private AccommodationController controller;

    @Mock
    private DatabaseConnection dbConnection;


    @Test
    void testOnCheckInButtonClick_GuestFound() throws SQLException, ParseException {

        String query = "select full_name, birth_date, address from Guest where phone_number = ?";


        MockitoAnnotations.openMocks(this);
        Connection connection = DatabaseConnection.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
preparedStatement.setString(1,"5551234");
        ResultSet resultSet = preparedStatement.executeQuery();

        controller.phoneNumberField = new TextField();
        controller.fullNameField = new TextField();
        controller.birthDateField = new TextField();
        controller.addressField = new TextField();
        controller.setDatabaseConnection(dbConnection);

        controller.phoneNumberField = new TextField();

        controller.phoneNumberField.setText("5551234");

        controller.onCheckInButtonClick();

        verify(preparedStatement).setString(1, "5551234");

        Assertions.assertTrue(resultSet.next());
        Assertions.assertEquals(resultSet.getString("full_name"), "John Doe");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        Assertions.assertEquals(resultSet.getDate("birth_date"), new java.sql.Date(dateFormat.parse("01.01.1990").getTime()));
        Assertions.assertEquals(resultSet.getString("address"), "123 Main St");
        assertThat(controller.getAlertMessage()).isEqualTo("Data autofill completed");
    }
}