package com.example.jopay;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PayrollDAO {
    private DatabaseConnector connect = new DatabaseConnector();

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
                COALESCE(sss_contribution, 0) as sss_contrib,
                COALESCE(philhealth_contribution, 0) as phic_contrib,
                COALESCE(pagibig_contribution, 0) as hdmf_contrib,
                COALESCE(sss_loan, 0) as sss_loan
            FROM deduction_config
            WHERE employee_Id = ?
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
            System.err.println("Hint: Check your deduction_config table column names");
            e.printStackTrace();
        }
        return new DeductionData();
    }

    public boolean saveSalaryConfig(String employeeId, double telecom, double travel,
                                    double rice, double nonTaxable, double perDiem,
                                    int perDiemCount, LocalDate startDate, LocalDate endDate) {
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

        try (PreparedStatement stmt = connect.getConnection().prepareStatement(query)) {
            stmt.setString(1, employeeId);
            stmt.setDouble(2, telecom);
            stmt.setDouble(3, travel);
            stmt.setDouble(4, rice);
            stmt.setDouble(5, nonTaxable);
            stmt.setDouble(6, perDiem);
            stmt.setInt(7, perDiemCount);
            stmt.setDate(8, java.sql.Date.valueOf(startDate));
            stmt.setDate(9, java.sql.Date.valueOf(endDate));
            stmt.setString(10, employeeId);

            int result = stmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error saving salary config: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public int getOrCreatePayrollPeriod(LocalDate startDate, LocalDate endDate) {
        try {
            // First try to find existing period
            String selectQuery = """
            SELECT period_ID 
            FROM payroll_period 
            WHERE start_Date = ? AND end_Date = ?
            LIMIT 1
        """;

            try (PreparedStatement stmt = connect.getConnection().prepareStatement(selectQuery)) {
                stmt.setDate(1, java.sql.Date.valueOf(startDate));
                stmt.setDate(2, java.sql.Date.valueOf(endDate));
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt("period_ID");
                }
            }

            // If not found, create new period
            String insertQuery = """
            INSERT INTO payroll_period (period_name, start_Date, end_Date, pay_Date, status)
            VALUES (?, ?, ?, ?, 'DRAFT')
        """;

            String periodName = startDate.toString() + " to " + endDate.toString();
            LocalDate payDate = endDate.plusDays(5); // Pay date is 5 days after period end

            try (PreparedStatement stmt = connect.getConnection().prepareStatement(insertQuery,
                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, periodName);
                stmt.setDate(2, java.sql.Date.valueOf(startDate));
                stmt.setDate(3, java.sql.Date.valueOf(endDate));
                stmt.setDate(4, java.sql.Date.valueOf(payDate));

                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

            // If we reach here, something went wrong
            System.err.println("Failed to create or retrieve payroll period");
            return -1; // Return -1 to indicate failure

        } catch (SQLException e) {
            System.err.println("Error in getOrCreatePayrollPeriod: " + e.getMessage());
            e.printStackTrace();
            return -1; // Return -1 to indicate failure
        }
    }

    public boolean updateDeductionContributions(String employeeId, double monthlySSS,
                                                double monthlyPHIC, double monthlyHDMF) {
        String query = """
        INSERT INTO deduction_config 
        (employee_Id, sss_contribution, philhealth_contribution, pagibig_contribution, sss_loan)
        VALUES (?, ?, ?, ?, 0.00)
        ON DUPLICATE KEY UPDATE
        sss_contribution = VALUES(sss_contribution),
        philhealth_contribution = VALUES(philhealth_contribution),
        pagibig_contribution = VALUES(pagibig_contribution)
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

    public int updateAllEmployeeContributions() {
        List<String> employeeIds = getAllEmployeeIds();
        int successCount = 0;

        System.out.println("Updating contributions for all employees...");

        for (String empId : employeeIds) {
            try {
                EmployeeInfo empInfo = getEmployeeInfo(empId);
                if (empInfo == null) continue;

                // Create temporary PayrollModel to compute contributions
                PayrollModel tempModel = new PayrollModel();
                tempModel.PayrollComputation(
                        empInfo.employeeId,
                        empInfo.employeeName,
                        empInfo.basicMonthlyPay,
                        empInfo.employmentStatus,
                        empInfo.dateHired,
                        empInfo.workingHoursPerDay
                );

                // Set dummy period (first period to get HDMF)
                tempModel.setPayrollPeriod(LocalDate.now(), LocalDate.now(), true);
                tempModel.setAllowances(0, 0, 0, 0, 0, 0);
                tempModel.setDeductions(0);
                tempModel.computePayroll();

                // Update deductions (multiply semi-monthly by 2 to get monthly)
                boolean updated = updateDeductionContributions(
                        empId,
                        tempModel.getSSSContribution() * 2,
                        tempModel.getPHICContribution() * 2,
                        tempModel.getHDMFContribution() // Already monthly
                );

                if (updated) {
                    successCount++;
                    System.out.println("✓ Updated: " + empInfo.employeeName + " (₱" +
                            String.format("%,.2f", empInfo.basicMonthlyPay) + ")");
                }

            } catch (Exception e) {
                System.err.println("✗ Failed to update employee " + empId + ": " + e.getMessage());
            }
        }

        System.out.println("\nBulk update complete: " + successCount + "/" + employeeIds.size() + " employees updated");
        return successCount;
    }

    public boolean savePayroll(String employeeId, int periodId, PayrollModel model,
                               SalaryConfig config, AttendanceData attendance,
                               double perDiem, int perDiemCount, double sssLoan) {
        String query = """
        INSERT INTO payroll_records 
        (employee_Id, period_id, basic_pay, telecom_Allowance, travel_Allowance,
         rice_Subsidy, non_Taxable_Salary, per_Deim, per_Deim_Count,
         overtime_Pay, overtime_hours, sss_Contribution, phic_contribution,
         hdmf_Contribution, sss_Loan, absences, num_Absences, withholding_tax,
         gross_pay, total_Deduction, net_Pay, status)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'DRAFT')
        ON DUPLICATE KEY UPDATE
        basic_pay = VALUES(basic_pay),
        telecom_Allowance = VALUES(telecom_Allowance),
        travel_Allowance = VALUES(travel_Allowance),
        rice_Subsidy = VALUES(rice_Subsidy),
        non_Taxable_Salary = VALUES(non_Taxable_Salary),
        per_Deim = VALUES(per_Deim),
        per_Deim_Count = VALUES(per_Deim_Count),
        overtime_Pay = VALUES(overtime_Pay),
        overtime_hours = VALUES(overtime_hours),
        sss_Contribution = VALUES(sss_Contribution),
        phic_contribution = VALUES(phic_contribution),
        hdmf_Contribution = VALUES(hdmf_Contribution),
        sss_Loan = VALUES(sss_Loan),
        absences = VALUES(absences),
        num_Absences = VALUES(num_Absences),
        withholding_tax = VALUES(withholding_tax),
        gross_pay = VALUES(gross_pay),
        total_Deduction = VALUES(total_Deduction),
        net_Pay = VALUES(net_Pay)
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
            stmt.setDouble(8, perDiem);
            stmt.setInt(9, perDiemCount);
            stmt.setDouble(10, attendance.regularOTHours * model.getHourlyRate() * 1.25);
            stmt.setDouble(11, attendance.regularOTHours);

            // Use computed values from PayrollModel
            stmt.setDouble(12, model.getSSSContribution());
            stmt.setDouble(13, model.getPHICContribution());
            stmt.setDouble(14, model.getHDMFContribution());

            stmt.setDouble(15, sssLoan);
            stmt.setDouble(16, attendance.daysAbsent * model.getGrossDailyRate());
            stmt.setInt(17, attendance.daysAbsent);
            stmt.setDouble(18, model.getWithholdingTax());
            stmt.setDouble(19, model.getSemiMonthlyGrossPay());
            stmt.setDouble(20, model.getTotalDeductions());
            stmt.setDouble(21, model.getNetPay());

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

    public PayrollRecord getPayrollRecord(String employeeId, int periodId) {
        String query = """
        SELECT * FROM payroll_records
        WHERE employee_Id = ? AND period_id = ?
        LIMIT 1
    """;

        try (PreparedStatement stmt = connect.getConnection().prepareStatement(query)) {
            stmt.setString(1, employeeId);
            stmt.setInt(2, periodId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                PayrollRecord record = new PayrollRecord();
                record.employeeId = rs.getString("employee_Id");
                record.periodId = rs.getInt("period_id");
                record.basicPay = rs.getDouble("basic_pay");
                record.telecomAllowance = rs.getDouble("telecom_Allowance");
                record.travelAllowance = rs.getDouble("travel_Allowance");
                record.riceSubsidy = rs.getDouble("rice_Subsidy");
                record.nonTaxableSalary = rs.getDouble("non_Taxable_Salary");
                record.perDiem = rs.getDouble("per_Deim");
                record.perDiemCount = rs.getInt("per_Deim_Count");
                record.overtimePay = rs.getDouble("overtime_Pay");
                record.overtimeHours = rs.getDouble("overtime_hours");
                record.sssContribution = rs.getDouble("sss_Contribution");
                record.phicContribution = rs.getDouble("phic_contribution");
                record.hdmfContribution = rs.getDouble("hdmf_Contribution");
                record.sssLoan = rs.getDouble("sss_Loan");
                record.absences = rs.getDouble("absences");
                record.numAbsences = rs.getInt("num_Absences");
                record.withholdingTax = rs.getDouble("withholding_tax");
                record.grossPay = rs.getDouble("gross_pay");
                record.totalDeduction = rs.getDouble("total_Deduction");
                record.netPay = rs.getDouble("net_Pay");
                record.status = rs.getString("status");
                return record;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching payroll record: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public PayrollPeriod getPayrollPeriod(int periodId) {
        String query = """
        SELECT period_ID, period_name, start_Date, end_Date, pay_Date, status
        FROM payroll_period
        WHERE period_ID = ?
        LIMIT 1
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

    public static class PayrollRecord {
        public String employeeId;
        public int periodId;
        public double basicPay;
        public double telecomAllowance;
        public double travelAllowance;
        public double riceSubsidy;
        public double nonTaxableSalary;
        public double perDiem;
        public int perDiemCount;
        public double overtimePay;
        public double overtimeHours;
        public double sssContribution;
        public double phicContribution;
        public double hdmfContribution;
        public double sssLoan;
        public double absences;
        public int numAbsences;
        public double withholdingTax;
        public double grossPay;
        public double totalDeduction;
        public double netPay;
        public String status;
    }

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