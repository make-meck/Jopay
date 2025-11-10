package com.example.jopay;

import java.time.LocalDate;

public class PayrollModel {

    private static final double FACTORATE = 21.75;
    private static final double OT_RATE = 1.25;
    private static final double NIGHT_DIFFERENTIAL = 1.10;
    private static final double SPECIAL_HOLIDAY_RATE = 1.30;
    private static final double REGULAR_HOLIDAY_RATE = 2.00;
    private static final double REST_DAY_RATE = 1.30;
    private static final double PHIC_RATE = 0.025;
    private static final double PHIC_MIN = 500.00;
    private static final double PHIC_MAX = 5000.00;
    private static final double HDMF = 200.00;
    private static final double VL_ACCRUAL_RATE = 1.25;
    private static final int TOTAL_VL_PER_YEAR = 15;
    private static final int TOTAL_SL_PER_YEAR = 10;
    private static final double NON_TAXABLE_13TH_MONTH_MAX = 90000.00;

    private String employeeId;
    private String employeeName;
    private double basicMonthlyPay;
    private String employmentStatus;
    private LocalDate dateHired;
    private int workingHoursPerDay;

    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isFirstHalf;

    // Attendance Data
    private int daysWorked;
    private int daysAbsent;
    private double regularOTHours;
    private double nightDifferentialOTHours;
    private int specialHolidaysWorked;
    private int regularHolidaysWorked;
    private int restDaysWorked;
    private double restDayOTHours;
    private double restDayNightDiffOTHours;
    private double undertimeHours;

    // Allowances (Non-taxable)
    private double telecomAllowance;
    private double travelAllowance;
    private double riceSubsidy;
    private double otherNonTaxable;
    private double dailyPerDiem;
    private int perDiemCount;

    // Deductions
    private double sssLoan;
    private double sssContribution;
    private double phicContribution;
    private double hdmfContribution;

    // Leave
    private double vlUsed;
    private double slUsed;
    private double vlBalance;
    private double slBalance;

    private double grossMonthlyPay;
    private double grossDailyRate;
    private double basicDailyRate;
    private double hourlyRate;
    private double semiMonthlyBasicPay;
    private double semiMonthlyGrossPay;
    private double totalEarnings;
    private double totalDeductions;
    private double taxableIncome;
    private double withholdingTax;
    private double netPay;

    // *** NEW: Fields for pre-computed contributions ***
    private boolean usePreComputedContributions = false;
    private double preComputedSSS = 0.0;
    private double preComputedPHIC = 0.0;
    private double preComputedHDMF = 0.0;

    // Constructor
    public void PayrollComputation(String employeeId, String employeeName, double basicMonthlyPay,
                                   String employmentStatus, LocalDate dateHired, int workingHoursPerDay) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.basicMonthlyPay = basicMonthlyPay;
        this.employmentStatus = employmentStatus;
        this.dateHired = dateHired;
        this.workingHoursPerDay = workingHoursPerDay;

