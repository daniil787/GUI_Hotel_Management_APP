package com.example.hotelmanagement;

public class Guest {
    private final int id;
    private final String fullName;
    private final String address;
    private final String birthDate;
    private final String phoneNumber;

    public Guest(int id, String fullName, String address, String birthDate, String phoneNumber) {
        this.id = id;
        this.fullName = fullName;
        this.address = address;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAddress() {
        return address;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
