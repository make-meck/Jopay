package com.example.jopay;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PayrollDAO {
    private DatabaseConnector connect = new DatabaseConnector();

    /**
     * Get complete employee information for PayrollModel
     */
    public EmployeeInfo getEmployeeInfo(String employeeId) {
        String query = """
            SELECT e.employee_Id, 
                   CONCAT(e.employee_FirstName, ' ', e.employee_LastName) AS employee_name,
                   e.employment_Status,
                   e.date_Hired,
                   e.basic_Salary
            FROM employee_info e
            WHERE e.employee_Id = ? AND e.is_Active = 1
        """;

        try (PreparedStatement stmt = connect.getConnection().prepareStatement(query)) {
            stmt.setString(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                EmployeeInfo info = new EmployeeInfo();
                info.employeeId = rs.getString("employee_Id");
                info.employeeName = rs.getString("employee_name");
                info.employmentStatus = rs.getString("employment_Status");
                info.dateHired = rs.getDate("date_Hired").toLocalDate();
                info.basicMonthlyPay = rs.getDouble("basic_Salary");
                info.workingHoursPerDay = 8; // Default
                return info;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching employee info: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public SalaryConfig getSalaryConfig(String employeeId) {
        SalaryConfig config = null;
        String query = "SELECT basic_Pay, telecom_Allowance, travel_allowance, rice_Subsidy, " +
                "non_Taxable_Salary, per_Diem, per_Diem_Count, " +
                "sss_Contribution, philc_contribution, hdmf_Contribution " +
                "FROM salary_config WHERE employee_id = ?";

        System.out.println("=== DEBUG getSalaryConfig ===");
        System.out.println("Searching for employee ID: " + employeeId);

        try (PreparedStatement pstmt = connect.prepareStatement(query)) {
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("✓ Found salary config!");
                config = new SalaryConfig();
                config.basicPay = rs.getDouble("basic_Pay");
                config.telecomAllowance = rs.getDouble("telecom_Allowance");
                config.travelAllowance = rs.getDouble("travel_allowance");
                config.riceSubsidy = rs.getDouble("rice_Subsidy");
                config.nonTaxableSalary = rs.getDouble("non_Taxable_Salary");
                config.perDiem = rs.getDouble("per_Diem");
                config.perDiemCount = rs.getInt("per_Diem_Count");

                config.sssContribution = rs.getDouble("sss_Contribution");
                config.phicContribution = rs.getDouble("philc_contribution");
                config.hdmfContribution = rs.getDouble("hdmf_Contribution");

                System.out.println("Basic Pay: " + config.basicPay);
                System.out.println("SSS: " + config.sssContribution);
                System.out.println("PHIC: " + config.phicContribution);
                System.out.println("HDMF: " + config.hdmfContribution);
            } else {
                System.out.println("✗ No salary config found for employee: " + employeeId);
            }
        } catch (SQLException e) {
            System.err.println("Error getting salary config: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("============================\n");
        return config;
    }

    public AttendanceData getAttendanceData(String employeeId, LocalDate startDate, LocalDate endDate) {
        AttendanceData data = new AttendanceData();

        // Query to count absences from time_log where status != 'Present'
        String absenceQuery = "SELECT COUNT(*) as absence_count " +
                "FROM time_log " +
                "WHERE employee_id = ? " +
                "AND log_date BETWEEN ? AND ? " +
                "AND status != 'Present'";

        // Query to count present days
        String presentQuery = "SELECT COUNT(*) as present_count " +
                "FROM time_log " +
                "WHERE employee_id = ? " +
                "AND log_date BETWEEN ? AND ? " +
                "AND status = 'Present'";

        // Query to calculate total hours worked
        String hoursQuery = "SELECT SUM(total_hours) as total_hours " +
                "FROM time_log " +
                "WHERE employee_id = ? " +
                "AND log_date BETWEEN ? AND ? " +
                "AND status = 'Present'";

        try {
            // Get absence count
            PreparedStatement absenceStmt = connect.prepareStatement(absenceQuery);
            absenceStmt.setString(1, employeeId);
            absenceStmt.setDate(2, java.sql.Date.valueOf(startDate));
            absenceStmt.setDate(3, java.sql.Date.valueOf(endDate));
            ResultSet absenceRs = absenceStmt.executeQuery();

            if (absenceRs.next()) {
                data.daysAbsent = absenceRs.getInt("absence_count");
            }

            // Get present days count
            PreparedStatement presentStmt = connect.prepareStatement(presentQuery);
            presentStmt.setString(1, employeeId);
            presentStmt.setDate(2, java.sql.Date.valueOf(startDate));
            presentStmt.setDate(3, java.sql.Date.valueOf(endDate));
            ResultSet presentRs = presentStmt.executeQuery();

            if (presentRs.next()) {
                data.daysWorked = presentRs.getInt("present_count");
            }

            // Get total hours worked
            PreparedStatement hoursStmt = connect.prepareStatement(hoursQuery);
            hoursStmt.setString(1, employeeId);
            hoursStmt.setDate(2, java.sql.Date.valueOf(startDate));
            hoursStmt.setDate(3, java.sql.Date.valueOf(endDate));
            ResultSet hoursRs = hoursStmt.executeQuery();

            double totalHours = 0;
            if (hoursRs.next()) {
                totalHours = hoursRs.getDouble("total_hours");
                // Calculate regular OT (hours beyond 8 per day)
                data.regularOTHours = Math.max(0, totalHours - (data.daysWorked * 8));
            }

            // Initialize other fields with default values
            data.nightDifferentialOTHours = 0.0;
            data.specialHolidaysWorked = 0;
            data.regularHolidaysWorked = 0;
            data.restDaysWorked = 0;
            data.restDayOTHours = 0.0;
            data.restDayNightDiffOTHours = 0.0;
            data.undertimeHours = 0.0;

            System.out.println("\n=== ATTENDANCE DATA FROM time_log ===");
            System.out.println("Employee: " + employeeId);
            System.out.println("Period: " + startDate + " to " + endDate);
            System.out.println("Days Present: " + data.daysWorked);
            System.out.println("Days Absent: " + data.daysAbsent);
            System.out.println("Total Hours: " + totalHours);
            System.out.println("Regular OT Hours: " + data.regularOTHours);
            System.out.println("====================================\n");

            absenceRs.close();
            presentRs.close();
            hoursRs.close();
            absenceStmt.close();
            presentStmt.close();
            hoursStmt.close();

        } catch (SQLException e) {
            System.err.println("Error getting attendance data: " + e.getMessage());
            e.printStackTrace();
        }

        return data;
    }

    /**
     * Get absence information for display (current/recent month)
     */
    public AbsenceInfo getRecentAbsenceInfo(String employeeId) {
        AbsenceInfo info = new AbsenceInfo();

        // Get absences for current month
        String query = "SELECT COUNT(*) as absence_count " +
                "FROM time_log " +
                "WHERE employee_id = ? " +
                "AND MONTH(log_date) = MONTH(CURDATE()) " +
                "AND YEAR(log_date) = YEAR(CURDATE()) " +
                "AND (status IS NULL OR status != 'Present')";

        try (PreparedStatement pstmt = connect.prepareStatement(query)) {
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                info.absenceCount = rs.getInt("absence_count");
            }

        } catch (SQLException e) {
            System.err.println("Error getting recent absence info: " + e.getMessage());
            e.printStackTrace();
        }

        return info;
    }

    /**
     * Inner class to hold absence information
     */
    public static class AbsenceInfo {
        public int absenceCount;
    }

    /**
     * Get total number of absences for an employee (all time or for a specific period)
     */
    public int getTotalAbsences(String employeeId) {
        int absenceCount = 0;

        // Query to count all absences where status is NOT 'Present'
        String query = "SELECT COUNT(*) as absence_count " +
                "FROM time_log " +
                "WHERE employee_id = ? " +
                "AND (status IS NULL OR status != 'Present')";

        try (PreparedStatement pstmt = connect.prepareStatement(query)) {
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                absenceCount = rs.getInt("absence_count");
            }

            System.out.println("Absences found for employee " + employeeId + ": " + absenceCount);

        } catch (SQLException e) {
            System.err.println("Error getting total absences: " + e.getMessage());
            e.printStackTrace();
        }

        return absenceCount;
    }

    /**
     * Get absences for a specific period (for payroll computation)
     */
    public int getAbsencesForPeriod(String employeeId, LocalDate startDate, LocalDate endDate) {
        int absenceCount = 0;

        String query = "SELECT COUNT(*) as absence_count " +
                "FROM time_log " +
                "WHERE employee_id = ? " +
                "AND log_date BETWEEN ? AND ? " +
                "AND (status IS NULL OR status != 'Present')";

        try (PreparedStatement pstmt = connect.prepareStatement(query)) {
            pstmt.setString(1, employeeId);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));
            pstmt.setDate(3, java.sql.Date.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                absenceCount = rs.getInt("absence_count");
            }

            System.out.println("Absences for period " + startDate + " to " + endDate + ": " + absenceCount);

        } catch (SQLException e) {
            System.err.println("Error getting absences for period: " + e.getMessage());
            e.printStackTrace();
        }

        return absenceCount;
    }

    /**
     * Automatically compute and save SSS, PHIC, HDMF contributions for an employee
     * This should be called whenever employee basic salary is updated
     */
    public boolean autoComputeAndSaveContributions(String employeeId, double basicMonthlySalary) {
        try {
            // Create a temporary PayrollModel instance to use its computation methods
            PayrollModel payrollModel = new PayrollModel();
            payrollModel.PayrollComputation(employeeId, "", basicMonthlySalary, "", null, 8);

            // Compute contributions using PayrollModel
            double monthlySSS = computeSSSMonthly(basicMonthlySalary);
            double monthlyPHIC = computePHICMonthly(basicMonthlySalary);
            double hdmfContribution = 200.00; // Fixed HDMF amount

            // Convert to semi-monthly
            double semiMonthlySSS = monthlySSS / 2;
            double semiMonthlyPHIC = monthlyPHIC / 2;

            // Save to database
            String query = """
            INSERT INTO contribution_config 
            (employee_Id, basic_monthly_salary, sss_contribution, phic_contribution, hdmf_contribution)
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            basic_monthly_salary = VALUES(basic_monthly_salary),
            sss_contribution = VALUES(sss_contribution),
            phic_contribution = VALUES(phic_contribution),
            hdmf_contribution = VALUES(hdmf_contribution),
            computed_date = CURRENT_TIMESTAMP
        """;

            try (PreparedStatement stmt = connect.getConnection().prepareStatement(query)) {
                stmt.setString(1, employeeId);
                stmt.setDouble(2, basicMonthlySalary);
                stmt.setDouble(3, semiMonthlySSS);
                stmt.setDouble(4, semiMonthlyPHIC);
                stmt.setDouble(5, hdmfContribution);

                int result = stmt.executeUpdate();

                System.out.println("Auto-computed contributions for employee " + employeeId);
                System.out.println("Basic Monthly Salary: ₱" + String.format("%,.2f", basicMonthlySalary));
                System.out.println("SSS (semi-monthly): ₱" + String.format("%,.2f", semiMonthlySSS));
                System.out.println("PHIC (semi-monthly): ₱" + String.format("%,.2f", semiMonthlyPHIC));
                System.out.println("HDMF (semi-monthly): ₱" + String.format("%,.2f", hdmfContribution));

                return result > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error auto-computing contributions: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Compute monthly SSS contribution using PayrollModel's logic
     */
    private double computeSSSMonthly(double basicMonthlySalary) {
        // Use the same SSS table logic from PayrollModel
        return getSSSEmployeeShare(basicMonthlySalary);
    }

    /**
     * Compute monthly PHIC contribution using PayrollModel's logic
     */
    private double computePHICMonthly(double basicMonthlySalary) {
        final double PHIC_RATE = 0.025;
        final double PHIC_MIN = 500.00;
        final double PHIC_MAX = 5000.00;

        double monthlyContribution;

        if (basicMonthlySalary < 10000) {
            monthlyContribution = PHIC_MIN;
        } else if (basicMonthlySalary > 100000) {
            monthlyContribution = PHIC_MAX;
        } else {
            // Employee share is half of total contribution
            monthlyContribution = (basicMonthlySalary * PHIC_RATE) / 2;
        }

        return monthlyContribution;
    }

    /**
     * SSS contribution table (matches PayrollModel)
     */
    private double getSSSEmployeeShare(double monthlySalary) {
        if (monthlySalary < 5250) return 250.00;
        else if (monthlySalary >= 5250 && monthlySalary <= 5749.99) return 275.00;
        else if (monthlySalary >= 5750 && monthlySalary <= 6249.99) return 300.00;
        else if (monthlySalary >= 6250 && monthlySalary <= 6749.99) return 325.00;
        else if (monthlySalary >= 6750 && monthlySalary <= 7249.99) return 350.00;
        else if (monthlySalary >= 7250 && monthlySalary <= 7749.99) return 375.00;
        else if (monthlySalary >= 7750 && monthlySalary <= 8249.99) return 400.00;
        else if (monthlySalary >= 8250 && monthlySalary <= 8749.99) return 425.00;
        else if (monthlySalary >= 8750 && monthlySalary <= 9249.99) return 450.00;
        else if (monthlySalary >= 9250 && monthlySalary <= 9749.99) return 475.00;
        else if (monthlySalary >= 9750 && monthlySalary <= 10249.99) return 500.00;
        else if (monthlySalary >= 10250 && monthlySalary <= 10749.99) return 525.00;
        else if (monthlySalary >= 10750 && monthlySalary <= 11249.99) return 550.00;
        else if (monthlySalary >= 11250 && monthlySalary <= 11749.99) return 575.00;
        else if (monthlySalary >= 11750 && monthlySalary <= 12249.99) return 600.00;
        else if (monthlySalary >= 12250 && monthlySalary <= 12749.99) return 625.00;
        else if (monthlySalary >= 12750 && monthlySalary <= 13249.99) return 650.00;
        else if (monthlySalary >= 13250 && monthlySalary <= 13749.99) return 675.00;
        else if (monthlySalary >= 13750 && monthlySalary <= 14249.99) return 700.00;
        else if (monthlySalary >= 14250 && monthlySalary <= 14749.99) return 725.00;
        else if (monthlySalary >= 14750 && monthlySalary <= 15249.99) return 750.00;
        else if (monthlySalary >= 15250 && monthlySalary <= 15749.99) return 775.00;
        else if (monthlySalary >= 15750 && monthlySalary <= 16249.99) return 800.00;
        else if (monthlySalary >= 16250 && monthlySalary <= 16749.99) return 825.00;
        else if (monthlySalary >= 16750 && monthlySalary <= 17249.99) return 850.00;
        else if (monthlySalary >= 17250 && monthlySalary <= 17749.99) return 875.00;
        else if (monthlySalary >= 17750 && monthlySalary <= 18249.99) return 900.00;
        else if (monthlySalary >= 18250 && monthlySalary <= 18749.99) return 925.00;
        else if (monthlySalary >= 18750 && monthlySalary <= 19249.99) return 950.00;
        else if (monthlySalary >= 19250 && monthlySalary <= 19749.99) return 975.00;
        else if (monthlySalary >= 19750 && monthlySalary <= 20249.99) return 1000.00;
        else if (monthlySalary >= 20250 && monthlySalary <= 20749.99) return 1025.00;
        else if (monthlySalary >= 20750 && monthlySalary <= 21249.99) return 1050.00;
        else if (monthlySalary >= 21250 && monthlySalary <= 21749.99) return 1075.00;
        else if (monthlySalary >= 21750 && monthlySalary <= 22249.99) return 1100.00;
        else if (monthlySalary >= 22250 && monthlySalary <= 22749.99) return 1125.00;
        else if (monthlySalary >= 22750 && monthlySalary <= 23249.99) return 1150.00;
        else if (monthlySalary >= 23250 && monthlySalary <= 23749.99) return 1175.00;
        else if (monthlySalary >= 23750 && monthlySalary <= 24249.99) return 1200.00;
        else if (monthlySalary >= 24250 && monthlySalary <= 24749.99) return 1225.00;
        else if (monthlySalary >= 24750 && monthlySalary <= 25249.99) return 1250.00;
        else if (monthlySalary >= 25250 && monthlySalary <= 25749.99) return 1275.00;
        else if (monthlySalary >= 25750 && monthlySalary <= 26249.99) return 1300.00;
        else if (monthlySalary >= 26250 && monthlySalary <= 26749.99) return 1325.00;
        else if (monthlySalary >= 26750 && monthlySalary <= 27249.99) return 1350.00;
        else if (monthlySalary >= 27250 && monthlySalary <= 27749.99) return 1375.00;
        else if (monthlySalary >= 27750 && monthlySalary <= 28249.99) return 1400.00;
        else if (monthlySalary >= 28250 && monthlySalary <= 28749.99) return 1425.00;
        else if (monthlySalary >= 28750 && monthlySalary <= 29249.99) return 1450.00;
        else if (monthlySalary >= 29250 && monthlySalary <= 29749.99) return 1475.00;
        else if (monthlySalary >= 29750 && monthlySalary <= 30249.99) return 1500.00;
        else if (monthlySalary >= 30250 && monthlySalary <= 30749.99) return 1525.00;
        else if (monthlySalary >= 30750 && monthlySalary <= 31249.99) return 1550.00;
        else if (monthlySalary >= 31250 && monthlySalary <= 31749.99) return 1575.00;
        else if (monthlySalary >= 31750 && monthlySalary <= 32249.99) return 1600.00;
        else if (monthlySalary >= 32250 && monthlySalary <= 32749.99) return 1625.00;
        else if (monthlySalary >= 32750 && monthlySalary <= 33249.99) return 1650.00;
        else if (monthlySalary >= 33250 && monthlySalary <= 33749.99) return 1675.00;
        else if (monthlySalary >= 33750 && monthlySalary <= 34249.99) return 1700.00;
        else if (monthlySalary >= 34250 && monthlySalary <= 34749.99) return 1725.00;
        else return 1800.00; // Maximum
    }

    /**
     * Get stored contributions from database
     */
    public ContributionData getContributions(String employeeId) {
        String query = """
        SELECT employee_Id, basic_monthly_salary, sss_contribution, 
               phic_contribution, hdmf_contribution, computed_date
        FROM contribution_config
        WHERE employee_Id = ?
    """;

        try (PreparedStatement stmt = connect.getConnection().prepareStatement(query)) {
            stmt.setString(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ContributionData data = new ContributionData();
                data.employeeId = rs.getString("employee_Id");
                data.basicMonthlySalary = rs.getDouble("basic_monthly_salary");
                data.sssContribution = rs.getDouble("sss_contribution");
                data.phicContribution = rs.getDouble("phic_contribution");
                data.hdmfContribution = rs.getDouble("hdmf_contribution");
                data.computedDate = rs.getTimestamp("computed_date").toLocalDateTime();
                return data;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching contributions: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Recalculate contributions for all employees
     * Call this when salary changes or periodically
     */
    public void recalculateAllContributions() {
        String query = "SELECT employee_Id, basic_Salary FROM employee_info WHERE is_Active = 1";

        try (PreparedStatement stmt = connect.getConnection().prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            int count = 0;
            while (rs.next()) {
                String empId = rs.getString("employee_Id");
                double basicSalary = rs.getDouble("basic_Salary");

                if (autoComputeAndSaveContributions(empId, basicSalary)) {
                    count++;
                }
            }

            System.out.println("Recalculated contributions for " + count + " employees");

        } catch (SQLException e) {
            System.err.println("Error recalculating contributions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper method to load pre-computed contributions into PayrollModel
     * Call this before computePayroll() to use database values
     */
    public void loadContributionsIntoPayrollModel(String employeeId, PayrollModel payrollModel, boolean isFirstHalf) {
        ContributionData data = getContributions(employeeId);

        if (data != null) {
            // For HDMF, only deduct on first half
            double hdmf = isFirstHalf ? data.hdmfContribution : 0.0;

            payrollModel.setPreComputedContributions(
                    data.sssContribution,
                    data.phicContribution,
                    hdmf
            );

            System.out.println("Loaded pre-computed contributions from database for employee: " + employeeId);
        } else {
            System.out.println("No pre-computed contributions found for employee: " + employeeId +
                    ". PayrollModel will use formula calculation.");
        }
    }

    // Add to existing helper classes
    public static class ContributionData {
        public String employeeId;
        public double basicMonthlySalary;
        public double sssContribution;
        public double phicContribution;
        public double hdmfContribution;
        public java.time.LocalDateTime computedDate;
    }

    public DeductionData getDeductions(String employeeId) {
        // First, check what columns actually exist
        String query = """
        SELECT 
            COALESCE(sss_contribution, 0) as sss_contrib,
            COALESCE(phic_contribution, 0) as phic_contrib,
            COALESCE(hdmf_contribution, 0) as hdmf_contrib,
            COALESCE(sss_loan, 0) as sss_loan
        FROM deduction_config
        WHERE employee_Id = ?
        ORDER BY deduction_configID DESC
        LIMIT 1
    """;

        try (PreparedStatement stmt = connect.getConnection().prepareStatement(query)) {
            stmt.setString(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                DeductionData data = new DeductionData();
                data.sssLoan = rs.getDouble("sss_loan");
                data.sssContribution = rs.getDouble("sss_contrib");
                data.phicContribution = rs.getDouble("phic_contrib");
                data.hdmfContribution = rs.getDouble("hdmf_contrib");
                return data;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching deductions: " + e.getMessage());
            e.printStackTrace();
        }
        return new DeductionData();
    }



    /**
     * Save or update salary configuration
     */
    /**
     * Save salary config AND automatically compute and update contributions
     */
    public boolean saveSalaryConfig(String employeeId, double telecom, double travel,
                                    double rice, double nonTaxable, double perDiem,
                                    int perDiemCount, LocalDate startDate, LocalDate endDate) {

        // First, get employee info to compute contributions
        EmployeeInfo empInfo = getEmployeeInfo(employeeId);
        if (empInfo == null) {
            System.err.println("Cannot save salary config: Employee not found");
            return false;
        }

        // Compute contributions using PayrollModel
        PayrollModel tempModel = new PayrollModel();
        tempModel.PayrollComputation(
                empInfo.employeeId,
                empInfo.employeeName,
                empInfo.basicMonthlyPay,
                empInfo.employmentStatus,
                empInfo.dateHired,
                empInfo.workingHoursPerDay
        );

        // Set first period to get HDMF contribution
        tempModel.setPayrollPeriod(LocalDate.now(), LocalDate.now(), true);
        tempModel.setAllowances(0, 0, 0, 0, 0, 0);
        tempModel.setDeductions(0);
        tempModel.computePayroll();

        // Get monthly contributions (semi-monthly * 2)
        double monthlySSS = tempModel.getSSSContribution() * 2;
        double monthlyPHIC = tempModel.getPHICContribution() * 2;
        double monthlyHDMF = tempModel.getHDMFContribution(); // Already monthly

        String query = """
            INSERT INTO salary_config 
            (employee_Id, basic_Pay, telecom_Allowance, travel_Allowance, rice_Subsidy, 
             non_Taxable_Salary, per_Diem, per_Diem_Count, starting_Date, end_Date)
            SELECT ?, basic_Salary, ?, ?, ?, ?, ?, ?, ?, ?
            FROM employee_info
            WHERE employee_Id = ?
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

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = connect.getConnection();
            conn.setAutoCommit(false);

            // Save salary config with contributions
            stmt = conn.prepareStatement(query);
            stmt.setString(1, employeeId);
            stmt.setDouble(2, telecom);
            stmt.setDouble(3, travel);
            stmt.setDouble(4, rice);
            stmt.setDouble(5, nonTaxable);
            stmt.setDouble(6, perDiem);
            stmt.setInt(7, perDiemCount);
            stmt.setDate(8, java.sql.Date.valueOf(startDate));
            stmt.setDate(9, java.sql.Date.valueOf(endDate));
            stmt.setDouble(10, monthlySSS);
            stmt.setDouble(11, monthlyPHIC);
            stmt.setDouble(12, monthlyHDMF);
            stmt.setString(13, employeeId);

            int result = stmt.executeUpdate();

            if (result > 0) {
                // Also update deduction_config table
                String deductionQuery = """
                    INSERT INTO deduction_config 
                    (employee_Id, sss_Contribution, phic_Contribution, hdmf_Contribution, sss_Loan, starting_Date, end_Date)
                    VALUES (?, ?, ?, ?, 0.00, ?, ?)
                    ON DUPLICATE KEY UPDATE
                    sss_Contribution = VALUES(sss_Contribution),
                    phic_Contribution = VALUES(phic_Contribution),
                    hdmf_Contribution = VALUES(hdmf_Contribution),
                    starting_Date = VALUES(starting_Date),
                    end_Date = VALUES(end_Date)
                """;

                PreparedStatement deductStmt = conn.prepareStatement(deductionQuery);
                deductStmt.setString(1, employeeId);
                deductStmt.setDouble(2, monthlySSS);
                deductStmt.setDouble(3, monthlyPHIC);
                deductStmt.setDouble(4, monthlyHDMF);
                deductStmt.setDate(5, java.sql.Date.valueOf(startDate));
                deductStmt.setDate(6, java.sql.Date.valueOf(endDate));

                int deductResult = deductStmt.executeUpdate();
                deductStmt.close();

                if (deductResult > 0) {
                    conn.commit();
                    System.out.println("✓ Salary config saved with computed contributions:");
                    System.out.println("  Employee: " + empInfo.employeeName);
                    System.out.println("  Basic Salary: ₱" + String.format("%,.2f", empInfo.basicMonthlyPay));
                    System.out.println("  SSS (monthly): ₱" + String.format("%,.2f", monthlySSS));
                    System.out.println("  PHIC (monthly): ₱" + String.format("%,.2f", monthlyPHIC));
                    System.out.println("  HDMF (monthly): ₱" + String.format("%,.2f", monthlyHDMF));
                    return true;
                } else {
                    conn.rollback();
                    System.err.println("Failed to update deduction_config");
                    return false;
                }
            } else {
                conn.rollback();
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error saving salary config: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Update deduction_config with monthly contributions
     */
    /*public boolean updateDeductionContributions(String employeeId, double monthlySSS,
                                                double monthlyPHIC, double monthlyHDMF) {
        String query = """
        INSERT INTO deduction_config
        (employee_Id, sss_Contribution, phic_Contribution, hdmf_Contribution, sss_loan)
        VALUES (?, ?, ?, ?, 0.00)
        ON DUPLICATE KEY UPDATE
        sss_Contribution = VALUES(sss_contribution),
        phic_Contribution = VALUES(philhealth_contribution),
        hdmf_Contribution = VALUES(pagibig_contribution)
    """;

        try (PreparedStatement stmt = connect.getConnection().prepareStatement(query)) {
            stmt.setString(1, employeeId);
            stmt.setDouble(2, monthlySSS);
            stmt.setDouble(3, monthlyPHIC);
            stmt.setDouble(4, monthlyHDMF);

            int result = stmt.executeUpdate();

            if (result > 0) {
                System.out.println("✓ Deduction contributions updated for employee " + employeeId);
                System.out.println("  Monthly SSS: ₱" + String.format("%,.2f", monthlySSS));
                System.out.println("  Monthly PHIC: ₱" + String.format("%,.2f", monthlyPHIC));
                System.out.println("  Monthly HDMF: ₱" + String.format("%,.2f", monthlyHDMF));
            }

            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error updating deduction contributions: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }*/

    /**
     * Save computed payroll to payroll_records table
     */
    public boolean savePayroll(String employeeId, int periodId, PayrollModel model,
                               SalaryConfig config, AttendanceData attendance) {
        String query = """
            INSERT INTO payroll_records 
            (employee_Id, period_id, basic_pay, telecom_Allowance, travel_Allowance,
             rice_Subsidy, non_Taxable_Salary, per_Deim, per_Deim_Count,
             overtime_Pay, overtime_hours, sss_Contribution, phic_contribution,
             hdmf_Contribution, sss_Loan, absences, num_Absences,
             gross_pay, total_Deduction, net_Pay, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'DRAFT')
        """;

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = connect.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(query);

            stmt.setString(1, employeeId);
            stmt.setInt(2, periodId);
            stmt.setDouble(3, model.getSemiMonthlyBasicPay());
            stmt.setDouble(4, config != null ? config.telecomAllowance : 0.0);
            stmt.setDouble(5, config != null ? config.travelAllowance : 0.0);
            stmt.setDouble(6, config != null ? config.riceSubsidy : 0.0);
            stmt.setDouble(7, config != null ? config.nonTaxableSalary : 0.0);
            stmt.setDouble(8, config != null ? config.perDiem : 0.0);
            stmt.setInt(9, config != null ? config.perDiemCount : 0);
            stmt.setDouble(10, attendance.regularOTHours * model.getHourlyRate() * 1.25);
            stmt.setDouble(11, attendance.regularOTHours);
            stmt.setDouble(12, model.getSSSContribution());
            stmt.setDouble(13, model.getPHICContribution());
            stmt.setDouble(14, model.getHDMFContribution());
            stmt.setDouble(15, 0.0); // SSS loan from deduction_config
            stmt.setDouble(16, attendance.daysAbsent * model.getGrossDailyRate());
            stmt.setInt(17, attendance.daysAbsent);
            stmt.setDouble(18, model.getSemiMonthlyGrossPay());
            stmt.setDouble(19, model.getTotalDeductions());
            stmt.setDouble(20, model.getNetPay());

            int result = stmt.executeUpdate();
            conn.commit();

            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error saving payroll: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error rolling back transaction: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("Error closing statement: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Error resetting auto-commit: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Get all active employee IDs
     */
    public List<String> getAllEmployeeIds() {
        List<String> employeeIds = new ArrayList<>();
        String query = "SELECT employee_Id FROM employee_info WHERE is_Active = 1";

        try (PreparedStatement stmt = connect.getConnection().prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                employeeIds.add(rs.getString("employee_Id"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching employee IDs: " + e.getMessage());
            e.printStackTrace();
        }
        return employeeIds;
    }

    /**
     * Get payroll period information
     */
    public PayrollPeriod getPayrollPeriod(int periodId) {
        String query = """
            SELECT period_ID, period_name, start_Date, end_Date, pay_Date, status
            FROM payroll_period
            WHERE period_ID = ?
        """;

        try (PreparedStatement stmt = connect.getConnection().prepareStatement(query)) {
            stmt.setInt(1, periodId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                PayrollPeriod period = new PayrollPeriod();
                period.periodId = rs.getInt("period_ID");
                period.periodName = rs.getString("period_name");
                period.startDate = rs.getDate("start_Date").toLocalDate();
                period.endDate = rs.getDate("end_Date").toLocalDate();
                period.payDate = rs.getDate("pay_Date").toLocalDate();
                period.status = rs.getString("status");
                return period;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching payroll period: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all payroll periods
     */
    public List<PayrollPeriod> getAllPayrollPeriods() {
        List<PayrollPeriod> periods = new ArrayList<>();
        String query = """
            SELECT period_ID, period_name, start_Date, end_Date, pay_Date, status
            FROM payroll_period
            ORDER BY start_Date DESC
        """;

        try (PreparedStatement stmt = connect.getConnection().prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                PayrollPeriod period = new PayrollPeriod();
                period.periodId = rs.getInt("period_ID");
                period.periodName = rs.getString("period_name");
                period.startDate = rs.getDate("start_Date").toLocalDate();
                period.endDate = rs.getDate("end_Date").toLocalDate();
                period.payDate = rs.getDate("pay_Date").toLocalDate();
                period.status = rs.getString("status");
                periods.add(period);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching payroll periods: " + e.getMessage());
            e.printStackTrace();
        }
        return periods;
    }

    /**
     * Get absence count from absences_table
     */
    public int getAbsenceCount(String employeeId, LocalDate startDate, LocalDate endDate) {
        String query = """
            SELECT COUNT(*) as absence_count
            FROM absences_table
            WHERE employee_Id = ? 
            AND absence_Date BETWEEN ? AND ?
        """;

        try (PreparedStatement stmt = connect.getConnection().prepareStatement(query)) {
            stmt.setString(1, employeeId);
            stmt.setDate(2, java.sql.Date.valueOf(startDate));
            stmt.setDate(3, java.sql.Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("absence_count");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching absence count: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get leave data for employee from leave_balance
     * Note: Adjust column names based on your actual schema
     */
    public LeaveData getLeaveData(String employeeId, int year) {
        String query = """
            SELECT 
                total_leave_balance,
                sick_leave_balance,
                vacation_leave_balance
            FROM leave_balance
            WHERE employee_Id = ? AND year = ?
            LIMIT 1
        """;

        try (PreparedStatement stmt = connect.getConnection().prepareStatement(query)) {
            stmt.setString(1, employeeId);
            stmt.setInt(2, year);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                LeaveData data = new LeaveData();
                data.slBalance = rs.getDouble("sick_leave_balance");
                data.vlBalance = rs.getDouble("vacation_leave_balance");
                data.slUsed = 0.0;
                data.vlUsed = 0.0;
                return data;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching leave data: " + e.getMessage());
            System.err.println("Hint: Check your leave_balance table column names");
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        connect.close();
    }

    // Helper classes to organize data
    public static class EmployeeInfo {
        public String employeeId;
        public String employeeName;
        public String employmentStatus;
        public LocalDate dateHired;
        public double basicMonthlyPay;
        public int workingHoursPerDay;
    }

    public static class SalaryConfig {
        public double telecomAllowance;
        public double travelAllowance;
        public double riceSubsidy;
        public double nonTaxableSalary;
        public double perDiem;
        public int perDiemCount;

        public double sssContribution;
        public double phicContribution;
        public double hdmfContribution;

        public double basicPay;
    }

    public static class AttendanceData {
        public int daysWorked;
        public int daysAbsent;
        public double regularOTHours;
        public double nightDifferentialOTHours;
        public int specialHolidaysWorked;
        public int regularHolidaysWorked;
        public int restDaysWorked;
        public double restDayOTHours;
        public double restDayNightDiffOTHours;
        public double undertimeHours;
    }

    public static class LeaveData {
        public double vlUsed;
        public double slUsed;
        public double vlBalance;
        public double slBalance;
    }

    public static class DeductionData {
        public double sssLoan;
        public double sssContribution;
        public double phicContribution;
        public double hdmfContribution;
    }

    public static class PayrollPeriod {
        public int periodId;
        public String periodName;
        public LocalDate startDate;
        public LocalDate endDate;
        public LocalDate payDate;
        public String status;
    }
}