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
    @FXML TextField sssLoanTF;
    @FXML TextField basicPayTF;
    @FXML TextField overtimeTF;
    @FXML TextField absencesTF;
    @FXML TextField numAbsencesTF;
    @FXML TextField sssContributionTF;
    @FXML TextField phicContributionTF;
    @FXML TextField hdmfContributionTF;
    @FXML TextField withholdingTaxTF;
    @FXML TextField grossPayTF;
    @FXML TextField totalDeductionTF;
    @FXML TextField netPayTF;
    @FXML Button computePayrollButton;
    @FXML Button savePayrollButton;
    @FXML Button clearPayrollButton;

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

    private void displayComputedPayroll(PayrollModel model, PayrollDAO.SalaryConfig config,
                                        PayrollDAO.AttendanceData attendance, double perDiem, int perDiemCount) {
        // Display all computed values in the text fields
        basicPayTF.setText(String.format("₱%,.2f", model.getSemiMonthlyBasicPay()));
        overtimeTF.setText(String.format("₱%,.2f", attendance.regularOTHours * model.getHourlyRate() * 1.25));
        absencesTF.setText(String.format("₱%,.2f", attendance.daysAbsent * model.getGrossDailyRate()));
        numAbsencesTF.setText(String.valueOf(attendance.daysAbsent));

        // Computed contributions (highlighted in green)
        sssContributionTF.setText(String.format("₱%,.2f", model.getSSSContribution()));
        phicContributionTF.setText(String.format("₱%,.2f", model.getPHICContribution()));
        hdmfContributionTF.setText(String.format("₱%,.2f", model.getHDMFContribution()));

        // Computed tax (highlighted in orange)
        withholdingTaxTF.setText(String.format("₱%,.2f", model.getWithholdingTax()));

        // Summary
        grossPayTF.setText(String.format("₱%,.2f", model.getSemiMonthlyGrossPay()));
        totalDeductionTF.setText(String.format("₱%,.2f", model.getTotalDeductions()));
        netPayTF.setText(String.format("₱%,.2f", model.getNetPay()));

        // Console output for debugging
        System.out.println("=== COMPUTED PAYROLL ===");
        System.out.println("SSS: " + String.format("₱%,.2f", model.getSSSContribution()));
        System.out.println("PHIC: " + String.format("₱%,.2f", model.getPHICContribution()));
        System.out.println("HDMF: " + String.format("₱%,.2f", model.getHDMFContribution()));
        System.out.println("Overtime: " + String.format("₱%,.2f", attendance.regularOTHours * model.getHourlyRate() * 1.25));
        System.out.println("Absences: " + String.format("₱%,.2f", attendance.daysAbsent * model.getGrossDailyRate()));
        System.out.println("Tax: " + String.format("₱%,.2f", model.getWithholdingTax()));
        System.out.println("Gross Pay: " + String.format("₱%,.2f", model.getSemiMonthlyGrossPay()));
        System.out.println("Total Deductions: " + String.format("₱%,.2f", model.getTotalDeductions()));
        System.out.println("Net Pay: " + String.format("₱%,.2f", model.getNetPay()));
    }

    // New method to clear payroll fields
    @FXML
    private void clearPayrollFields() {
        payrollemployeeName.clear();
        payrollEmployeeID.clear();
        telecoAllowance.clear();
        travelAllowance.clear();
        riceSubsidy.clear();
        nonTaxableTF.clear();
        perDeimTF.clear();
        perDeimCountTF.clear();
        sssLoanTF.clear();
        startingDatePicker.setValue(null);
        endDatePicker.setValue(null);

        // Clear computed fields
        basicPayTF.clear();
        overtimeTF.clear();
        absencesTF.clear();
        numAbsencesTF.clear();
        sssContributionTF.clear();
        phicContributionTF.clear();
        hdmfContributionTF.clear();
        withholdingTaxTF.clear();
        grossPayTF.clear();
        totalDeductionTF.clear();
        netPayTF.clear();

        errorLabelManagePayroll.setText("");
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
        basicPayTF.setText(String.format("₱%,.2f", empInfo.basicMonthlyPay / 2)); // Semi-monthly

        if (config != null) {
            telecoAllowance.setText(String.format("%.2f", config.telecomAllowance));
            travelAllowance.setText(String.format("%.2f", config.travelAllowance));
            riceSubsidy.setText(String.format("%.2f", config.riceSubsidy));
            nonTaxableTF.setText(String.format("%.2f", config.nonTaxableSalary));
            perDeimTF.setText(String.format("%.2f", config.perDiem));
            perDeimCountTF.setText(String.valueOf(config.perDiemCount));
        } else {
            telecoAllowance.setText("0.00");
            travelAllowance.setText("0.00");
            riceSubsidy.setText("0.00");
            nonTaxableTF.setText("0.00");
            perDeimTF.setText("0.00");
            perDeimCountTF.setText("0");
        }

        // Get SSS Loan from deduction_config
        PayrollDAO.DeductionData deductions = payrollDAO.getDeductions(searchID);
        if (deductions != null) {
            sssLoanTF.setText(String.format("%.2f", deductions.sssLoan));
        } else {
            sssLoanTF.setText("0.00");
        }

        // Clear computed fields until computation
        overtimeTF.clear();
        absencesTF.clear();
        numAbsencesTF.clear();
        sssContributionTF.clear();
        phicContributionTF.clear();
        hdmfContributionTF.clear();
        withholdingTaxTF.clear();
        grossPayTF.clear();
        totalDeductionTF.clear();
        netPayTF.clear();

        errorLabelManagePayroll.setText("");
        errorLabelManagePayroll.setStyle("-fx-text-fill: green;");
        errorLabelManagePayroll.setText("Employee data loaded successfully");

        payrollDAO.close();
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
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
            return;
        }

        LocalDate startDate = startingDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            errorLabelManagePayroll.setText("Please select payroll period dates");
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            // Parse inputs
            double perDiemInput = perDeimTF.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(perDeimTF.getText().trim());
            int perDiemCountInput = perDeimCountTF.getText().trim().isEmpty() ? 0 : Integer.parseInt(perDeimCountTF.getText().trim());
            double sssLoanInput = sssLoanTF.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(sssLoanTF.getText().trim());

            PayrollDAO dao = new PayrollDAO();

            // Get all required data
            PayrollDAO.EmployeeInfo empInfo = dao.getEmployeeInfo(empId);
            PayrollDAO.SalaryConfig config = dao.getSalaryConfig(empId);
            PayrollDAO.AttendanceData attendance = dao.getAttendanceData(empId, startDate, endDate);

            if (empInfo == null) {
                errorLabelManagePayroll.setText("Employee not found");
                errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
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
                        perDiemInput,
                        perDiemCountInput
                );
            } else {
                model.setAllowances(0, 0, 0, 0, perDiemInput, perDiemCountInput);
            }

            // Set deductions
            model.setDeductions(sssLoanInput);

            // Compute payroll
            model.computePayroll();

            // Display computed values
            displayComputedPayroll(model, config, attendance, perDiemInput, perDiemCountInput);

            errorLabelManagePayroll.setStyle("-fx-text-fill: green;");
            errorLabelManagePayroll.setText("Payroll computed successfully! Net Pay: ₱" +
                    String.format("%,.2f", model.getNetPay()));

            dao.close();

        } catch (NumberFormatException e) {
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
            errorLabelManagePayroll.setText("Please enter valid numeric values");
            e.printStackTrace();
        } catch (Exception e) {
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
            errorLabelManagePayroll.setText("Error computing payroll: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void saveComputedPayroll() {
        String empId = payrollEmployeeID.getText().trim();

        if (empId.isEmpty()) {
            errorLabelManagePayroll.setText("Please compute payroll first");
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
            return;
        }

        LocalDate startDate = startingDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            errorLabelManagePayroll.setText("Please select payroll period dates");
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
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
                errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
                dao.close();
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

            // Extract values from UI or config
            double perDiem = config != null ? config.perDiem : 0.0;
            int perDiemCount = config != null ? config.perDiemCount : 0;

            if (config != null) {
                model.setAllowances(
                        config.telecomAllowance, config.travelAllowance,
                        config.riceSubsidy, config.nonTaxableSalary,
                        perDiem, perDiemCount
                );
            } else {
                model.setAllowances(0, 0, 0, 0, perDiem, perDiemCount);
            }

            // Get SSS loan from deductions or from UI field
            double sssLoan = deductions != null ? deductions.sssLoan : 0.0;
            model.setDeductions(sssLoan);

            model.computePayroll();

            // Get or create payroll period
            int periodId = dao.getOrCreatePayrollPeriod(startDate, endDate);

            if (periodId == -1) {
                errorLabelManagePayroll.setText("Failed to create payroll period");
                errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
                dao.close();
                return;
            }

            // Save to database with all required parameters
            boolean saved = dao.savePayroll(
                    empId,
                    periodId,
                    model,
                    config,
                    attendance,
                    perDiem,
                    perDiemCount,
                    sssLoan
            );

            if (saved) {
                errorLabelManagePayroll.setStyle("-fx-text-fill: green;");
                errorLabelManagePayroll.setText("✓ Payroll saved successfully! Period ID: " + periodId);
                System.out.println("Payroll saved for employee " + empId + " in period " + periodId);
            } else {
                errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
                errorLabelManagePayroll.setText("Failed to save payroll to database");
            }

            dao.close();

        } catch (Exception e) {
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
            errorLabelManagePayroll.setText("Error saving payroll: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void viewSavedPayroll() {
        String empId = payrollEmployeeID.getText().trim();

        if (empId.isEmpty()) {
            errorLabelManagePayroll.setText("Please enter an Employee ID");
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
            return;
        }

        LocalDate startDate = startingDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            errorLabelManagePayroll.setText("Please select payroll period dates");
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            PayrollDAO dao = new PayrollDAO();

            // Get or find the period ID
            int periodId = dao.getOrCreatePayrollPeriod(startDate, endDate);

            if (periodId == -1) {
                errorLabelManagePayroll.setText("Payroll period not found");
                errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
                dao.close();
                return;
            }

            // Get saved payroll record
            PayrollDAO.PayrollRecord record = dao.getPayrollRecord(empId, periodId);

            if (record == null) {
                errorLabelManagePayroll.setText("No payroll record found for this employee and period");
                errorLabelManagePayroll.setStyle("-fx-text-fill: orange;");
                dao.close();
                return;
            }

            // Display the saved record
            basicPayTF.setText(String.format("₱%,.2f", record.basicPay));
            telecoAllowance.setText(String.format("%.2f", record.telecomAllowance));
            travelAllowance.setText(String.format("%.2f", record.travelAllowance));
            riceSubsidy.setText(String.format("%.2f", record.riceSubsidy));
            nonTaxableTF.setText(String.format("%.2f", record.nonTaxableSalary));
            perDeimTF.setText(String.format("%.2f", record.perDiem));
            perDeimCountTF.setText(String.valueOf(record.perDiemCount));

            overtimeTF.setText(String.format("₱%,.2f", record.overtimePay));
            absencesTF.setText(String.format("₱%,.2f", record.absences));
            numAbsencesTF.setText(String.valueOf(record.numAbsences));

            sssContributionTF.setText(String.format("₱%,.2f", record.sssContribution));
            phicContributionTF.setText(String.format("₱%,.2f", record.phicContribution));
            hdmfContributionTF.setText(String.format("₱%,.2f", record.hdmfContribution));
            sssLoanTF.setText(String.format("%.2f", record.sssLoan));

            withholdingTaxTF.setText(String.format("₱%,.2f", record.withholdingTax));
            grossPayTF.setText(String.format("₱%,.2f", record.grossPay));
            totalDeductionTF.setText(String.format("₱%,.2f", record.totalDeduction));
            netPayTF.setText(String.format("₱%,.2f", record.netPay));

            errorLabelManagePayroll.setStyle("-fx-text-fill: green;");
            errorLabelManagePayroll.setText("✓ Payroll record loaded successfully! Status: " + record.status);

            dao.close();

        } catch (Exception e) {
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
            errorLabelManagePayroll.setText("Error loading payroll: " + e.getMessage());
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

}