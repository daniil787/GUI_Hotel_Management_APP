package com.example.hotelmanagement;

public class PaymentDetails {
    private int idAccommodation;
    private int roomNumber;
    private String guestNames;
    private double toPay;

    public PaymentDetails(int idAccommodation, int roomNumber, String guestNames, double toPay) {
        this.idAccommodation = idAccommodation;
        this.roomNumber = roomNumber;
        this.guestNames = guestNames;
        this.toPay = toPay;
    }
    public PaymentDetails() {
    }

    public int getIdAccommodation() {
        return idAccommodation;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public String getGuestNames() {
        return guestNames;
    }

    public double getToPay() {
        return toPay;
    }
}

