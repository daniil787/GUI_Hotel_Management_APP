package com.example.hotelmanagement;


public class Service {
    private int id;
    private String name;
    private double price;

    public Service(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
}

