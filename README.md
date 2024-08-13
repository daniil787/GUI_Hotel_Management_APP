# About program

This app can give access to relevant information and functions, depending on the user's role(receptionist or director/deputy director) and manage information.

The following functions are available to the receptionist:
1)booking; 
2)accommodation; 
3) viewing information about room occupancy, bookings for accommodation; 
4)registration of additional services;
5)fixing payment for all services.

The following functions are available to the director/deputy director:
1) add, delete, change information about employeers;
2) add, delete, change information about rooms;
3) add, delete, change information about services.

## Steps before running the program

For your database connection (for example,
MySQL Connection), you need to run the scripts for creating the corresponding database and adding the prepared data to it for convenient familiarization with the program.

The scripts are in the db_scripts folder.

Then you need to specify the url, user_name, password of your connection in the DatabaseConnection file (located along the path hotelmanagement-->hotelmanagement-->src-->main-->java-->com.example.hotelmanagement)
before running, you also need to make sure that the local libraries, in the project structure (Project Structure tab) indicate the paths to the lib JavaFX folder.

### Use program
The program is used by logging in or registering

To register, you need to use the data of the receptionist/director from the database.

Please note that in the script "Adding.Info" there are employees with other professions and the program will not allow their registration.

Therefore, for correct registration, the following must be specified correctly: 1) the full name of the receptionist 2) his number.

When logging in, the program processes errors: incorrect password or login, non-existent user, etc.

After logging in, the user is provided with a menu corresponding to the role: for the receptionist, the menu consists of forms for selection:

1) "View Available Rooms": displays data on all rooms that have not been booked/occupied.
2) "Search Rooms" - when entering the type and/or amenities of the room in full or part of the word/words, the corresponding rooms are displayed,
if the fields are empty, all available rooms are displayed.
3) "Book Room" - a form for creating a booking entry, contains checks for entering data (entered data type, availability of the entered room
for the specified stay period),
in this form you can check the presence of a guest in the database, if he is, then his data is automatically filled in the fields
4) "Accommodation" - a form for creating an entry for accommodation, similar to the "Book Room" form, at this stage the cost of accommodation is calculated
by day and information about payment is added
5) "Service" - a form for processing additional services for entries for accommodation, contains: 1) a table of the required data, 2) drop-down lists of services
and employees providing such services, 4) fields for entering data.
!! there is no check for entering non-integer data in the "Accommodation ID" field!!
!!There is no check for indicating the previous date of service provision!!
6) "Payment" - a form for indicating the fact of payment for services (accommodation and additional services): contains a field for entering the ID of the accommodation, the "Pay" button, a table with data, and the output of the amount of all unpaid services.

After logging in, the user is provided with a menu corresponding to the role: for the director/deputy director(data is contained in the table "Manager" of DB), the menu consists of forms for selection:

1) list of employees with information about them, the ability to change it, delete and add records about employees
2) list of numbers: functions are similar to the previous list
3) list of services: capabilities are similar to the previous list
