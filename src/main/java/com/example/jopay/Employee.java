package com.example.jopay;

import java.time.LocalDate;

public class Employee {


    public Object setDOB;
    private int id;
    private String employeeId;
    private String password;
    private String firstName;
    private String lastName;
    private String middleName;
    private String Department;
    private String Status;
    private LocalDate DOB;
    private Double basicSalary;
    private String position;
    private String Title;
    private LocalDate value;
    private String text;



    public int getId() { return id; }
    public String getEmployeeId() { return employeeId; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Double getbasicSalary() {return basicSalary; }
    public String getMiddleName() {return middleName;}
    public String getDepartment() {return Department;}
    public LocalDate getDOB(){ return DOB;}
    public String getStatus(){return Status; }
    public String getPosition() { return position;}
    public String getTitle(){return Title;}
    public double getBasicSalary() { return basicSalary; }



    public void setId(int id) { this.id = id; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public void setPassword(String password) { this.password = password; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setMiddleName(String middleName){this.middleName = middleName;}
    public void setDob(LocalDate DOB) { this.DOB= DOB;}
    public void setDepartment(String Department) { this.Department = Department;}
    public void setPosition(String position) { this.position= position;}
    public void setTitle(String Title) { this.Title= Title;}
    public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary;}
    public void setEmploymentStatus(String Status) { this.Status= Status;}


    public String getFullName() {
        return firstName + " " + lastName;
    }




}
