package com.example.hotelmanagement;

public class Room {
    private  int roomNumber;
    private  int beds;
    private  String type;
    private  double cost;
    private String amenities;

    public Room(int roomNumber, int beds, String type, double cost, String amenities) {
        this.roomNumber = roomNumber;
        this.beds = beds;
        this.type = type;
        this.cost = cost;
        this.amenities = amenities;
    }

    public String getAmenities() {
        return amenities;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public int getBeds() {
        return beds;
    }

    public String getType() {
        return type;
    }

    public double getCost() {
        return cost;
    }

}
