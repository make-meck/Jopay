package com.example.jopay;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PayrollDAO {
    private DatabaseConnector connect = new DatabaseConnector();

    // retrieve employee info
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

    // computes sss, phic, hdmf
    public boolean autoComputeAndSaveContributions(String employeeId, double basicMonthlySalary) {
        try {
            // Create a temporary PayrollModel instance to use its computation methods
            PayrollModel payrollModel = new PayrollModel();
            payrollModel.PayrollComputation(employeeId, "", basicMonthlySalary, "", null, 8);

            payrollModel.setPayrollPeriod(LocalDate.now(), LocalDate.now(), true);  // Set period
            payrollModel.setAllowances(0, 0, 0, 0, 0, 0);  // Set allowances to zero
            payrollModel.setAttendanceData(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);  // Set attendance to zero
            payrollModel.setDeductions(0);
            payrollModel.computePayroll();

            // Compute contributions using PayrollModel
            double semiMonthlySSS = payrollModel.getSSSContribution();
            double semiMonthlyPHIC = payrollModel.getPHICContribution();
            double hdmfContribution = 200.00; // Fixed HDMF amount

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

                // *** ADD THIS DEBUG OUTPUT ***
                System.out.println("=== getContributions() DEBUG ===");
                System.out.println("Employee ID: " + data.employeeId);
                System.out.println("Basic Salary: ₱" + data.basicMonthlySalary);
                System.out.println("SSS from DB: ₱" + data.sssContribution);
                System.out.println("PHIC from DB: ₱" + data.phicContribution);
                System.out.println("HDMF from DB: ₱" + data.hdmfContribution);
                System.out.println("================================\n");

                return data;
            } else {
                System.out.println("⚠ NO DATA FOUND in contribution_config for employee: " + employeeId);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching contributions: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // when salary changes
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


    public void loadContributionsIntoPayrollModel(String employeeId, PayrollModel payrollModel, boolean isFirstHalf) {
        ContributionData data = getContributions(employeeId);

        if (data != null) {
            // *** FIX: Database stores MONTHLY, so divide by 2 for semi-monthly ***
            double semiMonthlySSS = data.sssContribution / 2;
            double semiMonthlyPHIC = data.phicContribution;
            double hdmf = isFirstHalf ? data.hdmfContribution : 0.0;

            System.out.println("=== loadContributionsIntoPayrollModel() DEBUG ===");
            System.out.println("From database (MONTHLY values):");
            System.out.println("  SSS (monthly): ₱" + data.sssContribution);
            System.out.println("  PHIC (monthly): ₱" + data.phicContribution);
            System.out.println("Converting to SEMI-MONTHLY for payroll:");
            System.out.println("  SSS (semi-monthly): ₱" + semiMonthlySSS);
            System.out.println("  PHIC (semi-monthly): ₱" + semiMonthlyPHIC);
            System.out.println("  HDMF: ₱" + hdmf);
            System.out.println("  isFirstHalf: " + isFirstHalf);

            payrollModel.setPreComputedContributions(
                    semiMonthlySSS,     // Use semi-monthly
                    semiMonthlyPHIC,    // Use semi-monthly
                    hdmf
            );

            System.out.println("✓ Pre-computed contributions SET in PayrollModel");
            System.out.println("================================================\n");
        } else {
            System.out.println("=== loadContributionsIntoPayrollModel() DEBUG ===");
            System.out.println("⚠ No pre-computed contributions found for employee: " + employeeId);
            System.out.println("PayrollModel will use formula calculation.");
            System.out.println("================================================\n");
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

                // Contributions
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

    // get attendance (absent/present) data
    public AttendanceData getAttendanceData(String employeeId, LocalDate startDate, LocalDate endDate) {
        String query = """
        SELECT 
            COALESCE(SUM(CASE WHEN status = 'Present' OR status = 'Overtime' THEN 1 ELSE 0 END), 0) as days_worked,
            COALESCE(SUM(CASE WHEN status = 'Absent' THEN 1 ELSE 0 END), 0) as days_absent,
            COALESCE(SUM(CASE WHEN total_hours > 8 THEN total_hours - 8 ELSE 0 END), 0) as regular_ot_hours,
            COALESCE(SUM(CASE 
                WHEN status LIKE 'Under%' OR (status = 'Present' AND total_hours < 8 AND total_hours > 0)
                THEN 8 - total_hours 
                ELSE 0 
            END), 0) as undertime_hours
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
                data.undertimeHours = rs.getDouble("undertime_hours");
                data.nightDifferentialOTHours = 0.0;
                data.specialHolidaysWorked = 0;
                data.regularHolidaysWorked = 0;
                data.restDaysWorked = 0;
                data.restDayOTHours = 0.0;
                data.restDayNightDiffOTHours = 0.0;

                System.out.println("\n=== ATTENDANCE DATA LOADED ===");
                System.out.println("Employee ID: " + employeeId);
                System.out.println("Period: " + startDate + " to " + endDate);
                System.out.println("Days Worked: " + data.daysWorked);
                System.out.println("Days Absent: " + data.daysAbsent);
                System.out.println("OT Hours: " + data.regularOTHours);
                System.out.println("Undertime Hours: " + data.undertimeHours);
                System.out.println("==============================\n");

                return data;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching attendance data: " + e.getMessage());
            e.printStackTrace();
        }

        AttendanceData emptyData = new AttendanceData();
        emptyData.undertimeHours = 0.0;
        return emptyData;
    }

    // retrieve absence inf0
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

    // act as container
    public static class AbsenceInfo {
        public int absenceCount;
    }

    public DeductionData getDeductions(String employeeId) {
        String query = """
        SELECT 
            COALESCE(sss_contribution, 0) as sss_contrib,
            COALESCE(phic_contribution, 0) as phic_contrib,
            COALESCE(hdmf_contribution, 0) as hdmf_contrib,
            COALESCE(sss_loan, 0) as sss_loan
        FROM contribution_config
        WHERE employee_Id = ?
        ORDER BY contribution_id DESC
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

    // Update Basic Salary in employee_info
    public boolean updateBasicSalary(String employeeId, double newMonthlyBasicSalary) {
        String query = """
            UPDATE employee_info
            SET basic_Salary = ?
            WHERE employee_Id = ?
        """;

        try (PreparedStatement stmt = connect.getConnection().prepareStatement(query)) {
            stmt.setDouble(1, newMonthlyBasicSalary);
            stmt.setString(2, employeeId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✓ Basic Salary updated: ₱" + String.format("%,.2f", newMonthlyBasicSalary) +
                        " for employee " + employeeId);
                return true;
            } else {
                System.err.println("No employee found with ID: " + employeeId);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error updating Basic Salary: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    //  Update Per Diem in salary_config
    public boolean updatePerDiem(String employeeId, double perDiem) {
        String query = """
            UPDATE salary_config
            SET per_diem = ?
            WHERE employee_Id = ?
        """;

        try (PreparedStatement stmt = connect.getConnection().prepareStatement(query)) {
            stmt.setDouble(1, perDiem);
            stmt.setString(2, employeeId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✓ Per Diem updated: ₱" + String.format("%,.2f", perDiem) +
                        " for employee " + employeeId);
                return true;
            } else {
                System.err.println("No salary_config found for employee " + employeeId);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error updating Per Diem: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // update Per Diem Count in salary_config
    public boolean updatePerDiemCount(String employeeId, int perDiemCount) {
        String query = """
            UPDATE salary_config
            SET per_diem_count = ?
            WHERE employee_Id = ?
        """;

        try (PreparedStatement stmt = connect.getConnection().prepareStatement(query)) {
            stmt.setInt(1, perDiemCount);
            stmt.setString(2, employeeId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✓ Per Diem Count updated: " + perDiemCount +
                        " days for employee " + employeeId);
                return true;
            } else {
                System.err.println("No salary_config found for employee " + employeeId);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error updating Per Diem Count: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateLeaveBalance(String employeeId, int year, double balance, String leaveType) {
        String column = leaveType.equalsIgnoreCase("VL") ? "vl_balance" : "sl_balance";

        // check if record exists
        String checkQuery = "SELECT COUNT(*) FROM leave_balances WHERE employee_Id = ? AND year = ?";

        try (PreparedStatement checkStmt = connect.getConnection().prepareStatement(checkQuery)) {
            checkStmt.setString(1, employeeId);
            checkStmt.setInt(2, year);

            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            boolean recordExists = rs.getInt(1) > 0;

            if (recordExists) {
                // Update existing record
                String updateQuery = "UPDATE leave_balances SET " + column + " = ? " +
                        "WHERE employee_Id = ? AND year = ?";

                try (PreparedStatement updateStmt = connect.getConnection().prepareStatement(updateQuery)) {
                    updateStmt.setDouble(1, balance);
                    updateStmt.setString(2, employeeId);
                    updateStmt.setInt(3, year);

                    int rowsAffected = updateStmt.executeUpdate();

                    if (rowsAffected > 0) {
                        System.out.println("✓ " + leaveType + " Balance updated: " + balance +
                                " days for employee " + employeeId + " (year " + year + ")");
                        return true;
                    }
                }
            } else {
                // Insert new record
                String insertQuery = """
                    INSERT INTO leave_balances 
                    (employee_Id, year, vl_balance, sl_balance, vl_used, sl_used)
                    VALUES (?, ?, ?, ?, 0, 0)
                """;

                try (PreparedStatement insertStmt = connect.getConnection().prepareStatement(insertQuery)) {
                    insertStmt.setString(1, employeeId);
                    insertStmt.setInt(2, year);

                    if (leaveType.equalsIgnoreCase("VL")) {
                        insertStmt.setDouble(3, balance);
                        insertStmt.setDouble(4, 0.0); // Default SL balance
                    } else {
                        insertStmt.setDouble(3, 0.0); // Default VL balance
                        insertStmt.setDouble(4, balance);
                    }

                    int rowsAffected = insertStmt.executeUpdate();

                    if (rowsAffected > 0) {
                        System.out.println("✓ " + leaveType + " Balance created: " + balance +
                                " days for employee " + employeeId + " (year " + year + ")");
                        return true;
                    }
                }
            }

            return false;

        } catch (SQLException e) {
            System.err.println("Error updating " + leaveType + " Balance: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Update allowances in salary_config
    public boolean updateAllowances(String employeeId, double telecom, double travel,
                                    double rice, double nonTaxable) {
        String query = """
            UPDATE salary_config
            SET telecom_allowance = ?,
                travel_allowance = ?,
                rice_subsidy = ?,
                non_taxable_salary = ?
            WHERE employee_Id = ?
        """;

        try (PreparedStatement stmt = connect.getConnection().prepareStatement(query)) {
            stmt.setDouble(1, telecom);
            stmt.setDouble(2, travel);
            stmt.setDouble(3, rice);
            stmt.setDouble(4, nonTaxable);
            stmt.setString(5, employeeId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✓ Allowances updated for employee " + employeeId);
                return true;
            } else {
                System.err.println("No salary_config found for employee " + employeeId);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error updating allowances: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // update sss loan
    public boolean updateSSSLoan(String employeeId, double sssLoan) {
        // Check if deduction record exists
        String checkQuery = "SELECT COUNT(*) FROM contribution_config WHERE employee_Id = ?";

        try (PreparedStatement checkStmt = connect.getConnection().prepareStatement(checkQuery)) {
            checkStmt.setString(1, employeeId);

            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            boolean recordExists = rs.getInt(1) > 0;

            if (recordExists) {
                // Update existing record
                String updateQuery = "UPDATE contribution_config SET sss_loan = ? WHERE employee_Id = ?";

                try (PreparedStatement updateStmt = connect.getConnection().prepareStatement(updateQuery)) {
                    updateStmt.setDouble(1, sssLoan);
                    updateStmt.setString(2, employeeId);

                    int rowsAffected = updateStmt.executeUpdate();

                    if (rowsAffected > 0) {
                        System.out.println("✓ SSS Loan updated: ₱" + String.format("%,.2f", sssLoan) +
                                " for employee " + employeeId);
                        return true;
                    }
                }
            } else {
                // Insert new record
                String insertQuery = "INSERT INTO contribution_config (employee_Id, sss_loan) VALUES (?, ?)";

                try (PreparedStatement insertStmt = connect.getConnection().prepareStatement(insertQuery)) {
                    insertStmt.setString(1, employeeId);
                    insertStmt.setDouble(2, sssLoan);

                    int rowsAffected = insertStmt.executeUpdate();

                    if (rowsAffected > 0) {
                        System.out.println("✓ SSS Loan created: ₱" + String.format("%,.2f", sssLoan) +
                                " for employee " + employeeId);
                        return true;
                    }
                }
            }

            return false;

        } catch (SQLException e) {
            System.err.println("Error updating SSS Loan: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * FOR ADMIN: Get periods with payroll data
     * Shows ONLY periods that have at least one payroll record
     */
    public List<PayrollPeriod> getPeriodsWithPayrollData() {
        List<PayrollPeriod> periods = new ArrayList<>();
        String query = """
        SELECT DISTINCT pp.period_ID, pp.period_name, pp.start_Date, 
               pp.end_Date, pp.pay_Date, pp.status
        FROM payroll_period pp
        INNER JOIN payroll_records pr ON pp.period_ID = pr.period_id
        ORDER BY pp.start_Date DESC
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

            System.out.println("✓ Admin: Found " + periods.size() + " periods with payroll data");

        } catch (SQLException e) {
            System.err.println("Error fetching periods with payroll data: " + e.getMessage());
            e.printStackTrace();
        }
        return periods;
    }

    /**
     * FOR EMPLOYEE: Get ONLY periods where employee has payroll records
     * Used in employee dashboard payslip dropdown
     * Shows only periods where payslip exists
     */
    public List<PayrollPeriod> getPeriodsWithPayrollDataForEmployee(String employeeId) {
        List<PayrollPeriod> periods = new ArrayList<>();

        String query = """
        SELECT DISTINCT pp.period_ID, pp.period_name, pp.start_Date, 
               pp.end_Date, pp.pay_Date, pp.status
        FROM payroll_period pp
        INNER JOIN payroll_records pr ON pp.period_ID = pr.period_id
        WHERE pr.employee_Id = ?
        ORDER BY pp.start_Date DESC
    """;

        try (PreparedStatement stmt = connect.getConnection().prepareStatement(query)) {
            stmt.setString(1, employeeId);
            ResultSet rs = stmt.executeQuery();

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

            System.out.println("✓ Employee " + employeeId + ": Found " + periods.size() +
                    " periods with payroll records");

        } catch (SQLException e) {
            System.err.println("Error fetching employee periods: " + e.getMessage());
            e.printStackTrace();
        }
        return periods;
    }

    /**
     * FOR ADMIN: Get ALL periods from employee's hire date onwards
     * Used in admin manage payroll page
     * Shows all periods so admin can create payroll for any period
     *
     * @param employeeId The employee ID to get periods for
     * @return List of periods from hire date onwards
     */
    public List<PayrollPeriod> getPeriodsFromHireDate(String employeeId) {
        List<PayrollPeriod> periods = new ArrayList<>();

        String query = """
        SELECT pp.period_ID, pp.period_name, pp.start_Date, pp.end_Date, 
               pp.pay_Date, pp.status
        FROM payroll_period pp
        WHERE pp.start_Date >= (
            SELECT ei.date_Hired 
            FROM employee_info ei 
            WHERE ei.employee_Id = ?
        )
        ORDER BY pp.start_Date DESC
    """;

        try (PreparedStatement stmt = connect.getConnection().prepareStatement(query)) {
            stmt.setString(1, employeeId);
            ResultSet rs = stmt.executeQuery();

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

            System.out.println("✓ Admin: Found " + periods.size() +
                    " periods from hire date for employee " + employeeId);

        } catch (SQLException e) {
            System.err.println("Error fetching periods from hire date: " + e.getMessage());
            e.printStackTrace();
        }
        return periods;
    }

    /* public boolean updatePayroll(int payrollId, String employeeId, int periodId,
                                 PayrollModel model, SalaryConfig config,
                                 AttendanceData attendance) {
        String query = """
        UPDATE payroll_records
        SET employee_Id = ?,
            period_id = ?,
            basic_pay = ?,
            telecom_Allowance = ?,
            travel_Allowance = ?,
            rice_Subsidy = ?,
            non_Taxable_Salary = ?,
            per_Deim = ?,
            per_Deim_Count = ?,
            overtime_Pay = ?,
            overtime_hours = ?,
            sss_Contribution = ?,
            phic_contribution = ?,
            hdmf_Contribution = ?,
            sss_Loan = ?,
            absences = ?,
            num_Absences = ?,
            gross_pay = ?,
            total_Deduction = ?,
            net_Pay = ?,
            taxable_Income = ?,
            withholding_Tax = ?,
            status = 'UPDATED',
            updated_at = CURRENT_TIMESTAMP
        WHERE payroll_Id = ?
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

            // Get SSS Loan from deduction_config
            DeductionData deductions = getDeductions(employeeId);
            stmt.setDouble(15, deductions != null ? deductions.sssLoan : 0.0);

            stmt.setDouble(16, attendance.daysAbsent * model.getGrossDailyRate());
            stmt.setInt(17, attendance.daysAbsent);
            stmt.setDouble(18, model.getSemiMonthlyGrossPay());
            stmt.setDouble(19, model.getTotalDeductions());
            stmt.setDouble(20, model.getNetPay());
            stmt.setDouble(21, model.getTaxableIncome());
            stmt.setDouble(22, model.getWithholdingTax());
            stmt.setInt(23, payrollId);

            int result = stmt.executeUpdate();
            conn.commit();

            if (result > 0) {
                System.out.println("✓ Payroll record updated successfully!");
                System.out.println("  Payroll ID: " + payrollId);
                System.out.println("  Employee: " + employeeId);
                System.out.println("  Period: " + periodId);
                System.out.println("  Gross Pay: ₱" + String.format("%,.2f", model.getSemiMonthlyGrossPay()));
                System.out.println("  Total Deductions: ₱" + String.format("%,.2f", model.getTotalDeductions()));
                System.out.println("  Net Pay: ₱" + String.format("%,.2f", model.getNetPay()));
            }

            return result > 0;

        } catch (SQLException e) {
            System.err.println("Error updating payroll: " + e.getMessage());
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
    } */

    /* public Integer getExistingPayrollId(String employeeId, int periodId) {
        String query = """
        SELECT payroll_Id
        FROM payroll_records
        WHERE employee_Id = ? AND period_id = ?
        LIMIT 1
    """;

        try (PreparedStatement stmt = connect.getConnection().prepareStatement(query)) {
            stmt.setString(1, employeeId);
            stmt.setInt(2, periodId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int payrollId = rs.getInt("payroll_Id");
                System.out.println("Found existing payroll record: ID = " + payrollId);
                return payrollId;
            } else {
                System.out.println("No existing payroll record found for employee " + employeeId +
                        " in period " + periodId);
            }
        } catch (SQLException e) {
            System.err.println("Error checking existing payroll: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    } */


    // update salary config (multiple fields at once)
    /*public boolean updateSalaryConfig(String employeeId, SalaryConfig config) {
        String query = """
        UPDATE salary_config
        SET basic_Pay = ?,
            telecom_allowance = ?,
            travel_allowance = ?,
            rice_subsidy = ?,
            non_taxable_salary = ?,
            per_diem = ?,
            per_diem_count = ?
        WHERE employee_Id = ?
    """;

        try (PreparedStatement stmt = connect.getConnection().prepareStatement(query)) {
            stmt.setDouble(1, config.basicPay);
            stmt.setDouble(2, config.telecomAllowance);
            stmt.setDouble(3, config.travelAllowance);
            stmt.setDouble(4, config.riceSubsidy);
            stmt.setDouble(5, config.nonTaxableSalary);
            stmt.setDouble(6, config.perDiem);
            stmt.setInt(7, config.perDiemCount);
            stmt.setString(8, employeeId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✓ Salary config updated completely for employee " + employeeId);

                // Auto-compute contributions when basic pay changes
                autoComputeAndSaveContributions(employeeId, config.basicPay);

                return true;
            } else {
                System.err.println("No salary_config found for employee " + employeeId);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error updating salary config: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }*/


    /* public boolean saveSalaryConfig(String employeeId, double telecom, double travel,
                                    double rice, double nonTaxable, double perDiem,
                                    int perDiemCount, LocalDate startDate, LocalDate endDate) {

        // get employee info to compute contributions
        EmployeeInfo empInfo = getEmployeeInfo(employeeId);
        if (empInfo == null) {
            System.err.println("Cannot save salary config: Employee not found");
            return false;
        }

        // use PayrollModel for formulas
        PayrollModel tempModel = new PayrollModel();
        tempModel.PayrollComputation(
                empInfo.employeeId,
                empInfo.employeeName,
                empInfo.basicMonthlyPay,
                empInfo.employmentStatus,
                empInfo.dateHired,
                empInfo.workingHoursPerDay
        );

        // Set period to get HDMF contribution
        tempModel.setPayrollPeriod(LocalDate.now(), LocalDate.now(), true);
        tempModel.setAllowances(0, 0, 0, 0, 0, 0);
        tempModel.setDeductions(0);
        tempModel.computePayroll();

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
    }*/



    public boolean savePayroll(String employeeId, int periodId, PayrollModel model,
                               SalaryConfig config, AttendanceData attendance) {
        String query = """
        INSERT INTO payroll_records 
        (employee_Id, period_id, basic_pay, telecom_Allowance, travel_Allowance,
         rice_Subsidy, non_Taxable_Salary, per_Deim, per_Deim_Count,
         overtime_Pay, overtime_hours, undertime_Pay, undertime_hours,
         sss_Contribution, phic_contribution, hdmf_Contibution, sss_Loan, 
         absences, num_Absences, taxable_income, withholding_tax, 
         gross_pay, total_Deduction, net_Pay, status)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'DRAFT')
    """;

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = connect.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(query);

            // Get SSS Loan from deduction_config
            DeductionData deductions = getDeductions(employeeId);
            double sssLoan = deductions != null ? deductions.sssLoan : 0.0;

            // Set parameters in correct order
            stmt.setString(1, employeeId);                                          // employee_Id
            stmt.setInt(2, periodId);                                               // period_id
            stmt.setDouble(3, model.getSemiMonthlyBasicPay());                     // basic_pay
            stmt.setDouble(4, config != null ? config.telecomAllowance : 0.0);     // telecom_Allowance
            stmt.setDouble(5, config != null ? config.travelAllowance : 0.0);      // travel_Allowance
            stmt.setDouble(6, config != null ? config.riceSubsidy : 0.0);          // rice_Subsidy
            stmt.setDouble(7, config != null ? config.nonTaxableSalary : 0.0);     // non_Taxable_Salary
            stmt.setDouble(8, config != null ? config.perDiem : 0.0);              // per_Deim
            stmt.setInt(9, config != null ? config.perDiemCount : 0);              // per_Deim_Count
            stmt.setDouble(10, attendance.regularOTHours * model.getHourlyRate() * 1.25); // overtime_Pay
            stmt.setDouble(11, attendance.regularOTHours);                          // overtime_hours
            stmt.setDouble(12, attendance.undertimeHours * model.getHourlyRate()); // undertime_Pay
            stmt.setDouble(13, attendance.undertimeHours);                          // undertime_hours
            stmt.setDouble(14, model.getSSSContribution());                         // sss_Contribution
            stmt.setDouble(15, model.getPHICContribution());                        // phic_contribution
            stmt.setDouble(16, model.getHDMFContribution());                        // hdmf_Contibution
            stmt.setDouble(17, sssLoan);                                            // sss_Loan
            stmt.setDouble(18, attendance.daysAbsent * model.getGrossDailyRate()); // absences
            stmt.setInt(19, attendance.daysAbsent);                                 // num_Absences
            stmt.setDouble(20, model.getTaxableIncome());                           // taxable_income
            stmt.setDouble(21, model.getWithholdingTax());                          // withholding_tax
            stmt.setDouble(22, model.getSemiMonthlyGrossPay());                     // gross_pay
            stmt.setDouble(23, model.getTotalDeductions());                         // total_Deduction
            stmt.setDouble(24, model.getNetPay());                                  // net_Pay
            // Parameter 25 is 'DRAFT' which is hardcoded in the SQL

            int result = stmt.executeUpdate();
            conn.commit();

            if (result > 0) {
                System.out.println("✓ Payroll record saved successfully!");
                System.out.println("  Employee: " + employeeId);
                System.out.println("  Period: " + periodId);
                System.out.println("  Gross Pay: ₱" + String.format("%,.2f", model.getSemiMonthlyGrossPay()));
                System.out.println("  Total Deductions: ₱" + String.format("%,.2f", model.getTotalDeductions()));
                System.out.println("  Net Pay: ₱" + String.format("%,.2f", model.getNetPay()));
            }

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


    // retrieve absence count
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

    // get leave data
    public LeaveData getLeaveData(String employeeId, int year) {
        String query = """
        SELECT 
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
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        connect.close();
    }

    // data organizers
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
        public double sssContribution;
        public double phicContribution;
        public double hdmfContribution;
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

    public static class PayrollRecord {
        public int payrollId;
        public String employeeId;
        public String employeeName;
        public int periodId;
        public String periodName;
        public LocalDate startDate;
        public LocalDate endDate;
        public double basicPay;
        public double grossPay;
        public double totalDeduction;
        public double netPay;
        public String status;
    }

}