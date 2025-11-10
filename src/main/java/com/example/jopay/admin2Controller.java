package com.example.jopay;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class admin2Controller {

    @FXML TextField admin_id;
    @FXML PasswordField pass_word;
    @FXML Label error;
    @FXML Button loginButton;
    @FXML Button adminBackButton;
    @FXML Button manageEmployeeButton;
    @FXML Button managePayrollButton;
    @FXML Button reportAnalysisButton;
    @FXML AnchorPane employeeTablePane;
    @FXML Button addEmployeeButton;
    @FXML Button addEmpBackButton;
    @FXML Button saveEmployeeButton;
    @FXML Button clearButton;
    @FXML Label addEmpPaneErrorLabel;
    @FXML Button removeEmployeeButton;
    @FXML Button removeEmployeeButtonRED;
    @FXML AnchorPane pane1;
    @FXML AnchorPane addEmpPane;
    @FXML AnchorPane removeEmpPane;
    @FXML AnchorPane confirmationPane;
    @FXML Button yesRemoveButton;
    @FXML Button noBackButton;
    @FXML HBox searchHBox;
    @FXML AnchorPane managePayrollPane;
    @FXML AnchorPane reportAnalysisPane;
    @FXML Label payrollLabel;
    @FXML Label manageEmpLabel;
    @FXML Label reportAnalysisLabel;
    @FXML Label employeeIDErrorLabel;
    @FXML TextField employeeIDToRemoveTextfield;

    // Add Employee Fields
    @FXML TextField firstName;
    @FXML TextField lastName;
    @FXML TextField middleName;
    @FXML TextField employeeID;
    @FXML TextField tempPassword;
    @FXML DatePicker dateOfBirth;
    @FXML TextField department;
    @FXML TextField employmentStatus;
    @FXML DatePicker dateHired;
    @FXML TextField jobTitle;
    @FXML TextField basicSalary;

    // Remove Employee Pane
    @FXML Label removeEmployeeLabel;
    @FXML Label removeIdLabel;
    @FXML Label removeDeptLabel;
    @FXML Label removeEmployLabel;
    @FXML Label removeJobLabel;

    // Manage Payroll Fields
    @FXML TextField payrollemployeeName;
    @FXML TextField payrollEmployeeID;
    @FXML TextField telecoAllowance;
    @FXML TextField travelAllowance;
    @FXML TextField riceSubsidy;
    @FXML TextField nonTaxableTF;
    @FXML TextField perDeimTF;
    @FXML TextField perDeimCountTF;
    @FXML DatePicker startingDatePicker;
    @FXML DatePicker endDatePicker;
    @FXML TextField searchIDManager;
    @FXML Label errorLabelManagePayroll;
    @FXML Button savePayrollButton;
    @FXML Button computePayrollButton;

    // Employee Table
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> colId;
    @FXML private TableColumn<Employee, String> colName;
    @FXML private TableColumn<Employee, String> colDept;
    @FXML private TableColumn<Employee, String> colStatus;
    @FXML private TextField searchEmployeeID;

    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();

    // Admin credentials
    private int adminID = 11111;
    private String password = "0000";

    @FXML
    public void initialize() {
        if (employeeTable != null) {
            setupEmployeeTable();
        }
    }

    @FXML
    public void setupEmployeeTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colDept.setCellValueFactory(new PropertyValueFactory<>("department"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        EmployeeDAO.updateEmploymentStatus();
        loadEmployeeTable();

        searchEmployeeID.textProperty().addListener((obs, oldValue, newValue) -> {
            searchEmployee(newValue);
        });
    }

    private void loadEmployeeTable() {
        employeeList.clear();
        employeeList.addAll(EmployeeDAO.getAllEmployees());
        employeeTable.setItems(employeeList);
    }

    private void searchEmployee(String keyword) {
        employeeList.clear();
        employeeList.addAll(EmployeeDAO.searchEmployees(keyword));
        employeeTable.setItems(employeeList);
    }

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
        loadEmployeeTable();
    }

    @FXML
    private void onSearchEmployee() {
        PayrollDAO payrollDAO = new PayrollDAO();
        String searchID = searchIDManager.getText().trim();

        if (searchID.isEmpty()) {
            errorLabelManagePayroll.setText("Please enter an Employee ID");
            return;
        }

        // Get employee info
        PayrollDAO.EmployeeInfo empInfo = payrollDAO.getEmployeeInfo(searchID);
        if (empInfo == null) {
            clearPayrollFields();
            errorLabelManagePayroll.setText("Employee not found");
            return;
        }

        // Get salary config
        PayrollDAO.SalaryConfig config = payrollDAO.getSalaryConfig(searchID);

        payrollemployeeName.setText(empInfo.employeeName);
        payrollEmployeeID.setText(empInfo.employeeId);

        if (config != null) {
            telecoAllowance.setText(String.valueOf(config.telecomAllowance));
            travelAllowance.setText(String.valueOf(config.travelAllowance));
            riceSubsidy.setText(String.valueOf(config.riceSubsidy));
            nonTaxableTF.setText(String.valueOf(config.nonTaxableSalary));
            perDeimTF.setText(String.valueOf(config.perDiem));
            perDeimCountTF.setText(String.valueOf(config.perDiemCount));
        } else {
            telecoAllowance.setText("0.00");
            travelAllowance.setText("0.00");
            riceSubsidy.setText("0.00");
            nonTaxableTF.setText("0.00");
            perDeimTF.setText("0.00");
            perDeimCountTF.setText("0");
        }

        errorLabelManagePayroll.setText("");
        errorLabelManagePayroll.setStyle("-fx-text-fill: green;");
        errorLabelManagePayroll.setText("Employee data loaded successfully");
    }

    @FXML
    private void savePayrollConfig() {
        String empId = payrollEmployeeID.getText().trim();

        if (empId.isEmpty()) {
            errorLabelManagePayroll.setText("Please search for an employee first");
            return;
        }

        try {
            double telecom = Double.parseDouble(telecoAllowance.getText().trim());
            double travel = Double.parseDouble(travelAllowance.getText().trim());
            double rice = Double.parseDouble(riceSubsidy.getText().trim());
            double nonTaxable = Double.parseDouble(nonTaxableTF.getText().trim());
            double perDiem = Double.parseDouble(perDeimTF.getText().trim());
            int perDiemCount = Integer.parseInt(perDeimCountTF.getText().trim());
            LocalDate startDate = startingDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (startDate == null || endDate == null) {
                errorLabelManagePayroll.setText("Please select start and end dates");
                return;
            }

            // Save to salary_config table
            String query = """
                INSERT INTO salary_config 
                (employee_Id, telecom_Allowance, travel_Allowance, rice_Subsidy, 
                 non_Taxable_Salary, per_Diem, per_Diem_Count, starting_Date, end_Date)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                telecom_Allowance = VALUES(telecom_Allowance),
                travel_Allowance = VALUES(travel_Allowance),
                rice_Subsidy = VALUES(rice_Subsidy),
                non_Taxable_Salary = VALUES(non_Taxable_Salary),
                per_Diem = VALUES(per_Diem),
                per_Diem_Count = VALUES(per_Diem_Count),
                starting_Date = VALUES(starting_Date),
                end_Date = VALUES(end_Date)
            """;

            DatabaseConnector db = new DatabaseConnector();
            var stmt = db.prepareStatement(query);
            stmt.setString(1, empId);
            stmt.setDouble(2, telecom);
            stmt.setDouble(3, travel);
            stmt.setDouble(4, rice);
            stmt.setDouble(5, nonTaxable);
            stmt.setDouble(6, perDiem);
            stmt.setInt(7, perDiemCount);
            stmt.setDate(8, java.sql.Date.valueOf(startDate));
            stmt.setDate(9, java.sql.Date.valueOf(endDate));

            int result = stmt.executeUpdate();

            if (result > 0) {
                errorLabelManagePayroll.setStyle("-fx-text-fill: green;");
                errorLabelManagePayroll.setText("Payroll configuration saved successfully!");
            } else {
                errorLabelManagePayroll.setText("Failed to save configuration");
            }

        } catch (NumberFormatException e) {
            errorLabelManagePayroll.setText("Please enter valid numeric values");
        } catch (SQLException e) {
            errorLabelManagePayroll.setText("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void computePayroll() {
        String empId = payrollEmployeeID.getText().trim();

        if (empId.isEmpty()) {
            errorLabelManagePayroll.setText("Please search for an employee first");
            return;
        }

        LocalDate startDate = startingDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            errorLabelManagePayroll.setText("Please select payroll period dates");
            return;
        }

        try {
            PayrollDAO dao = new PayrollDAO();

            // Get all required data
            PayrollDAO.EmployeeInfo empInfo = dao.getEmployeeInfo(empId);
            PayrollDAO.SalaryConfig config = dao.getSalaryConfig(empId);
            PayrollDAO.AttendanceData attendance = dao.getAttendanceData(empId, startDate, endDate);
            PayrollDAO.DeductionData deductions = dao.getDeductions(empId);

            if (empInfo == null) {
                errorLabelManagePayroll.setText("Employee not found");
                return;
            }

            // Initialize PayrollModel
            PayrollModel model = new PayrollModel();
            model.PayrollComputation(
                    empInfo.employeeId,
                    empInfo.employeeName,
                    empInfo.basicMonthlyPay,
                    empInfo.employmentStatus,
                    empInfo.dateHired,
                    empInfo.workingHoursPerDay
            );

            // Set payroll period
            boolean isFirstHalf = startDate.getDayOfMonth() <= 15;
            model.setPayrollPeriod(startDate, endDate, isFirstHalf);

            // Set attendance data
            model.setAttendanceData(
                    attendance.daysWorked,
                    attendance.daysAbsent,
                    attendance.regularOTHours,
                    attendance.nightDifferentialOTHours,
                    attendance.specialHolidaysWorked,
                    attendance.regularHolidaysWorked,
                    attendance.restDaysWorked,
                    attendance.restDayOTHours,
                    attendance.restDayNightDiffOTHours,
                    attendance.undertimeHours
            );

            // Set allowances
            if (config != null) {
                model.setAllowances(
                        config.telecomAllowance,
                        config.travelAllowance,
                        config.riceSubsidy,
                        config.nonTaxableSalary,
                        config.perDiem,
                        config.perDiemCount
                );
            } else {
                model.setAllowances(0, 0, 0, 0, 0, 0);
            }

            // Set deductions
            if (deductions != null) {
                model.setDeductions(deductions.sssLoan);
            } else {
                model.setDeductions(0);
            }

            // Compute payroll
            model.computePayroll();

            // Optional: Save to database (you need to provide periodId)
            // For now, we'll just display the results
            // int periodId = 1; // Get this from a payroll period selector
            // dao.savePayroll(empId, periodId, model, config, attendance);

            // Display results
            String summary = model.generatePayslipSummary();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Payroll Computation Result");
            alert.setHeaderText("Payroll computed successfully");
            alert.setContentText(summary);
            alert.showAndWait();

            errorLabelManagePayroll.setStyle("-fx-text-fill: green;");
            errorLabelManagePayroll.setText("Payroll computed! Net Pay: ₱" +
                    String.format("%,.2f", model.getNetPay()));

        } catch (Exception e) {
            errorLabelManagePayroll.setText("Error computing payroll: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void saveComputedPayroll() {
        String empId = payrollEmployeeID.getText().trim();

        if (empId.isEmpty()) {
            errorLabelManagePayroll.setText("Please compute payroll first");
            return;
        }

        LocalDate startDate = startingDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            errorLabelManagePayroll.setText("Please select payroll period dates");
            return;
        }

        try {
            PayrollDAO dao = new PayrollDAO();

            // Get all required data (recompute)
            PayrollDAO.EmployeeInfo empInfo = dao.getEmployeeInfo(empId);
            PayrollDAO.SalaryConfig config = dao.getSalaryConfig(empId);
            PayrollDAO.AttendanceData attendance = dao.getAttendanceData(empId, startDate, endDate);
            PayrollDAO.DeductionData deductions = dao.getDeductions(empId);

            if (empInfo == null) {
                errorLabelManagePayroll.setText("Employee not found");
                return;
            }

            // Initialize and compute PayrollModel
            PayrollModel model = new PayrollModel();
            model.PayrollComputation(
                    empInfo.employeeId,
                    empInfo.employeeName,
                    empInfo.basicMonthlyPay,
                    empInfo.employmentStatus,
                    empInfo.dateHired,
                    empInfo.workingHoursPerDay
            );

            boolean isFirstHalf = startDate.getDayOfMonth() <= 15;
            model.setPayrollPeriod(startDate, endDate, isFirstHalf);
            model.setAttendanceData(
                    attendance.daysWorked, attendance.daysAbsent,
                    attendance.regularOTHours, attendance.nightDifferentialOTHours,
                    attendance.specialHolidaysWorked, attendance.regularHolidaysWorked,
                    attendance.restDaysWorked, attendance.restDayOTHours,
                    attendance.restDayNightDiffOTHours, attendance.undertimeHours
            );

            if (config != null) {
                model.setAllowances(
                        config.telecomAllowance, config.travelAllowance,
                        config.riceSubsidy, config.nonTaxableSalary,
                        config.perDiem, config.perDiemCount
                );
            } else {
                model.setAllowances(0, 0, 0, 0, 0, 0);
            }

            if (deductions != null) {
                model.setDeductions(deductions.sssLoan);
            } else {
                model.setDeductions(0);
            }

            model.computePayroll();

            // Save to database (using period ID 1 as default, you may want to add a selector)
            int periodId = 1; // You should get this from a ComboBox or determine it automatically
            boolean saved = dao.savePayroll(empId, periodId, model, config, attendance);

            if (saved) {
                errorLabelManagePayroll.setStyle("-fx-text-fill: green;");
                errorLabelManagePayroll.setText("Payroll saved successfully to database!");
            } else {
                errorLabelManagePayroll.setText("Failed to save payroll to database");
            }

        } catch (Exception e) {
            errorLabelManagePayroll.setText("Error saving payroll: " + e.getMessage());
            e.printStackTrace();
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
            addEmpPaneErrorLabel.setText("Error generating Employee ID: " + e.getMessage());
        }
    }

    @FXML
    private void addEmpBackButtonClick() {
        pane1.setVisible(true);
        employeeTablePane.setVisible(true);
        addEmpPane.setVisible(false);
        loadEmployeeTable();
    }

    @FXML
    private void saveEmployeeClick() {
        if (firstName.getText().trim().isEmpty() || lastName.getText().trim().isEmpty() ||
                employeeID.getText().trim().isEmpty() || tempPassword.getText().trim().isEmpty() ||
                dateOfBirth.getValue() == null || department.getText().trim().isEmpty() ||
                dateHired.getValue() == null || jobTitle.getText().trim().isEmpty() ||
                basicSalary.getText().trim().isEmpty()) {

            addEmpPaneErrorLabel.setText("Please fill in all required fields.");
            return;
        }

        try {
            Employee emp = new Employee();
            int nextID = EmployeeDAO.getNextEmployeeId();
            emp.setEmployeeId(String.valueOf(nextID));

            emp.setFirstName(firstName.getText().trim());
            emp.setLastName(lastName.getText().trim());
            emp.setMiddleName(middleName.getText().trim());
            emp.setDob(dateOfBirth.getValue());
            emp.setDepartment(department.getText().trim());
            emp.setTitle(jobTitle.getText().trim());
            emp.setBasicSalary(Double.parseDouble(basicSalary.getText().trim()));
            emp.setDateHired(String.valueOf(dateHired.getValue()));
            emp.setEmploymentStatus("Probationary");

            String tempPass = tempPassword.getText().trim();
            EmployeeDAO.addEmployee(emp, tempPass);

            addEmpPaneErrorLabel.setStyle("-fx-text-fill: green;");
            addEmpPaneErrorLabel.setText("Employee added successfully!");
            clearClick();
            loadEmployeeTable();

        } catch (NumberFormatException ex) {
            addEmpPaneErrorLabel.setText("Please enter a valid number for Basic Salary.");
        } catch (SQLException ex) {
            addEmpPaneErrorLabel.setText("Database error: " + ex.getMessage());
        }
    }

    @FXML
    private void clearClick() {
        firstName.setText("");
        lastName.setText("");
        middleName.setText("");
        tempPassword.setText("");
        dateOfBirth.setValue(null);
        department.setText("");
        dateHired.setValue(null);
        jobTitle.setText("");
        basicSalary.setText("");

        try {
            int nextId = EmployeeDAO.getNextEmployeeId();
            employeeID.setText(String.valueOf(nextId));
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

            addEmpPaneErrorLabel.setStyle("-fx-text-fill: green;");
            addEmpPaneErrorLabel.setText("Employee deactivated successfully.");
            confirmationPane.setVisible(false);
            clearRemoveLabels();
            loadEmployeeTable();

        } catch (SQLException e) {
            addEmpPaneErrorLabel.setText("Error deactivating employee: " + e.getMessage());
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
            addEmpPaneErrorLabel.setText("Please enter an Employee ID");
            return;
        }

        try {
            int empId = Integer.parseInt(findEmp);
            Employee emp = EmployeeDAO.getEmployeeById(empId);

            if (emp != null) {
                removeEmployeeLabel.setText("Employee Name: " + emp.getFullName());
                removeIdLabel.setText("Employee ID: " + emp.getEmployeeId());
                removeDeptLabel.setText("Department: " + emp.getDepartment());
                removeEmployLabel.setText("Employment Status: " + emp.getStatus());
                removeJobLabel.setText("Job Title: " + emp.getTitle());
                addEmpPaneErrorLabel.setText("");
            } else {
                addEmpPaneErrorLabel.setText("Employee not found or inactive");
                clearRemoveLabels();
            }
        } catch (NumberFormatException e) {
            addEmpPaneErrorLabel.setText("Invalid employee ID format");
            clearRemoveLabels();
        } catch (SQLException e) {
            addEmpPaneErrorLabel.setText("Database error: " + e.getMessage());
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

    private void clearPayrollFields() {
        payrollemployeeName.clear();
        payrollEmployeeID.clear();
        telecoAllowance.clear();
        travelAllowance.clear();
        riceSubsidy.clear();
        nonTaxableTF.clear();
        perDeimTF.clear();
        perDeimCountTF.clear();
        startingDatePicker.setValue(null);
        endDatePicker.setValue(null);
    }
}