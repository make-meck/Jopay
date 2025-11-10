package com.example.jopay;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;
import java.sql.SQLException;

public class admin2Controller {

    @FXML
    TextField admin_id;
    @FXML
    PasswordField pass_word;
    @FXML
    Label error;
    @FXML
    Button loginButton;
    @FXML
    Button adminBackButton;
    @FXML
    Button manageEmployeeButton;
    @FXML
    Button managePayrollButton;
    @FXML
    Button reportAnalysisButton;
    @FXML
    AnchorPane employeeTablePane;
    @FXML
    Button addEmployeeButton;
    @FXML
    Button addEmpBackButton;
    @FXML
    Button saveEmployeeButton;
    @FXML
    Button clearButton;
    @FXML
    Label addEmpPaneErrorLabel;
    @FXML
    Button removeEmployeeButton;
    @FXML
    Button removeEmployeeButtonRED;
    @FXML
    AnchorPane pane1;
    @FXML
    AnchorPane addEmpPane;
    @FXML
    AnchorPane removeEmpPane;
    @FXML
    AnchorPane confirmationPane;
    @FXML
    Button yesRemoveButton;
    @FXML
    Button noBackButton;
    @FXML
    HBox searchHBox;

    @FXML
    AnchorPane managePayrollPane;

    @FXML
    AnchorPane reportAnalysisPane;
    @FXML
    Label payrollLabel;
    @FXML
    Label manageEmpLabel;
    @FXML
    Label reportAnalysisLabel;
    @FXML
    Label employeeIDErrorLabel;
    @FXML
    TextField employeeIDToRemoveTextfield;

    //Add Employee Pane Fields
    @FXML
    TextField firstName;
    @FXML
    TextField lastName;
    @FXML
    TextField middleName;
    @FXML
    TextField employeeID;
    @FXML
    TextField tempPassword;
    @FXML
    DatePicker dateOfBirth;
    @FXML
    TextField department;
    @FXML
    TextField employmentStatus;
    @FXML
    DatePicker dateHired;
    @FXML
    TextField jobTitle;
    @FXML
    TextField basicSalary;

    //remove employee pane
    @FXML
    Label removeEmployeeLabel;
    @FXML
    Label removeIdLabel;
    @FXML
    Label removeDeptLabel;
    @FXML
    Label removeEmployLabel;
    @FXML
    Label removeJobLabel;

    //manage payroll
    @FXML
    TextField payrollemployeeName;
    @FXML
    TextField payrollEmployeeID;
    @FXML
    TextField telecoAllowance;
    @FXML
    TextField travelAllowance;
    @FXML
    TextField riceSubsidy;
    @FXML
    TextField nonTaxableTF;
    @FXML
    TextField perDeimTF;
    @FXML
    TextField perDeimCountTF;
    @FXML
    DatePicker startingDatePicker;
    @FXML
    DatePicker endDatePicker;
    @FXML
    TextField searchIDManager;
    @FXML
    Label errorLabelManagePayroll;





    // Temporary admin login inputs
    private int adminID = 11111;
    private String password = "0000";


    @FXML
    private void loginClick() throws IOException {

        try {
            String adminIDAsString = admin_id.getText();
            int adminIDAsInt = Integer.parseInt(adminIDAsString);
            String adminPassword = pass_word.getText();

            if (adminPassword.equals("") || adminIDAsString.equals("")) {
                error.setText("Please fill in all fields.");
            } else if (adminIDAsInt != adminID || !adminPassword.equals(password)) {
                error.setText("Account not found. Please try again.");
            } else {
                FXMLLoader fxmlLoader2 = new FXMLLoader(getClass().getResource("admin2Dashboard.fxml"));
                Stage stage = (Stage) loginButton.getScene().getWindow();
                Scene scene2 = new Scene(fxmlLoader2.load());
                stage.setScene(scene2);
                stage.show();
            }
        } catch (NumberFormatException e) {
            error.setText("Please fill in all fields.");
        }

    }

    @FXML
    void backButtonClick() throws IOException {
        FXMLLoader employeeDashboardLoader = new FXMLLoader(getClass().getResource("employeelogin.fxml"));
        Stage stage = (Stage) adminBackButton.getScene().getWindow();
        Scene employeeLoginScene = new Scene(employeeDashboardLoader.load());
        stage.setScene(employeeLoginScene);
        stage.show();
    }

