package hotelmanagement;

import com.example.hotelmanagement.AccommodationController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class Phones {


    @Test
    public void testIsPhoneNumberValid() {
        AccommodationController controller = new AccommodationController();
        assertTrue(controller.isPhoneNumberValid("1234567"));
          }

    @Test
    public void testIsPhoneNumberInvalid() {
        AccommodationController controller = new AccommodationController();
        assertFalse(controller.isPhoneNumberValid("12345"));
        assertFalse(controller.isPhoneNumberValid("12345678"));

        assertFalse(controller.isPhoneNumberValid("1234abc"));
        assertFalse(controller.isPhoneNumberValid("abcdefg"));
        assertFalse(controller.isPhoneNumberValid("1234 567"));
    }
    @Test
    public void testIsPhoneNumberIsEmpty() {
        AccommodationController controller = new AccommodationController();
        assertFalse(controller.isPhoneNumberValid(""));
    }
}