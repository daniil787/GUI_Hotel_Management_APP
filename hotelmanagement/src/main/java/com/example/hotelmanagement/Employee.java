package com.example.hotelmanagement;

public  class Employee {
    private final int id;
    private final String name;
    private final String position;
    private final String address;
    private final String phone;
    private final double salary;

    public Employee(int id, String name, String position, String address, String phone, double salary) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.address = address;
        this.phone = phone;
        this.salary = salary;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPosition() {
        return position;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public double getSalary() {
        return salary;
    }
}