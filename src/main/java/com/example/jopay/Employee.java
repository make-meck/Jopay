package com.example.jopay;

public class Employee {

    private int id;
    private String employeeId;
    private String password;
    private String firstName;
    private String lastName;


    public int getId() { return id; }
    public String getEmployeeId() { return employeeId; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }


    public void setId(int id) { this.id = id; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public void setPassword(String password) { this.password = password; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }


    public String getFullName() {
        return firstName + " " + lastName;
    }
}
