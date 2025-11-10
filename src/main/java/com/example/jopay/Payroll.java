package com.example.jopay;
import java.time.LocalDate;


    public class Payroll {
        private int employeeId;
        private String employeeName;
        private double basicPay, telecom, travel, rice, nonTaxable, perDiem;
        private int perDiemCount;
        private LocalDate startDate, endDate;

        // Getters and setters
        public int getEmployeeId() { return employeeId; }
        public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

        public double getBasicPay() { return basicPay; }
        public void setBasicPay(double basicPay) { this.basicPay = basicPay; }

        public String getTelecom() { return telecom; }
        public void setTelecom(double telecom) { this.telecom = telecom; }

        public double getTravel() { return travel; }
        public void setTravel(double travel) { this.travel = travel; }

        public double getRice() { return rice; }
        public void setRice(double rice) { this.rice = rice; }

        public double getNonTaxable() { return nonTaxable; }
        public void setNonTaxable(double nonTaxable) { this.nonTaxable = nonTaxable; }

        public double getPerDiem() { return perDiem; }
        public void setPerDiem(double perDiem) { this.perDiem = perDiem; }

        public int getPerDiemCount() { return perDiemCount; }
        public void setPerDiemCount(int perDiemCount) { this.perDiemCount = perDiemCount; }

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    }


