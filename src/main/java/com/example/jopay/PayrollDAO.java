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

    /**
     * Get salary configuration for employee
     */
    public SalaryConfig getSalaryConfig(String employeeId) {
        String query = """
            SELECT employee_Id, basic_Pay, telecom_Allowance, travel_Allowance,
                   rice_Subsidy, non_Taxable_Salary, per_Diem, per_Diem_Count,
                   starting_Date, end_Date
            FROM salary_config
            WHERE employee_Id = ?
            ORDER BY salaray_infoId DESC
            LIMIT 1
        """;

        try (PreparedStatement stmt = connect.getConnection().prepareStatement(query)) {
            stmt.setString(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                SalaryConfig config = new SalaryConfig();
                config.basicPay = rs.getDouble("basic_Pay");
                config.telecomAllowance = rs.getDouble("telecom_Allowance");
                config.travelAllowance = rs.getDouble("travel_Allowance");
                config.riceSubsidy = rs.getDouble("rice_Subsidy");
                config.nonTaxableSalary = rs.getDouble("non_Taxable_Salary");
                config.perDiem = rs.getDouble("per_Diem");
                config.perDiemCount = rs.getInt("per_Diem_Count");
                return config;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching salary config: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get attendance data for payroll period from time_log
     */
    public AttendanceData getAttendanceData(String employeeId, LocalDate startDate, LocalDate endDate) {
        String query = """
            SELECT 
                COALESCE(SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END), 0) as days_worked,
                COALESCE(SUM(CASE WHEN status = 'Absent' THEN 1 ELSE 0 END), 0) as days_absent,
                COALESCE(SUM(CASE WHEN total_hours > 8 THEN total_hours - 8 ELSE 0 END), 0) as regular_ot_hours
            FROM time_log
            WHERE employee_Id = ? 
            AND log_date BETWEEN ? AND ?
        """;

        try (PreparedStatement stmt = connect.getConnection().prepareStatement(query)) {
            stmt.setString(1, employeeId);
            stmt.setDate(2, java.sql.Date.valueOf(startDate));
            stmt.setDate(3, java.sql.Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                AttendanceData data = new AttendanceData();
                data.daysWorked = rs.getInt("days_worked");
                data.daysAbsent = rs.getInt("days_absent");
                data.regularOTHours = rs.getDouble("regular_ot_hours");
                data.nightDifferentialOTHours = 0.0;
                data.specialHolidaysWorked = 0;
                data.regularHolidaysWorked = 0;
                data.restDaysWorked = 0;
                data.restDayOTHours = 0.0;
                data.restDayNightDiffOTHours = 0.0;
                data.undertimeHours = 0.0;
                return data;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching attendance data: " + e.getMessage());
            e.printStackTrace();
        }
        return new AttendanceData();
    }

    public DeductionData getDeductions(String employeeId) {
        String query = """
        SELECT 
            COALESCE(sss_Contribution, 0) as sss_contrib,
            COALESCE(phic_Contribution, 0) as phic_contrib,
            COALESCE(hdmf_Contribution, 0) as hdmf_contrib,
            COALESCE(sss_Loan, 0) as sss_loan
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
    public boolean updateDeductionContributions(String employeeId, double monthlySSS,
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
    }

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
        public double basicPay;
        public double telecomAllowance;
        public double travelAllowance;
        public double riceSubsidy;
        public double nonTaxableSalary;
        public double perDiem;
        public int perDiemCount;
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