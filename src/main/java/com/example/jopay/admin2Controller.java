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

import java.io.IOException;

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
            }
            else if (adminIDAsInt != adminID || !adminPassword.equals(password)){
                error.setText("Account not found. Please try again.");
            }

            else {
                FXMLLoader fxmlLoader2 = new FXMLLoader(getClass().getResource("admin2Dashboard.fxml"));
                Stage stage = (Stage) loginButton.getScene().getWindow();
                Scene scene2 = new Scene(fxmlLoader2.load());
                stage.setScene(scene2);
                stage.show();
            }
        }
        catch (NumberFormatException e){
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
        }
        else {
            addEmpPaneErrorLabel.setText("");
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
        if (employeeIDToRemoveTextfield.getText().isEmpty()){
            employeeIDErrorLabel.setText("Please enter an Employee ID.");
        }
        else if (!employeeIDToRemoveTextfield.getText().matches("^[0-9]*$")) {
            employeeIDErrorLabel.setText("Please enter a valid Employee ID.");
        }
        else {
            confirmationPane.setVisible(true);
        }

    }

    @FXML
    private void yesRemoveClick() {

    }

    @FXML
    private void noBackClick() {
        confirmationPane.setVisible(false);
    }


}