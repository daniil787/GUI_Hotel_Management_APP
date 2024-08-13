module com.example.hotelmanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.hotelmanagement to javafx.fxml;
    exports com.example.hotelmanagement;
}
