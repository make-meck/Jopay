package com.example.jopay;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.jopay.EmployeeDAO.connect;

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

    @FXML ComboBox<PayrollPeriodItem> payrollPeriodComboBox;

    // Store selected period information
    private LocalDate selectedStartDate;
    private LocalDate selectedEndDate;
    private int selectedPeriodId;
    private EmployeeDAO employeeDAO;

    // Employee Table
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> colId;
    @FXML private TableColumn<Employee, String> colName;
    @FXML private TableColumn<Employee, String> colDept;
    @FXML private TableColumn<Employee, String> colStatus;
    @FXML private TextField searchEmployeeID;

    //report analysis
    @FXML private Label headCountLabel;
    @FXML BarChart<String, Number> departmentWiseCount;
    @FXML PieChart attendancePieChart;


    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();

    // Admin credentials
    private int adminID = 11111;
    private String password = "0000";

    @FXML
    public void initialize() {

        employeeDAO = new EmployeeDAO();

        System.out.println("headCountLabel is null? " + (headCountLabel == null));

        if (employeeTable != null) {
            setupEmployeeTable();
        }

        // *** NEW: Initialize period ComboBox ***
        if (payrollPeriodComboBox != null) {
            setupPeriodComboBox();
        }

        if (departmentWiseCount != null && employeeDAO != null) {

        }
        Platform.runLater(() -> {
            displayActiveEmployees();
            loadDepartmentChart();
            loadWeeklyAttendanceChart();
        });


    }

    /**
     * NEW: Setup the payroll period ComboBox with data from database
     */
    private void setupPeriodComboBox() {
        PayrollDAO dao = new PayrollDAO();
        List<PayrollDAO.PayrollPeriod> periods = dao.getAllPayrollPeriods();

        ObservableList<PayrollPeriodItem> periodItems = FXCollections.observableArrayList();

        for (PayrollDAO.PayrollPeriod period : periods) {
            periodItems.add(new PayrollPeriodItem(period));
        }

        payrollPeriodComboBox.setItems(periodItems);

        // Set listener to capture selected period
        payrollPeriodComboBox.setOnAction(event -> {
            PayrollPeriodItem selected = payrollPeriodComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selectedStartDate = selected.getPeriod().startDate;
                selectedEndDate = selected.getPeriod().endDate;
                selectedPeriodId = selected.getPeriod().periodId;

                // Determine period type and show info
                boolean isFirstPeriod = selectedStartDate.getDayOfMonth() >= 11 &&
                        selectedStartDate.getDayOfMonth() <= 25;
                String periodType = isFirstPeriod ? "FIRST PERIOD (11-25) - Pag-IBIG WILL be deducted" :
                        "SECOND PERIOD (26-10) - Pag-IBIG NOT deducted";

                errorLabelManagePayroll.setStyle("-fx-text-fill: blue;");
                errorLabelManagePayroll.setText("✓ Selected: " + periodType);

                System.out.println("Period selected: " + selected.getPeriod().periodName);
                System.out.println("Dates: " + selectedStartDate + " to " + selectedEndDate);
                System.out.println("Period ID: " + selectedPeriodId);
                System.out.println("Type: " + periodType);
            }
        });

        dao.close();
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
    private void displayComputedPayroll(PayrollModel model, PayrollDAO.SalaryConfig config,
                                        PayrollDAO.AttendanceData attendance, double perDiem, int perDiemCount) {
        // Display all computed values in the text fields
        basicPayTF.setText(String.format("₱%,.2f", model.getSemiMonthlyBasicPay()));
        overtimeTF.setText(String.format("₱%,.2f", attendance.regularOTHours * model.getHourlyRate() * 1.25));
        absencesTF.setText(String.format("₱%,.2f", attendance.daysAbsent * model.getGrossDailyRate()));
        numAbsencesTF.setText(String.valueOf(attendance.daysAbsent));

        // Computed contributions
        sssContributionTF.setText(String.format("₱%,.2f", model.getSSSContribution()));
        phicContributionTF.setText(String.format("₱%,.2f", model.getPHICContribution()));

        // *** UPDATED: Show Pag-IBIG with visual indicator ***
        double hdmf = model.getHDMFContribution();
        if (hdmf > 0) {
            hdmfContributionTF.setText(String.format("₱%,.2f ✓", hdmf));
            hdmfContributionTF.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else {
            hdmfContributionTF.setText("₱0.00 (Not deducted)");
            hdmfContributionTF.setStyle("-fx-text-fill: gray;");
        }

        // Computed tax
        withholdingTaxTF.setText(String.format("₱%,.2f", model.getWithholdingTax()));

        // Summary
        grossPayTF.setText(String.format("₱%,.2f", model.getSemiMonthlyGrossPay()));
        totalDeductionTF.setText(String.format("₱%,.2f", model.getTotalDeductions()));
        netPayTF.setText(String.format("₱%,.2f", model.getNetPay()));

        // Console output for debugging
        System.out.println("\n=== COMPUTED PAYROLL ===");
        System.out.println("Period: " + selectedStartDate + " to " + selectedEndDate);
        System.out.println("SSS: " + String.format("₱%,.2f", model.getSSSContribution()));
        System.out.println("PHIC: " + String.format("₱%,.2f", model.getPHICContribution()));
        System.out.println("HDMF: " + String.format("₱%,.2f", model.getHDMFContribution()) +
                (hdmf > 0 ? " ✓ DEDUCTED" : " ✗ NOT DEDUCTED"));
        System.out.println("Overtime: " + String.format("₱%,.2f", attendance.regularOTHours * model.getHourlyRate() * 1.25));
        System.out.println("Absences: " + String.format("₱%,.2f", attendance.daysAbsent * model.getGrossDailyRate()));
        System.out.println("Tax: " + String.format("₱%,.2f", model.getWithholdingTax()));
        System.out.println("Gross Pay: " + String.format("₱%,.2f", model.getSemiMonthlyGrossPay()));
        System.out.println("Total Deductions: " + String.format("₱%,.2f", model.getTotalDeductions()));
        System.out.println("Net Pay: " + String.format("₱%,.2f", model.getNetPay()));
        System.out.println("========================\n");
    }

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

        // *** UPDATED: Clear period selection ***
        if (payrollPeriodComboBox != null) {
            payrollPeriodComboBox.getSelectionModel().clearSelection();
        }
        selectedStartDate = null;
        selectedEndDate = null;
        selectedPeriodId = 0;

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
        basicPayTF.setText(String.format("₱%,.2f", empInfo.basicMonthlyPay / 2));

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

        // Get SSS Loan
        PayrollDAO.DeductionData deductions = payrollDAO.getDeductions(searchID);
        if (deductions != null) {
            sssLoanTF.setText(String.format("%.2f", deductions.sssLoan));
        } else {
            sssLoanTF.setText("0.00");
        }

        // Clear computed fields
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
        errorLabelManagePayroll.setText("✓ Employee data loaded successfully");

        payrollDAO.close();
    }

    @FXML
    private void computePayroll() {
        String empId = payrollEmployeeID.getText().trim();

        if (empId.isEmpty()) {
            errorLabelManagePayroll.setText("Please search for an employee first");
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
            return;
        }

        // *** UPDATED: Check if period is selected from ComboBox ***
        if (selectedStartDate == null || selectedEndDate == null) {
            errorLabelManagePayroll.setText("Please select a payroll period from the dropdown");
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            // Parse inputs
            double perDiemInput = perDeimTF.getText().trim().isEmpty() ? 0.0 :
                    Double.parseDouble(perDeimTF.getText().trim());
            int perDiemCountInput = perDeimCountTF.getText().trim().isEmpty() ? 0 :
                    Integer.parseInt(perDeimCountTF.getText().trim());
            double sssLoanInput = sssLoanTF.getText().trim().isEmpty() ? 0.0 :
                    Double.parseDouble(sssLoanTF.getText().trim());

            PayrollDAO dao = new PayrollDAO();

            // Get all required data
            PayrollDAO.EmployeeInfo empInfo = dao.getEmployeeInfo(empId);
            PayrollDAO.SalaryConfig config = dao.getSalaryConfig(empId);
            PayrollDAO.AttendanceData attendance = dao.getAttendanceData(empId, selectedStartDate, selectedEndDate);

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

            // *** CRITICAL: Determine if first period (11-25) or second period (26-10) ***
            boolean isFirstPeriod = selectedStartDate.getDayOfMonth() >= 11 &&
                    selectedStartDate.getDayOfMonth() <= 25;

            String periodType = isFirstPeriod ? "FIRST PERIOD (11-25)" : "SECOND PERIOD (26-10)";
            System.out.println("Computing for: " + periodType);

            // Set payroll period with correct flag
            model.setPayrollPeriod(selectedStartDate, selectedEndDate, isFirstPeriod);

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

            // Show success message
            errorLabelManagePayroll.setStyle("-fx-text-fill: green;");
            errorLabelManagePayroll.setText(String.format(
                    "✓ Payroll computed! Period: %s | Net Pay: ₱%,.2f | Pag-IBIG: ₱%,.2f",
                    periodType,
                    model.getNetPay(),
                    model.getHDMFContribution()
            ));

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

        // *** UPDATED: Use selected period ***
        if (selectedStartDate == null || selectedEndDate == null) {
            errorLabelManagePayroll.setText("Please select a payroll period");
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            PayrollDAO dao = new PayrollDAO();

            // Get all required data
            PayrollDAO.EmployeeInfo empInfo = dao.getEmployeeInfo(empId);
            PayrollDAO.SalaryConfig config = dao.getSalaryConfig(empId);
            PayrollDAO.AttendanceData attendance = dao.getAttendanceData(empId, selectedStartDate, selectedEndDate);
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

            // Determine period type
            boolean isFirstPeriod = selectedStartDate.getDayOfMonth() >= 11 &&
                    selectedStartDate.getDayOfMonth() <= 25;
            model.setPayrollPeriod(selectedStartDate, selectedEndDate, isFirstPeriod);

            model.setAttendanceData(
                    attendance.daysWorked, attendance.daysAbsent,
                    attendance.regularOTHours, attendance.nightDifferentialOTHours,
                    attendance.specialHolidaysWorked, attendance.regularHolidaysWorked,
                    attendance.restDaysWorked, attendance.restDayOTHours,
                    attendance.restDayNightDiffOTHours, attendance.undertimeHours
            );

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

            double sssLoan = deductions != null ? deductions.sssLoan : 0.0;
            model.setDeductions(sssLoan);

            model.computePayroll();

            // *** FIXED: Call savePayroll with correct 5 parameters ***
            boolean saved = dao.savePayroll(
                    empId,
                    selectedPeriodId,
                    model,
                    config,
                    attendance
            );

            if (saved) {
                String periodType = isFirstPeriod ? "FIRST (11-25)" : "SECOND (26-10)";
                errorLabelManagePayroll.setStyle("-fx-text-fill: green;");
                errorLabelManagePayroll.setText(String.format(
                        "✓ Payroll saved! Period: %s | ID: %d | Net Pay: ₱%,.2f",
                        periodType, selectedPeriodId, model.getNetPay()
                ));
                System.out.println("Payroll saved for employee " + empId + " in period " + selectedPeriodId);
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
    private void reportAnalysisLoad(){
        displayActiveEmployees();
        loadDepartmentChart();
    }
    private void displayActiveEmployees() {

        int total = employeeDAO.getActiveEmployeeCount();
        headCountLabel.setText(String.valueOf(total));
    }
    public void loadDepartmentChart() {
        System.out.println("departmentWiseCount is null? " + (departmentWiseCount == null)); // ADD THIS

        if (departmentWiseCount == null) {
            System.out.println("ERROR: Chart is null!");
            return;
        }
        Map<String, Integer> deptCounts = employeeDAO.getEmployeeDepartmentCounts();

        // ADD THESE DEBUG LINES:
        System.out.println("Department counts: " + deptCounts);
        System.out.println("Number of departments: " + deptCounts.size());

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        //series.setName("Active Employees");

        deptCounts.forEach((dept, count) -> {
            System.out.println("Adding to chart: " + dept + " = " + count); // ADD THIS
            series.getData().add(new XYChart.Data<>(dept, count));
        });

        System.out.println("Series data size: " + series.getData().size()); // ADD THIS


        departmentWiseCount.getData().clear();
        departmentWiseCount.getData().add(series);

        System.out.println("Chart data added!"); // ADD THIS
    }

    public void loadWeeklyAttendanceChart() {
        Map<String, Integer> summary = employeeDAO.getWeeklyAttendanceSummary();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        summary.forEach((status, count) -> {
            pieData.add(new PieChart.Data(status + " (" + count + ")", count));
        });

        attendancePieChart.setData(pieData);
        attendancePieChart.setTitle("Weekly Attendance Summary");
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

    /**
     * Optional: Set current period based on today's date
     * Add this if you have a "Set Current Period" button in FXML
     */
    @FXML
    private void setCurrentPeriod() {
        LocalDate now = LocalDate.now();
        int day = now.getDayOfMonth();

        // Find the appropriate period in the ComboBox
        PayrollPeriodItem currentPeriod = null;

        for (PayrollPeriodItem item : payrollPeriodComboBox.getItems()) {
            LocalDate start = item.getPeriod().startDate;
            LocalDate end = item.getPeriod().endDate;

            // Check if today falls within this period
            if (!now.isBefore(start) && !now.isAfter(end)) {
                currentPeriod = item;
                break;
            }
        }

        if (currentPeriod != null) {
            payrollPeriodComboBox.getSelectionModel().select(currentPeriod);
            errorLabelManagePayroll.setStyle("-fx-text-fill: blue;");
            errorLabelManagePayroll.setText("✓ Current period selected automatically");
        } else {
            errorLabelManagePayroll.setStyle("-fx-text-fill: orange;");
            errorLabelManagePayroll.setText("No active period found for current date");
        }
    }

    /**
     * Inner class to represent a period item in the ComboBox
     */
    public static class PayrollPeriodItem {
        private PayrollDAO.PayrollPeriod period;

        public PayrollPeriodItem(PayrollDAO.PayrollPeriod period) {
            this.period = period;
        }

        public PayrollDAO.PayrollPeriod getPeriod() {
            return period;
        }

        @Override
        public String toString() {
            // This is what displays in the ComboBox
            // Format: "October 2025 - First Period (26-10)"
            return period.periodName;
        }
    }
}