    @FXML
    private void manageEmployeeClick() {
        pane1.setVisible(true);
        employeeTablePane.setVisible(true);
        searchHBox.setVisible(true);
        addEmpPane.setVisible(false);
        removeEmpPane.setVisible(false);
        managePayrollPane.setVisible(false);
        reportAnalysisPane.setVisible(false);
        manageEmpLabel.setVisible(true);
        payrollLabel.setVisible(false);
        reportAnalysisLabel.setVisible(false);

    }

    @FXML
    private void onSearchEmployee(){
        PayrollDAO payrollDAO = new PayrollDAO();

        String searchID= searchIDManager.getText().trim();
        if(searchID.isEmpty()){
            errorLabelManagePayroll.setText("Please enter an Employee ID");
            return;
        }

        Payroll info = payrollDAO.getEmployeePayroll(searchID);
        if(info != null) {
            payrollemployeeName.setText(info.getEmployeeName());
            payrollEmployeeID.setText(String.valueOf(info.getEmployeeId()));
            telecoAllowance.setText(info.getTelecom());
            travelAllowance.setText(String.valueOf(info.getTravel()));
            riceSubsidy.setText(String.valueOf(info.getRice()));
            nonTaxableTF.setText(String.valueOf(info.getNonTaxable()));
            perDeimTF.setText(String.valueOf(info.getPerDiem()));
            perDeimCountTF.setText(String.valueOf(info.getPerDiemCount()));
            startingDatePicker.setValue(info.getStartDate());
            endDatePicker.setValue(info.getEndDate());

        } else {
            clearFields;
            errorLabelManagePayroll.setText("Employee not found");
        }
    }

    @FXML
    private void managePayrollClick() {
        pane1.setVisible(false);
        managePayrollPane.setVisible(true);
        reportAnalysisPane.setVisible(false);
        payrollLabel.setVisible(true);
        manageEmpLabel.setVisible(false);
        reportAnalysisLabel.setVisible(false);


    }



    @FXML
    private void reportAnalysisClick() {
        pane1.setVisible(false);
        reportAnalysisPane.setVisible(true);
        managePayrollPane.setVisible(false);
        reportAnalysisLabel.setVisible(true);
        payrollLabel.setVisible(false);
        manageEmpLabel.setVisible(false);
    }

