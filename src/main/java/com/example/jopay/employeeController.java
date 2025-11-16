package com.example.jopay;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.util.Map;

public class employeeController {

    @FXML
    private Label welcomeLabel;

    // *** CHANGED: ComboBox type to use PayslipPeriodItem ***
    @FXML
    private ComboBox<PayslipPeriodItem> comboBoxPeriod;

    @FXML
    private Hyperlink downloadLink;

    @FXML
    private ImageView downloadBtn;

    @FXML
    private Hyperlink updatePasswordLink;

    @FXML
    private Hyperlink logoutLink;

    @FXML
    private ImageView logoutImageView;

    private String selectedPeriod = "";
    private int selectedPeriodId = 0;  // *** ADD THIS ***

    @FXML
    private Button updateBackBtn;
    @FXML private PieChart employeeAttendancePieChart;

    // Employee data
    private String employeeId;
    private String employeeName;
    private String employmentStatus;

    // Earnings
    private double basicPay = 0.0;
    private double overtime = 0.0;
    private double overtimeHours = 0.0;
    private double undertime = 0.0;
    private double undertimeHours = 0.0;

    // Allowances
    private double telecom = 0.0;
    private double travel = 0.0;
    private double riceSubsidy = 0.0;
    private double nonTaxableSalary = 0.0;
    private double perDiem = 0.0;
    private int perDiemCount = 1;

    // Deductions
    private double sssContributions = 0.0;
    private double phicContributions = 0.0;
    private double hdmfContributions = 0.0;
    private double sssLoan = 0.0;
    private double absences = 0.0;
    private int numAbsences = 0;
    private double withholdingTax = 0.0;
    private double taxableIncome = 0.0;

    // Summary
    private double grossPay = 0.0;
    private double totalDeductions = 0.0;
    private double netPay = 0.0;
    private int loggedInEmployeeId;



    @FXML
    void initialize() {
        System.out.println("=== employeeController initialized ===");
        System.out.println("PieChart is null? " + (employeeAttendancePieChart == null));

        // *** REMOVED: loadPayrollPeriods() - will be called in loadEmployeeData() ***

        downloadLink.setVisible(false);
        downloadBtn.setVisible(false);

        File payslipsDir = new File("payslips");
        if (!payslipsDir.exists()) {
            payslipsDir.mkdir();
        }
    }

    /**
     * Load ONLY periods where THIS employee has payroll records
     * Used for payslip dropdown - employee can only view existing payslips
     */
    private void loadPayrollPeriods() {
        if (employeeId == null || employeeId.isEmpty()) {
            System.err.println("⚠ Cannot load payroll periods: Employee ID not set");
            return;
        }

        PayrollDAO dao = new PayrollDAO();

        // ✅ CORRECT: Get ONLY periods where employee has payroll records
        java.util.List<PayrollDAO.PayrollPeriod> periods =
                dao.getPeriodsWithPayrollDataForEmployee(employeeId);

        comboBoxPeriod.getItems().clear();

        if (periods.isEmpty()) {
            System.out.println("ℹ No payslips found for employee " + employeeId);
            comboBoxPeriod.setPromptText("No payslips available");
            comboBoxPeriod.setDisable(true);

            // Hide download controls
            downloadLink.setVisible(false);
            downloadBtn.setVisible(false);
        } else {
            System.out.println("✓ Loaded " + periods.size() +
                    " payslip periods for employee " + employeeId);

            for (PayrollDAO.PayrollPeriod period : periods) {
                comboBoxPeriod.getItems().add(new PayslipPeriodItem(period));
            }

            comboBoxPeriod.setPromptText("Select period");
            comboBoxPeriod.setDisable(false);
        }

        dao.close();
    }

    // *** UPDATED METHOD ***
    // load employee info
    public void loadEmployeeData(String empId) {
        this.employeeId = empId;

        PayrollDAO dao = new PayrollDAO();
        PayrollDAO.EmployeeInfo empInfo = dao.getEmployeeInfo(empId);

        if (empInfo != null) {
            this.employeeName = empInfo.employeeName;
            this.employmentStatus = empInfo.employmentStatus;

            welcomeLabel.setText("Mabuhay " + employeeName + "!");

            System.out.println("Employee data loaded: " + employeeName);

            // *** ADD THIS: Load payroll periods after employee data is loaded ***
            loadPayrollPeriods();
        } else {
            System.err.println("Failed to load employee data for ID: " + empId);
        }

        dao.close();
    }

