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
    private String Title;
   private LocalDate dateHired;
   public boolean active;



    public int getId() { return id; }
    public String getEmployeeId() { return employeeId; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getMiddleName() {return middleName;}
    public String getDepartment() {return Department;}
    public LocalDate getDOB(){ return DOB;}
    public String getStatus(){return Status; }
    public String getTitle(){return Title;}
    public double getBasicSalary() { return basicSalary; }
    public LocalDate getDateHired(){return dateHired; }
    public boolean getIsactive() {return active; }




    public void setId(int id) { this.id = id; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public void setPassword(String password) { this.password = password; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setMiddleName(String middleName){this.middleName = middleName;}
    public void setDob(LocalDate DOB) { this.DOB= DOB;}
    public void setDepartment(String Department) { this.Department = Department;}
    public void setTitle(String Title) { this.Title= Title;}
    public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary;}
    public void setEmploymentStatus(String Status) { this.Status= Status;}
    public void setDateHired(String dateHired){ this.dateHired= LocalDate.parse(dateHired);}




    public String getFullName() {
        return firstName + " " + lastName;
    }


    public boolean isActive() {
        return active;
    }
}
