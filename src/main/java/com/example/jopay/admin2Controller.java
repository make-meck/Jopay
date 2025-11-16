package com.example.jopay;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;




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
    @FXML ImageView adminLogoutImage;
    @FXML Hyperlink adminLogoutHpl;

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
    @FXML TextField undertimeTF;
    @FXML TextField slBalanceTF;
    @FXML TextField vlBalanceTF;
    @FXML TextField absencesTF;
    @FXML TextField numAbsencesTF;
    @FXML TextField sssContributionTF;
    @FXML TextField phicContributionTF;
    @FXML TextField hdmfContributionTF;
    @FXML TextField taxablePayTF;
    @FXML TextField withholdingTaxTF;
    @FXML TextField grossPayTF;
    @FXML TextField totalDeductionTF;
    @FXML TextField netPayTF;
    @FXML Button payrollUpdateButton;
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
    @FXML private BarChart<String, Number> departmentWiseCount;
    @FXML private PieChart attendancePieChart;
    @FXML private CategoryAxis departmentAxis;


    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();

    // Admin credentials
    private int adminID = 11111;
    private String password = "0000";

    @FXML
    public void initialize() {

        employeeDAO = new EmployeeDAO();

        if (employeeTable != null) {
            setupEmployeeTable();
        }
        setupAutoUpdateListeners();

        Platform.runLater(() -> {
            if (headCountLabel != null) {
                displayActiveEmployees();
            }

            if (departmentWiseCount != null) {
                loadDepartmentChart();  // Just load data, no wrapping needed
            }

            if (attendancePieChart != null) {
                loadWeeklyAttendanceChart();
            }
        });


    }

    private void setupAutoUpdateListeners() {
        if (basicPayTF != null) {
            basicPayTF.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (wasFocused && !isNowFocused) {
                    handleBasicPayUpdate();
                }
            });
        }

        if (perDeimTF != null) {
            perDeimTF.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (wasFocused && !isNowFocused) {
                    handlePerDiemUpdate();
                }
            });
        }

        if (perDeimCountTF != null) {
            perDeimCountTF.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (wasFocused && !isNowFocused) {
                    handlePerDiemCountUpdate();
                }
            });
        }

        if (sssLoanTF != null) {
            sssLoanTF.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (wasFocused && !isNowFocused) {
                    handleSSSLoanUpdate();
                }
            });
        }
    }

    // retrieve period data from database
    private void setupPeriodComboBox(String employeeId) {
        if (employeeId == null || employeeId.isEmpty()) {
            System.err.println("⚠ Cannot load periods: No employee selected");
            payrollPeriodComboBox.getItems().clear();
            payrollPeriodComboBox.setPromptText("Select employee first");
            payrollPeriodComboBox.setDisable(true);
            return;
        }

        PayrollDAO dao = new PayrollDAO();

        List<PayrollDAO.PayrollPeriod> periods = dao.getPeriodsFromHireDate(employeeId);

        ObservableList<PayrollPeriodItem> periodItems =
                FXCollections.observableArrayList();

        for (PayrollDAO.PayrollPeriod period : periods) {
            periodItems.add(new PayrollPeriodItem(period));
        }

        payrollPeriodComboBox.setItems(periodItems);

        if (periods.isEmpty()) {
            System.out.println("ℹ No periods available from hire date for employee " + employeeId);
            payrollPeriodComboBox.setPromptText("No periods available");
            payrollPeriodComboBox.setDisable(true);
        } else {
            System.out.println("✓ Loaded " + periods.size() +
                    " periods from hire date for employee " + employeeId);
            payrollPeriodComboBox.setPromptText("Select period");
            payrollPeriodComboBox.setDisable(false);
        }

        // Listener for period selection
        payrollPeriodComboBox.setOnAction(event -> {
            PayrollPeriodItem selected =
                    payrollPeriodComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selectedStartDate = selected.getPeriod().startDate;
                selectedEndDate = selected.getPeriod().endDate;
                selectedPeriodId = selected.getPeriod().periodId;

                boolean isFirstPeriod = selectedStartDate.getDayOfMonth() >= 26 ||
                        selectedStartDate.getDayOfMonth() <= 10;
                String periodType = isFirstPeriod ?
                        "FIRST PERIOD (11-25) - Pag-IBIG WILL be deducted" :
                        "SECOND PERIOD (26-10) - Pag-IBIG NOT deducted";

                String empId = payrollEmployeeID.getText().trim();
                if (!empId.isEmpty()) {
                    refreshLeaveBalance(empId);  // ← ADD THIS LINE
                    autoRecomputePayroll();
                }

                System.out.println("Period selected: " + selected.getPeriod().periodName);
                System.out.println("Dates: " + selectedStartDate + " to " + selectedEndDate);
                System.out.println("Period ID: " + selectedPeriodId);
                System.out.println("Type: " + periodType);

                if (!payrollEmployeeID.getText().trim().isEmpty()) {
                    autoRecomputePayroll();
                }
            }
        });

        dao.close();
    }
    // this is for the search employee table under the manage employee tab in admin dashboard
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
    //admin authetication method
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
                Parent root = fxmlLoader2.load();
                stage.getScene().setRoot(root);
            }
        } catch (NumberFormatException e) {
            error.setText("Please fill in all fields.");
        }
    }
    //this methods is intended for the back button in the admin login page
    @FXML
    void backButtonClick() throws IOException {
        FXMLLoader employeeDashboardLoader = new FXMLLoader(getClass().getResource("employeelogin.fxml"));
        Stage stage = (Stage) adminBackButton.getScene().getWindow();
        Parent root = employeeDashboardLoader.load();
        stage.getScene().setRoot(root);
    }
    //when the admin logout, it will go to the admin login page
    @FXML
    void adminLogoutClick() throws IOException {
        FXMLLoader adminLoginLoader = new FXMLLoader(getClass().getResource("admin2.fxml"));
        Stage stage = (Stage) adminLogoutHpl.getScene().getWindow();
        Parent root = adminLoginLoader.load();
        stage.getScene().setRoot(root);
    }

    @FXML
    void adminLogoutImageClick() throws IOException {
        FXMLLoader adminLoginLoader = new FXMLLoader(getClass().getResource("admin2.fxml"));
        Stage stage = (Stage) adminLogoutImage.getScene().getWindow();
        Parent root = adminLoginLoader.load();
        stage.getScene().setRoot(root);
    }

    //Once the Manage Employee button was clicked, it will make the other elements that is unrelated to manage employee will not be visible
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
        // Calculate absence amount
        double absenceAmount = attendance.daysAbsent * model.getGrossDailyRate();

        // Display all computed values in the text fields
        basicPayTF.setText(String.format("₱%,.2f", model.getSemiMonthlyBasicPay()));
        overtimeTF.setText(String.format("₱%,.2f", attendance.regularOTHours * model.getHourlyRate() * 1.25));
        undertimeTF.setText(String.format("₱%,.2f", attendance.undertimeHours * model.getHourlyRate()));

        // *** DISPLAY ABSENCE INFO ***
        absencesTF.setText(String.format("₱%,.2f", absenceAmount));
        numAbsencesTF.setText(String.valueOf(attendance.daysAbsent));

        // Computed contributions
        sssContributionTF.setText(String.format("₱%,.2f", model.getSSSContribution()));
        phicContributionTF.setText(String.format("₱%,.2f", model.getPHICContribution()));

        double hdmf = model.getHDMFContribution();
        if (hdmf > 0) {
            hdmfContributionTF.setText(String.format("₱%,.2f ✓", hdmf));
            hdmfContributionTF.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else {
            hdmfContributionTF.setText("₱0.00 (Not deducted)");
            hdmfContributionTF.setStyle("-fx-text-fill: gray;");
        }

        taxablePayTF.setText(String.format("₱%,.2f", model.getTaxableIncome()));
        withholdingTaxTF.setText(String.format("₱%,.2f", model.getWithholdingTax()));

        grossPayTF.setText(String.format("₱%,.2f", model.getSemiMonthlyGrossPay()));
        totalDeductionTF.setText(String.format("₱%,.2f", model.getTotalDeductions()));
        netPayTF.setText(String.format("₱%,.2f", model.getNetPay()));

        // Console output for debugging
        System.out.println("\n=== COMPUTED PAYROLL ===");
        System.out.println("Period: " + selectedStartDate + " to " + selectedEndDate);
        System.out.println("Basic Pay: " + String.format("₱%,.2f", model.getSemiMonthlyBasicPay()));
        System.out.println("Gross Pay: " + String.format("₱%,.2f", model.getSemiMonthlyGrossPay()));
        System.out.println("SSS: " + String.format("₱%,.2f", model.getSSSContribution()));
        System.out.println("PHIC: " + String.format("₱%,.2f", model.getPHICContribution()));
        System.out.println("HDMF: " + String.format("₱%,.2f", model.getHDMFContribution()) +
                (hdmf > 0 ? " ✓ DEDUCTED" : " ✗ NOT DEDUCTED"));
        System.out.println("Overtime: " + String.format("₱%,.2f", attendance.regularOTHours * model.getHourlyRate() * 1.25));
        System.out.println("Undertime: " + String.format("₱%,.2f", attendance.undertimeHours * model.getHourlyRate()));

        // *** ADD ABSENCE DEBUG OUTPUT ***
        System.out.println("Days Absent: " + attendance.daysAbsent);
        System.out.println("Absence Deduction: " + String.format("₱%,.2f", absenceAmount));

        System.out.println("Taxable Income: " + String.format("₱%,.2f", model.getTaxableIncome()));
        System.out.println("Withholding Tax: " + String.format("₱%,.2f", model.getWithholdingTax()));
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

        if (payrollPeriodComboBox != null) {
            payrollPeriodComboBox.getSelectionModel().clearSelection();
        }
        selectedStartDate = null;
        selectedEndDate = null;
        selectedPeriodId = 0;

        // Clear computed fields
        basicPayTF.clear();
        overtimeTF.clear();
        undertimeTF.clear();
        absencesTF.clear();
        numAbsencesTF.clear();
        sssContributionTF.clear();
        phicContributionTF.clear();
        hdmfContributionTF.clear();
        taxablePayTF.clear();  // *** ADD THIS LINE ***
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
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
            return;
        }

        // Get employee info
        PayrollDAO.EmployeeInfo empInfo = payrollDAO.getEmployeeInfo(searchID);
        if (empInfo == null) {
            clearPayrollFields();
            errorLabelManagePayroll.setText("Employee not found");
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
            payrollDAO.close();
            return;
        }

        // Auto-compute contributions if they don't exist or are zero
        PayrollDAO.ContributionData existingContrib = payrollDAO.getContributions(searchID);
        if (existingContrib == null || existingContrib.sssContribution == 0.0) {
            System.out.println("⚠ No valid contributions found. Auto-computing...");
            boolean computed = payrollDAO.autoComputeAndSaveContributions(searchID, empInfo.basicMonthlyPay);

            if (computed) {
                System.out.println("✓ Contributions auto-computed and saved!");
                errorLabelManagePayroll.setStyle("-fx-text-fill: blue;");
                errorLabelManagePayroll.setText("ℹ Contributions auto-computed for this employee");
            } else {
                System.err.println("✗ Failed to auto-compute contributions");
            }
        }

        // Display employee basic info
        payrollemployeeName.setText(empInfo.employeeName);
        payrollEmployeeID.setText(empInfo.employeeId);

        // Calculate semi-monthly basic pay
        double semiMonthlyPay = empInfo.basicMonthlyPay / 2;
        basicPayTF.setText(String.format("₱%,.2f", semiMonthlyPay));

        // Get salary config
        PayrollDAO.SalaryConfig config = payrollDAO.getSalaryConfig(searchID);
        if (config != null) {
            telecoAllowance.setText(String.format("%.2f", config.telecomAllowance));
            travelAllowance.setText(String.format("%.2f", config.travelAllowance));
            riceSubsidy.setText(String.format("%.2f", config.riceSubsidy));
            nonTaxableTF.setText(String.format("%.2f", config.nonTaxableSalary));
            perDeimTF.setText(String.format("%.2f", config.perDiem));
            perDeimCountTF.setText(String.valueOf(config.perDiemCount));

            // Refresh contributions after auto-compute
            PayrollDAO.ContributionData refreshedContrib = payrollDAO.getContributions(searchID);
            if (refreshedContrib != null) {
                sssContributionTF.setText(String.format("₱%,.2f", refreshedContrib.sssContribution / 2));
                phicContributionTF.setText(String.format("₱%,.2f", refreshedContrib.phicContribution));
                hdmfContributionTF.setText(String.format("₱%,.2f", refreshedContrib.hdmfContribution));

                System.out.println("=== CONTRIBUTION DISPLAY DEBUG ===");
                System.out.println("SSS from DB: ₱" + refreshedContrib.sssContribution);
                System.out.println("PHIC from DB: ₱" + refreshedContrib.phicContribution);
                System.out.println("HDMF from DB: ₱" + refreshedContrib.hdmfContribution);
                System.out.println("==================================");
            }
        } else {
            telecoAllowance.setText("0.00");
            travelAllowance.setText("0.00");
            riceSubsidy.setText("0.00");
            nonTaxableTF.setText("0.00");
            perDeimTF.setText("0.00");
            perDeimCountTF.setText("0");
            sssContributionTF.setText("₱0.00");
            phicContributionTF.setText("₱0.00");
            hdmfContributionTF.setText("₱0.00");
        }

        // *** NEW: Load leave balances ***
        // Load leave balances - show current balance, clear usage fields
        int currentYear = LocalDate.now().getYear();
        PayrollDAO.LeaveData leaveData = payrollDAO.getLeaveData(searchID, currentYear);

        if (leaveData == null || (leaveData.vlBalance == 0.0 && leaveData.slBalance == 0.0)) {
            // No leave balance or balance is zero - auto-calculate and allocate
            System.out.println("═══ AUTO-ALLOCATING LEAVE ═══");
            System.out.println("Employee: " + empInfo.employeeName);
            System.out.println("Hire Date: " + empInfo.dateHired);

            // Calculate based on hire date
            double vlDays = PayrollDAO.calculateVLBalance(empInfo.dateHired, currentYear);
            double slDays = PayrollDAO.calculateSLBalance(empInfo.dateHired, currentYear);

            // Save to database
            payrollDAO.updateLeaveBalance(searchID, currentYear, vlDays, "VL");
            payrollDAO.updateLeaveBalance(searchID, currentYear, slDays, "SL");

            System.out.println("✓ Leave auto-allocated: VL " + vlDays + ", SL " + slDays);
            System.out.println("════════════════════════════════");

            // Show info message to admin
            errorLabelManagePayroll.setStyle("-fx-text-fill: blue;");
            errorLabelManagePayroll.setText(String.format(
                    "ℹ Leave auto-allocated: VL %.2f days, SL %.0f days (based on hire date %s)",
                    vlDays, slDays, empInfo.dateHired
            ));

            // Reload leave data
            leaveData = payrollDAO.getLeaveData(searchID, currentYear);
        }