    /**
     * Load payroll data from database for the selected period
     */
    private boolean loadPayrollDataForPeriod(int periodId) {
        PayrollDAO dao = new PayrollDAO();
        DatabaseConnector dbConnect = new DatabaseConnector();

        try {
            String query = """
                    SELECT 
                        pr.basic_pay,
                        pr.telecom_Allowance,
                        pr.travel_Allowance,
                        pr.rice_Subsidy,
                        pr.non_Taxable_Salary,
                        pr.per_Deim,
                        pr.per_Deim_Count,
                        pr.overtime_Pay,
                        pr.overtime_hours,
                        pr.undertime_Pay,
                        pr.undertime_hours,
                        pr.sss_Contribution,
                        pr.phic_contribution,
                        pr.hdmf_Contibution,
                        pr.sss_Loan,
                        pr.absences,
                        pr.num_Absences,
                        pr.withholding_Tax,
                        pr.taxable_Income,
                        pr.gross_pay,
                        pr.total_Deduction,
                        pr.net_Pay
                    FROM payroll_records pr
                    WHERE pr.employee_Id = ? AND pr.period_id = ?
                    ORDER BY pr.payroll_Id DESC
                    LIMIT 1
                """;

            PreparedStatement stmt = dbConnect.getConnection().prepareStatement(query);
            stmt.setString(1, employeeId);
            stmt.setInt(2, periodId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Load all payroll data
                this.basicPay = rs.getDouble("basic_pay");
                this.telecom = rs.getDouble("telecom_Allowance");
                this.travel = rs.getDouble("travel_Allowance");
                this.riceSubsidy = rs.getDouble("rice_Subsidy");
                this.nonTaxableSalary = rs.getDouble("non_Taxable_Salary");
                this.perDiem = rs.getDouble("per_Deim");
                this.perDiemCount = rs.getInt("per_Deim_Count");
                this.overtime = rs.getDouble("overtime_Pay");
                this.overtimeHours = rs.getDouble("overtime_hours");
                this.undertime = rs.getDouble("undertime_Pay");
                this.undertimeHours = rs.getDouble("undertime_hours");
                this.sssContributions = rs.getDouble("sss_Contribution");
                this.phicContributions = rs.getDouble("phic_contribution");
                this.hdmfContributions = rs.getDouble("hdmf_Contibution");
                this.sssLoan = rs.getDouble("sss_Loan");
                this.absences = rs.getDouble("absences");
                this.numAbsences = rs.getInt("num_Absences");
                this.withholdingTax = rs.getDouble("withholding_Tax");
                this.taxableIncome = rs.getDouble("taxable_Income");
                this.grossPay = rs.getDouble("gross_pay");
                this.totalDeductions = rs.getDouble("total_Deduction");
                this.netPay = rs.getDouble("net_Pay");

                System.out.println("\n=== PAYROLL DATA LOADED ===");
                System.out.println("Employee: " + employeeName);
                System.out.println("Period ID: " + periodId);
                System.out.println("Basic Pay: ₱" + String.format("%,.2f", basicPay));
                System.out.println("Gross Pay: ₱" + String.format("%,.2f", grossPay));
                System.out.println("Net Pay: ₱" + String.format("%,.2f", netPay));
                System.out.println("===========================\n");

                rs.close();
                stmt.close();
                dbConnect.close();
                dao.close();
                return true;
            } else {
                System.err.println("No payroll record found for employee " + employeeId +
                        " in period " + periodId);
                rs.close();
                stmt.close();
                dbConnect.close();
                dao.close();
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error loading payroll data: " + e.getMessage());
            e.printStackTrace();
            dbConnect.close();
            dao.close();
            return false;
        }
    }

    // *** UPDATED METHOD ***
    @FXML
    void handleMonthSelection() {
        PayslipPeriodItem selection = comboBoxPeriod.getValue();

        if (selection != null) {
            // *** CHANGED: Get data from PayslipPeriodItem object ***
            selectedPeriod = selection.getPeriod().periodName;
            selectedPeriodId = selection.getPeriod().periodId;

            System.out.println("Period selected: " + selectedPeriod + " (ID: " + selectedPeriodId + ")");

            // Load payroll data for this period
            boolean dataLoaded = loadPayrollDataForPeriod(selectedPeriodId);

            if (dataLoaded) {
                downloadLink.setVisible(true);
                downloadBtn.setVisible(true);
            } else {
                downloadLink.setVisible(false);
                downloadBtn.setVisible(false);
                showAlert("No payroll data found for the selected period.");
            }
        } else {
            downloadLink.setVisible(false);
            downloadBtn.setVisible(false);
        }
    }

    @FXML
    void handleDownloadPayslip() {
        if (selectedPeriod == null || selectedPeriod.isEmpty()) {
            showAlert("Please select a month first!");
            return;
        }

        try {
            String filename = "payslips/" + employeeId + "_" + selectedPeriod + "_payslip.pdf";
            File pdfFile = new File(filename);

            generatePayslipPDF(pdfFile);

            if (pdfFile.exists()) {
                Desktop.getDesktop().open(pdfFile);
                Stage stage = (Stage) downloadBtn.getScene().getWindow();
                stage.setFullScreen(true);
                stage.show();
                showAlert("Payslip generated and opened successfully!");
            } else {
                showAlert("Error: PDF file was not created.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error generating PDF file: " + e.getMessage());
        }
    }

    private void generatePayslipPDF(File file) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4, 50, 50, 80, 50);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

        java.net.URL jopayLogo = getClass().getResource("/com/example/jopay/logo.png");
        Image photo = Image.getInstance(jopayLogo);
        photo.scaleToFit(100, 120);
        photo.setAlignment(Element.ALIGN_CENTER);
        document.add(photo);
        document.add(new Paragraph("\n", normalFont));

        Paragraph empName = new Paragraph("Employee Name: " + employeeName, normalFont);
        empName.setSpacingAfter(5);
        document.add(empName);

        Paragraph empId = new Paragraph("Employee ID: " + employeeId, normalFont);
        empId.setSpacingAfter(5);
        document.add(empId);

        Paragraph period = new Paragraph("Period: " + selectedPeriod, normalFont);
        period.setSpacingAfter(5);
        document.add(period);

        Paragraph empStatus = new Paragraph("Employment Status: " + employmentStatus, normalFont);
        empStatus.setSpacingAfter(15);
        document.add(empStatus);

        // Main table
        PdfPTable mainTable = new PdfPTable(4);
        mainTable.setWidthPercentage(100);
        float[] columnWidths = {35f, 15f, 35f, 15f};
        mainTable.setWidths(columnWidths);

        addCell(mainTable, "Basic Pay", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(basicPay), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);
        addCell(mainTable, "Undertime (" + String.format("%.1f hrs)", undertimeHours), normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(undertime), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);

        // Undertime & Overtime
        addCell(mainTable, "", normalFont, Rectangle.BOX);
        addCell(mainTable, "", normalFont, Rectangle.BOX);
        addCell(mainTable, "Overtime (" + String.format("%.1f hrs)", overtimeHours), normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(overtime), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);

        addCell(mainTable, "Allowance and De Minimis", boldFont, Rectangle.BOX);
        addCell(mainTable, "", normalFont, Rectangle.BOX);
        addCell(mainTable, "Deductions", boldFont, Rectangle.BOX);
        addCell(mainTable, "", normalFont, Rectangle.BOX);

        // Allowances and Deductions
        addCell(mainTable, "Telecom", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(telecom), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);
        addCell(mainTable, "SSS Contributions", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(sssContributions), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);

        addCell(mainTable, "Travel", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(travel), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);
        addCell(mainTable, "PHIC Contributions", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(phicContributions), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);

        addCell(mainTable, "Rice subsidy", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(riceSubsidy), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);
        addCell(mainTable, "HDMF Contributions", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(hdmfContributions), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);

        addCell(mainTable, "Non-taxable salary", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(nonTaxableSalary), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);
        addCell(mainTable, "SSS Loan", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(sssLoan), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);

        addCell(mainTable, "Per Diem", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(perDiem*perDiemCount), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);
        addCell(mainTable, "Absences (" + numAbsences + " days)", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(absences), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);

        // Taxable Income and Withholding Tax
        addCell(mainTable, "", normalFont, Rectangle.BOX);
        addCell(mainTable, "", normalFont, Rectangle.BOX);
        addCell(mainTable, "Taxable Income", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(taxableIncome), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);

        addCell(mainTable, "", normalFont, Rectangle.BOX);
        addCell(mainTable, "", normalFont, Rectangle.BOX);
        addCell(mainTable, "Withholding Tax", normalFont, Rectangle.BOX);
        addCell(mainTable, formatAmount(withholdingTax), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);

        document.add(mainTable);

        // Summary table
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(20);
        float[] summaryWidths = {75f, 25f};
        summaryTable.setWidths(summaryWidths);

        addCell(summaryTable, "Gross Pay", boldFont, Rectangle.BOX);
        addCell(summaryTable, formatAmount(grossPay), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);

        addCell(summaryTable, "Total Deductions & Taxes", boldFont, Rectangle.BOX);
        addCell(summaryTable, formatAmount(totalDeductions), boldFont, Rectangle.BOX, Element.ALIGN_RIGHT);

        PdfPCell netPayLabel = new PdfPCell(new Phrase("Net Pay", boldFont));
        netPayLabel.setPadding(5);
        netPayLabel.setBorder(Rectangle.BOX);
        summaryTable.addCell(netPayLabel);

        Font redFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.RED);
        PdfPCell netPayAmount = new PdfPCell(new Phrase(formatAmount(netPay), redFont));
        netPayAmount.setPadding(5);
        netPayAmount.setBorder(Rectangle.BOX);
        netPayAmount.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summaryTable.addCell(netPayAmount);

