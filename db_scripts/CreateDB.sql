CREATE SCHEMA IF NOT EXISTS `Hotel`;

use `Hotel`;
CREATE TABLE IF NOT EXISTS `Amenity` (
  `ID_Amenity` INT NOT NULL auto_increment,
  `Amenity` VARCHAR(50) NOT NULL,
  `Cost` DECIMAL NOT NULL,
  `State` VARCHAR(50) NULL,
  PRIMARY KEY (`ID_Amenity`))
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `Room_Type` (
  `ID_Room_Type` INT NOT NULL auto_increment,
  `Room_Type` VARCHAR(25) NOT NULL,
  `Cost` DECIMAL NOT NULL,
  PRIMARY KEY (`ID_Room_Type`))
ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS `Room` (
  `Room_Number` INT NOT NULL,
  `ID_Room_Type` INT not NULL,
  `Capacity` INT not NULL,
  PRIMARY KEY (`Room_Number`),

	constraint `fk_ID_Room_Type`    
    FOREIGN KEY (`ID_Room_Type`)
    REFERENCES `Room_Type` (`ID_Room_Type`)
    on delete  restrict
    on update cascade)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `Amenity_Room` (
  `ID_Room` INT NOT NULL,
  `ID_Amenity` INT NOT NULL,
	PRIMARY KEY (`ID_Room`, `ID_Amenity`),

	constraint `fk_ID_Room_AR`    
    FOREIGN KEY (`ID_Room`)
    REFERENCES `Room` (`Room_Number`)
    on delete  restrict
    on update cascade,

	constraint `fk_ID_Amenity_AR`
    FOREIGN KEY (`ID_Amenity`)
    REFERENCES `Amenity` (`ID_Amenity`)
    on delete  restrict
    on update cascade) 
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `Guest` (
  `ID_Guest` INT NOT NULL auto_increment,
  `Full_Name` VARCHAR(100) NOT NULL,
  `Address` VARCHAR(100) NOT NULL,
  `Birth_Date` DATE NOT NULL,
  `Phone_Number` VARCHAR(15) NOT NULL,
  PRIMARY KEY (`ID_Guest`))
ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS `Booking` (
  `ID_Booking` INT NOT NULL auto_increment,
  `ID_Room` INT NOT NULL,
  `ID_Guest` INT NOT NULL,
  `Date_Booking` DATE NOT NULL,
  `Date_Settlement` DATE NOT NULL,
  `Date_Departure` DATE NOT NULL,
  `Date_Payment` DATE NOT NULL,
  PRIMARY KEY (`ID_Booking`),
  
	constraint `fk_ID_Guest_Book`
    FOREIGN KEY (`ID_Guest`)
    REFERENCES `Guest` (`ID_Guest`)
    on delete  restrict
    on update cascade,

	constraint `fk_ID_Room_Book`    
    FOREIGN KEY (`ID_Room`)
    REFERENCES `Room` (`Room_Number`)
    on delete  restrict
    on update cascade)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `Profesion` (
  `ID_Profesion` INT NOT NULL auto_increment,
  `Profesion` VARCHAR(50) NULL,
  
  `Category` VARCHAR(50) NULL,
  PRIMARY KEY (`ID_Profesion`))
ENGINE = InnoDB;