    @FXML
    private void addEmployeeClick() {
        addEmpPane.setVisible(true);
        searchHBox.setVisible(true);
        addEmpPaneErrorLabel.setText("");
        employeeTablePane.setVisible(false);
        removeEmpPane.setVisible(false);

        try {
            int nextId = EmployeeDAO.getNextEmployeeId();
            employeeID.setText(String.valueOf(nextId));
        } catch (SQLException e) {
            addEmpPaneErrorLabel.setText("Error in generating Employee ID: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void addEmpBackButtonClick() {
        pane1.setVisible(true);
        employeeTablePane.setVisible(true);
        addEmpPane.setVisible(false);
    }


    @FXML
    private void saveEmployeeClick() {
        if (firstName.getText().trim().isEmpty() || lastName.getText().trim().isEmpty() || employeeID.getText().trim().isEmpty() ||
                tempPassword.getText().trim().isEmpty() || dateOfBirth.getValue() == null || department.getText().trim().isEmpty() ||
                employmentStatus.getText().trim().isEmpty() || dateHired.getValue() == null || jobTitle.getText().trim().isEmpty() ||
                basicSalary.getText().trim().isEmpty()) {
            addEmpPaneErrorLabel.setText("Please fill in all required fields.");
        } else {
            addEmpPaneErrorLabel.setText("");
        }
        try {
            Employee emp = new Employee();

            int nextID = EmployeeDAO.getNextEmployeeId();
            emp.setEmployeeId(String.valueOf(nextID));
            employeeID.setText(String.valueOf(nextID));

            emp.setFirstName(firstName.getText());
            emp.setLastName(lastName.getText());
            emp.setMiddleName(middleName.getText());
            emp.setDob(dateOfBirth.getValue()); // LocalDate
            emp.setDepartment(department.getText());
            emp.setTitle(jobTitle.getText()); // if Title is same as jobTitle
            emp.setBasicSalary(Double.parseDouble(basicSalary.getText()));
            emp.setEmploymentStatus(employmentStatus.getText());
            emp.setDateHired(String.valueOf(dateHired.getValue()));

            String tempPass = tempPassword.getText();

            EmployeeDAO.addEmployee(emp, tempPass);


            addEmpPaneErrorLabel.setText("Employee added successfully!");
            clearClick();

        } catch (NumberFormatException ex) {
            addEmpPaneErrorLabel.setText("Please enter a valid number for Basic Salary.");
        } catch (SQLException ex) {
            addEmpPaneErrorLabel.setText("Database error: " + ex.getMessage());
            ex.printStackTrace();
        }

    }

    @FXML
    private void clearClick() {
        firstName.setText("");
        lastName.setText("");
        middleName.setText("");
        employeeID.setText("");
        tempPassword.setText("");
        dateOfBirth.setValue(null);
        department.setText("");
        employmentStatus.setText("");
        dateHired.setValue(null);
        jobTitle.setText("");
        basicSalary.setText("");
    }

    @FXML
    private void removeEmployeeClick() {
        removeEmpPane.setVisible(true);
        employeeTablePane.setVisible(false);
        employeeIDErrorLabel.setText("");
        addEmpPane.setVisible(false);
        searchHBox.setVisible(false);
    }

    @FXML
    private void removeEmployeeREDClick() {
        if (employeeIDToRemoveTextfield.getText().isEmpty()) {
            employeeIDErrorLabel.setText("Please enter an Employee ID.");
        } else if (!employeeIDToRemoveTextfield.getText().matches("^[0-9]*$")) {
            employeeIDErrorLabel.setText("Please enter a valid Employee ID.");
        } else {
            confirmationPane.setVisible(true);
        }

    }

    @FXML
    private void yesRemoveClick() {
        try {
            int employeeId = Integer.parseInt(employeeIDToRemoveTextfield.getText());
            EmployeeDAO.deactivateEmployee(employeeId);

            addEmpPaneErrorLabel.setText("Employee has been deactivated.");
            confirmationPane.setVisible(false);
            clearRemoveLabels();
        } catch (SQLException e) {
            addEmpPaneErrorLabel.setText("Error deactivating employee: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            addEmpPaneErrorLabel.setText("Invalid Employee ID");
        }

    }

    @FXML
    private void noBackClick() {
        confirmationPane.setVisible(false);
    }

    @FXML
    private void searchEmployeebyID() {
        String findEmp = employeeIDToRemoveTextfield.getText().trim();


        if (findEmp.isEmpty()) {
            addEmpPaneErrorLabel.setText("Please Enter an Employee Id");
            return;
        }
        try {
            int empId = Integer.parseInt(findEmp);


            Employee emp = EmployeeDAO.getEmployeeById(empId);

            if (emp != null) {
                removeEmployeeLabel.setText("Employee Name: " + emp.getFullName());
                removeIdLabel.setText("Employee ID: " + emp.getEmployeeId());
                removeDeptLabel.setText("Department: " + emp.getDepartment());
                removeEmployLabel.setText("Employement Status: " + emp.getStatus());
                removeJobLabel.setText("Job Title: " + emp.getTitle());
                addEmpPaneErrorLabel.setText("");
                System.out.println("Labels updated!");
            } else {
                System.out.println("Employee NOT found!");
                addEmpPaneErrorLabel.setText("Employee not found or inactive");
                clearRemoveLabels();
            }

        }
        catch(NumberFormatException emp){
            addEmpPaneErrorLabel.setText("Invalid employee ID format");
            clearRemoveLabels();
        }
        catch(SQLException emp){
            addEmpPaneErrorLabel.setText("Database error: " + emp.getMessage());
        }

    }

    private void clearRemoveLabels() {
        employeeIDToRemoveTextfield.clear();
        removeEmployeeLabel.setText("Employee Name:");
        removeIdLabel.setText("Employee ID:");
        removeDeptLabel.setText("Department:");
        removeEmployLabel.setText("Employment Status:");
        removeJobLabel.setText("Job Title:");
    }
    private void clearFields(){
        payrollemployeeName.clear();
        payrollEmployeeID.clear();
        travelAllowance.clear();
        riceSubsidy.clear();
        nonTaxableTF.clear();
        perDeimTF.clear();
        perDeimCountTF.clear();
        startingDatePicker.setValue(null);
        endDatePicker.setValue(null);
    }

}