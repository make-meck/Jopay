package com.example.jopay;

import java.time.LocalDate;

/**
 * Service class that integrates PayrollModel with the actual database
 */
public class PayrollService {
    private PayrollDAO dao;

    public PayrollService() {
        this.dao = new PayrollDAO();
    }

    public PayrollModel processPayroll(String employeeId, int periodId) {
        try {
            // 1. Get payroll period information
            PayrollDAO.PayrollPeriod period = dao.getPayrollPeriod(periodId);
            if (period == null) {
                System.err.println("Payroll period not found: " + periodId);
                return null;
            }

            LocalDate startDate = period.startDate;
            LocalDate endDate = period.endDate;

            boolean isFirstPeriod = startDate.getDayOfMonth() >= 11 && startDate.getDayOfMonth() <= 25;

            PayrollDAO.EmployeeInfo empInfo = dao.getEmployeeInfo(employeeId);
            if (empInfo == null) {
                System.err.println("Employee not found: " + employeeId);
                return null;
            }

            PayrollDAO.SalaryConfig salaryConfig = dao.getSalaryConfig(employeeId);
            if (salaryConfig == null) {
                System.err.println("Salary configuration not found for: " + employeeId);
                return null;
            }

            PayrollModel payroll = new PayrollModel();
            payroll.PayrollComputation(
                    empInfo.employeeId,
                    empInfo.employeeName,
                    salaryConfig.basicPay,  // Use basic_Pay from salary_config
                    empInfo.employmentStatus,
                    empInfo.dateHired,
                    empInfo.workingHoursPerDay
            );

            payroll.setPayrollPeriod(startDate, endDate, isFirstPeriod);

            // *** NEW: Load pre-computed contributions from database ***
            dao.loadContributionsIntoPayrollModel(employeeId, payroll, isFirstPeriod);

            PayrollDAO.AttendanceData attendance = dao.getAttendanceData(employeeId, startDate, endDate);
            if (attendance != null) {
                int absences = dao.getAbsenceCount(employeeId, startDate, endDate);
                attendance.daysAbsent = Math.max(attendance.daysAbsent, absences);

                payroll.setAttendanceData(
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
            }

            payroll.setAllowances(
                    salaryConfig.telecomAllowance,
                    salaryConfig.travelAllowance,
                    salaryConfig.riceSubsidy,
                    salaryConfig.nonTaxableSalary,
                    salaryConfig.perDiem,
                    salaryConfig.perDiemCount
            );

            PayrollDAO.DeductionData deductions = dao.getDeductions(employeeId);
            if (deductions != null) {
                payroll.setDeductions(deductions.sssLoan);
            }

            int currentYear = startDate.getYear();
            PayrollDAO.LeaveData leave = dao.getLeaveData(employeeId, currentYear);
            if (leave != null) {
                payroll.setLeaveData(
                        leave.vlUsed,
                        leave.slUsed,
                        leave.vlBalance,
                        leave.slBalance
                );
            }

            // Compute payroll (will use pre-computed contributions if available)
            payroll.computePayroll();

            // Save payroll with the correct method signature
            boolean saved = dao.savePayroll(employeeId, periodId, payroll, salaryConfig, attendance);
            if (!saved) {
                System.err.println("Warning: Failed to save payroll to database");
            } else {
                System.out.println("✓ Payroll saved successfully");
            }

            return payroll;

        } catch (Exception e) {
            System.err.println("Error processing payroll: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void processPayrollForAllEmployees(int periodId) {
        var employeeIds = dao.getAllEmployeeIds();

        System.out.println("Processing payroll for period ID: " + periodId);
        System.out.println("Total employees: " + employeeIds.size() + "\n");

        int successCount = 0;
        int failCount = 0;

        for (String employeeId : employeeIds) {
            System.out.println("Processing employee: " + employeeId);
            PayrollModel payroll = processPayroll(employeeId, periodId);

            if (payroll != null) {
                System.out.println("✓ Successfully processed");
                System.out.println("  Net Pay: ₱" + String.format("%,.2f", payroll.getNetPay()));
                successCount++;
            } else {
                System.err.println("✗ Failed to process");
                failCount++;
            }
            System.out.println();
        }

        System.out.println("=== Payroll Processing Summary ===");
        System.out.println("Total Employees: " + employeeIds.size());
        System.out.println("Successfully Processed: " + successCount);
        System.out.println("Failed: " + failCount);
    }

    public String getPayslip(String employeeId, int periodId) {
        PayrollModel payroll = processPayroll(employeeId, periodId);

        if (payroll != null) {
            return payroll.generatePayslipSummary();
        } else {
            return "Error: Unable to generate payslip for employee " + employeeId;
        }
    }

    /**
     * Utility method to auto-compute and save contributions for an employee
     * Call this when an employee's salary is updated
     */
    public boolean updateEmployeeContributions(String employeeId) {
        try {
            PayrollDAO.SalaryConfig salaryConfig = dao.getSalaryConfig(employeeId);
            if (salaryConfig == null) {
                System.err.println("Salary configuration not found for employee: " + employeeId);
                return false;
            }

            return dao.autoComputeAndSaveContributions(employeeId, salaryConfig.basicPay);
        } catch (Exception e) {
            System.err.println("Error updating contributions: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Utility method to recalculate contributions for all active employees
     */
    public void recalculateAllEmployeeContributions() {
        System.out.println("=== RECALCULATING CONTRIBUTIONS FOR ALL EMPLOYEES ===");
        dao.recalculateAllContributions();
        System.out.println("=== RECALCULATION COMPLETE ===\n");
    }

    public static void main(String[] args) {
        PayrollService service = new PayrollService();

        // Test database connection
        System.out.println("=== TESTING DATABASE CONNECTION ===");
        DatabaseConnector db = new DatabaseConnector();
        db.testConnection();
        System.out.println();

        // Optional: Recalculate contributions for all employees first
        System.out.println("=== STEP 1: RECALCULATE CONTRIBUTIONS ===");
        service.recalculateAllEmployeeContributions();

        // Example 1: Process payroll for a single employee
        System.out.println("=== EXAMPLE 1: Single Employee Payroll ===");
        String employeeId = "6700001"; // Employee ID from your database
        int periodId = 1; // Period ID from payroll_period table

        PayrollModel payroll = service.processPayroll(employeeId, periodId);

        if (payroll != null) {
            System.out.println(payroll.generatePayslipSummary());

            System.out.println("\n=== Detailed Breakdown ===");
            System.out.println("Basic Daily Rate: ₱" + String.format("%,.2f", payroll.getBasicDailyRate()));
            System.out.println("Hourly Rate: ₱" + String.format("%,.2f", payroll.getHourlyRate()));
            System.out.println("Gross Monthly: ₱" + String.format("%,.2f", payroll.getGrossMonthlyPay()));
            System.out.println("Taxable Income: ₱" + String.format("%,.2f", payroll.getTaxableIncome()));
            System.out.println("Withholding Tax: ₱" + String.format("%,.2f", payroll.getWithholdingTax()));
        }

        service.close();
    }

    public void close() {
        if (dao != null) {
            dao.close();
        }
    }
}