        this.basicDailyRate = basicMonthlyPay / FACTORATE;
        this.hourlyRate = basicDailyRate / workingHoursPerDay;
        this.semiMonthlyBasicPay = basicMonthlyPay / 2;
    }

    public void setPayrollPeriod(LocalDate startDate, LocalDate endDate, boolean isFirstHalf) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.isFirstHalf = isFirstHalf;
    }

    public void setAttendanceData(int daysWorked, int daysAbsent, double regularOTHours,
                                  double nightDifferentialOTHours, int specialHolidaysWorked,
                                  int regularHolidaysWorked, int restDaysWorked,
                                  double restDayOTHours, double restDayNightDiffOTHours,
                                  double undertimeHours) {
        this.daysWorked = daysWorked;
        this.daysAbsent = daysAbsent;
        this.regularOTHours = regularOTHours;
        this.nightDifferentialOTHours = nightDifferentialOTHours;
        this.specialHolidaysWorked = specialHolidaysWorked;
        this.regularHolidaysWorked = regularHolidaysWorked;
        this.restDaysWorked = restDaysWorked;
        this.restDayOTHours = restDayOTHours;
        this.restDayNightDiffOTHours = restDayNightDiffOTHours;
        this.undertimeHours = undertimeHours;
    }

    public void setAllowances(double telecomAllowance, double travelAllowance,
                              double riceSubsidy, double otherNonTaxable,
                              double dailyPerDiem, int perDiemCount) {
        this.telecomAllowance = telecomAllowance;
        this.travelAllowance = travelAllowance;
        this.riceSubsidy = riceSubsidy;
        this.otherNonTaxable = otherNonTaxable;
        this.dailyPerDiem = dailyPerDiem;
        this.perDiemCount = perDiemCount;

        this.grossMonthlyPay = basicMonthlyPay + telecomAllowance + travelAllowance + riceSubsidy + otherNonTaxable;
        this.grossDailyRate = grossMonthlyPay / FACTORATE;
    }

    public void setDeductions(double sssLoan) {
        this.sssLoan = sssLoan;
    }

    public void setLeaveData(double vlUsed, double slUsed, double vlBalance, double slBalance) {
        this.vlUsed = vlUsed;
        this.slUsed = slUsed;
        this.vlBalance = vlBalance;
        this.slBalance = slBalance;
    }

    /**
     * *** NEW METHOD ***
     * Set pre-computed contributions from database
     * This bypasses the formula calculation
     *
     * @param sss Semi-monthly SSS contribution from database
     * @param phic Semi-monthly PHIC contribution from database
     * @param hdmf Semi-monthly HDMF contribution from database (0 if second period)
     */
    public void setPreComputedContributions(double sss, double phic, double hdmf) {
        this.usePreComputedContributions = true;
        this.preComputedSSS = sss;
        this.preComputedPHIC = phic;
        this.preComputedHDMF = hdmf;

        System.out.println("\n*** USING PRE-COMPUTED CONTRIBUTIONS FROM DATABASE ***");
        System.out.println("  SSS (semi-monthly): ₱" + String.format("%,.2f", sss));
        System.out.println("  PHIC (semi-monthly): ₱" + String.format("%,.2f", phic));
        System.out.println("  HDMF (semi-monthly): ₱" + String.format("%,.2f", hdmf));
        System.out.println("**************************************************\n");
    }

    public void computePayroll() {
        // Calculate Earnings/Gross Pay
        double basicPayForPeriod = calculateBasicPay();
        double absentDeduction = calculateAbsentDeduction();
        double undertimeDeduction = calculateUndertimeDeduction();
        double regularOTPay = calculateRegularOT();
        double nightDiffOTPay = calculateNightDifferentialOT();
        double specialHolidayPay = calculateSpecialHolidayPay();
        double regularHolidayPay = calculateRegularHolidayPay();
        double restDayPay = calculateRestDayPay();
        double restDayOTPay = calculateRestDayOTPay();
        double restDayNightDiffOTPay = calculateRestDayNightDiffOTPay();
        double vlPay = calculateVLPay();
        double slPay = calculateSLPay();
        double perDiemPay = calculatePerDiem();

        totalEarnings = basicPayForPeriod - absentDeduction - undertimeDeduction
                + regularOTPay + nightDiffOTPay + specialHolidayPay
                + regularHolidayPay + restDayPay + restDayOTPay
                + restDayNightDiffOTPay + vlPay + slPay;

        semiMonthlyGrossPay = totalEarnings;

        // *** MODIFIED: Use pre-computed contributions if available ***
        if (usePreComputedContributions) {
            sssContribution = preComputedSSS;
            phicContribution = preComputedPHIC;
            hdmfContribution = preComputedHDMF;

            System.out.println("✓ Using PRE-COMPUTED contributions from database");
            System.out.println("  SSS: ₱" + String.format("%,.2f", sssContribution));
            System.out.println("  PHIC: ₱" + String.format("%,.2f", phicContribution));
            System.out.println("  HDMF: ₱" + String.format("%,.2f", hdmfContribution));
        } else {
            // Calculate Mandatory Contributions using formulas (fallback)
            sssContribution = calculateSSS();
            phicContribution = calculatePHIC();
            hdmfContribution = calculateHDMF();

            System.out.println("⚠ Using FORMULA-COMPUTED contributions (no pre-computed values found)");
            System.out.println("  SSS: ₱" + String.format("%,.2f", sssContribution));
            System.out.println("  PHIC: ₱" + String.format("%,.2f", phicContribution));
            System.out.println("  HDMF: ₱" + String.format("%,.2f", hdmfContribution));
        }

        // Calculate Total Non-taxable
        double totalNonTaxable = telecomAllowance + travelAllowance + riceSubsidy
                + otherNonTaxable + perDiemPay;

        // Calculate Taxable Income
        taxableIncome = semiMonthlyGrossPay - (sssContribution + phicContribution
                + hdmfContribution + totalNonTaxable);

        // Calculate Withholding Tax
        withholdingTax = calculateWithholdingTax(taxableIncome);

        // Calculate Total Deductions
        totalDeductions = sssContribution + phicContribution + hdmfContribution
                + sssLoan + withholdingTax;

        // Calculate Net Pay
        netPay = totalEarnings + totalNonTaxable - totalDeductions;

        // Reset flag for next computation
        usePreComputedContributions = false;
    }

    private double calculateBasicPay() {
        return semiMonthlyBasicPay;
    }

    private double calculateAbsentDeduction() {
        return daysAbsent * grossDailyRate;
    }

    private double calculateUndertimeDeduction() {
        return undertimeHours * hourlyRate * OT_RATE;
    }

    private double calculateRegularOT() {
        return regularOTHours * hourlyRate * OT_RATE;
    }

    private double calculateNightDifferentialOT() {
        return nightDifferentialOTHours * hourlyRate * OT_RATE * NIGHT_DIFFERENTIAL;
    }

    private double calculateSpecialHolidayPay() {
        return specialHolidaysWorked * grossDailyRate * SPECIAL_HOLIDAY_RATE;
    }

    private double calculateRegularHolidayPay() {
        return regularHolidaysWorked * grossDailyRate * REGULAR_HOLIDAY_RATE;
    }

    private double calculateRestDayPay() {
        return restDaysWorked * grossDailyRate * REST_DAY_RATE;
    }

    private double calculateRestDayOTPay() {
        return restDayOTHours * hourlyRate * REST_DAY_RATE * REST_DAY_RATE;
    }

    private double calculateRestDayNightDiffOTPay() {
        return restDayNightDiffOTHours * hourlyRate * REST_DAY_RATE * REST_DAY_RATE * NIGHT_DIFFERENTIAL;
    }

    private double calculateVLPay() {
        return vlUsed * basicDailyRate;
    }

    private double calculateSLPay() {
        return slUsed * basicDailyRate;
    }

    private double calculatePerDiem() {
        return dailyPerDiem * perDiemCount;
    }

    private double calculateSSS() {
        double employeeShare = getSSSEmployeeShare(basicMonthlyPay);
        return employeeShare / 2;
    }

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
        else return 1800.00;
    }

    private double calculatePHIC() {
        double monthlyContribution;

        if (basicMonthlyPay < 10000) {
            monthlyContribution = PHIC_MIN;
        } else if (basicMonthlyPay > 100000) {
            monthlyContribution = PHIC_MAX;
        } else {
            monthlyContribution = (basicMonthlyPay * PHIC_RATE) / 2;
        }
        double semiMonthlyContribution = monthlyContribution / 2;

        if (semiMonthlyContribution < 250 && isFirstHalf) {
            return 250.00;
        } else if (semiMonthlyContribution < 250 && !isFirstHalf) {
            return monthlyContribution - 250.00;
        }

        return semiMonthlyContribution;
    }

    private double calculateHDMF() {
        return isFirstHalf ? HDMF : 0.00;
    }

    private double calculateWithholdingTax(double semiMonthlyTaxableIncome) {
        double tableTax = 0;
        double over = 0;
        double bracket = 0;

        if (semiMonthlyTaxableIncome <= 10416.67) {
            tableTax = 0;
            over = 0;
            bracket = 0;
        } else if (semiMonthlyTaxableIncome <= 16666.67) {
            tableTax = 0;
            over = 0.15;
            bracket = 10416.67;
        } else if (semiMonthlyTaxableIncome <= 33333.33) {
            tableTax = 937.50;
            over = 0.20;
            bracket = 16666.67;
        } else if (semiMonthlyTaxableIncome <= 83333.33) {
            tableTax = 4270.83;
            over = 0.25;
            bracket = 33333.33;
        } else if (semiMonthlyTaxableIncome <= 333333.33) {
            tableTax = 16770.83;
            over = 0.30;
            bracket = 83333.33;
        } else {
            tableTax = 91770.83;
            over = 0.35;
            bracket = 333333.33;
        }

        return tableTax + ((semiMonthlyTaxableIncome - bracket) * over);
    }

    public double calculateVLAccrual(int monthsWorked) {
        if (!employmentStatus.equalsIgnoreCase("Regular")) {
            return 0;
        }
        return monthsWorked * VL_ACCRUAL_RATE;
    }

    public static double calculate13thMonthPay(double totalBasicSalaryForYear) {
        return totalBasicSalaryForYear / 12;
    }

    public static double calculate13thMonthTax(double thirteenthMonthPay, double annualTaxableIncome) {
        if (thirteenthMonthPay <= NON_TAXABLE_13TH_MONTH_MAX) {
            return 0;
        }

        double taxableAmount = thirteenthMonthPay - NON_TAXABLE_13TH_MONTH_MAX;
        double taxRate = getAnnualTaxRate(annualTaxableIncome);

        return taxableAmount * taxRate;
    }

    private static double getAnnualTaxRate(double annualIncome) {
        if (annualIncome <= 250000) return 0;
        else if (annualIncome <= 400000) return 0.15;
        else if (annualIncome <= 800000) return 0.20;
        else if (annualIncome <= 2000000) return 0.25;
        else if (annualIncome <= 8000000) return 0.30;
        else return 0.35;
    }

    // Getters
    public double getGrossMonthlyPay() { return grossMonthlyPay; }
    public double getGrossDailyRate() { return grossDailyRate; }
    public double getBasicDailyRate() { return basicDailyRate; }
    public double getHourlyRate() { return hourlyRate; }
    public double getSemiMonthlyBasicPay() { return semiMonthlyBasicPay; }
    public double getSemiMonthlyGrossPay() { return semiMonthlyGrossPay; }
    public double getTotalEarnings() { return totalEarnings; }
    public double getTotalDeductions() { return totalDeductions; }
    public double getTaxableIncome() { return taxableIncome; }
    public double getWithholdingTax() { return withholdingTax; }
    public double getNetPay() { return netPay; }
    public double getSSSContribution() { return sssContribution; }
    public double getPHICContribution() { return phicContribution; }
    public double getHDMFContribution() { return hdmfContribution; }

    public String generatePayslipSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PAYSLIP ===\n");
        sb.append("Employee ID: ").append(employeeId).append("\n");
        sb.append("Employee Name: ").append(employeeName).append("\n");
        sb.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n");
        sb.append("Status: ").append(employmentStatus).append("\n\n");

        sb.append("EARNINGS:\n");
        sb.append("Basic Pay: ").append(String.format("%.2f", semiMonthlyBasicPay)).append("\n");
        sb.append("Gross Pay: ").append(String.format("%.2f", semiMonthlyGrossPay)).append("\n");
        sb.append("Total Earnings: ").append(String.format("%.2f", totalEarnings)).append("\n\n");

        sb.append("DEDUCTIONS:\n");
        sb.append("SSS: ").append(String.format("%.2f", sssContribution)).append("\n");
        sb.append("PhilHealth: ").append(String.format("%.2f", phicContribution)).append("\n");
        sb.append("Pag-IBIG: ").append(String.format("%.2f", hdmfContribution)).append("\n");
        sb.append("SSS Loan: ").append(String.format("%.2f", sssLoan)).append("\n");
        sb.append("Withholding Tax: ").append(String.format("%.2f", withholdingTax)).append("\n");
        sb.append("Total Deductions: ").append(String.format("%.2f", totalDeductions)).append("\n\n");

        sb.append("NET PAY: ").append(String.format("%.2f", netPay)).append("\n");

        return sb.toString();
    }
}