        document.add(summaryTable);
        document.close();
    }

    private void addCell(PdfPTable table, String text, Font font, int border) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setBorder(border);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font, int border, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setBorder(border);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }

    private String formatAmount(double amount) {
        if (amount == 0.00) {
            return "0.00";
        }
        return String.format("%,.2f", amount);
    }

    @FXML
    void handleUpdatePassword() throws IOException {
        FXMLLoader updatePasswordLoader = new FXMLLoader(getClass().getResource("updatepassword.fxml"));
        Stage stage = (Stage) updatePasswordLink.getScene().getWindow();
        Parent root = updatePasswordLoader.load();
        stage.getScene().setRoot(root);
    }

    @FXML
    void handleLogout() throws IOException {
        FXMLLoader employeeLoginLoader = new FXMLLoader(getClass().getResource("employeelogin.fxml"));
        Stage stage = (Stage) logoutLink.getScene().getWindow();
        Parent root = employeeLoginLoader.load();
        stage.getScene().setRoot(root);
    }

    @FXML
    void handleLogoutImageClick() throws IOException {
        FXMLLoader employeeLoginLoader = new FXMLLoader(getClass().getResource("employeelogin.fxml"));
        Stage stage = (Stage) logoutImageView.getScene().getWindow();
        Parent root = employeeLoginLoader.load();
        stage.getScene().setRoot(root);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Payslip");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setEmployeeName(String name) {
        this.employeeName = name;
        welcomeLabel.setText("Mabuhay " + name + "!");
    }


    public void setLoggedInEmployeeId(String employeeId) {
        System.out.println("=== setLoggedInEmployeeId called ===");
        System.out.println("Employee ID: " + employeeId);

        try {
            this.loggedInEmployeeId = Integer.parseInt(employeeId);
            System.out.println("Employee ID successfully parsed: " + loggedInEmployeeId);
        } catch (NumberFormatException e) {
            System.err.println("Invalid numeric employee ID: " + employeeId);
            return;
        }

        Platform.runLater(this::loadAttendancePieChart);
    }



    public void loadAttendancePieChart() {
        System.out.println("=== loadAttendancePieChart called ===");
        System.out.println("PieChart is null? " + (employeeAttendancePieChart == null));
        System.out.println("Employee ID: " + loggedInEmployeeId);

        if (employeeAttendancePieChart == null) {
            System.out.println("ERROR: PieChart node is null!");
            return;
        }

        if (loggedInEmployeeId > 0) {
            EmployeeDAO employeeDAO = new EmployeeDAO();
            Map<String, Integer> summary = employeeDAO.getEmployeeAttendanceSummary(loggedInEmployeeId);

            System.out.println("Attendance summary size: " + summary.size());
            System.out.println("Attendance summary contents: " + summary);

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

            if (summary.isEmpty()) {
                System.out.println("WARNING: No attendance data found.");
                pieData.add(new PieChart.Data("No Data", 1));
            } else {
                summary.forEach((status, count) -> {
                    System.out.println("Adding to pie chart: " + status + " = " + count);
                    pieData.add(new PieChart.Data(status + " (" + count + ")", count));
                });
            }

            employeeAttendancePieChart.setData(pieData);
            employeeAttendancePieChart.setTitle("Your Attendance Summary");
        } else {
            System.out.println("ERROR: Employee ID not set or invalid!");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // *** NEW INNER CLASS: PayslipPeriodItem ***
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Wrapper class for displaying period items in the ComboBox
     * This allows us to show the period name but store the full period object
     */
    public static class PayslipPeriodItem {
        private PayrollDAO.PayrollPeriod period;

        public PayslipPeriodItem(PayrollDAO.PayrollPeriod period) {
            this.period = period;
        }

        public PayrollDAO.PayrollPeriod getPeriod() {
            return period;
        }

        @Override
        public String toString() {
            // This is what displays in the ComboBox dropdown
            return period.periodName;
        }
    }
}