package hotelmanagement;

import java.sql.Connection;

import static org.mockito.Mockito.mock;

public class MockDatabaseConnection {
    public static Connection getConnection() {
        return mock(Connection.class);
    }
}
