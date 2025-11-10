package com.example.jopay;

import com.mysql.cj.protocol.Resultset;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PayrollDAO {
    DatabaseConnector connect = new DatabaseConnector();

    public Payroll getEmployeePayroll(String employeeId){
        String query = """
            SELECT e.employee_id, 
                   CONCAT(e.first_name, ' ', e.last_name) AS employee_name,
                   s.basic_Pay, s.telecom_Allowance, s.travel_Allowance,
                   s.rice_Subsidy, s.non_Taxable_Salary, s.per_Diem, s.per_Deim_Count,
                   s.starting_Date, s.end_Date
            FROM employee_info e
            JOIN salary_config s ON e.employee_id = s.employee_Id
            WHERE e.employee_id = ?;
        """;

        try (PreparedStatement stmt= connect.prepareStatement(query)) {
            stmt.setString(1, employeeId);

            ResultSet rs= stmt.executeQuery();

            if (rs.next()) {
                Payroll info = new Payroll();
                info.setEmployeeId(rs.getInt("employee_id"));
                info.setEmployeeName(rs.getString("employee_name"));
                info.setBasicPay(rs.getDouble("basic_Pay"));
                info.setTelecom(rs.getDouble("telecom_Allowance"));
                info.setTravel(rs.getDouble("travel_Allowance"));
                info.setRice(rs.getDouble("rice_Subsidy"));
                info.setNonTaxable(rs.getDouble("non_Taxable_Salary"));
                info.setPerDiem(rs.getDouble("per_Diem"));
                info.setPerDiemCount(rs.getInt("per_Deim_Count"));
                info.setStartDate(rs.getDate("starting_Date").toLocalDate());
                info.setEndDate(rs.getDate("end_Date").toLocalDate());
                return info;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