create table if not exists User(
`ID_User` int not null auto_increment,
`Username` varchar(30) not null unique,
`Password` varchar(30) not null,
`Role` varchar(30) not null,
primary key(`ID_User`))
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `Employee` (
  `ID_Employee` INT NOT NULL auto_increment,
  `Full_Name` VARCHAR(100) NOT NULL,
  `Birth_Date` DATE NOT NULL,
  `Address` VARCHAR(100) NOT NULL,
  `Phone_Number` VARCHAR(15) NOT NULL,
  `Salary` DECIMAL NULL,
  `ID_Profesion` INT NOT NULL,
  ID_User int null,
  PRIMARY KEY (`ID_Employee`),
  
	constraint `fk_ID_Profesion`    
    FOREIGN KEY (`ID_Profesion`)
    REFERENCES `Profesion` (`ID_Profesion`)
    on delete  restrict
    on update cascade,
    
    constraint fk_ID_User_Empl
    foreign key(ID_User)
    References User (ID_User)
    on delete restrict
    on update cascade
    )
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `Service` (
  `ID_Service` INT NOT NULL auto_increment,
  `Service` VARCHAR(100) NOT NULL,
  `Cost` DECIMAL NOT NULL,
  PRIMARY KEY (`ID_Service`))
ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS `Accommodation` (
  `ID_Accommodation` INT NOT NULL auto_increment,
  `ID_Room` INT NOT NULL,
  `ID_Guest` INT NOT NULL,
  `Date_Settlement` DATE NOT NULL,
  `Date_Departure` DATE NOT NULL,
  Total_Cost decimal(10,2)
  default 0.00,
  To_Pay decimal(10,2)
  default 0.00,
  
  PRIMARY KEY (`ID_Accommodation`),
  
	constraint `fk_ID_Guest_Accom`    
    FOREIGN KEY (`ID_Guest`)
    REFERENCES `Guest` (`ID_Guest`)
    on delete  restrict
    on update cascade,
    
	constraint `fk_ID_Room_Accom`    
    FOREIGN KEY (`ID_Room`)
    REFERENCES `Room` (`Room_Number`)
    on delete  restrict
    on update cascade)
ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS `Service_Accommodation` (
  `ID_Service` INT NOT NULL,
  `ID_Accommodation` INT NOT NULL,
  `ID_Employee` INT NOT NULL,
  `Date_Serv_Begin` DATE not NULL,
  `Date_Serv_End` DATE not NULL,
  PRIMARY KEY (`ID_Service`, `ID_Accommodation`, `ID_Employee`),
  
	constraint `fk_ID_Employee`    
    
    FOREIGN KEY (`ID_Employee`)
    REFERENCES `Employee` (`ID_Employee`)
    on delete  restrict
    on update cascade,
      
	constraint `fk_ID_Service`    
	FOREIGN KEY (`ID_Service`)
    REFERENCES `Service` (`ID_Service`)
    on delete  restrict
    on update cascade,
    
    constraint `fk_ID_Accommodation` 
    FOREIGN KEY (`ID_Accommodation`)
    REFERENCES `Accommodation` (`ID_Accommodation`)
    on delete  restrict
    on update cascade)
ENGINE = InnoDB;



 CREATE TABLE IF NOT EXISTS Service_Profesion (
    ID_Service INT NOT NULL,
    ID_Profesion INT NOT NULL,
    PRIMARY KEY (ID_Service, ID_Profesion),
    CONSTRAINT fk_ID_Service_SP
        FOREIGN KEY (ID_Service) REFERENCES Service (ID_Service),
    CONSTRAINT fk_ID_Profesion_SP
        FOREIGN KEY (ID_Profesion) REFERENCES Profesion (ID_Profesion)
)ENGINE = InnoDB;

create table if not exists Payment(
ID_Payment int not null auto_increment,
ID_Accommodation int not null,
Cost decimal not null,
Data_Payment date not null,

primary key(ID_Payment),

constraint fk_ID_Accommodation_Pay
foreign key(ID_Accommodation)
references Accommodation(ID_Accommodation)
on update cascade
on delete restrict
) Engine = InnoDB;

CREATE TABLE if not exists Manager (
    ID_Manager INT AUTO_INCREMENT,
    ID_User int null,
    Full_Name VARCHAR(100) NOT NULL,
    Birth_Date DATE NOT NULL,
    Phone_Number VARCHAR(20) NOT NULL,
    Position Varchar(30) not null,
    primary key(ID_Manager),
	
    Constraint fk_ID_User_Manager
    foreign key(ID_User)
    references User(ID_User)
)Engine = InnoDB;
 