// Display current balance
        if (leaveData != null) {
            vlBalanceTF.setPromptText("Available: " + String.format("%.2f", leaveData.vlBalance) + " days");
            slBalanceTF.setPromptText("Available: " + String.format("%.2f", leaveData.slBalance) + " days");
            vlBalanceTF.clear();
            slBalanceTF.clear();

            System.out.println("=== CURRENT LEAVE BALANCE ===");
            System.out.println("VL Balance: " + leaveData.vlBalance + " days");
            System.out.println("SL Balance: " + leaveData.slBalance + " days");
            System.out.println("============================");
        } else {
            vlBalanceTF.setPromptText("No balance");
            slBalanceTF.setPromptText("No balance");
            vlBalanceTF.clear();
            slBalanceTF.clear();
        }


        // Load SSS Loan
        PayrollDAO.DeductionData deductions = payrollDAO.getDeductions(searchID);
        if (deductions != null) {
            sssLoanTF.setText(String.format("%.2f", deductions.sssLoan));
        } else {
            sssLoanTF.setText("0.00");
        }

        setupPeriodComboBox(searchID);
        payrollDAO.close();
    }



    private void handleBasicPayUpdate() {
        String empId = payrollEmployeeID.getText().trim();
        if (empId.isEmpty()) return;

        try {
            // Parse the basic pay from textfield (remove currency symbols)
            String basicPayText = basicPayTF.getText().trim()
                    .replace("₱", "")
                    .replace(",", "");

            if (basicPayText.isEmpty()) return;

            double newBasicPay = Double.parseDouble(basicPayText);

            // Convert semi-monthly to monthly
            double newMonthlyBasicPay = newBasicPay * 2;

            PayrollDAO dao = new PayrollDAO();

            // Update basic salary in employee_info table
            boolean updated = dao.updateBasicSalary(empId, newMonthlyBasicPay);

            if (updated) {
                System.out.println("✓ Basic Salary updated to: ₱" + String.format("%,.2f", newMonthlyBasicPay));

                // Auto-recompute contributions based on new salary
                dao.autoComputeAndSaveContributions(empId, newMonthlyBasicPay);

                // Auto-recompute payroll if period is selected
                if (selectedStartDate != null && selectedEndDate != null) {
                    autoRecomputePayroll();
                }
            } else {
                System.err.println("Failed to update Basic Salary");
            }

            dao.close();

        } catch (NumberFormatException e) {
            errorLabelManagePayroll.setText("Invalid Basic Pay value");
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
        }
    }

    private void handlePerDiemUpdate() {
        String empId = payrollEmployeeID.getText().trim();
        if (empId.isEmpty()) return;

        try {
            double perDiem = perDeimTF.getText().trim().isEmpty() ? 0.0 :
                    Double.parseDouble(perDeimTF.getText().trim().replace("₱", "").replace(",", ""));

            PayrollDAO dao = new PayrollDAO();

            // Update in database
            boolean updated = dao.updatePerDiem(empId, perDiem);

            if (updated) {
                System.out.println("✓ Per Diem updated to: ₱" + String.format("%,.2f", perDiem));

                if (selectedStartDate != null && selectedEndDate != null) {
                    autoRecomputePayroll();
                }
            } else {
                System.err.println("Failed to update Per Diem");
            }

            dao.close();

        } catch (NumberFormatException e) {
            errorLabelManagePayroll.setText("Invalid Per Diem value");
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
        }
    }


    private void handlePerDiemCountUpdate() {
        String empId = payrollEmployeeID.getText().trim();
        if (empId.isEmpty()) return;

        try {
            int perDiemCount = perDeimCountTF.getText().trim().isEmpty() ? 0 :
                    Integer.parseInt(perDeimCountTF.getText().trim());

            PayrollDAO dao = new PayrollDAO();

            // Update in database
            boolean updated = dao.updatePerDiemCount(empId, perDiemCount);

            if (updated) {
                System.out.println("✓ Per Diem Count updated to: " + perDiemCount);

                // Auto-recompute if period is selected
                if (selectedStartDate != null && selectedEndDate != null) {
                    autoRecomputePayroll();
                }
            } else {
                System.err.println("Failed to update Per Diem Count");
            }

            dao.close();

        } catch (NumberFormatException e) {
            errorLabelManagePayroll.setText("Invalid Per Diem Count value");
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
        }
    }

    private void handleSSSLoanUpdate() {
        String empId = payrollEmployeeID.getText().trim();
        if (empId.isEmpty()) return;

        try {
            double sssLoan = sssLoanTF.getText().trim().isEmpty() ? 0.0 :
                    Double.parseDouble(sssLoanTF.getText().trim().replace("₱", "").replace(",", ""));

            PayrollDAO dao = new PayrollDAO();

            // Update in database
            boolean updated = dao.updateSSSLoan(empId, sssLoan);

            if (updated) {
                System.out.println("✓ SSS Loan updated to: ₱" + String.format("%,.2f", sssLoan));

                if (selectedStartDate != null && selectedEndDate != null) {
                    autoRecomputePayroll();
                }
            } else {
                System.err.println("Failed to update SSS Loan");
            }

            dao.close();

        } catch (NumberFormatException e) {
            errorLabelManagePayroll.setText("Invalid SSS Loan value");
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
        }
    }


    @FXML
    private void updateComputedPayroll() {
        saveComputedPayroll();

        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Update Successful");
        successAlert.setHeaderText(null);
        successAlert.setContentText("✓ Payroll has been updated successfully!");
        successAlert.showAndWait();
    }



    @FXML
    private void computePayroll() {
        String empId = payrollEmployeeID.getText().trim();

        if (empId.isEmpty()) {
            errorLabelManagePayroll.setText("Please search for an employee first");
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
            return;
        }

        if (selectedStartDate == null || selectedEndDate == null) {
            errorLabelManagePayroll.setText("Please select a payroll period from the dropdown");
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            double perDiemInput = perDeimTF.getText().trim().isEmpty() ? 0.0 :
                    Double.parseDouble(perDeimTF.getText().trim());
            int perDiemCountInput = perDeimCountTF.getText().trim().isEmpty() ? 0 :
                    Integer.parseInt(perDeimCountTF.getText().trim());
            double sssLoanInput = sssLoanTF.getText().trim().isEmpty() ? 0.0 :
                    Double.parseDouble(sssLoanTF.getText().trim());
            double vlDaysTaken = vlBalanceTF.getText().trim().isEmpty() ? 0.0 :
                    Double.parseDouble(vlBalanceTF.getText().trim());
            double slDaysTaken = slBalanceTF.getText().trim().isEmpty() ? 0.0 :
                    Double.parseDouble(slBalanceTF.getText().trim());



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

            boolean isFirstPeriod = selectedStartDate.getDayOfMonth() >= 26 ||
                    selectedStartDate.getDayOfMonth() <= 10;

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

            // Get current leave balance
            int currentYear = selectedStartDate.getYear();
            PayrollDAO.LeaveData leave = dao.getLeaveData(empId, currentYear);
            if (leave != null) {
                model.setLeaveData(
                        vlDaysTaken,
                        slDaysTaken,
                        leave.vlBalance,
                        leave.slBalance
                );
                System.out.println("Leave tracking: VL taken=" + vlDaysTaken + ", SL taken=" + slDaysTaken);
            } else {
                model.setLeaveData(vlDaysTaken, slDaysTaken, 0.0, 0.0);
            }

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

    // auto-recompute payroll when updated
    private void autoRecomputePayroll() {
        String empId = payrollEmployeeID.getText().trim();

        if (empId.isEmpty() || selectedStartDate == null || selectedEndDate == null) {
            return;
        }

        try {
            // Get updated values from text fields
            double perDiemInput = perDeimTF.getText().trim().isEmpty() ? 0.0 :
                    Double.parseDouble(perDeimTF.getText().trim().replace("₱", "").replace(",", ""));
            int perDiemCountInput = perDeimCountTF.getText().trim().isEmpty() ? 0 :
                    Integer.parseInt(perDeimCountTF.getText().trim());
            double sssLoanInput = sssLoanTF.getText().trim().isEmpty() ? 0.0 :
                    Double.parseDouble(sssLoanTF.getText().trim().replace("₱", "").replace(",", ""));

            PayrollDAO dao = new PayrollDAO();

            // Get all required data (will now have updated values)
            PayrollDAO.EmployeeInfo empInfo = dao.getEmployeeInfo(empId);
            PayrollDAO.SalaryConfig config = dao.getSalaryConfig(empId);
            PayrollDAO.AttendanceData attendance = dao.getAttendanceData(empId, selectedStartDate, selectedEndDate);

            if (empInfo == null) {
                return;
            }

            // Initialize PayrollModel using PayrollService pattern
            PayrollModel model = new PayrollModel();
            model.PayrollComputation(
                    empInfo.employeeId,
                    empInfo.employeeName,
                    empInfo.basicMonthlyPay,
                    empInfo.employmentStatus,
                    empInfo.dateHired,
                    empInfo.workingHoursPerDay
            );

            boolean isFirstPeriod = selectedStartDate.getDayOfMonth() >= 26 ||
                    selectedStartDate.getDayOfMonth() <= 10;
            model.setPayrollPeriod(selectedStartDate, selectedEndDate, isFirstPeriod);

            dao.loadContributionsIntoPayrollModel(empId, model, isFirstPeriod);

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

            // Set allowances with updated per diem values
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

            // Get updated leave data
            int currentYear = selectedStartDate.getYear();
            PayrollDAO.LeaveData leave = dao.getLeaveData(empId, currentYear);
            if (leave != null) {
                model.setLeaveData(
                        leave.vlUsed,
                        leave.slUsed,
                        leave.vlBalance,
                        leave.slBalance
                );
            }

            // Compute payroll
            model.computePayroll();

            // Display computed values
            displayComputedPayroll(model, config, attendance, perDiemInput, perDiemCountInput);


            dao.close();

        } catch (NumberFormatException e) {
            // Silently fail if invalid number - user is still typing
        } catch (Exception e) {
            System.err.println("Error auto-recomputing: " + e.getMessage());
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

        if (selectedStartDate == null || selectedEndDate == null) {
            errorLabelManagePayroll.setText("Please select a payroll period");
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            double sssLoanInput = sssLoanTF.getText().trim().isEmpty() ? 0.0 :
                    Double.parseDouble(sssLoanTF.getText().trim().replace("₱", "").replace(",", ""));
            double perDiemInput = perDeimTF.getText().trim().isEmpty() ? 0.0 :
                    Double.parseDouble(perDeimTF.getText().trim());
            int perDiemCountInput = perDeimCountTF.getText().trim().isEmpty() ? 0 :
                    Integer.parseInt(perDeimCountTF.getText().trim());

            PayrollDAO dao = new PayrollDAO();

            // Get all required data
            PayrollDAO.EmployeeInfo empInfo = dao.getEmployeeInfo(empId);
            PayrollDAO.SalaryConfig config = dao.getSalaryConfig(empId);
            PayrollDAO.AttendanceData attendance = dao.getAttendanceData(empId, selectedStartDate, selectedEndDate);

            if (empInfo == null) {
                errorLabelManagePayroll.setText("Employee not found");
                errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
                dao.close();
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

            // Determine period type
            boolean isFirstPeriod = selectedStartDate.getDayOfMonth() >= 26 ||
                    selectedStartDate.getDayOfMonth() <= 10;
            model.setPayrollPeriod(selectedStartDate, selectedEndDate, isFirstPeriod);

            // *** ADD THIS: Load pre-computed contributions ***
            dao.loadContributionsIntoPayrollModel(empId, model, isFirstPeriod);

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
                        perDiemInput, perDiemCountInput
                );
            } else {
                model.setAllowances(0, 0, 0, 0, perDiemInput, perDiemCountInput);
            }

            model.setDeductions(sssLoanInput);

            // Get leave data
            int currentYear = selectedStartDate.getYear();
            PayrollDAO.LeaveData leave = dao.getLeaveData(empId, currentYear);
            if (leave != null) {
                double vlDaysTaken = vlBalanceTF.getText().trim().isEmpty() ? 0.0 :
                        Double.parseDouble(vlBalanceTF.getText().trim());
                double slDaysTaken = slBalanceTF.getText().trim().isEmpty() ? 0.0 :
                        Double.parseDouble(slBalanceTF.getText().trim());

                model.setLeaveData(vlDaysTaken, slDaysTaken, leave.vlBalance, leave.slBalance);
            }

            model.computePayroll();

            // *** ADD DEBUG OUTPUT ***
            System.out.println("\n=== BEFORE SAVING TO DATABASE ===");
            System.out.println("SSS: ₱" + model.getSSSContribution());
            System.out.println("PHIC: ₱" + model.getPHICContribution());
            System.out.println("HDMF: ₱" + model.getHDMFContribution());
            System.out.println("Withholding Tax: ₱" + model.getWithholdingTax());
            System.out.println("Total Deductions: ₱" + model.getTotalDeductions());
            System.out.println("Net Pay: ₱" + model.getNetPay());
            System.out.println("==================================\n");

            // Save to database
            boolean saved = dao.savePayroll(
                    empId,
                    selectedPeriodId,
                    model,
                    config,
                    attendance
            );

            if (saved) {
                // Handle leave balance updates...
                // (rest of your existing code)
            }

            dao.close();

        } catch (Exception e) {
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
            errorLabelManagePayroll.setText("Error saving payroll: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Refresh and display current leave balance
     * Called when period changes
     */
    private void refreshLeaveBalance(String employeeId) {
        if (employeeId == null || employeeId.isEmpty()) {
            return;
        }

        try {
            PayrollDAO dao = new PayrollDAO();
            int currentYear = LocalDate.now().getYear();

            // Get current leave balance from database
            PayrollDAO.LeaveData leaveData = dao.getLeaveData(employeeId, currentYear);

            if (leaveData != null) {
                // Update prompt text with current balance
                vlBalanceTF.setPromptText("Available: " + String.format("%.2f", leaveData.vlBalance) + " days");
                slBalanceTF.setPromptText("Available: " + String.format("%.2f", leaveData.slBalance) + " days");

                // Clear input fields
                vlBalanceTF.clear();
                slBalanceTF.clear();

                System.out.println("═══ BALANCE REFRESHED ═══");
                System.out.println("VL: " + leaveData.vlBalance + " days");
                System.out.println("SL: " + leaveData.slBalance + " days");
                System.out.println("═════════════════════════");
            }

            dao.close();

        } catch (Exception e) {
            System.err.println("Error refreshing leave: " + e.getMessage());
        }
    }

    /**
     * Special method for February leave conversion
     * Converts unused VL/SL to cash payout
     */
    @FXML
    private void computeFebruaryLeaveConversion() {
        String empId = payrollEmployeeID.getText().trim();

        if (empId.isEmpty()) {
            errorLabelManagePayroll.setText("Please search for an employee first");
            return;
        }

        try {
            PayrollDAO dao = new PayrollDAO();
            int currentYear = LocalDate.now().getYear();

            // Get current leave balances
            PayrollDAO.LeaveData leave = dao.getLeaveData(empId, currentYear);
            if (leave == null) {
                errorLabelManagePayroll.setText("No leave balance found");
                return;
            }

            PayrollDAO.EmployeeInfo empInfo = dao.getEmployeeInfo(empId);
            if (empInfo == null) return;

            // Calculate leave conversion
            PayrollModel model = new PayrollModel();
            model.PayrollComputation(
                    empInfo.employeeId,
                    empInfo.employeeName,
                    empInfo.basicMonthlyPay,
                    empInfo.employmentStatus,
                    empInfo.dateHired,
                    empInfo.workingHoursPerDay
            );

            // Set unused leave balances
            model.setLeaveData(0, 0, leave.vlBalance, leave.slBalance);

            // Calculate conversion amount
            double leaveConversionPay = model.calculateUnusedLeaveConversion();

            errorLabelManagePayroll.setStyle("-fx-text-fill: blue;");
            errorLabelManagePayroll.setText(String.format(
                    "February Leave Conversion: VL %.1f days + SL %.1f days = ₱%,.2f",
                    leave.vlBalance, leave.slBalance, leaveConversionPay
            ));

            System.out.println("=== FEBRUARY LEAVE CONVERSION ===");
            System.out.println("Unused VL: " + leave.vlBalance + " days");
            System.out.println("Unused SL: " + leave.slBalance + " days");
            System.out.println("Conversion Amount: ₱" + String.format("%,.2f", leaveConversionPay));
            System.out.println("=================================");

            dao.close();

        } catch (Exception e) {
            errorLabelManagePayroll.setText("Error: " + e.getMessage());
        }
    }

    // Add this method to your admin2Controller class

    @FXML
    private void deletePayroll() {
        String empId = payrollEmployeeID.getText().trim();

        // Validation: Check if employee is selected
        if (empId.isEmpty()) {
            errorLabelManagePayroll.setText("Please search for an employee first");
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
            return;
        }

        // Validation: Check if period is selected
        if (selectedStartDate == null || selectedEndDate == null || selectedPeriodId == 0) {
            errorLabelManagePayroll.setText("Please select a payroll period from the dropdown");
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            PayrollDAO dao = new PayrollDAO();

            // Check if payroll record exists
            boolean exists = dao.payrollExists(empId, selectedPeriodId);

            if (!exists) {
                errorLabelManagePayroll.setText("No payroll record found for this period");
                errorLabelManagePayroll.setStyle("-fx-text-fill: orange;");
                dao.close();
                return;
            }

            // Show confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Delete Payroll Confirmation");
            confirmAlert.setHeaderText("Delete Payroll Record");
            confirmAlert.setContentText(String.format(
                    "Are you sure you want to delete the payroll record for:\n\n" +
                            "Employee ID: %s\n" +
                            "Employee Name: %s\n" +
                            "Period: %s to %s\n\n" +
                            "This action cannot be undone!",
                    empId,
                    payrollemployeeName.getText(),
                    selectedStartDate.toString(),
                    selectedEndDate.toString()
            ));

            Optional<ButtonType> result = confirmAlert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                // User confirmed deletion
                boolean deleted = dao.deletePayroll(empId, selectedPeriodId);

                if (deleted) {
                    errorLabelManagePayroll.setStyle("-fx-text-fill: green;");
                    errorLabelManagePayroll.setText(String.format(
                            "✓ Payroll record deleted successfully for Period ID: %d",
                            selectedPeriodId
                    ));

                    // Clear the computed fields to reflect deletion
                    clearComputedFields();

                    System.out.println("Payroll deleted for employee " + empId +
                            " in period " + selectedPeriodId);
                } else {
                    errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
                    errorLabelManagePayroll.setText("Failed to delete payroll record");
                }
            } else {
                // User cancelled deletion
                errorLabelManagePayroll.setStyle("-fx-text-fill: blue;");
                errorLabelManagePayroll.setText("Deletion cancelled");
            }

            dao.close();

        } catch (Exception e) {
            errorLabelManagePayroll.setStyle("-fx-text-fill: red;");
            errorLabelManagePayroll.setText("Error deleting payroll: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Clear only the computed payroll fields (not employee info or allowances)
     */
    private void clearComputedFields() {
        basicPayTF.clear();
        overtimeTF.clear();
        undertimeTF.clear();
        absencesTF.clear();
        numAbsencesTF.clear();
        sssContributionTF.clear();
        phicContributionTF.clear();
        hdmfContributionTF.clear();
        taxablePayTF.clear();
        withholdingTaxTF.clear();
        grossPayTF.clear();
        totalDeductionTF.clear();
        netPayTF.clear();
    }

    //When the admin clicked "Manage Payroll" button, the element related will be visible.
    @FXML
    private void managePayrollClick() {
        pane1.setVisible(false);
        managePayrollPane.setVisible(true);
        reportAnalysisPane.setVisible(false);
        payrollLabel.setVisible(true);
        manageEmpLabel.setVisible(false);
        reportAnalysisLabel.setVisible(false);
    }
    //When the admin, clicked the "Report Analysis" button it will show the elements related to this screen such as the charts.
    @FXML
    private void reportAnalysisClick() {
        pane1.setVisible(false);
        reportAnalysisPane.setVisible(true);
        managePayrollPane.setVisible(false);
        reportAnalysisLabel.setVisible(true);
        payrollLabel.setVisible(false);
        manageEmpLabel.setVisible(false);
    }

    //It displays the number of active employees in the company
    private void displayActiveEmployees() {

        int total = employeeDAO.getActiveEmployeeCount();
        headCountLabel.setText(String.valueOf(total));
    }

//This charts displays the number of employees per department
    public void loadDepartmentChart() {
        if (departmentWiseCount == null) {
            System.out.println("ERROR: Chart is null!");
            return;
        }

        Map<String, Integer> deptCounts = employeeDAO.getEmployeeDepartmentCounts();

        departmentWiseCount.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Active Employees");

        deptCounts.forEach((dept, count) -> {
            series.getData().add(new XYChart.Data<>(dept, count));
        });

        departmentWiseCount.getData().add(series);
        departmentWiseCount.setTitle("Department Wise Headcount");


        CategoryAxis xAxis = departmentAxis;
        if (xAxis != null) {
            xAxis.setTickLabelRotation(-45);
            xAxis.setTickLabelGap(10);
            xAxis.setStyle("-fx-tick-label-font-size: 10px; -fx-font-weight: normal;");

        }

        departmentWiseCount.setCategoryGap(10);
        departmentWiseCount.setBarGap(10);


        int categoryCount = deptCounts.size();
        int widthPerCategory = 70; // Pixels per department
        int calculatedWidth = categoryCount * widthPerCategory;
        int minWidth = 1000;


        departmentWiseCount.setPrefWidth(Math.max(calculatedWidth, minWidth));
        departmentWiseCount.setMinWidth(calculatedWidth);

        departmentWiseCount.setPadding(new Insets(10, 10, 40, 10));

        if (departmentWiseCount.getParent() instanceof javafx.scene.control.ScrollPane) {
            javafx.scene.control.ScrollPane scrollPane =
                    (javafx.scene.control.ScrollPane) departmentWiseCount.getParent();
            scrollPane.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setFitToHeight(true);
        }
    }
    //This displays the weekly attendance of the employees
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
        searchHBox.setVisible(false);
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
            return period.periodName;
        }
    }
}